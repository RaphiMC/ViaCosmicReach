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
package net.raphimc.viacosmicreach.protocol.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntOpenHashMap;
import net.raphimc.viacosmicreach.ClientboundCosmicReachPackets;
import net.raphimc.viacosmicreach.ServerboundCosmicReachPackets;

import java.util.Map;

public class ProtocolStorage implements StorableObject {

    private final Int2ObjectMap<ClientboundCosmicReachPackets> clientboundPacketRegistry = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<ServerboundCosmicReachPackets> serverboundPacketRegistry = new Object2IntOpenHashMap<>();

    public ProtocolStorage() {
        for (ClientboundCosmicReachPackets packet : ClientboundCosmicReachPackets.values()) {
            this.clientboundPacketRegistry.put(packet.getId(), packet);
        }
        for (ServerboundCosmicReachPackets packet : ServerboundCosmicReachPackets.values()) {
            this.serverboundPacketRegistry.put(packet, packet.getId());
        }
    }

    public ProtocolStorage(final Map<String, Integer> packetMapping) {
        for (Map.Entry<String, Integer> entry : packetMapping.entrySet()) {
            boolean found = false;
            if (ClientboundCosmicReachPackets.getPacket(entry.getValue()) != null) {
                this.clientboundPacketRegistry.put(entry.getValue().intValue(), ClientboundCosmicReachPackets.getPacket(entry.getValue()));
                found = true;
            }
            if (ServerboundCosmicReachPackets.getPacket(entry.getValue()) != null) {
                this.serverboundPacketRegistry.put(ServerboundCosmicReachPackets.getPacket(entry.getValue()), entry.getValue().intValue());
                found = true;
            }
            if (!found) {
                throw new IllegalArgumentException("Missing packet: " + entry.getValue());
            }
        }
    }

    public ClientboundCosmicReachPackets getClientboundPacket(final int id) {
        if (!this.clientboundPacketRegistry.containsKey(id)) {
            throw new IllegalArgumentException("No packet found with id " + id);
        } else {
            return this.clientboundPacketRegistry.get(id);
        }
    }

    public int getServerboundPacketId(final ServerboundCosmicReachPackets packet) {
        if (!this.serverboundPacketRegistry.containsKey(packet)) {
            throw new IllegalArgumentException("No packet found with type " + packet);
        } else {
            return this.serverboundPacketRegistry.getInt(packet);
        }
    }

}
