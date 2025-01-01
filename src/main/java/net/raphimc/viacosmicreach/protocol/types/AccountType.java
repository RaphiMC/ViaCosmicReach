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
package net.raphimc.viacosmicreach.protocol.types;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import net.raphimc.viacosmicreach.api.util.JsonUtil;
import net.raphimc.viacosmicreach.protocol.model.account.Account;
import net.raphimc.viacosmicreach.protocol.model.account.ItchAccount;
import net.raphimc.viacosmicreach.protocol.model.account.OfflineAccount;

public class AccountType extends Type<Account> {

    public AccountType() {
        super(Account.class);
    }

    @Override
    public Account read(ByteBuf buffer) {
        final String type = CosmicReachTypes.STRING.read(buffer);
        JsonObject obj = CosmicReachTypes.JSON_OBJECT.read(buffer);

        final String username = obj.get("username").getAsString();
        final String uniqueId = obj.get("uniqueId").getAsString();
        return switch (type) {
            case OfflineAccount.TYPE -> new OfflineAccount(username, uniqueId);
            case ItchAccount.TYPE -> {
                final long expiresAtEpochSecond = obj.get("expiresAtEpochSecond").getAsLong();
                final JsonObject profile = obj.getAsJsonObject("profile");

                final String displayName = JsonUtil.getStringOr(profile, "display_name", username);
                final String coverUrl = JsonUtil.getStringOr(profile, "cover_url", null);
                final String url = profile.get("url").getAsString().trim();
                final long id = profile.get("id").getAsLong();
                yield new ItchAccount(username, uniqueId, displayName, coverUrl, url, id, expiresAtEpochSecond);
            }
            default -> throw new IllegalArgumentException("Unexpected account type: " + type);
        };
    }

    @Override
    public void write(ByteBuf buffer, Account value) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("username", value.username());
        obj.addProperty("uniqueId", value.uniqueId());

        if (value instanceof ItchAccount itchAccount) {
            final JsonObject profile = new JsonObject();
            profile.addProperty("username", itchAccount.username().replace(itchAccount.type() + ':', ""));
            profile.addProperty("display_name", itchAccount.displayName());
            profile.addProperty("cover_url", itchAccount.coverUrl());
            profile.addProperty("url", itchAccount.url());
            profile.addProperty("id", itchAccount.id());
            profile.addProperty("expiresAtEpochSecond", itchAccount.expiresAtEpochSecond());
            obj.add("profile", profile);
        }

        CosmicReachTypes.STRING.write(buffer, value.type());
        CosmicReachTypes.JSON_OBJECT.write(buffer, obj);
    }

}
