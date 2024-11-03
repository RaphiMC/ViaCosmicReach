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
import io.netty.buffer.ByteBuf;
import net.raphimc.viacosmicreach.protocol.model.UniqueEntityId;

public class UniqueEntityIdType extends Type<UniqueEntityId> {

    public UniqueEntityIdType() {
        super(UniqueEntityId.class);
    }

    @Override
    public UniqueEntityId read(ByteBuf buffer) {
        return new UniqueEntityId(buffer.readLong(), buffer.readInt(), buffer.readInt());
    }

    @Override
    public void write(ByteBuf buffer, UniqueEntityId value) {
        buffer.writeLong(value.time());
        buffer.writeInt(value.rand());
        buffer.writeInt(value.number());
    }

}
