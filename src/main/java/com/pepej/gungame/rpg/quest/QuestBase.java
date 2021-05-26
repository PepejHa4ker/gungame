package com.pepej.gungame.rpg.quest;

import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.services.Services;
import lombok.Data;
import lombok.ToString;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
    private final Economy economy;
    private final UserService userService;

    public QuestBase(final UUID id, QuestType type) {
        this.reward = type.getReward();
        this.type = type;
        this.creationTime = System.currentTimeMillis();
        this.id = id;
        this.expireTime = type.getExpiryTimeMs();
        this.userService = Services.load(UserService.class);
        this.economy = Services.load(Economy.class);
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

    @Override
    public void preComplete(@NotNull User user) {
        userService.sendMessage(user, "&aВы успешно выполнили квест &6" + this.getType().getDisplayName());
        final Player player = user.asPlayer();
        if (player != null) {
            economy.depositPlayer(player, this.getReward());
        }
    }

    public void complete(User user) {

    }

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
