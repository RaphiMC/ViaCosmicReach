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

import net.raphimc.viacosmicreach.platform.ViaCosmicReachPlatform;

public class ViaCosmicReach {

    public static final String VERSION = "${version}";
    public static final String IMPL_VERSION = "${impl_version}";

    private static ViaCosmicReachPlatform platform;

    private ViaCosmicReach() {
    }

    public static void init(final ViaCosmicReachPlatform platform) {
        if (ViaCosmicReach.platform != null) throw new IllegalStateException("ViaCosmicReach is already initialized");

        ViaCosmicReach.platform = platform;
    }

    public static ViaCosmicReachPlatform getPlatform() {
        return ViaCosmicReach.platform;
    }

}
