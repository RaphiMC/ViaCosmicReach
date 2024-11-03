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
package net.raphimc.viacosmicreach.protocol.types;

import com.viaversion.viaversion.api.type.Type;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.savelib.blockdata.LayeredBlockData;
import finalforeach.cosmicreach.savelib.blockdata.SingleBlockData;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.BlockLightLayeredData;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.IBlockLightData;
import finalforeach.cosmicreach.savelib.lightdata.blocklight.layers.IBlockLightLayer;
import finalforeach.cosmicreach.savelib.lightdata.skylight.ISkylightData;
import finalforeach.cosmicreach.savelib.lightdata.skylight.SkylightLayeredData;
import finalforeach.cosmicreach.savelib.lightdata.skylight.SkylightSingleData;
import finalforeach.cosmicreach.savelib.lightdata.skylight.layers.SkylightDataNibbleLayer;
import finalforeach.cosmicreach.savelib.lightdata.skylight.layers.SkylightDataSingleLayer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import net.raphimc.viacosmicreach.ViaCosmicReach;
import net.raphimc.viacosmicreach.api.chunk.CosmicReachBlockEntity;
import net.raphimc.viacosmicreach.api.chunk.CosmicReachChunkSection;
import net.raphimc.viacosmicreach.api.io.NettyChunkByteReader;
import net.raphimc.viacosmicreach.api.model.CosmicReachBlockState;
import net.raphimc.viacosmicreach.protocol.CosmicReachProtocol;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;

import static finalforeach.cosmicreach.savelib.SaveFileConstants.*;

public class ChunkSectionType extends Type<CosmicReachChunkSection> {

    private static final Function<String, Integer> SAVE_KEY_TO_BLOCK_VALUE = blockStateString -> {
        final CosmicReachBlockState blockState = CosmicReachBlockState.fromString(blockStateString);
        final int blockStateId = CosmicReachProtocol.MAPPINGS.getCosmicReachBlockStates().getInt(blockState);
        if (blockStateId != -1) {
            return blockStateId;
        } else {
            ViaCosmicReach.getPlatform().getLogger().log(Level.WARNING, "Missing cosmic reach block state mapping for " + blockStateString);
            return CosmicReachProtocol.MAPPINGS.getCosmicReachBlockStates().getInt(CosmicReachBlockState.AIR);
        }
    };

    public ChunkSectionType() {
        super(CosmicReachChunkSection.class);
    }

    @Override
    public CosmicReachChunkSection read(ByteBuf buffer) {
        final NettyChunkByteReader reader = new NettyChunkByteReader(buffer);
        try {
            final byte blockDataType = buffer.readByte();
            final IBlockData<Integer> blockData = switch (blockDataType) {
                case BLOCK_SINGLE -> SingleBlockData.readFrom(reader, SAVE_KEY_TO_BLOCK_VALUE);
                case BLOCK_LAYERED -> LayeredBlockData.readFrom(reader, SAVE_KEY_TO_BLOCK_VALUE);
                default -> throw new RuntimeException("Unknown block data type: " + blockDataType);
            };

            final byte skylightDataType = reader.readByte();
            final ISkylightData skylightData = switch (skylightDataType) {
                case SKYLIGHTDATA_NULL -> null;
                case SKYLIGHTDATA_LAYERED -> {
                    final SkylightLayeredData skylightLayeredData = new SkylightLayeredData();
                    for (int i = 0; i < 16; i++) {
                        final byte layerType = reader.readByte();
                        switch (layerType) {
                            case 1 -> skylightLayeredData.setLayer(i, SkylightDataSingleLayer.getForLightValue(reader.readByte()));
                            case 2 -> skylightLayeredData.setLayer(i, new SkylightDataNibbleLayer(ByteBufUtil.getBytes(buffer.readSlice(128))));
                            default -> throw new RuntimeException("Unknown layered skylight layer type: " + layerType);
                        }
                    }
                    yield skylightLayeredData;
                }
                case SKYLIGHTDATA_SINGLE -> SkylightSingleData.getForLightValue(reader.readByte());
                default -> throw new RuntimeException("Unknown skylight data type: " + skylightDataType);
            };

            final byte blockLightDataType = reader.readByte();
            final IBlockLightData blockLightData = switch (blockLightDataType) {
                case BLOCKLIGHTDATA_NULL -> null;
                case BLOCKLIGHTDATA_LAYERED -> {
                    final BlockLightLayeredData blockLightLayeredData = new BlockLightLayeredData();
                    for (int i = 0; i < 16; i++) {
                        blockLightLayeredData.setLayer(i, IBlockLightLayer.readFrom(reader.readByte(), i, blockLightLayeredData, reader));
                    }
                    yield blockLightLayeredData;
                }
                default -> throw new RuntimeException("Unknown block light data type: " + blockLightDataType);
            };

            final List<CosmicReachBlockEntity> blockEntities = new ArrayList<>();
            final byte blockEntityDataType = reader.readByte();
            switch (blockEntityDataType) {
                case BLOCKENTITY_NULL -> {
                }
                case BLOCKENTITY_DATA -> {
                    final byte[] crBinData = new byte[reader.readInt()];
                    reader.readFully(crBinData);
                    final CRBinDeserializer crBinDeserializer = new CRBinDeserializer();
                    crBinDeserializer.prepareForRead(ByteBuffer.wrap(crBinData));
                    final CRBinDeserializer[] crBinBlockEntities = crBinDeserializer.readRawObjArray("blockEntities");
                    for (CRBinDeserializer crBinBlockEntity : crBinBlockEntities) {
                        blockEntities.add(new CosmicReachBlockEntity(crBinBlockEntity));
                    }
                }
                default -> throw new RuntimeException("Unknown block entity data type: " + blockEntityDataType);
            }

            return new CosmicReachChunkSection(blockData, skylightData, blockLightData, blockEntities);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(ByteBuf buffer, CosmicReachChunkSection value) {
        throw new UnsupportedOperationException("Cannot serialize CosmicReachChunkSection");
    }

}
