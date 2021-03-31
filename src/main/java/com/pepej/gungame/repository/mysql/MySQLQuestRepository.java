package com.pepej.gungame.repository.mysql;

import com.pepej.gungame.repository.QuestRepository;
import com.pepej.gungame.repository.mapper.QuestRowMapper;
import com.pepej.gungame.rpg.quest.Quest;
import com.pepej.gungame.rpg.quest.QuestType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface MySQLQuestRepository extends QuestRepository {


    @Override
    @SqlQuery("SELECT * FROM gg_quests WHERE user_id = :userId")
    @RegisterRowMapper(QuestRowMapper.class)
    @NonNull
    @Unmodifiable
    List<Quest> getUserQuests(@Bind("userId") String userId);

    @Override
    @SqlQuery("SELECT EXISTS(SELECT * FROM gg_quests WHERE quest_id = :questId)")
    boolean questExists(@Bind("questId") String questId);

    @Override
    @SqlUpdate("INSERT INTO gg_quests(user_id, quest_id, creation_time, completed, completion_time, progress, type) VALUES (:userId, :questId, :creationTime, :completed, :completionTime, :progress, :type)")
    void saveQuests(
            @Bind("questId") String questId,
            @Bind("userId") String userId,
            @Bind("creationTime") long creationTime,
            @Bind("completed") boolean completed,
            @Bind("completionTime") long completionTime,
            @Bind("progress") int progress,
            @Bind("type") QuestType type
    );

    @Override
    @SqlUpdate("UPDATE gg_quests SET completed = :completed, progress = :progress, completion_time = :completionTime WHERE quest_id = :questId")
    void updateQuest(@Bind("questId") String questId, @Bind("completed") boolean completed, @Bind("completionTime") long completionTime, @Bind("progress") int progress);
}
