package com.pepej.gungame.rpg.quest;

public enum QuestType {

    KILL_TEN_PLAYER("Убить 10 игроков", 10, 10*60*1000),
    KILL_HUNDRED_PLAYER("Убить 100 игроков", 100, 20*60*1000),
    PLAY_FIVE_GAMES("Сыграть 5 игр", 5, 10*60*1000);


    private final String displayName;
    private final int count;
    private final long expiryTimeMs;


    QuestType(final String displayName, final int count, final long expiryTimeMs) {
        this.displayName = displayName;
        this.count = count;
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

}
