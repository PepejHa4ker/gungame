package com.pepej.gungame.service;

import com.pepej.gungame.rpg.trap.Trap;
import com.pepej.gungame.rpg.trap.TrapBase;
import com.pepej.gungame.rpg.trap.TrapType;
import com.pepej.gungame.service.impl.TrapServiceImpl;
import com.pepej.papi.serialize.Position;
import com.pepej.papi.services.Implementor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
import java.util.Set;

@Implementor(TrapServiceImpl.class)
public interface TrapService {

    void register(@NonNull String id, @NonNull TrapBase trap);

    void unregister(@NonNull TrapBase trap);

    Optional<TrapBase> getTrap(@NonNull String id);

    Optional<TrapBase> getTrap(@NonNull Position position);

    @NonNull Set<TrapBase> getTraps();

    Trap createTrapByType(TrapType trapType);
}
