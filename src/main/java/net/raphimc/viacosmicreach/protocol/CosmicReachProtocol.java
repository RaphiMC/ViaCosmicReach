/*
 * This file is part of ViaCosmicReach - https://github.com/RaphiMC/ViaCosmicReach
 * Copyright (C) 2024-2024 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.viacosmicreach.protocol;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.Vector3f;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundHandshakePackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.base.v1_7.ClientboundBaseProtocol1_7;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import net.raphimc.viabedrock.api.util.BitSets;
import net.raphimc.viabedrock.api.util.MathUtil;
import net.raphimc.viabedrock.protocol.data.enums.java.GameEventType;
import net.raphimc.viabedrock.protocol.data.enums.java.GameMode;
import net.raphimc.viabedrock.protocol.data.enums.java.PlayerInfoUpdateAction;
import net.raphimc.viacosmicreach.ClientboundCosmicReachPackets;
import net.raphimc.viacosmicreach.ServerboundCosmicReachPackets;
import net.raphimc.viacosmicreach.ViaCosmicReach;
import net.raphimc.viacosmicreach.api.chunk.CosmicReachChunkSection;
import net.raphimc.viacosmicreach.api.util.TextUtil;
import net.raphimc.viacosmicreach.protocol.data.CosmicReachMappingData;
import net.raphimc.viacosmicreach.protocol.data.ProtocolConstants;
import net.raphimc.viacosmicreach.protocol.model.Account;
import net.raphimc.viacosmicreach.protocol.model.OfflineAccount;
import net.raphimc.viacosmicreach.protocol.storage.*;
import net.raphimc.viacosmicreach.protocol.task.ChunkTrackerTickTask;
import net.raphimc.viacosmicreach.protocol.task.KeepAliveTask;
import net.raphimc.viacosmicreach.protocol.types.CosmicReachTypes;
import net.raphimc.vialegacy.api.protocol.StatelessTransitionProtocol;

import java.util.*;

public class CosmicReachProtocol extends StatelessTransitionProtocol<ClientboundCosmicReachPackets, ClientboundPackets1_21, ServerboundCosmicReachPackets, ServerboundPackets1_20_5> {

    public static final CosmicReachMappingData MAPPINGS = new CosmicReachMappingData();

    public CosmicReachProtocol() {
        super(ClientboundCosmicReachPackets.class, ClientboundPackets1_21.class, ServerboundCosmicReachPackets.class, ServerboundPackets1_20_5.class);
    }

    @Override
    protected void registerPackets() {
        this.registerClientbound(ClientboundCosmicReachPackets.PROTOCOL_SYNC, null, wrapper -> {
            wrapper.cancel();
            final int count = wrapper.read(Types.INT); // packet definition count
            final Map<String, Integer> packetMapping = new HashMap<>(count);
            for (int i = 0; i < count; i++) {
                final String packetName = wrapper.read(CosmicReachTypes.STRING); // full packet name
                final int packetId = wrapper.read(Types.INT); // packet id
                packetMapping.put(packetName, packetId);
            }
            wrapper.user().put(new ProtocolStorage(packetMapping));
        });
        this.registerClientbound(ClientboundCosmicReachPackets.TRANSACTION, ClientboundPackets1_21.PING, wrapper -> {
            final TransactionStorage transactionStorage = wrapper.user().get(TransactionStorage.class);
            wrapper.write(Types.INT, transactionStorage.createJavaTransactionId(wrapper.read(Types.LONG))); // id
        });
        this.registerClientbound(ClientboundCosmicReachPackets.DISCONNECT, ClientboundPackets1_21.REMOVE_ENTITIES, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final String playerUniqueId = wrapper.read(CosmicReachTypes.STRING); // player unique id

            if (!entityTracker.hasPlayer(playerUniqueId)) {
                wrapper.cancel();
                return;
            }

            final int entityId = entityTracker.getEntityIdByUniquePlayerId(playerUniqueId);
            wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[]{entityId}); // entity ids

            final Account account = entityTracker.getAccountByUniquePlayerId(playerUniqueId);
            final PacketWrapper systemChat = PacketWrapper.create(ClientboundPackets1_21.SYSTEM_CHAT, wrapper.user());
            systemChat.write(Types.TAG, TextUtil.stringToNbt(account.displayName() + " has left the game."));
            systemChat.write(Types.BOOLEAN, false); // overlay
            systemChat.send(CosmicReachProtocol.class);

            final PacketWrapper playerInfoRemove = PacketWrapper.create(ClientboundPackets1_21.PLAYER_INFO_REMOVE, wrapper.user());
            playerInfoRemove.write(Types.UUID_ARRAY, new UUID[]{new UUID(0L, entityId)}); // uuid
            playerInfoRemove.send(CosmicReachProtocol.class);

            entityTracker.removePlayer(playerUniqueId);
        });
        this.registerClientbound(ClientboundCosmicReachPackets.PLAYER, ClientboundPackets1_21.ADD_ENTITY, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final Account account = wrapper.read(CosmicReachTypes.ACCOUNT); // account
            wrapper.read(CosmicReachTypes.JSON_OBJECT); // player
            final boolean justJoined = wrapper.read(Types.BOOLEAN); // just joined

            if (entityTracker.hasPlayer(account.uniqueId())) {
                wrapper.cancel();
                return;
            }
            if (justJoined) {
                final PacketWrapper systemChat = PacketWrapper.create(ClientboundPackets1_21.SYSTEM_CHAT, wrapper.user());
                systemChat.write(Types.TAG, TextUtil.stringToNbt(account.displayName() + " has joined the game."));
                systemChat.write(Types.BOOLEAN, false); // overlay
                systemChat.send(CosmicReachProtocol.class);
            }

            final int entityId = entityTracker.addPlayer(account);
            final UUID uuid = new UUID(0L, entityId);

            final PacketWrapper playerInfoUpdate = PacketWrapper.create(ClientboundPackets1_21.PLAYER_INFO_UPDATE, wrapper.user());
            playerInfoUpdate.write(Types.PROFILE_ACTIONS_ENUM, BitSets.create(6, PlayerInfoUpdateAction.ADD_PLAYER.ordinal(), PlayerInfoUpdateAction.UPDATE_LISTED.ordinal())); // actions
            playerInfoUpdate.write(Types.VAR_INT, 1); // length
            playerInfoUpdate.write(Types.UUID, uuid); // uuid
            playerInfoUpdate.write(Types.STRING, account.displayName()); // username
            playerInfoUpdate.write(Types.VAR_INT, 0); // property count
            playerInfoUpdate.write(Types.BOOLEAN, true); // listed
            playerInfoUpdate.send(CosmicReachProtocol.class);

            wrapper.write(Types.VAR_INT, entityId); // entity id
            wrapper.write(Types.UUID, uuid); // uuid
            wrapper.write(Types.VAR_INT, EntityTypes1_20_5.PLAYER.getId()); // type id
            wrapper.write(Types.DOUBLE, 0D); // x
            wrapper.write(Types.DOUBLE, 0D); // y
            wrapper.write(Types.DOUBLE, 0D); // z
            wrapper.write(Types.BYTE, MathUtil.float2Byte(0F)); // pitch
            wrapper.write(Types.BYTE, MathUtil.float2Byte(0F)); // yaw
            wrapper.write(Types.BYTE, MathUtil.float2Byte(0F)); // head yaw
            wrapper.write(Types.VAR_INT, 0); // data
            wrapper.write(Types.SHORT, (short) 0); // velocity x
            wrapper.write(Types.SHORT, (short) 0); // velocity y
            wrapper.write(Types.SHORT, (short) 0); // velocity z
        });
        this.registerClientbound(ClientboundCosmicReachPackets.MESSAGE, ClientboundPackets1_21.SYSTEM_CHAT, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final String message = wrapper.read(CosmicReachTypes.STRING); // message
            final String playerUniqueId = wrapper.read(CosmicReachTypes.STRING); // player unique id

            if (entityTracker.hasPlayer(playerUniqueId)) {
                wrapper.write(Types.TAG, TextUtil.stringToNbt(entityTracker.getAccountByUniquePlayerId(playerUniqueId).displayName() + "> " + message));
            } else {
                wrapper.write(Types.TAG, TextUtil.stringToNbt(message));
            }
            wrapper.write(Types.BOOLEAN, false); // overlay
        });
        this.registerClientbound(ClientboundCosmicReachPackets.PLAYER_POSITION, ClientboundPackets1_21.TELEPORT_ENTITY, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final String playerUniqueId = wrapper.read(CosmicReachTypes.STRING); // player unique id
            final Vector3f position = wrapper.read(Types.VECTOR3F); // position
            final Vector3f viewDirection = wrapper.read(Types.VECTOR3F); // view direction
            wrapper.read(Types.VECTOR3F); // view direction offset
            wrapper.read(CosmicReachTypes.STRING); // zone id
            // TODO: Handle other fields

            if (!entityTracker.hasPlayer(playerUniqueId)) {
                wrapper.cancel();
                return;
            }
            if (entityTracker.getClientPlayerAccount().uniqueId().equals(playerUniqueId)) {
                wrapper.setPacketType(ClientboundPackets1_21.PLAYER_POSITION);
                wrapper.write(Types.DOUBLE, (double) position.x()); // x
                wrapper.write(Types.DOUBLE, (double) position.y()); // y
                wrapper.write(Types.DOUBLE, (double) position.z()); // z
                wrapper.write(Types.FLOAT, 0F); // yaw
                wrapper.write(Types.FLOAT, 0F); // pitch
                wrapper.write(Types.BYTE, (byte) 0); // flags
                wrapper.write(Types.VAR_INT, 0); // teleport id

                wrapper.user().get(PlayerPositionTracker.class).updatePosition(position.x(), position.y(), position.z());
            } else {
                wrapper.write(Types.VAR_INT, entityTracker.getEntityIdByUniquePlayerId(playerUniqueId)); // entity id
                wrapper.write(Types.DOUBLE, (double) position.x()); // x
                wrapper.write(Types.DOUBLE, (double) position.y()); // y
                wrapper.write(Types.DOUBLE, (double) position.z()); // z
                wrapper.write(Types.BYTE, MathUtil.float2Byte(0F)); // yaw
                wrapper.write(Types.BYTE, MathUtil.float2Byte(0F)); // pitch
                wrapper.write(Types.BOOLEAN, false); // on ground
            }

        });
        this.registerClientbound(ClientboundCosmicReachPackets.ZONE, ClientboundPackets1_21.SYSTEM_CHAT, wrapper -> {
            final JsonObject zoneData = wrapper.read(CosmicReachTypes.JSON_OBJECT); // zone data
            final ZoneStorage zoneStorage = new ZoneStorage(zoneData);
            wrapper.user().put(zoneStorage);

            wrapper.write(Types.TAG, TextUtil.stringToNbt("[ViaCosmicReach] Spawned in zone " + zoneStorage.getId() + " at " + zoneStorage.getSpawnX() + ", " + zoneStorage.getSpawnY() + ", " + zoneStorage.getSpawnZ() + "\nTo respawn, use /respawn")); // message
            wrapper.write(Types.BOOLEAN, false); // overlay
        });
        this.registerClientbound(ClientboundCosmicReachPackets.CHUNK_COLUMN, null, wrapper -> {
            wrapper.cancel();
            final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
            wrapper.read(CosmicReachTypes.STRING); // zone id
            // TODO: Handle other fields
            final int count = wrapper.read(Types.INT); // chunk count
            for (int i = 0; i < count; i++) {
                final int chunkX = wrapper.read(Types.INT); // x
                final int sectionY = wrapper.read(Types.INT); // y
                final int chunkZ = wrapper.read(Types.INT); // z
                final CosmicReachChunkSection chunkSection = wrapper.read(CosmicReachTypes.CHUNK_SECTION); // chunk section
                chunkTracker.mergeChunkSection(chunkX, sectionY, chunkZ, chunkSection);
            }
        });
        this.registerClientbound(ClientboundCosmicReachPackets.BLOCK_REPLACE, ClientboundPackets1_21.BLOCK_UPDATE, wrapper -> {
            final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
            wrapper.read(CosmicReachTypes.STRING); // zone id
            final String blockState = wrapper.read(CosmicReachTypes.STRING); // block state
            final BlockPosition blockPosition = wrapper.read(CosmicReachTypes.BLOCK_POSITION); // position

            wrapper.write(Types.BLOCK_POSITION1_14, blockPosition); // position
            wrapper.write(Types.VAR_INT, chunkTracker.handleBlockChange(blockPosition, blockState)); // block state
        });
        this.registerClientbound(ClientboundCosmicReachPackets.PLAY_SOUND_2D, ClientboundPackets1_21.SOUND, wrapper -> {
            wrapper.cancel();
            // TODO: Implement
        });
        this.registerClientbound(ClientboundCosmicReachPackets.PLAY_SOUND_3D, ClientboundPackets1_21.SOUND, wrapper -> {
            wrapper.cancel();
            // TODO: Implement
        });
        this.registerClientbound(ClientboundCosmicReachPackets.CONTAINER_SYNC, null, wrapper -> {
            wrapper.cancel();
            // TODO: Implement
        });
        this.registerClientbound(ClientboundCosmicReachPackets.BLOCK_ENTITY_CONTAINER_SYNC, null, wrapper -> {
            wrapper.cancel();
            // TODO: Implement
        });
        this.registerClientbound(ClientboundCosmicReachPackets.BLOCK_ENTITY_SCREEN, null, wrapper -> {
            wrapper.cancel();
            // TODO: Implement
        });
        this.registerClientbound(ClientboundCosmicReachPackets.BLOCK_ENTITY_DATA, null, wrapper -> {
            wrapper.cancel();
            // TODO: Implement
        });

        this.registerServerboundTransition(ServerboundHandshakePackets.CLIENT_INTENTION, null, PacketWrapper::cancel);
        this.registerServerboundTransition(ServerboundLoginPackets.HELLO, ServerboundCosmicReachPackets.LOGIN, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final String username = wrapper.read(Types.STRING); // name
            final UUID uuid = wrapper.read(Types.UUID); // uuid

            final Account account = OfflineAccount.create(username);
            entityTracker.setClientPlayerAccount(account);
            final int entityId = entityTracker.addPlayer(account);

            wrapper.write(CosmicReachTypes.ACCOUNT, account); // account

            final ProtocolInfo info = wrapper.user().getProtocolInfo();
            info.setUsername(username);
            info.setUuid(uuid);
            ClientboundBaseProtocol1_7.onLoginSuccess(wrapper.user());

            final PacketWrapper gameProfile = PacketWrapper.create(ClientboundLoginPackets.GAME_PROFILE, wrapper.user());
            gameProfile.write(Types.UUID, uuid); // uuid
            gameProfile.write(Types.STRING, username); // username
            gameProfile.write(Types.VAR_INT, 0); // properties length
            gameProfile.write(Types.BOOLEAN, true); // strict error handling
            gameProfile.send(CosmicReachProtocol.class);
            wrapper.user().getProtocolInfo().setServerState(State.CONFIGURATION);

            for (Map.Entry<String, Tag> registry : MAPPINGS.getMinecraftRegistries().copy().entrySet()) {
                final CompoundTag registryTag = (CompoundTag) registry.getValue();
                final PacketWrapper registryData = PacketWrapper.create(ClientboundConfigurationPackets1_21.REGISTRY_DATA, wrapper.user());
                registryData.write(Types.STRING, registry.getKey()); // registry key
                final List<RegistryEntry> entries = new ArrayList<>();
                for (Map.Entry<String, Tag> entry : registryTag.entrySet()) {
                    entries.add(new RegistryEntry(entry.getKey(), entry.getValue()));
                }
                registryData.write(Types.REGISTRY_ENTRY_ARRAY, entries.toArray(new RegistryEntry[0])); // registry entries
                registryData.send(CosmicReachProtocol.class);
            }

            final PacketWrapper updateTags = PacketWrapper.create(ClientboundConfigurationPackets1_21.UPDATE_TAGS, wrapper.user());
            updateTags.write(Types.VAR_INT, MAPPINGS.getMinecraftTags().size()); // number of registries
            for (Map.Entry<String, Tag> registryEntry : MAPPINGS.getMinecraftTags().entrySet()) {
                final CompoundTag tag = (CompoundTag) registryEntry.getValue();
                updateTags.write(Types.STRING, registryEntry.getKey()); // registry key
                updateTags.write(Types.VAR_INT, tag.size()); // number of tags
                for (Map.Entry<String, Tag> tagEntry : tag.entrySet()) {
                    updateTags.write(Types.STRING, tagEntry.getKey()); // tag name
                    updateTags.write(Types.VAR_INT_ARRAY_PRIMITIVE, ((IntArrayTag) tagEntry.getValue()).getValue().clone()); // tag ids
                }
            }
            updateTags.send(CosmicReachProtocol.class);

            final PacketWrapper finishConfiguration = PacketWrapper.create(ClientboundConfigurationPackets1_21.FINISH_CONFIGURATION, wrapper.user());
            finishConfiguration.send(CosmicReachProtocol.class);
            wrapper.user().getProtocolInfo().setServerState(State.PLAY);

            final PacketWrapper login = PacketWrapper.create(ClientboundPackets1_21.LOGIN, wrapper.user());
            login.write(Types.INT, entityId); // entity id
            login.write(Types.BOOLEAN, false); // hardcore
            login.write(Types.STRING_ARRAY, new String[]{"minecraft:overworld"}); // dimension types
            login.write(Types.VAR_INT, 100); // max players
            login.write(Types.VAR_INT, ProtocolConstants.MINECRAFT_VIEW_DISTANCE); // view distance
            login.write(Types.VAR_INT, ProtocolConstants.MINECRAFT_VIEW_DISTANCE); // simulation distance
            login.write(Types.BOOLEAN, false); // reduced debug info
            login.write(Types.BOOLEAN, true); // show death screen
            login.write(Types.BOOLEAN, false); // limited crafting
            login.write(Types.VAR_INT, 0); // dimension id
            login.write(Types.STRING, "minecraft:overworld"); // dimension name
            login.write(Types.LONG, 0L); // hashed seed
            login.write(Types.BYTE, (byte) GameMode.CREATIVE.ordinal()); // game mode
            login.write(Types.BYTE, (byte) -1); // previous game mode
            login.write(Types.BOOLEAN, false); // is debug
            login.write(Types.BOOLEAN, false); // is flat
            login.write(Types.OPTIONAL_GLOBAL_POSITION, null); // last death location
            login.write(Types.VAR_INT, 0); // portal cooldown
            login.write(Types.BOOLEAN, false); // enforce secure chat
            login.send(CosmicReachProtocol.class);

            final PacketWrapper tabList = PacketWrapper.create(ClientboundPackets1_21.TAB_LIST, wrapper.user());
            tabList.write(Types.TAG, TextUtil.stringToNbt("§7https://github.com/RaphiMC/ViaCosmicReach")); // header
            tabList.write(Types.TAG, TextUtil.stringToNbt("§3" + ViaCosmicReach.IMPL_VERSION)); // footer
            tabList.send(CosmicReachProtocol.class);

            final PacketWrapper playerInfoUpdate = PacketWrapper.create(ClientboundPackets1_21.PLAYER_INFO_UPDATE, wrapper.user());
            playerInfoUpdate.write(Types.PROFILE_ACTIONS_ENUM, BitSets.create(6, PlayerInfoUpdateAction.ADD_PLAYER.ordinal(), PlayerInfoUpdateAction.UPDATE_GAME_MODE.ordinal(), PlayerInfoUpdateAction.UPDATE_LISTED.ordinal())); // actions
            playerInfoUpdate.write(Types.VAR_INT, 1); // length
            playerInfoUpdate.write(Types.UUID, uuid); // uuid
            playerInfoUpdate.write(Types.STRING, username); // username
            playerInfoUpdate.write(Types.VAR_INT, 0); // property count
            playerInfoUpdate.write(Types.VAR_INT, GameMode.CREATIVE.ordinal()); // game mode
            playerInfoUpdate.write(Types.BOOLEAN, true); // listed
            playerInfoUpdate.send(CosmicReachProtocol.class);

            final PacketWrapper playerPosition = PacketWrapper.create(ClientboundPackets1_21.PLAYER_POSITION, wrapper.user());
            playerPosition.write(Types.DOUBLE, 0D); // x
            playerPosition.write(Types.DOUBLE, 300D); // y
            playerPosition.write(Types.DOUBLE, 0D); // z
            playerPosition.write(Types.FLOAT, 0F); // yaw
            playerPosition.write(Types.FLOAT, 0F); // pitch
            playerPosition.write(Types.BYTE, (byte) 0); // flags
            playerPosition.write(Types.VAR_INT, 0); // teleport id
            playerPosition.send(CosmicReachProtocol.class);

            final PacketWrapper gameEvent = PacketWrapper.create(ClientboundPackets1_21.GAME_EVENT, wrapper.user());
            gameEvent.write(Types.UNSIGNED_BYTE, (short) GameEventType.LEVEL_CHUNKS_LOAD_START.ordinal()); // event id
            gameEvent.write(Types.FLOAT, 0F); // value
            gameEvent.send(CosmicReachProtocol.class);
        });
        this.registerServerboundTransition(ServerboundLoginPackets.LOGIN_ACKNOWLEDGED, null, wrapper -> {
            wrapper.cancel();
            wrapper.user().getProtocolInfo().setServerState(State.PLAY); // ViaVersion workaround. Needed because ServerboundBaseProtocol1_7#LOGIN_ACKNOWLEDGED overrides the server state
        });
        this.registerServerboundTransition(ServerboundConfigurationPackets1_20_5.FINISH_CONFIGURATION, null, wrapper -> {
            wrapper.cancel();
            wrapper.user().getProtocolInfo().setClientState(State.PLAY);
        });
        this.registerServerbound(ServerboundPackets1_20_5.CONFIGURATION_ACKNOWLEDGED, null, wrapper -> {
            wrapper.cancel();
            wrapper.user().getProtocolInfo().setClientState(State.CONFIGURATION);
        });
        this.registerServerbound(ServerboundPackets1_20_5.CHAT, ServerboundCosmicReachPackets.MESSAGE, new PacketHandlers() {
            @Override
            protected void register() {
                map(Types.STRING, CosmicReachTypes.STRING); // message
                handler(PacketWrapper::clearInputBuffer);
                create(CosmicReachTypes.STRING, null); // player unique id
                handler(wrapper -> {
                    final Account clientPlayerAccount = wrapper.user().get(EntityTracker.class).getClientPlayerAccount();
                    final String message = wrapper.get(CosmicReachTypes.STRING, 0);
                    final PacketWrapper systemChat = PacketWrapper.create(ClientboundPackets1_21.SYSTEM_CHAT, wrapper.user());
                    systemChat.write(Types.TAG, TextUtil.stringToNbt(clientPlayerAccount.displayName() + "> " + message));
                    systemChat.write(Types.BOOLEAN, false); // overlay
                    systemChat.send(CosmicReachProtocol.class);
                });
            }
        });
        this.registerServerbound(ServerboundPackets1_20_5.CHAT_COMMAND, null, wrapper -> {
            wrapper.cancel();
            final String command = wrapper.read(Types.STRING); // command
            if (command.equals("respawn")) {
                final ZoneStorage zoneStorage = wrapper.user().get(ZoneStorage.class);
                final PacketWrapper playerPosition = PacketWrapper.create(ClientboundPackets1_21.PLAYER_POSITION, wrapper.user());
                playerPosition.write(Types.DOUBLE, (double) zoneStorage.getSpawnX()); // x
                playerPosition.write(Types.DOUBLE, (double) zoneStorage.getSpawnY()); // y
                playerPosition.write(Types.DOUBLE, (double) zoneStorage.getSpawnZ()); // z
                playerPosition.write(Types.FLOAT, 0F); // yaw
                playerPosition.write(Types.FLOAT, 0F); // pitch
                playerPosition.write(Types.BYTE, (byte) 0); // flags
                playerPosition.write(Types.VAR_INT, 0); // teleport id
                playerPosition.send(CosmicReachProtocol.class);
            }
        });
        this.registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_POS, ServerboundCosmicReachPackets.PLAYER_POSITION, wrapper -> {
            final PlayerPositionTracker playerPositionTracker = wrapper.user().get(PlayerPositionTracker.class);
            final double x = wrapper.read(Types.DOUBLE); // x
            final double y = wrapper.read(Types.DOUBLE); // y
            final double z = wrapper.read(Types.DOUBLE); // z
            wrapper.read(Types.BOOLEAN); // on ground

            playerPositionTracker.updatePosition((float) x, (float) y, (float) z);
            playerPositionTracker.writePlayerPosition(wrapper);
        });
        this.registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_POS_ROT, ServerboundCosmicReachPackets.PLAYER_POSITION, wrapper -> {
            final PlayerPositionTracker playerPositionTracker = wrapper.user().get(PlayerPositionTracker.class);
            final double x = wrapper.read(Types.DOUBLE); // x
            final double y = wrapper.read(Types.DOUBLE); // y
            final double z = wrapper.read(Types.DOUBLE); // z
            final float yaw = wrapper.read(Types.FLOAT); // yaw
            final float pitch = wrapper.read(Types.FLOAT); // pitch
            wrapper.read(Types.BOOLEAN); // on ground

            playerPositionTracker.updatePosition((float) x, (float) y, (float) z);
            playerPositionTracker.updateRotation(yaw, pitch);
            playerPositionTracker.writePlayerPosition(wrapper);
        });
        this.registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_ROT, ServerboundCosmicReachPackets.PLAYER_POSITION, wrapper -> {
            final PlayerPositionTracker playerPositionTracker = wrapper.user().get(PlayerPositionTracker.class);
            final float yaw = wrapper.read(Types.FLOAT); // yaw
            final float pitch = wrapper.read(Types.FLOAT); // pitch
            wrapper.read(Types.BOOLEAN); // on ground

            playerPositionTracker.updateRotation(yaw, pitch);
            playerPositionTracker.writePlayerPosition(wrapper);
        });
        this.registerServerbound(ServerboundPackets1_20_5.PONG, ServerboundCosmicReachPackets.TRANSACTION, wrapper -> {
            final TransactionStorage transactionStorage = wrapper.user().get(TransactionStorage.class);
            wrapper.write(Types.LONG, transactionStorage.getAndRemoveCosmicReachTransactionId(wrapper.read(Types.INT))); // id
        });

        // Cancel all unmapped serverbound packets
        for (ServerboundPackets1_20_5 packet : this.unmappedServerboundPacketType.getEnumConstants()) {
            if (!this.hasRegisteredServerbound(packet)) {
                this.cancelServerbound(packet);
            }
        }
        for (ServerboundConfigurationPackets1_20_5 packet : ServerboundConfigurationPackets1_20_5.values()) {
            if (!this.hasRegisteredServerbound(packet.state(), packet.getId())) {
                this.cancelServerbound(packet.state(), packet.getId());
            }
        }
    }

    @Override
    protected void onMappingDataLoaded() {
        super.onMappingDataLoaded();

        final CompoundTag registries = MAPPINGS.getMinecraftRegistries();
        final CompoundTag dimensionRegistry = registries.getCompoundTag("minecraft:dimension_type");
        final CompoundTag biomeRegistry = registries.getCompoundTag("minecraft:worldgen/biome");
        final CompoundTag dimensionTag = dimensionRegistry.getCompoundTag("minecraft:overworld");
        final int worldHeight = dimensionTag.getNumberTag("height").asInt();
        CosmicReachTypes.MINECRAFT_CHUNK = new ChunkType1_20_2(worldHeight >> 4, com.viaversion.viaversion.util.MathUtil.ceilLog2(MAPPINGS.getMinecraftBlockStates().size()), com.viaversion.viaversion.util.MathUtil.ceilLog2(biomeRegistry.size()));
    }

    @Override
    public void register(ViaProviders providers) {
        Via.getPlatform().runRepeatingSync(new KeepAliveTask(), 20L);
        Via.getPlatform().runRepeatingSync(new ChunkTrackerTickTask(), 2L);
    }

    @Override
    public void init(UserConnection user) {
        user.put(new ProtocolStorage());
        user.put(new TransactionStorage());
        user.put(new PlayerPositionTracker(user));
        user.put(new EntityTracker());
        user.put(new ChunkTracker(user));
    }

    @Override
    protected void registerConfigurationChangeHandlers() {
    }

    @Override
    public void transform(Direction direction, State state, PacketWrapper wrapper) throws InformativeException, CancelException {
        final ProtocolStorage protocolStorage = wrapper.user().get(ProtocolStorage.class);
        if (direction == Direction.CLIENTBOUND) {
            wrapper.setPacketType(protocolStorage.getClientboundPacket(wrapper.getId()));
        }

        super.transform(direction, state, wrapper);

        if (direction == Direction.SERVERBOUND) {
            wrapper.setId(protocolStorage.getServerboundPacketId((ServerboundCosmicReachPackets) wrapper.getPacketType()));
        }
    }

    @Override
    public CosmicReachMappingData getMappingData() {
        return MAPPINGS;
    }

}
