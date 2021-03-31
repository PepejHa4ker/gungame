package com.pepej.gungame.rpg.trap;

import com.pepej.gungame.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class TrapBase {

    String name;
    TrapType type;

    protected TrapBase(final String name, final TrapType type) {
        this.name = name;
        this.type = type;
    }

    public abstract void onActivate(User user);
}
