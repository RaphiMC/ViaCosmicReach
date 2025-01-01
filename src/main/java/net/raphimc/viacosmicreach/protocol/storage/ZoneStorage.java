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
import com.viaversion.viaversion.api.minecraft.Vector3f;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viacosmicreach.api.util.JsonUtil;

public class ZoneStorage implements StorableObject {

    private final String id;
    private final Vector3f spawnPoint;

    public ZoneStorage(final JsonObject zoneData) {
        this.id = zoneData.get("zoneId").getAsString();
        this.spawnPoint = JsonUtil.getVector3f(zoneData, "spawnPoint", new Vector3f(0F, 0F, 0F));
    }

    public String getId() {
        return this.id;
    }

    public Vector3f getSpawnPoint() {
        return this.spawnPoint;
    }

}
