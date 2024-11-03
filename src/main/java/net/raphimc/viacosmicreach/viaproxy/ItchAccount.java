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
package net.raphimc.viacosmicreach.viaproxy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.lenni0451.commons.httpclient.HttpClient;
import net.lenni0451.commons.httpclient.constants.Headers;
import net.lenni0451.commons.httpclient.handler.ThrowingResponseHandler;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.viaproxy.saves.impl.accounts.Account;

import java.util.UUID;

public class ItchAccount extends Account {

    private boolean refreshed;

    private final String apiKey;
    private String username;
    private String displayName;
    private String coverUrl;
    private String url;
    private long id;

    public ItchAccount(final JsonObject jsonObject) {
        this.apiKey = jsonObject.get("apiKey").getAsString();
        this.username = jsonObject.get("username").getAsString();
        this.displayName = jsonObject.get("displayName").getAsString();
        this.coverUrl = jsonObject.has("coverUrl") ? jsonObject.get("coverUrl").getAsString() : null;
        this.url = jsonObject.get("url").getAsString();
        this.id = jsonObject.get("id").getAsLong();
    }

    public ItchAccount(final String apiKey) throws Exception {
        this.apiKey = apiKey;
        this.refresh();
    }

    @Override
    public JsonObject toJson() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("apiKey", this.apiKey);
        jsonObject.addProperty("username", this.username);
        jsonObject.addProperty("displayName", this.displayName);
        if (this.coverUrl != null) {
            jsonObject.addProperty("coverUrl", this.coverUrl);
        }
        jsonObject.addProperty("url", this.url);
        jsonObject.addProperty("id", this.id);
        return jsonObject;
    }

    @Override
    public String getName() {
        return this.displayName;
    }

    @Override
    public UUID getUUID() {
        return new UUID(this.id, 0);
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public String getUsername() {
        return this.username;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getCoverUrl() {
        return this.coverUrl;
    }

    public String getUrl() {
        return this.url;
    }

    public long getId() {
        return this.id;
    }

    @Override
    public String getDisplayString() {
        return this.getName() + " (Itch)";
    }

    @Override
    public boolean refresh() throws Exception {
        if (this.refreshed) {
            return false;
        } else {
            final HttpClient httpClient = MinecraftAuth.createHttpClient();
            final JsonObject response = JsonParser.parseString(httpClient
                    .get("https://itch.io/api/1/key/me")
                    .appendHeader(Headers.AUTHORIZATION, "Bearer " + this.apiKey)
                    .execute(new ThrowingResponseHandler())
                    .getContentAsString()).getAsJsonObject();

            final JsonObject user = response.get("user").getAsJsonObject();
            this.username = user.get("username").getAsString();
            this.displayName = user.has("display_name") ? user.get("display_name").getAsString() : this.username;
            this.coverUrl = user.has("cover_url") ? user.get("cover_url").getAsString() : null;
            this.url = user.get("url").getAsString();
            this.id = user.get("id").getAsLong();

            this.refreshed = true;
            return true;
        }
    }

}
