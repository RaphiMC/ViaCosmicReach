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
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import io.netty.buffer.ByteBuf;
import net.raphimc.viacosmicreach.protocol.model.CRBin;

public class CRBinType extends Type<CRBin> {

    public CRBinType() {
        super(CRBin.class);
    }

    @Override
    public CRBin read(ByteBuf buffer) {
        final CRBinDeserializer crBinDeserializer = new CRBinDeserializer();
        crBinDeserializer.prepareForRead(buffer.nioBuffer());
        return new CRBin(crBinDeserializer);
    }

    @Override
    public void write(ByteBuf buffer, CRBin value) {
        buffer.writeBytes(value.serializer().toBytes());
    }

}
