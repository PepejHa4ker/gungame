package com.pepej.gungame.rpg.quest;

import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.services.Services;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.util.UUID;

public class KillHundredPlayersQuest extends QuestBase {


    private final UserService userService;
    private final Economy economy;
    public KillHundredPlayersQuest(final UUID id) {
        super(id, QuestType.KILL_HUNDRED_PLAYER);
        userService = Services.load(UserService.class);
        economy = Services.load(Economy.class);
    }

    @Override
    public void complete(final User user) {
        userService.sendMessage(user, "&aВы успешно выполнили квест &6" + this.getType().getDisplayName());
        final Player player = user.asPlayer();
        if (player != null) {
            economy.depositPlayer(player, this.getReward());
        }
    }
}
