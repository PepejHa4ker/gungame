package com.pepej.gungame;

import com.pepej.papi.metadata.MetadataKey;
import com.pepej.papi.scoreboard.ScoreboardObjective;

import java.util.UUID;

public final class Metadata {

    private Metadata() {
        throw new UnsupportedOperationException("This class cannot be initialized");
    }

    public static final MetadataKey<UUID> LAST_ATTACKER_KEY = MetadataKey.createUuidKey("last-attacker");
    public static final MetadataKey<Boolean> CAN_BE_ATTACKED_KEY = MetadataKey.createBooleanKey("can-be-attacked");
    public static final MetadataKey<ScoreboardObjective> SCOREBOARD_KEY = MetadataKey.create("scoreboard", ScoreboardObjective.class);

}
