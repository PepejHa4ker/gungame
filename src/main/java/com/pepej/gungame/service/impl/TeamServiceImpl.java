package com.pepej.gungame.service.impl;

import com.pepej.gungame.service.TeamService;
import com.pepej.gungame.user.User;
import com.pepej.papi.utils.Log;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeamServiceImpl implements TeamService {

    @NonNull Map<String, Set<Team>> teamArenaMap;

    public TeamServiceImpl() {
        this.teamArenaMap = new LinkedHashMap<>();
    }

    @Override
    public void register(@NonNull String arenaName, @NonNull Team team) {
        if (teamArenaMap.containsKey(arenaName)) {
            Set<Team> storedTeams = teamArenaMap.get(arenaName);
            if (storedTeams.stream().anyMatch(t -> t.getContext().getName().equals(team.getContext().getName()))) {
                Log.info("Team with name " + team.getContext().getName() + " already register!");
                return;
            }
            storedTeams.add(team);
        } else {
            Set<Team> singleTeam = new LinkedHashSet<>(1);
            singleTeam.add(team);
            teamArenaMap.put(arenaName, singleTeam);
        }
        Log.info("Team with name " + team.getContext().getName() + " successfully registered!");
    }

    @Override
    public void unregister(@NonNull String arenaName, @NonNull String teamName) {
        teamArenaMap.get(arenaName).removeIf(team -> team.getContext().getName().equals(teamName));
    }

    @Override
    public Optional<Team> getTeam(@NonNull String arenaName, @NonNull String teamName) {
        return teamArenaMap.get(arenaName)
                           .stream()
                           .filter(team -> team.getContext().getName().equals(teamName))
                           .findFirst();
    }

    @Override
    public Optional<Team> getUserTeam(@NonNull User user) {
        Collection<Set<Team>> storedTeams = teamArenaMap.values();
        for (Set<Team> teams : storedTeams) {
            for (Team team : teams) {
                if (team.getUsers().contains(user)) {
                    return Optional.of(team);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public @NonNull Set<Team> getArenaTeams(@NonNull String arenaName) {
        return teamArenaMap.get(arenaName);
    }

}
