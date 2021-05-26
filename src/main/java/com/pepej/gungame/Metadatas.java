package com.pepej.gungame;

import com.pepej.gungame.arena.ArenaConfig;
import com.pepej.gungame.rpg.trap.traps.DeathJailTrap;
import com.pepej.gungame.rpg.trap.TrapSetting;
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
    public static final MetadataKey<TrapSetting> TRAP_SETTING = MetadataKey.create("trap-setting", TrapSetting.class);
    public static final MetadataKey<Integer> SUPER_SHOTS = MetadataKey.createIntegerKey("super-shots");
    public static final MetadataKey<DeathJailTrap> JAIL_TRAP = MetadataKey.create("jail-trap", DeathJailTrap.class);
    public static final MetadataKey<Integer> JAIL_TRAP_DEATH_TIMER = MetadataKey.createIntegerKey("jail-trap-death-timer");
    public static final MetadataKey<Integer> HELP_WRITE_COUNT = MetadataKey.createIntegerKey("help-write-count");
    public static final MetadataKey<Boolean> IS_WAITING_HELP = MetadataKey.createBooleanKey("is-waiting-help");

}
