package com.pepej.gungame.repository;

import com.pepej.gungame.rpg.quest.Quest;
import com.pepej.gungame.rpg.quest.QuestType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface QuestRepository {

    @NonNull
    @Unmodifiable
    List<Quest> getUserQuests(String userId);

    void updateQuest(String questId, boolean completed, long completionTime, int progress);

    boolean questExists(String questId);

    void saveQuests(String questId, String userId, long creationTime, boolean completed, long completionTime, int progress, QuestType type);

}
