package com.pepej.gungame;

import com.pepej.gungame.arena.ArenaConfig;
import com.pepej.papi.metadata.MetadataKey;
import com.pepej.papi.scoreboard.ScoreboardObjective;

import java.util.UUID;

public final class Metadatas {

    private Metadatas() {
        throw new UnsupportedOperationException("This class cannot be initialized");
    }

    public static final MetadataKey<UUID> LAST_ATTACKER_KEY = MetadataKey.createUuidKey("last-attacker");
    public static final MetadataKey<Boolean> CAN_BE_ATTACKED_KEY = MetadataKey.createBooleanKey("can-be-attacked");
    public static final MetadataKey<ScoreboardObjective> SCOREBOARD_KEY = MetadataKey.create("scoreboard", ScoreboardObjective.class);
    public static final MetadataKey<ArenaConfig.ArenaConfigBuilder> CREATED_ARENA = MetadataKey.create("created-arena", ArenaConfig.ArenaConfigBuilder.class);

}
