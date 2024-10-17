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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.viaversion.viaversion.api.connection.StorableObject;
import net.raphimc.viacosmicreach.protocol.model.Account;

import java.util.concurrent.atomic.AtomicInteger;

public class EntityTracker implements StorableObject {

    private final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private Account clientPlayerAccount;
    private final BiMap<String, Integer> playerMap = HashBiMap.create();
    private final BiMap<String, Account> accountMap = HashBiMap.create();

    public Account getClientPlayerAccount() {
        return this.clientPlayerAccount;
    }

    public void setClientPlayerAccount(final Account clientPlayerAccount) {
        this.clientPlayerAccount = clientPlayerAccount;
    }

    public int addPlayer(final Account account) {
        final int entityId = this.ID_COUNTER.getAndIncrement();
        this.playerMap.put(account.uniqueId(), entityId);
        this.accountMap.put(account.uniqueId(), account);
        return entityId;
    }

    public void removePlayer(final String uniquePlayerId) {
        this.playerMap.remove(uniquePlayerId);
        this.accountMap.remove(uniquePlayerId);
    }

    public boolean hasPlayer(final String uniquePlayerId) {
        return this.playerMap.containsKey(uniquePlayerId);
    }

    public int getEntityIdByUniquePlayerId(final String uniquePlayerId) {
        return this.playerMap.get(uniquePlayerId);
    }

    public String getUniquePlayerIdByEntityId(final int entityId) {
        return this.playerMap.inverse().get(entityId);
    }

    public Account getAccountByUniquePlayerId(final String uniquePlayerId) {
        return this.accountMap.get(uniquePlayerId);
    }

}
