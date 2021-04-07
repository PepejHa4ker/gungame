package com.pepej.gungame.rpg.trap;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.user.User;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.checkerframework.checker.nullness.qual.NonNull;

public class JumpTrap extends TrapBase {
    public JumpTrap() {
        super("jump", TrapType.BLINDNESS);
    }


    @Override
    public void onActivate(@NonNull final User user, @NonNull final Arena arena) {
        final Player player = user.asPlayer();
        if (player != null) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5*20, 1));
        }
    }
}
