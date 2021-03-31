package com.pepej.gungame.rpg.bonus;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.user.User;
import com.pepej.papi.serialize.Position;

public interface BonusHandler {

    void spawn(Arena arena, Bonus bonus, Position position);

    Bonus selectRandomBonus(Arena arena);

    void handle(Arena arena, Bonus bonus, User user);
}
