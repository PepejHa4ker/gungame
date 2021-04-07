package com.pepej.gungame.rpg.trap.handler;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.api.trap.TrapHandler;
import com.pepej.gungame.user.User;
import com.pepej.papi.serialize.Position;

import java.util.Map;

public class TrapHandlerImpl implements TrapHandler {

    @Override
    public void handle(final User user, Arena arena) {
        final Position position = Position.of(user.location());
        arena.getContext().getTraps().entrySet().stream()
             .filter(e -> e.getKey().inRegion(position))
             .findFirst()
             .map(Map.Entry::getValue)
             .filter(trap -> trap.getCooldown().test())
             .ifPresent(t -> t.onActivate(user, arena));
    }
}
