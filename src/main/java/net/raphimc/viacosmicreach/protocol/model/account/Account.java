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
package net.raphimc.viacosmicreach.protocol.model.account;

public abstract class Account {

    private final String username;
    private final String uniqueId;

    public Account(final String username, final String uniqueId) {
        this.username = username;
        this.uniqueId = uniqueId;
    }

    public abstract String type();

    public String displayName() {
        return this.username.replace(this.type() + ':', "");
    }

    public String username() {
        return this.username;
    }

    public String uniqueId() {
        return this.uniqueId;
    }

}
