package com.pepej.gungame.rpg.trap;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.user.User;
import com.pepej.papi.cooldown.Cooldown;
import com.pepej.papi.terminable.Terminable;
import com.pepej.papi.terminable.TerminableConsumer;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Trap extends Terminable, TerminableConsumer {

    TrapType getType();

    Cooldown getCooldown();

    void onActivate(@NonNull User user, @NonNull Arena arena);
}
