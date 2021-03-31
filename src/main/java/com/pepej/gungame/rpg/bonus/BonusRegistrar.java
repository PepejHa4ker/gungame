package com.pepej.gungame.rpg.bonus;

import com.pepej.gungame.api.Arena;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BonusRegistrar {

    @Getter
    private final Map<Arena, List<Bonus>> bonuses;

    public BonusRegistrar() {
        this.bonuses = new HashMap<>();
    }

    public void register(Arena arena, List<Bonus> toReg) {
        bonuses.put(arena, toReg);
    }
}
