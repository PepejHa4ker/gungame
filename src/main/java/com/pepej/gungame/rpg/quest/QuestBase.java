package com.pepej.gungame.rpg.quest;
import com.pepej.gungame.user.User;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString(exclude = "holder")
public abstract class QuestBase implements Quest {


    private int progress;
    private boolean completed;
    private long creationTime;
    private long expireTime;
    private long completionTime;

    private User holder;
    private final double reward;
    private final QuestType type;
    private final UUID id;

    public QuestBase(final UUID id, QuestType type) {
        this.reward = type.getReward();
        this.type = type;
        this.creationTime = System.currentTimeMillis();
        this.id = id;
        this.expireTime = type.getExpiryTimeMs();
    }

    @Override
    public void setProgress(final int progress) {
        this.progress = Math.min(progress, type.getCount());
        if (progress >= this.getType().getCount()) {
            tryComplete(holder);
        }
    }

    @Override
    public boolean isExpired() {
        return this.creationTime + expireTime < System.currentTimeMillis();

    }

    public abstract void complete(User user);

    @Override
    public boolean tryComplete(final User user) {
        if (this.isExpired() || this.isCompleted()) {
            return false;
        }

        this.setCompletionTime(System.currentTimeMillis());
        this.setCompleted(true);
        complete(user);

        return true;


    }

}
