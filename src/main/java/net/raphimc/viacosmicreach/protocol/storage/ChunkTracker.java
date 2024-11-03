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
package net.raphimc.viacosmicreach.protocol.storage;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.ChunkPosition;
import com.viaversion.viaversion.api.minecraft.chunks.*;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.IBlockLightData;
import finalforeach.cosmicreach.savelib.lightdata.skylight.ISkylightData;
import net.raphimc.viacosmicreach.ViaCosmicReach;
import net.raphimc.viacosmicreach.api.chunk.CosmicReachChunk;
import net.raphimc.viacosmicreach.api.chunk.CosmicReachChunkSection;
import net.raphimc.viacosmicreach.api.model.CosmicReachBlockState;
import net.raphimc.viacosmicreach.protocol.CosmicReachProtocol;
import net.raphimc.viacosmicreach.protocol.data.ProtocolConstants;
import net.raphimc.viacosmicreach.protocol.types.CosmicReachTypes;

import java.util.*;
import java.util.logging.Level;

public class ChunkTracker extends StoredObject {

    private static final byte[] FULL_LIGHT = new byte[2048];

    static {
        Arrays.fill(FULL_LIGHT, (byte) 0xFF);
    }

    private final Map<Long, CosmicReachChunk> chunks = new HashMap<>();
    private final Set<Long> dirtyChunks = new HashSet<>();

    public ChunkTracker(final UserConnection user) {
        super(user);
    }

    public CosmicReachChunk getChunk(final int chunkX, final int chunkZ) {
        return this.chunks.get(ChunkPosition.chunkKey(chunkX, chunkZ));
    }

    public CosmicReachChunkSection getChunkSection(final int chunkX, final int subChunkY, final int chunkZ) {
        final CosmicReachChunk chunk = this.getChunk(chunkX, chunkZ);
        if (chunk == null) return null;
        return chunk.sections().get(subChunkY);
    }

    public CosmicReachChunkSection getChunkSection(final BlockPosition blockPosition) {
        return this.getChunkSection(blockPosition.x() >> 4, blockPosition.y() >> 4, blockPosition.z() >> 4);
    }

    public void mergeChunkSection(final int chunkX, final int sectionY, final int chunkZ, final CosmicReachChunkSection chunkSection) {
        final long key = ChunkPosition.chunkKey(chunkX, chunkZ);
        final CosmicReachChunk chunk = this.chunks.computeIfAbsent(key, k -> new CosmicReachChunk(chunkX, chunkZ, new Int2ObjectOpenHashMap<>()));
        chunk.sections().put(sectionY, chunkSection);
        this.dirtyChunks.add(key);
    }

    public int handleBlockChange(final BlockPosition blockPosition, final String blockState) {
        final CosmicReachChunkSection chunkSection = this.getChunkSection(blockPosition);
        if (chunkSection == null) {
            return 0;
        }

        final CosmicReachBlockState cosmicReachBlockState = CosmicReachBlockState.fromString(blockState);
        int blockStateId = CosmicReachProtocol.MAPPINGS.getCosmicReachBlockStates().getInt(cosmicReachBlockState);
        if (blockStateId == -1) {
            ViaCosmicReach.getPlatform().getLogger().log(Level.WARNING, "Missing cosmic reach block state mapping for " + blockState);
            blockStateId = CosmicReachProtocol.MAPPINGS.getCosmicReachBlockStates().getInt(CosmicReachBlockState.AIR);
        }
        chunkSection.blockData().setBlockValue(blockStateId, blockPosition.x() & 15, blockPosition.y() & 15, blockPosition.z() & 15);

        final int minecraftBlockState = CosmicReachProtocol.MAPPINGS.getCosmicReachToMinecraftBlockStates().get(blockStateId);
        if (minecraftBlockState != -1) {
            return minecraftBlockState;
        } else {
            ViaCosmicReach.getPlatform().getLogger().log(Level.WARNING, "Missing cosmic reach -> minecraft block state mapping for " + blockState);
            return 1;
        }
    }

    public void updateChunkCenter(final int chunkX, final int chunkZ) {
        final PacketWrapper setChunkCacheCenter = PacketWrapper.create(ClientboundPackets1_21.SET_CHUNK_CACHE_CENTER, this.user());
        setChunkCacheCenter.write(Types.VAR_INT, chunkX); // chunk x
        setChunkCacheCenter.write(Types.VAR_INT, chunkZ); // chunk z
        setChunkCacheCenter.send(CosmicReachProtocol.class);
    }

