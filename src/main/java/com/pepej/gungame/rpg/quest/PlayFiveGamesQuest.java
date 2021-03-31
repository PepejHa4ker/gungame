package com.pepej.gungame.rpg.quest;

import com.pepej.gungame.user.User;

import java.util.UUID;

public class PlayFiveGamesQuest extends QuestBase {
    public PlayFiveGamesQuest(final UUID id) {
        super(100, id, QuestType.PLAY_FIVE_GAMES);
    }

    @Override
    public void complete(final User user) {

    }
}
