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
package net.raphimc.viacosmicreach.viaproxy;

import com.viaversion.vialoader.util.JLoggerToSLF4J;
import net.raphimc.viacosmicreach.platform.ViaCosmicReachPlatform;
import org.slf4j.LoggerFactory;

import java.util.logging.Logger;

public class ViaCosmicReachPlatformImpl implements ViaCosmicReachPlatform {

    private static final Logger LOGGER = new JLoggerToSLF4J(LoggerFactory.getLogger("ViaCosmicReach"));

    public ViaCosmicReachPlatformImpl() {
        this.init();
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

}
