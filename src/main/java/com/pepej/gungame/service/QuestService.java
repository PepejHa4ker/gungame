package com.pepej.gungame.service;

import com.pepej.gungame.rpg.quest.Quest;
import com.pepej.gungame.rpg.quest.QuestType;
import com.pepej.gungame.service.impl.QuestServiceImpl;
import com.pepej.gungame.user.User;
import com.pepej.papi.services.Implementor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Implementor(QuestServiceImpl.class)
public interface QuestService {

    void register(User user, Quest quest);

    void unregister(User user, QuestType quest);

    Quest getQuestByType(final QuestType type, UUID id);

    Set<Quest> getActiveQuests(User user);

    List<Quest> getActiveQuests(User user, QuestType questType);

    @Nullable
    Quest getLastCreationQuestTime(User user, QuestType questType);

    boolean canUserTakeQuest(User user,  QuestType questType);

    void onUpdate(Quest quest);

}
