package com.pepej.gungame.rpg.trap;

public enum TrapType {

    BLINDNESS(45),
    POISON_ARENA(45),
    SUPER_BOW(90),
    ;

    private final int delayInSeconds;

    TrapType(final int delayInSeconds) {
        this.delayInSeconds = delayInSeconds;
    }

    public int getDelayInSeconds() {
        return delayInSeconds;
    }
}
