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
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.concurrent.atomic.AtomicInteger;

public class TransactionStorage implements StorableObject {

    private final AtomicInteger transactionId = new AtomicInteger(1);
    private final Int2ObjectMap<Long> pendingTransactions = new Int2ObjectOpenHashMap<>();

    public int createMinecraftTransactionId(final Long cosmicReachId) {
        final int minecraftId = this.transactionId.getAndIncrement();
        this.pendingTransactions.put(minecraftId, cosmicReachId);
        return minecraftId;
    }

    public Long getAndRemoveCosmicReachTransactionId(final int minecraftId) {
        if (!this.pendingTransactions.containsKey(minecraftId)) {
            throw new IllegalArgumentException("No transaction found with id " + minecraftId);
        } else {
            return this.pendingTransactions.remove(minecraftId);
        }
    }

}
