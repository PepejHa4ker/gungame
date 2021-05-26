package com.pepej.gungame.rpg.quest;

import com.pepej.gungame.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface Quest {

    UUID getId();
    
    QuestType getType();

    long getCreationTime();

    void setCreationTime(long creationTime);

    long getExpireTime();

    boolean isExpired();

    void setExpireTime(long expireTime);

    void setCompletionTime(long completeTime);

    long getCompletionTime();

    int getProgress();

    void setProgress(int progress);

    boolean isCompleted();

    default boolean isActive() {
        return !this.isCompleted() && !this.isExpired();
    }

    void setCompleted(boolean completed);

    double getReward();

    boolean tryComplete(User user);

    void preComplete(@NotNull User user);

    void complete(User user);

    void setHolder(User user);

    User getHolder();


}
