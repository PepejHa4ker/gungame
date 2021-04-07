package com.pepej.gungame.api.trap;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.rpg.trap.handler.TrapHandlerImpl;
import com.pepej.gungame.user.User;
import com.pepej.papi.services.Implementor;

@Implementor(TrapHandlerImpl.class)
public interface TrapHandler {

    void handle(User user, Arena arena);

}
