package com.pepej.gungame.service;

import com.pepej.gungame.service.impl.ScoreboardServiceImpl;
import com.pepej.papi.scoreboard.Scoreboard;
import com.pepej.papi.services.Implementor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
import java.util.Set;

@Implementor(ScoreboardServiceImpl.class)
public interface ScoreboardService {

    void register(@NonNull String id, @NonNull Scoreboard scoreboard);

    void unregister(@NonNull String id);

    Optional<Scoreboard> getScoreboard(@NonNull String id);

    @NonNull Set<Scoreboard> getScoreboards();

}
