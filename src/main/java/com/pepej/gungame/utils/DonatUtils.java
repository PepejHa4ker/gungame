package com.pepej.gungame.utils;

import com.pepej.papi.function.chain.Chain;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public final class DonatUtils {

    public static void applyIfHasPermission(Player user, String permission, Consumer<? super Player> action) {
        Chain.start(user)
             .applyIf(p -> p.hasPermission(permission), action);
    }



    public static void applyIfVip(Player user, Consumer<? super Player> action) {
        applyIfHasPermission(user, "gungame.vip", action);
    }

    public static void applyIfPremium(Player user, Consumer<? super Player> action) {
        applyIfHasPermission(user, "gungame.premium", action);
    }
    public static void applyIfGrand(Player user, Consumer<? super Player> action) {
        applyIfHasPermission(user, "gungame.grand", action);
    }
}