    public void tick() {
        for (Long dirtyChunk : this.dirtyChunks) {
            final CosmicReachChunk chunk = this.chunks.get(dirtyChunk);
            if (chunk == null) {
                return;
            }
            final Chunk remappedChunk = this.remapChunk(chunk);

            final PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_21.LEVEL_CHUNK_WITH_LIGHT, this.user());
            final BitSet skyLightMask = new BitSet();
            final BitSet blockLightMask = new BitSet();
            final BitSet emptySkyLightMask = new BitSet();
            final BitSet emptyBlockLightMask = new BitSet();
            final List<byte[]> skyLights = new ArrayList<>();
            final List<byte[]> blockLights = new ArrayList<>();

            skyLights.add(FULL_LIGHT.clone());
            skyLightMask.set(0);
            emptyBlockLightMask.set(0);

            for (int i = 0; i < remappedChunk.getSections().length; i++) {
                final ChunkSection chunkSection = remappedChunk.getSections()[i];
                if (chunkSection.hasLight()) {
                    if (chunkSection.getLight().hasSkyLight()) {
                        skyLights.add(chunkSection.getLight().getSkyLight());
                        skyLightMask.set(1 + i);
                    } else {
                        //emptySkyLightMask.set(1 + i);
                    }
                    if (chunkSection.getLight().hasBlockLight()) {
                        blockLights.add(chunkSection.getLight().getBlockLight());
                        blockLightMask.set(1 + i);
                    } else {
                        //emptyBlockLightMask.set(1 + i);
                    }
                } else {
                    //emptySkyLightMask.set(1 + i);
                    //emptyBlockLightMask.set(1 + i);
                }
            }

            skyLights.add(FULL_LIGHT.clone());
            skyLightMask.set(remappedChunk.getSections().length + 1);
            emptyBlockLightMask.set(remappedChunk.getSections().length + 1);

            wrapper.write(CosmicReachTypes.MINECRAFT_CHUNK, remappedChunk); // chunk
            wrapper.write(Types.LONG_ARRAY_PRIMITIVE, skyLightMask.toLongArray()); // sky light mask
            wrapper.write(Types.LONG_ARRAY_PRIMITIVE, blockLightMask.toLongArray()); // block light mask
            wrapper.write(Types.LONG_ARRAY_PRIMITIVE, emptySkyLightMask.toLongArray()); // empty sky light mask
            wrapper.write(Types.LONG_ARRAY_PRIMITIVE, emptyBlockLightMask.toLongArray()); // empty block light mask
            wrapper.write(Types.VAR_INT, skyLights.size()); // sky light length
            for (byte[] skyLight : skyLights) {
                wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, skyLight); // sky light
            }
            wrapper.write(Types.VAR_INT, blockLights.size()); // block light length
            for (byte[] blockLight : blockLights) {
                wrapper.write(Types.BYTE_ARRAY_PRIMITIVE, blockLight); // block light
            }
            wrapper.send(CosmicReachProtocol.class);
        }
        this.dirtyChunks.clear();
    }

    private Chunk remapChunk(final CosmicReachChunk chunk) {
        final int airId = CosmicReachProtocol.MAPPINGS.getCosmicReachBlockStates().getInt(CosmicReachBlockState.AIR);
        final Chunk remappedChunk = new Chunk1_18(chunk.x(), chunk.z(), new ChunkSection[ProtocolConstants.WORLD_HEIGHT >> 4], new CompoundTag(), new ArrayList<>());

        // TODO: Translate block entities

        final Int2ObjectMap<CosmicReachChunkSection> cosmicReachSections = chunk.sections();
        final ChunkSection[] remappedSections = remappedChunk.getSections();
        for (int idx = 0; idx < remappedSections.length; idx++) {
            final int cosmicReachIdx = idx + (ProtocolConstants.WORLD_MIN_Y >> 4);
            final CosmicReachChunkSection cosmicReachChunkSection = cosmicReachSections.get(cosmicReachIdx);

            final ChunkSection remappedSection = remappedSections[idx] = new ChunkSectionImpl(cosmicReachChunkSection != null);
            final DataPalette remappedBlockPalette = remappedSection.palette(PaletteType.BLOCKS);
            final DataPalette remappedBiomePalette = new DataPaletteImpl(ChunkSection.BIOME_SIZE);
            remappedSection.addPalette(PaletteType.BIOMES, remappedBiomePalette);
            remappedBlockPalette.addId(0);
            remappedBiomePalette.addId(CosmicReachProtocol.MAPPINGS.getMinecraftPlainsBiomeId());
            if (cosmicReachChunkSection != null) {
                final IBlockData<Integer> blockData = cosmicReachChunkSection.blockData();
                int nonAirBlockCount = 0;
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            final int cosmicReachBlockState = blockData.getBlockValue(x, y, z);
                            if (cosmicReachBlockState != airId) {
                                final int minecraftBlockState = CosmicReachProtocol.MAPPINGS.getCosmicReachToMinecraftBlockStates().get(cosmicReachBlockState);
                                if (minecraftBlockState != -1) {
                                    remappedBlockPalette.setIdAt(x, y, z, minecraftBlockState);
                                } else {
                                    // TODO: Log missing block state
                                    //ViaCosmicReach.getPlatform().getLogger().log(Level.WARNING, "Missing cosmic reach -> minecraft block state mapping for " + cosmicReachBlockState);
                                    remappedBlockPalette.setIdAt(x, y, z, 1);
                                }
                                nonAirBlockCount++;
                            }
                        }
                    }
                }
                remappedSection.setNonAirBlocksCount(nonAirBlockCount);

                final ChunkSectionLight chunkSectionLight = remappedSection.getLight();
                //final ISkylightData skylightData = cosmicReachChunkSection.skylightData();
                final ISkylightData skylightData = null; // TODO: Skylight is broken in CR
                final IBlockLightData blockLightData = cosmicReachChunkSection.blockLightData();
                /*if (skylightData != null) {
                    chunkSectionLight.setSkyLight(new byte[2048]);
                }*/
                chunkSectionLight.setSkyLight(FULL_LIGHT.clone());
                final NibbleArray remappedSkyLight = chunkSectionLight.getSkyLightNibbleArray();
                final NibbleArray remappedBlockLight = chunkSectionLight.getBlockLightNibbleArray();
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            if (skylightData != null) {
                                remappedSkyLight.set(x, y, z, skylightData.getSkyLight(x, y, z));
                            }
                            if (blockLightData != null) {
                                final short blockLightLevel = blockLightData.getBlockLight(x, y, z);
                                final int r = (byte) ((blockLightLevel & 0xF00) >> 8);
                                final int g = (byte) ((blockLightLevel & 0x0F0) >> 4);
                                final int b = (byte) (blockLightLevel & 0x00F);
                                remappedBlockLight.set(x, y, z, Math.max(r, Math.max(g, b)));
                            }
                        }
                    }
                }
            }
        }

        return remappedChunk;
    }

}
