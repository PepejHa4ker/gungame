package com.pepej.gungame.utils;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.pepej.papi.gson.GsonProvider;
import com.pepej.papi.npc.CitizensNpc;
import com.pepej.papi.npc.CitizensNpcFactory;
import com.pepej.papi.promise.Promise;
import com.pepej.papi.serialize.Point;
import com.pepej.papi.services.Services;
import com.pepej.papi.utils.UndashedUuids;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.pepej.papi.text.Text.colorize;
import static java.lang.String.format;

public final class SquarelandApiUtils {

    @SneakyThrows
    public static Promise<GameProfile> parseProfile(String username) {
        return Promise.supplyingAsync(() -> {
            try {
                URL url = new URL(format("http://auth.squareland.ru/profile?user=%s", username));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                connection.connect();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line = reader.readLine();
                    if (line != null) {
                        final JsonObject object = GsonProvider.parser().parse(line).getAsJsonObject();
                        GameProfile profile = new GameProfile(UndashedUuids.fromString(object.get("id").getAsString()), object.get("name").getAsString());
                        profile.getProperties().put("textures", new Property("textures", object.get("properties").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString(), ""));
                        return profile;
                    }
                }
            } catch (Exception e) {
                return null;

            }
            return null;
        });
    }


    public static Promise<CitizensNpc> getUserNpc(String username, Point position) {
            CitizensNpcFactory npcFactory = Services.load(CitizensNpcFactory.class);
            return parseProfile(username).thenApplySync(profile -> {
                if (profile == null) {
                    return null;
                }
                return npcFactory.spawnNpc(position.toLocation(), colorize("&c#1&a " + username), profile.getProperties().get("textures").stream().findFirst().get().getValue(), "");
            });

    }

}
