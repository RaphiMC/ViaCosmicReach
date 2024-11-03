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

import com.viaversion.viaversion.api.connection.StorableObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NetworkSettingsStorage implements StorableObject {

    private final Map<String, Integer> intProperties = new HashMap<>();
    private final Map<String, Boolean> booleanProperties = new HashMap<>();

    public int getIntProperty(final String key, final int defaultValue) {
        return this.intProperties.getOrDefault(key, defaultValue);
    }

    public boolean setIntProperty(final String key, final int value) {
        return !Objects.equals(this.intProperties.put(key, value), value);
    }

    public boolean getBooleanProperty(final String key, final boolean defaultValue) {
        return this.booleanProperties.getOrDefault(key, defaultValue);
    }

    public boolean setBooleanProperty(final String key, final boolean value) {
        return !Objects.equals(this.booleanProperties.put(key, value), value);
    }

}
