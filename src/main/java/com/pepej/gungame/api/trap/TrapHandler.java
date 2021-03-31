package com.pepej.gungame.api.trap;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.rpg.trap.TrapBase;
import com.pepej.gungame.user.User;

public interface TrapHandler {

    void handle(User user, TrapBase trap, Arena arena);

}
