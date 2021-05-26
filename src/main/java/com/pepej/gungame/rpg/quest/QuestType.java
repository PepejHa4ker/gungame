package com.pepej.gungame.rpg.quest;

import java.time.Duration;

public enum QuestType {

    KILL_TEN_PLAYER("Убить 10 игроков", "gungame.player", 10,500, Duration.ofHours(1).toMillis()),
    HELP_TEN_PLAYERS("Помочь 10 игрокам", "gungame.player", 10, 1000, Duration.ofDays(1).toMillis()),
    KILL_HUNDRED_PLAYER("Убить 100 игроков &a(Vip+)", "gungame.vip", 100, 2500,  Duration.ofHours(12).toMillis()),
    PLAY_FIVE_GAMES("Сыграть 5 игр &a(Vip+)", "gungame.vip", 5, 750, Duration.ofHours(1).toMillis());


    private final String displayName;
    private final String permission;
    private final int count;
    private final int reward;
    private final long expiryTimeMs;


    QuestType(final String displayName, final String permission, final int count, final int reward, final long expiryTimeMs) {
        this.displayName = displayName;
        this.permission = permission;
        this.count = count;
        this.reward = reward;
        this.expiryTimeMs = expiryTimeMs;
    }


    public String getDisplayName() {
        return displayName;
    }

    public long getExpiryTimeMs() {
        return expiryTimeMs;
    }

    public int getCount() {
        return count;
    }

    public int getReward() {
        return reward;
    }

    public String getPermission() {
        return permission;
    }
}
