package com.pepej.gungame.rpg.trap;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.user.User;
import com.pepej.papi.cooldown.Cooldown;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Trap {

    TrapType getType();

    Cooldown getCooldown();

    void onActivate(@NonNull User user, @NonNull Arena arena);
}
