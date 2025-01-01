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
package net.raphimc.viacosmicreach.protocol.model.account;

import java.util.concurrent.ThreadLocalRandom;

public class OfflineAccount extends Account {

    public static final String TYPE = "offline";

    public static OfflineAccount create(final String username) {
        return new OfflineAccount(TYPE + ':' + username, TYPE + "_id:" + ThreadLocalRandom.current().nextLong(Long.MAX_VALUE));
    }

    public OfflineAccount(final String username, final String uniqueId) {
        super(username, uniqueId);
    }

    @Override
    public String type() {
        return TYPE;
    }

}
