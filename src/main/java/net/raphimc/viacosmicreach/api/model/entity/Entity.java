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
package net.raphimc.viacosmicreach.api.model.entity;

import com.viaversion.viaversion.api.minecraft.Vector3f;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.raphimc.viacosmicreach.api.util.JsonUtil;
import net.raphimc.viacosmicreach.protocol.model.UniqueEntityId;

public class Entity {

    private final UniqueEntityId uniqueId;
    private final Vector3f viewDirection;
    private final Vector3f position;
    private final Vector3f viewPositionOffset;

    public Entity(final JsonObject json) {
        this.uniqueId = JsonUtil.getUniqueId(json, "uniqueId", null);
        this.viewDirection = JsonUtil.getVector3f(json, "viewDirection", new Vector3f(0F, 0F, 0F));
        this.position = JsonUtil.getVector3f(json, "position", new Vector3f(0F, 0F, 0F));
        this.viewPositionOffset = JsonUtil.getVector3f(json, "viewPositionOffset", new Vector3f(0F, 0F, 0F));
    }

    public UniqueEntityId uniqueId() {
        return this.uniqueId;
    }

    public Vector3f viewDirection() {
        return this.viewDirection;
    }

    public Vector3f position() {
        return this.position;
    }

    public Vector3f viewPositionOffset() {
        return this.viewPositionOffset;
    }

}
