package com.pepej.gungame.rpg.trap;

import com.pepej.gungame.service.UserService;
import com.pepej.papi.cooldown.Cooldown;
import com.pepej.papi.services.Services;
import com.pepej.papi.terminable.composite.CompositeTerminable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.TimeUnit;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class TrapBase implements Trap {

    String name;
    TrapType type;
    Cooldown cooldown;
    UserService userService;
    CompositeTerminable compositeTerminable;

    public TrapBase(final String name, final TrapType type) {
        this.cooldown = Cooldown.of(type.getDelayInSeconds(), TimeUnit.SECONDS);
        this.name = name;
        this.type = type;
        this.userService = Services.load(UserService.class);
        this.compositeTerminable = CompositeTerminable.create();
    }

    @Override
    public <T extends AutoCloseable> @NonNull T bind(@NonNull final T t) {
        return this.compositeTerminable.bind(t);
    }

    @Override
    public void close() throws Exception {
        this.compositeTerminable.closeAndReportException();
    }
}
