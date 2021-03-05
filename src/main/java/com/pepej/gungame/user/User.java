package com.pepej.gungame.user;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pepej.gungame.api.Arena;
import com.pepej.papi.Papi;
import com.pepej.papi.Services;
import com.pepej.papi.adventure.platform.bukkit.BukkitAudiences;
import com.pepej.papi.adventure.text.Component;
import com.pepej.papi.gson.GsonProvider;
import com.pepej.papi.gson.GsonSerializable;
import com.pepej.papi.gson.JsonBuilder;
import com.pepej.papi.utils.UndashedUuids;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

@Data
@EqualsAndHashCode(of = {"id", "username"})
public class User implements GsonSerializable {
    private final UUID id;
    private final String username;
    private int gamesPlayed;
    private int wins;
    private int kills;
    private int levelsReached;

    @Nullable
    private Arena currentArena;

    public static User deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();
        Preconditions.checkArgument(object.has("username"));
        Preconditions.checkArgument(object.has("id"));
        return GsonProvider.prettyPrinting().fromJson(object, User.class);
    }

    public Player asPlayer() {
        return Papi.server().getPlayer(this.id);
    }

    public void sendMessage(@NonNull Component component) {
        BukkitAudiences audiences = Services.load(BukkitAudiences.class);
        audiences.sender(asPlayer()).sendMessage(component);

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