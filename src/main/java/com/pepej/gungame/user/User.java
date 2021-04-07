package com.pepej.gungame.user;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.rpg.quest.Quest;
import com.pepej.papi.serialize.Point;
import com.pepej.papi.utils.Players;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
@EqualsAndHashCode(of = {"id", "username"})
@ToString(exclude = "currentArena")
public class User  {
    private final UUID id;
    private final String username;
    private boolean spectator;
    private boolean died;
    private int gamesPlayed;
    private int deaths;
    private int wins;
    private int kills;
    private int levelsReached;
    private int exp;
    private Set<Quest> quests;
    //local stats
    private int localExp;
    private int localKills;
    private int localLevelsReached;


    public User(final UUID id, final String username) {
        quests = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.id = id;
        this.username = username;
    }

    @Nullable
    private Arena currentArena;

    @NonNull
    public Optional<Arena> getCurrentArenaSafe() {
        return Optional.ofNullable(currentArena);
    }


    public Player asPlayer() {
        return Players.get(this.id).orElse(null);
    }

    public Location location() {
        return asPlayer().getLocation();
    }

    public void teleport(Point pos) {
        Player player = asPlayer();
        if (player != null) {
            player.teleport(pos.toLocation());
        }
    }




}