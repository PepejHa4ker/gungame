package com.pepej.gungame.rpg.trap;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.user.User;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.lang.String.format;

public class PoisonTrap extends TrapBase {
    public PoisonTrap() {
        super("poison arena", TrapType.POISON_ARENA);
    }

    @Override
    public void onActivate(@NonNull final User user, @NonNull final Arena arena) {
        getUserService().broadcastMessage(arena, format("&c%s&e видимо вчера что-то съел и траванулся ", user.getUsername()));
        final Player player = user.asPlayer();
        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 9));
        player.setHealth(Math.max(player.getHealth() - 10, 1));

    }
}
