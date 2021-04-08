package com.pepej.gungame.rpg.trap;

public enum TrapType {

    POISON_ARENA(45, PoisonTrap.class),
    SUPER_BOW(90, SuperBowTrap.class),
    ;

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
