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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.viaversion.viaversion.api.connection.StorableObject;
import net.raphimc.viacosmicreach.protocol.model.UniqueEntityId;
import net.raphimc.viacosmicreach.protocol.model.account.Account;

import java.util.concurrent.atomic.AtomicInteger;

public class EntityTracker implements StorableObject {

    private final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private Account clientPlayerAccount;
    private final BiMap<String, UniqueEntityId> playerMap = HashBiMap.create();
    private final BiMap<UniqueEntityId, Integer> entityMap = HashBiMap.create();
    private final BiMap<String, Account> accountMap = HashBiMap.create();

    public Account getClientPlayerAccount() {
        return this.clientPlayerAccount;
    }

    public void setClientPlayerAccount(final Account clientPlayerAccount) {
        this.clientPlayerAccount = clientPlayerAccount;
    }

    public int addPlayer(final Account account, final UniqueEntityId uniqueEntityId) {
        return this.addPlayer(account, uniqueEntityId, this.ID_COUNTER.getAndIncrement());
    }

    public int addPlayer(final Account account, final UniqueEntityId uniqueEntityId, final int entityId) {
        this.playerMap.put(account.uniqueId(), uniqueEntityId);
        this.entityMap.put(uniqueEntityId, entityId);
        this.accountMap.put(account.uniqueId(), account);
        return entityId;
    }

    public int addEntity(final UniqueEntityId uniqueEntityId) {
        final int entityId = this.ID_COUNTER.getAndIncrement();
        this.entityMap.put(uniqueEntityId, entityId);
        return entityId;
    }

    public void removePlayer(final String uniquePlayerId) {
        final UniqueEntityId uniqueEntityId = this.playerMap.remove(uniquePlayerId);
        this.entityMap.remove(uniqueEntityId);
        this.accountMap.remove(uniquePlayerId);
    }

    public void removeEntity(final UniqueEntityId uniqueEntityId) {
        this.entityMap.remove(uniqueEntityId);
    }

    public boolean hasPlayer(final String uniquePlayerId) {
        return this.playerMap.containsKey(uniquePlayerId);
    }

    public boolean hasEntity(final UniqueEntityId uniqueEntityId) {
        return this.entityMap.containsKey(uniqueEntityId);
    }

    public int getEntityIdByUniquePlayerId(final String uniquePlayerId) {
        return this.entityMap.get(this.playerMap.get(uniquePlayerId));
    }

    public int getEntityIdByUniqueEntityId(final UniqueEntityId uniqueEntityId) {
        return this.entityMap.get(uniqueEntityId);
    }

    public UniqueEntityId getUniqueEntityIdByEntityId(final int entityId) {
        return this.entityMap.inverse().get(entityId);
    }

    public Account getAccountByUniquePlayerId(final String uniquePlayerId) {
        return this.accountMap.get(uniquePlayerId);
    }

}
