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
package net.raphimc.viacosmicreach.api.io;

import finalforeach.cosmicreach.savelib.IChunkByteReader;
import io.netty.buffer.ByteBuf;
import net.raphimc.viacosmicreach.protocol.types.CosmicReachTypes;

public record NettyChunkByteReader(ByteBuf buf) implements IChunkByteReader {

    @Override
    public int readInt() {
        return this.buf.readInt();
    }

    @Override
    public byte readByte() {
        return this.buf.readByte();
    }

    @Override
    public String readString() {
        return CosmicReachTypes.STRING.read(this.buf);
    }

    @Override
    public short readShort() {
        return this.buf.readShort();
    }

    @Override
    public void readFully(final byte[] bytes) {
        this.buf.readBytes(bytes);
    }

}
