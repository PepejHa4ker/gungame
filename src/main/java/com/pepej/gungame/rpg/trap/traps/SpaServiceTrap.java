package com.pepej.gungame.rpg.trap.traps;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.rpg.trap.TrapBase;
import com.pepej.gungame.rpg.trap.TrapType;
import com.pepej.gungame.user.User;
import com.pepej.papi.random.VariableAmount;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.lang.String.format;

public class SpaServiceTrap extends TrapBase {

    public SpaServiceTrap() {
        super("Spa service", TrapType.SPA_SERVICE);
    }

    @Override
    public void onActivate(@NonNull final User user, @NonNull final Arena arena) {
        final Player player = user.asPlayer();
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 2), true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 2), true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 2), true);
        int regenLevel = VariableAmount.range(0, 1).getFlooredAmount();
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, regenLevel), true);
        getUserService().broadcastMessage(arena, format("&e%s&a прилег отдохнуть в сауну", user.getUsername()));
    }
}
