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
package net.raphimc.viacosmicreach;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntOpenHashMap;

public enum ClientboundCosmicReachPackets implements ClientboundPacketType {

    PROTOCOL_SYNC(1, "finalforeach.cosmicreach.networking.packets.meta.ProtocolSyncPacket"),
    TRANSACTION(2, "finalforeach.cosmicreach.networking.packets.meta.TransactionPacket"),
    REMOVED_PLAYER(4, "finalforeach.cosmicreach.networking.packets.meta.RemovedPlayerPacket"),
    END_TICK(5, "finalforeach.cosmicreach.networking.packets.EndTickPacket"),
    SET_NETWORK_SETTING(7, "finalforeach.cosmicreach.networking.packets.meta.SetNetworkSetting"),
    CHALLENGE_LOGIN(8, "finalforeach.cosmicreach.networking.packets.meta.ChallengeLoginPacket"),
    PLAYER(10, "finalforeach.cosmicreach.networking.packets.entities.PlayerPacket"),
    MESSAGE(11, "finalforeach.cosmicreach.networking.packets.MessagePacket"),
    PLAYER_POSITION(12, "finalforeach.cosmicreach.networking.packets.entities.PlayerPositionPacket"),
    ENTITY_POSITION(13, "finalforeach.cosmicreach.networking.packets.entities.EntityPositionPacket"),
    ZONE(14, "finalforeach.cosmicreach.networking.packets.ZonePacket"),
    CHUNK_COLUMN(15, "finalforeach.cosmicreach.networking.packets.ChunkColumnPacket"),
    DISCONNECT(17, "finalforeach.cosmicreach.networking.packets.meta.DisconnectPacket"),
    BLOCK_REPLACE(21, "finalforeach.cosmicreach.networking.packets.blocks.BlockReplacePacket"),
    PLAY_SOUND_2D(22, "finalforeach.cosmicreach.networking.packets.sounds.PlaySound2DPacket"),
    PLAY_SOUND_3D(23, "finalforeach.cosmicreach.networking.packets.sounds.PlaySound3DPacket"),
    CONTAINER_SYNC(26, "finalforeach.cosmicreach.networking.packets.ContainerSyncPacket"),
    BLOCK_ENTITY_CONTAINER_SYNC(27, "finalforeach.cosmicreach.networking.packets.blocks.BlockEntityContainerSyncPacket"),
    BLOCK_ENTITY_SCREEN(28, "finalforeach.cosmicreach.networking.packets.blocks.BlockEntityScreenPacket"),
    BLOCK_ENTITY_DATA(29, "finalforeach.cosmicreach.networking.packets.blocks.BlockEntityDataPacket"),
    SPAWN_ENTITY(31, "finalforeach.cosmicreach.networking.packets.entities.SpawnEntityPacket"),
    DESPAWN_ENTITY(32, "finalforeach.cosmicreach.networking.packets.entities.DespawnEntityPacket"),
    HIT_ENTITY(35, "finalforeach.cosmicreach.networking.packets.entities.HitEntityPacket");

    private static final ClientboundCosmicReachPackets[] REGISTRY = new ClientboundCosmicReachPackets[128];
    private static final Object2IntMap<String> CLASS_TO_ID = new Object2IntOpenHashMap<>();

    static {
        for (ClientboundCosmicReachPackets packet : values()) {
            REGISTRY[packet.id] = packet;
            CLASS_TO_ID.put(packet.className, packet.id);
        }
    }

    public static ClientboundCosmicReachPackets getPacket(final int id) {
        if (id < 0 || id >= REGISTRY.length) return null;

        return REGISTRY[id];
    }

    public static ClientboundCosmicReachPackets getPacket(final String className) {
        return CLASS_TO_ID.containsKey(className) ? REGISTRY[CLASS_TO_ID.getInt(className)] : null;
    }

    ClientboundCosmicReachPackets(final int id, final String className) {
        this.id = id;
        this.className = className;
    }

    private final int id;
    private final String className;

    @Override
    public int getId() {
        return this.id;
    }

    public String getClassName() {
        return this.className;
    }

    @Override
    public String getName() {
        return name();
    }

}
