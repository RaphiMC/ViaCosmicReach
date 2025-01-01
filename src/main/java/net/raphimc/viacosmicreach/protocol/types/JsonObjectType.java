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
import com.viaversion.viaversion.libs.gson.*;
import io.netty.buffer.ByteBuf;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonObjectType extends Type<JsonObject> {

    public JsonObjectType() {
        super(JsonObject.class);
    }

    @Override
    public JsonObject read(ByteBuf buffer) {
        final String json = CosmicReachTypes.STRING.read(buffer);
        if (json == null) {
            return null;
        } else {
            return convert(new UnquotedValueJSONTokener(json).nextValue()).getAsJsonObject();
        }
    }

    @Override
    public void write(ByteBuf buffer, JsonObject value) {
        if (value == null) {
            CosmicReachTypes.STRING.write(buffer, null);
        } else {
            CosmicReachTypes.STRING.write(buffer, value.toString());
        }
    }

    private static JsonElement convert(final Object object) {
        if (object instanceof Boolean b) {
            return new JsonPrimitive(b);
        } else if (object instanceof Number n) {
            return new JsonPrimitive(n);
        } else if (object instanceof String s) {
            return new JsonPrimitive(s);
        } else if (object instanceof JSONObject jsonObject) {
            final JsonObject json = new JsonObject();
            for (final String key : jsonObject.keySet()) {
                json.add(key, convert(jsonObject.get(key)));
            }
            return json;
        } else if (object instanceof JSONArray jsonArray) {
            final JsonArray json = new JsonArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                json.add(convert(jsonArray.get(i)));
            }
            return json;
        } else if (object == JSONObject.NULL) {
            return JsonNull.INSTANCE;
        } else {
            throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
        }
    }

    private static class UnquotedValueJSONTokener extends JSONTokener {

        public UnquotedValueJSONTokener(final String json) {
            super(json);
        }

        @Override
        public Object nextValue() throws JSONException {
            char c = this.nextClean();
            switch (c) {
                case '{':
                    this.back();
                    try {
                        return new JSONObject(this);
                    } catch (StackOverflowError e) {
                        throw new JSONException("JSON Array or Object depth too large to process.", e);
                    }
                case '[':
                    this.back();
                    try {
                        return new JSONArray(this);
                    } catch (StackOverflowError e) {
                        throw new JSONException("JSON Array or Object depth too large to process.", e);
                    }
            }
            return this.nextSimpleValue(c);
        }

        private Object nextSimpleValue(char c) {
            String string;

            switch (c) {
                case '"':
                case '\'':
                    return this.nextString(c);
            }

            final StringBuilder sb = new StringBuilder();
            while (c >= ' ' && ",]}/\\\"[{;#".indexOf(c) < 0) {
                sb.append(c);
                c = this.next();
            }
            if (!this.end()) {
                this.back();
            }

            string = sb.toString().trim();
            if (string.isEmpty()) {
                throw this.syntaxError("Missing value");
            }
            return JSONObject.stringToValue(string);
        }

    }

}
