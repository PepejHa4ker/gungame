package com.pepej.gungame.rpg.bonus;

import com.pepej.gungame.user.User;
import org.bukkit.entity.Player;

public class JumpBoostBonus extends Bonus {
    protected JumpBoostBonus() {
        super(BonusType.EFFECT, "just boost", null);
    }

    @Override
    public void onCollect(final User user) {
        Player player = user.asPlayer();
        player.sendMessage("collected");
    }
}
