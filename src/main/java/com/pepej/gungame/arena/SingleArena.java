package com.pepej.gungame.arena;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.api.Team;
import com.pepej.gungame.service.TeamService;
import com.pepej.gungame.user.User;
import com.pepej.papi.serialize.Point;
import com.pepej.papi.utils.Log;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashSet;
import java.util.Set;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SingleArena implements Arena {

    @Getter @NonNull World world;
    @Getter @NonNull ArenaContext context;

    @Getter @NonNull @NonFinal ArenaState state;

    public SingleArena(final @NonNull World world, final @NonNull ArenaContext context) {
        this.world = world;
        this.context = context;
        this.state = ArenaState.DISABLED;
    }

    private void setStatus(@NonNull ArenaState state) {
        this.state = state;
    }

    @Override
    public void enable() {
        if (state != ArenaState.DISABLED) {
            Log.warn("To enable the arena, it must have state = DISABLED. Current state = " + state);
            return;
        }

        Log.info("Enabling arena %s", context.getName());
        setStatus(ArenaState.ENABLED);
    }


    @Override
    public void disable() {

    }

    @Override
    public void start() {

    }

    @Override
    public void start(final long afterTics) {

    }

    @Override
    public void stop(final ArenaStopCause cause) {

    }

    @Override
    public void stop(final ArenaStopCause cause, final long afterTics) {

    }

    @Override
    public @NonNull Team selectRandomTeam(@NonNull final User user) {
        return null;
    }

    @Override
    public void selectTeam(@NonNull final User user, final Team team) {

    }

    @Override
    public void join(@NonNull final User user) {

    }

    @ToString
    @EqualsAndHashCode
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class SingleArenaContext implements ArenaContext {

        @Getter @NonNull String name;
        @Getter @NonNull Point lobby;
        @Getter @NonNull Set<Team> teams;
        @Getter @NonNull Set<User> users;
        @NonNull TeamService teamService;

        public SingleArenaContext(
                final @NonNull String name,
                final @NonNull Point lobby,
                final @NonNull TeamService teamService) {
            this.teamService = teamService;
            this.name = name;
            this.lobby = lobby;
            this.teams = new LinkedHashSet<>(5);
            this.users = new LinkedHashSet<>(16);
//            registerTeams(new ImposterTeam(imposterSpawns, imposters),
//                    new InnocentTeam(innocentSpawns, innocents),
//                    new SpectatorTeam(spectatorSpawn));
        }

        private void registerTeams(@NonNull Team... teams) {
            for (Team team : teams) {
                teamService.register(name, team);
            }
            this.teams.addAll(teamService.getArenaTeams(name));
        }
    }
}
