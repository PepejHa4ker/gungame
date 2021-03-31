package com.pepej.gungame.rpg.quest;

import com.pepej.gungame.user.User;

import java.util.UUID;

public class KillTenPlayersQuest extends QuestBase {


    public KillTenPlayersQuest(final UUID id) {
        super(10, id, QuestType.KILL_TEN_PLAYER);
    }

    @Override
    public void complete(final User user) {
        user.asPlayer().sendMessage("Completed.");
    }
}
