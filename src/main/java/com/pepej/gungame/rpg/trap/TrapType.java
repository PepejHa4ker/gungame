package com.pepej.gungame.rpg.trap;

import com.pepej.gungame.rpg.trap.traps.*;

public enum TrapType {

    POISON_ARENA(45, PoisonTrap.class),
    SUPER_BOW(90, SuperBowTrap.class),
    DEATH_JAIL(120, DeathJailTrap.class),
    SPA_SERVICE(90, SpaServiceTrap.class),
    FIREWORK_SMOKE(90, FireworkSmokeTrap.class);

    private final int delayInSeconds;
    private final Class<? extends Trap> trapClass;

    TrapType(final int delayInSeconds, final Class<? extends Trap> trapClass) {
        this.delayInSeconds = delayInSeconds;
        this.trapClass = trapClass;
    }

    public int getDelayInSeconds() {
        return delayInSeconds;
    }

    public Class<? extends Trap> getTrapClass() {
        return trapClass;
    }
}
