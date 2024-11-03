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
package net.raphimc.viacosmicreach.api.util;


import com.viaversion.viaversion.api.minecraft.Vector3f;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;

public class JsonUtil {

    public static String getStringOr(final JsonObject obj, final String key, final String defaultValue) {
        final JsonElement element = obj.get(key);
        if (element != null && !element.isJsonNull()) {
            return element.getAsString();
        } else {
            return defaultValue;
        }
    }

    public static boolean getBooleanOr(final JsonObject obj, final String key, final boolean defaultValue) {
        final JsonElement element = obj.get(key);
        if (element != null && !element.isJsonNull()) {
            return element.getAsBoolean();
        } else {
            return defaultValue;
        }
    }

    public static int getIntOr(final JsonObject obj, final String key, final int defaultValue) {
        final JsonElement element = obj.get(key);
        if (element != null && !element.isJsonNull()) {
            return element.getAsInt();
        } else {
            return defaultValue;
        }
    }

    public static long getLongOr(final JsonObject obj, final String key, final long defaultValue) {
        final JsonElement element = obj.get(key);
        if (element != null && !element.isJsonNull()) {
            return element.getAsLong();
        } else {
            return defaultValue;
        }
    }

    public static float getFloatOr(final JsonObject obj, final String key, final float defaultValue) {
        final JsonElement element = obj.get(key);
        if (element != null && !element.isJsonNull()) {
            return element.getAsFloat();
        } else {
            return defaultValue;
        }
    }

    public static Vector3f getVector3f(final JsonObject obj, final String key) {
        final JsonObject vector = obj.getAsJsonObject(key);
        return new Vector3f(getFloatOr(vector, "x", 0F), getFloatOr(vector, "y", 0F), getFloatOr(vector, "z", 0F));
    }

}
