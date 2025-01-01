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
package net.raphimc.viacosmicreach.api.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record CosmicReachBlockState(String id, Map<String, String> properties) {

    public static final CosmicReachBlockState AIR = new CosmicReachBlockState("base:air", Collections.singletonMap("default", null));

    public CosmicReachBlockState(final String id, final Map<String, String> properties) {
        this.id = id;
        this.properties = Collections.unmodifiableMap(properties);
    }

    public static CosmicReachBlockState fromString(final String string) {
        final String[] blockStringSplit = string.split("\\[", 2);
        if (blockStringSplit.length == 1) {
            return new CosmicReachBlockState(string, Collections.emptyMap());
        } else {
            final String id = blockStringSplit[0];
            final String propertiesString = blockStringSplit[1].substring(0, blockStringSplit[1].length() - 1);
            final Map<String, String> properties = new HashMap<>();
            for (final String property : propertiesString.split(",")) {
                final String[] split = property.split("=", 2);
                if (split.length == 2) {
                    properties.put(split[0], split[1]);
                } else {
                    properties.put(split[0], null);
                }
            }
            return new CosmicReachBlockState(id, properties);
        }
    }

}
