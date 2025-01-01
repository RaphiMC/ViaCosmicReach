/*
 * This file is part of ViaCosmicReach - https://github.com/RaphiMC/ViaCosmicReach
 * Copyright (C) 2024-2025 RK_01/RaphiMC and contributors
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
package net.raphimc.viacosmicreach.protocol.data;

import com.viaversion.nbt.io.NBTIO;
import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntOpenHashMap;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.GsonUtil;
import net.raphimc.viabedrock.api.model.BlockState;
import net.raphimc.viabedrock.api.util.RegistryUtil;
import net.raphimc.viacosmicreach.ViaCosmicReach;
import net.raphimc.viacosmicreach.api.CosmicReachProtocolVersion;
import net.raphimc.viacosmicreach.api.model.CosmicReachBlockState;
import net.raphimc.viacosmicreach.protocol.CosmicReachProtocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class CosmicReachMappingData extends MappingDataBase {

    private CompoundTag minecraftRegistries;
    private CompoundTag minecraftTags;
    private Object2IntMap<BlockState> minecraftBlockStates;
    private Object2IntMap<CosmicReachBlockState> cosmicReachBlockStates;
    private Int2IntMap cosmicReachToMinecraftBlockStates;
    private int minecraftPlainsBiomeId;

    public CosmicReachMappingData() {
        super(CosmicReachProtocolVersion.cosmicReachLatest.getName(), ProtocolConstants.MINECRAFT_VERSION.getName());
    }

    @Override
    public void load() {
        if (Via.getManager().isDebug()) {
            this.getLogger().info("Loading " + this.unmappedVersion + " -> " + this.mappedVersion + " mappings...");
        }

        final JsonObject viaMappingJson = this.readJson("minecraft/via_mappings.json");
        this.minecraftRegistries = this.readNBT("minecraft/registries.nbt");
        this.minecraftTags = this.readNBT("minecraft/tags.nbt");

        { // Modify dimension registry
            final CompoundTag dimensionRegistry = this.minecraftRegistries.getCompoundTag("minecraft:dimension_type");
            dimensionRegistry.remove("minecraft:overworld_caves");
            dimensionRegistry.remove("minecraft:the_nether");
            dimensionRegistry.remove("minecraft:the_end");
            final CompoundTag overworld = dimensionRegistry.getCompoundTag("minecraft:overworld");
            overworld.putInt("min_y", ProtocolConstants.WORLD_MIN_Y);
            overworld.putInt("height", ProtocolConstants.WORLD_HEIGHT);
            overworld.putInt("logical_height", ProtocolConstants.WORLD_HEIGHT);
        }

        final JsonArray cosmicReachBlockStatesJson = this.readJson("cosmicreach/blockstates.json", JsonArray.class);
        this.cosmicReachBlockStates = new Object2IntOpenHashMap<>(cosmicReachBlockStatesJson.size());
        this.cosmicReachBlockStates.defaultReturnValue(-1);
        for (int i = 0; i < cosmicReachBlockStatesJson.size(); i++) {
            final CosmicReachBlockState cosmicReachBlockState = CosmicReachBlockState.fromString(cosmicReachBlockStatesJson.get(i).getAsString());
            this.cosmicReachBlockStates.put(cosmicReachBlockState, i);
        }
        final JsonArray minecraftBlockStatesJson = viaMappingJson.getAsJsonArray("blockstates");
        this.minecraftBlockStates = new Object2IntOpenHashMap<>(minecraftBlockStatesJson.size());
        this.minecraftBlockStates.defaultReturnValue(-1);
        for (int i = 0; i < minecraftBlockStatesJson.size(); i++) {
            final BlockState blockState = BlockState.fromString(minecraftBlockStatesJson.get(i).getAsString());
            this.minecraftBlockStates.put(blockState, i);
        }
        final JsonObject cosmicReachToMinecraftBlockStateMappingsJson = this.readJson("custom/blockstate_mappings.json");
        this.cosmicReachToMinecraftBlockStates = new Int2IntOpenHashMap(cosmicReachToMinecraftBlockStateMappingsJson.size());
        this.cosmicReachToMinecraftBlockStates.defaultReturnValue(-1);
        for (Map.Entry<String, JsonElement> entry : cosmicReachToMinecraftBlockStateMappingsJson.entrySet()) {
            final CosmicReachBlockState cosmicReachBlockState = CosmicReachBlockState.fromString(entry.getKey());
            if (!this.cosmicReachBlockStates.containsKey(cosmicReachBlockState)) {
                throw new RuntimeException("Unknown cosmic reach block state: " + entry.getKey());
            }
            if (entry.getValue().getAsString().isEmpty()) {
                continue; // TODO: Remove this once all mappings are done
            }
            final BlockState minecraftBlockState = BlockState.fromString(entry.getValue().getAsString());
            if (!this.minecraftBlockStates.containsKey(minecraftBlockState)) {
                throw new RuntimeException("Unknown minecraft block state: " + entry.getValue().getAsString());
            }
            if (this.cosmicReachToMinecraftBlockStates.put(this.cosmicReachBlockStates.getInt(cosmicReachBlockState), this.minecraftBlockStates.getInt(minecraftBlockState)) != -1) {
                throw new RuntimeException("Duplicate cosmic reach -> minecraft block state mapping for " + entry.getKey());
            }
        }
        /*for (CosmicReachBlockState cosmicReachBlockState : cosmicReachBlockStates) { // TODO: Add this once all mappings are done
            if (!this.cosmicReachToMinecraftBlockStates.containsKey(cosmicReachBlockState)) {
                throw new RuntimeException("Missing cosmic reach -> minecraft block state mapping for " + cosmicReachBlockState);
            }
        }*/

        final CompoundTag biomeRegistry = CosmicReachProtocol.MAPPINGS.getMinecraftRegistries().getCompoundTag("minecraft:worldgen/biome");
        this.minecraftPlainsBiomeId = RegistryUtil.getRegistryIndex(biomeRegistry, biomeRegistry.getCompoundTag("minecraft:plains"));
    }

    public CompoundTag getMinecraftRegistries() {
        return this.minecraftRegistries;
    }

    public CompoundTag getMinecraftTags() {
        return this.minecraftTags;
    }

    public Object2IntMap<BlockState> getMinecraftBlockStates() {
        return this.minecraftBlockStates;
    }

    public Object2IntMap<CosmicReachBlockState> getCosmicReachBlockStates() {
        return this.cosmicReachBlockStates;
    }

    public Int2IntMap getCosmicReachToMinecraftBlockStates() {
        return this.cosmicReachToMinecraftBlockStates;
    }

    public int getMinecraftPlainsBiomeId() {
        return this.minecraftPlainsBiomeId;
    }

    @Override
    protected Logger getLogger() {
        return ViaCosmicReach.getPlatform().getLogger();
    }

    private CompoundTag readNBT(String file) {
        file = "assets/viacosmicreach/data/" + file;
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(file)) {
            if (inputStream == null) {
                this.getLogger().severe("Could not open " + file);
                return null;
            }

            return NBTIO.readTag(new DataInputStream(new GZIPInputStream(inputStream)), TagLimiter.noop(), true, CompoundTag.class);
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not read " + file, e);
            return null;
        }
    }

    private JsonObject readJson(String file) {
        return this.readJson(file, JsonObject.class);
    }

    private <T> T readJson(String file, final Class<T> classOfT) {
        file = "assets/viacosmicreach/data/" + file;
        try (final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(file)) {
            if (inputStream == null) {
                this.getLogger().severe("Could not open " + file);
                return null;
            }

            return GsonUtil.getGson().fromJson(new InputStreamReader(inputStream), classOfT);
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not read " + file, e);
            return null;
        }
    }

}
