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
package net.raphimc.viacosmicreach.api.model.entity;

import com.viaversion.viaversion.api.minecraft.Vector3f;
import com.viaversion.viaversion.libs.gson.JsonObject;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;

public class Entity {

    private Vector3f position = new Vector3f(0F, 0F, 0F);

    public void readFromCrBin(final CRBinDeserializer crBin) {

    }

    public void readFromJson(final JsonObject json) {

    }

}