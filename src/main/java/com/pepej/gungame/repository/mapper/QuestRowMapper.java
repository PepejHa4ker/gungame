package com.pepej.gungame.repository.mapper;

import com.pepej.gungame.rpg.quest.Quest;
import com.pepej.gungame.rpg.quest.QuestType;
import com.pepej.gungame.service.QuestService;
import com.pepej.papi.services.Services;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class QuestRowMapper implements RowMapper<Quest> {

    private final QuestService questService;

    public QuestRowMapper() {
        questService = Services.load(QuestService.class);
    }

    @Override
    public Quest map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        final QuestType type = QuestType.valueOf(rs.getString("type"));
        Quest quest = questService.getQuestByType(type, UUID.fromString(rs.getString("quest_id")));
        quest.setCreationTime(rs.getLong("creation_time"));
        quest.setCompleted(rs.getBoolean("completed"));
        quest.setCompletionTime(rs.getLong("completion_time"));
        quest.setProgress(rs.getInt("progress"));
        return quest;
    }
}
