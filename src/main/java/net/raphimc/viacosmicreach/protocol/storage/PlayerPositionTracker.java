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
package net.raphimc.viacosmicreach.protocol.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Vector3f;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import net.raphimc.viacosmicreach.protocol.types.CosmicReachTypes;

public class PlayerPositionTracker extends StoredObject {

    private float x;
    private float y;
    private float z;
    private float yaw;
    private float pitch;

    private Integer lastChunkX;
    private Integer lastChunkZ;

    public PlayerPositionTracker(final UserConnection user) {
        super(user);
    }

    public void updatePosition(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;

        final int currentChunkX = (int) x >> 4;
        final int currentChunkZ = (int) z >> 4;
        if (this.lastChunkX == null || this.lastChunkX != currentChunkX || this.lastChunkZ == null || this.lastChunkZ != currentChunkZ) {
            this.lastChunkX = currentChunkX;
            this.lastChunkZ = currentChunkZ;

            this.user().get(ChunkTracker.class).updateChunkCenter(currentChunkX, currentChunkZ);
        }
    }

    public void updateRotation(final float yaw, final float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void writePlayerPosition(final PacketWrapper playerPosition) {
        playerPosition.write(CosmicReachTypes.STRING, playerPosition.user().get(EntityTracker.class).getClientPlayerAccount().uniqueId()); // player unique id
        playerPosition.write(Types.VECTOR3F, new Vector3f(this.x, this.y, this.z)); // position
        playerPosition.write(Types.VECTOR3F, new Vector3f(0F, 0F, 0F)); // view direction
        playerPosition.write(Types.VECTOR3F, new Vector3f(0F, 1.8F, 0F)); // view direction offset
        playerPosition.write(CosmicReachTypes.STRING, null); // zone id
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

}
