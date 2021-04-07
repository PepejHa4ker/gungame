package com.pepej.gungame.rpg.trap;

import com.pepej.gungame.service.UserService;
import com.pepej.papi.cooldown.Cooldown;
import com.pepej.papi.services.Services;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.TimeUnit;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class TrapBase implements Trap {

    String name;
    TrapType type;
    Cooldown cooldown;
    UserService userService;

    public TrapBase(final String name, final TrapType type) {
        this.cooldown = Cooldown.of(type.getDelayInSeconds(), TimeUnit.SECONDS);
        this.name = name;
        this.type = type;
        this.userService = Services.load(UserService.class);
    }

}
