package com.pepej.gungame.service.impl;

import com.pepej.gungame.service.ScoreboardService;
import com.pepej.papi.scoreboard.Scoreboard;
import com.pepej.papi.utils.Log;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScoreboardServiceImpl implements ScoreboardService {

    @NonNull Map<String, Scoreboard> scoreboards;

    public ScoreboardServiceImpl() {
        this.scoreboards = new HashMap<>();
    }

    @Override
    public void register(@NonNull String id, @NonNull Scoreboard scoreboard) {
        if (scoreboards.containsKey(id)) {
            Log.warn("Scoreboard with ID " + id + " already register!");
            return;
        }
        scoreboards.put(id, scoreboard);
    }

    @Override
    public void unregister(@NonNull String id) {
        scoreboards.remove(id);
    }

    @Override
    public Optional<Scoreboard> getScoreboard(@NonNull String id) {
        return Optional.ofNullable(scoreboards.get(id));
    }

    @Override
    public @NonNull Set<Scoreboard> getScoreboards() {
        return new HashSet<>(scoreboards.values());
    }

}
