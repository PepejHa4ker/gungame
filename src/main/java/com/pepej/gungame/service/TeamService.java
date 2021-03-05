package com.pepej.gungame.service;

import com.pepej.gungame.user.User;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
import java.util.Set;

public interface TeamService {

    void register(@NonNull String arenaName, @NonNull Team team);

    void unregister(@NonNull String arenaName, @NonNull String teamName);

    Optional<Team> getTeam(@NonNull String arenaName, @NonNull String teamName);

    Optional<Team> getUserTeam(@NonNull User user);

    @NonNull Set<Team> getArenaTeams(@NonNull String arenaName);

}
