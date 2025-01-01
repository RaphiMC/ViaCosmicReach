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
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParser;
import net.raphimc.viacosmicreach.protocol.model.account.ItchAccount;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public record ItchAccountStorage(ItchAccount account, String apiKey) implements StorableObject {

    private static final URI AUTH_URL = URI.create("https://cosmicreach-auth.finalforeach.com/verify-itch");

    public String auth(final String challenge) {
        try {
            final JsonObject request = new JsonObject();
            request.addProperty("itchApiKey", this.apiKey);
            request.addProperty("serverChallenge", challenge);
            request.addProperty("keyType", "itchApi");

            final HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(AUTH_URL)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(request.toString()))
                    .build();
            final HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.of(5, ChronoUnit.SECONDS))
                    .build();

            final HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.statusCode() != 200) {
                throw new IllegalStateException("Invalid response: " + httpResponse.body());
            }

            final JsonObject response = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
            return response.get("sessionToken").getAsString();
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to authenticate with CosmicReach", e);
        }
    }

}
