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

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.libs.mcstructs.text.serializer.TextComponentCodec;

public class ProtocolConstants {

    public static final ProtocolVersion MINECRAFT_VERSION = ProtocolVersion.v1_21;
    public static final TextComponentCodec MINECRAFT_TEXT_COMPONENT_SERIALIZER = TextComponentCodec.V1_20_5;
    public static final int MINECRAFT_VIEW_DISTANCE = 32;

    public static final int WORLD_MIN_Y = -1024;
    public static final int WORLD_MAX_Y = 1024;
    public static final int WORLD_HEIGHT = WORLD_MAX_Y - WORLD_MIN_Y;

}
