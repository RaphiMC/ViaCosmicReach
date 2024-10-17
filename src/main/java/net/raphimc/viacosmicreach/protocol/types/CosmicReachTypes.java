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

import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viacosmicreach.api.chunk.CosmicReachChunkSection;
import net.raphimc.viacosmicreach.protocol.model.Account;

public class CosmicReachTypes {

    public static final Type<String> STRING = new StringType();
    public static final Type<JsonObject> JSON_OBJECT = new JsonObjectType();

    public static final Type<Account> ACCOUNT = new AccountType();
    public static final Type<CosmicReachChunkSection> CHUNK_SECTION = new ChunkSectionType();
    public static final Type<BlockPosition> BLOCK_POSITION = new BlockPositionType();

    public static Type<Chunk> MINECRAFT_CHUNK;

}