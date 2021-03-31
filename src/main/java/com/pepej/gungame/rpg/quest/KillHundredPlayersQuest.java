package com.pepej.gungame.rpg.quest;

import com.pepej.gungame.user.User;

import java.util.UUID;

public class KillHundredPlayersQuest extends QuestBase {
    public KillHundredPlayersQuest(final UUID id) {
        super(500, id, QuestType.KILL_HUNDRED_PLAYER);
    }

    @Override
    public void complete(final User user) {

    }
}
