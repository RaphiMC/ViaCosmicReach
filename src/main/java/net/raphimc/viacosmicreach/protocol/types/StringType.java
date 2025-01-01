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
package net.raphimc.viacosmicreach.protocol.types;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class StringType extends Type<String> {

    public StringType() {
        super(String.class);
    }

    @Override
    public String read(ByteBuf buffer) {
        final int length = buffer.readInt();
        if (length == -1) {
            return null;
        } else {
            final byte[] bytes = new byte[length];
            buffer.readBytes(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    @Override
    public void write(ByteBuf buffer, String value) {
        if (value == null) {
            buffer.writeInt(-1);
        } else {
            final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            buffer.writeInt(bytes.length);
            buffer.writeBytes(bytes);
        }
    }

}
