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
package net.raphimc.viacosmicreach.protocol.task;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import net.raphimc.viacosmicreach.ViaCosmicReach;
import net.raphimc.viacosmicreach.protocol.CosmicReachProtocol;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class KeepAliveTask implements Runnable {

    @Override
    public void run() {
        for (UserConnection info : Via.getManager().getConnectionManager().getConnections()) {
            if (info.getProtocolInfo().getServerState() == State.PLAY && info.getProtocolInfo().getPipeline().contains(CosmicReachProtocol.class)) {
                info.getChannel().eventLoop().submit(() -> {
                    if (!info.getChannel().isActive()) return;

                    try {
                        final PacketWrapper keepAlive = PacketWrapper.create(ClientboundPackets1_21.KEEP_ALIVE, info);
                        keepAlive.write(Types.LONG, ThreadLocalRandom.current().nextLong()); // id
                        keepAlive.send(CosmicReachProtocol.class);
                    } catch (Throwable e) {
                        ViaCosmicReach.getPlatform().getLogger().log(Level.WARNING, "Error sending keep alive packet. See console for details.", e);
                    }
                });
            }
        }
    }

}
