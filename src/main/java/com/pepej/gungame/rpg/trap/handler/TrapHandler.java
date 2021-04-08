package com.pepej.gungame.rpg.trap.handler;

import com.pepej.gungame.user.User;

@FunctionalInterface
public interface TrapHandler {

    void handle(User user);

}
