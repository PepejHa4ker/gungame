package com.pepej.gungame.user;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pepej.papi.gson.GsonProvider;
import com.pepej.papi.gson.GsonSerializable;
import com.pepej.papi.gson.JsonBuilder;
import com.pepej.papi.utils.UndashedUuids;
import lombok.Data;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

@Data
@ToString

public class User implements GsonSerializable {

    private final String username;
    private final UUID id;
    private int gamesPlayed;
    private int wins;
    private int kills;
    private int levelsReached;

    public static User deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();
        Preconditions.checkArgument(object.has("username"));
        Preconditions.checkArgument(object.has("id"));
        return GsonProvider.prettyPrinting().fromJson(object, User.class);
    }



    @Override
    @NonNull
    public JsonElement serialize() {
        return JsonBuilder.object()
                .add("username", username)
                .add("id", UndashedUuids.toString(id))
                .add("games_played", gamesPlayed)
                .add("wins", wins)
                .add("levels_reached", levelsReached)
                .build();
    }

}