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
package net.raphimc.viacosmicreach.platform;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.ProtocolManager;
import net.raphimc.viacosmicreach.ViaCosmicReach;
import net.raphimc.viacosmicreach.api.CosmicReachProtocolVersion;
import net.raphimc.viacosmicreach.protocol.CosmicReachProtocol;
import net.raphimc.viacosmicreach.protocol.data.ProtocolConstants;

import java.util.logging.Logger;

public interface ViaCosmicReachPlatform {

    default void init() {
        ViaCosmicReach.init(this);
        Via.getManager().getSubPlatforms().add(ViaCosmicReach.IMPL_VERSION);

        final ProtocolManager protocolManager = Via.getManager().getProtocolManager();
        protocolManager.registerProtocol(new CosmicReachProtocol(), ProtocolConstants.MINECRAFT_VERSION, CosmicReachProtocolVersion.cosmicReachLatest);
    }

    Logger getLogger();

}
