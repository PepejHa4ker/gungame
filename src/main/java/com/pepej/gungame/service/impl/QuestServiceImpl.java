package com.pepej.gungame.service.impl;

import com.pepej.gungame.rpg.quest.*;
import com.pepej.gungame.service.QuestService;
import com.pepej.gungame.user.User;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class QuestServiceImpl implements QuestService {


    @Override
    public void register(final User user, final Quest quest) {
        user.getQuests().add(quest);
    }

    @Override
    public void unregister(final User user, final QuestType quest) {
        user.getQuests().stream().filter(q -> q.getType() == quest).findFirst().ifPresent(q -> user.getQuests().remove(q));
    }

    @Override
    public Quest getQuestByType(final QuestType type, UUID id) {
        if (id == null) {
            id = UUID.randomUUID();
        }
        switch (type) {
            case KILL_TEN_PLAYER:
                return new KillTenPlayersQuest(id);
            case KILL_HUNDRED_PLAYER:
                return new KillHundredPlayersQuest(id);
            case PLAY_FIVE_GAMES:
                return new PlayFiveGamesQuest(id);
        }

        throw new AssertionError();
    }

    @Override
    public Set<Quest> getActiveQuests(final User user) {
        return user.getQuests();
    }

    @Override
    public List<Quest> getActiveQuests(final User user, final QuestType questType) {
        return user.getQuests().stream().filter(q -> q.getType() == questType && q.isActive()).collect(toList());

    }

    @Override
    public @Nullable Quest getLastCreationQuestTime(final User user, final QuestType questType) {
        return user.getQuests()
                   .stream()
                   .filter(q -> q.getType() == questType)
                   .max(Comparator.comparingLong(Quest::getCreationTime))
                   .orElse(null);
    }

    @Override
    public void onUpdate(final Quest quest) {
        if (quest != null && quest.isActive()) {
            quest.setProgress(quest.getProgress() + 1);
        }
    }

    @Override
    public boolean canUserTakeQuest(final User user, final QuestType questType) {
        Quest last = getLastCreationQuestTime(user, questType);
        if (last != null) {
           return (System.currentTimeMillis() - last.getCreationTime()) >= questType.getExpiryTimeMs();
        }
        return true;
    }
}
