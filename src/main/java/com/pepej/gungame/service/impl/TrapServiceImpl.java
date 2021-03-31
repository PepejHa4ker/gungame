package com.pepej.gungame.service.impl;

import com.pepej.gungame.rpg.trap.TrapBase;
import com.pepej.gungame.service.TrapService;
import com.pepej.papi.serialize.Position;
import com.pepej.papi.utils.Log;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrapServiceImpl implements TrapService {

    @Getter
    Set<TrapBase> traps;

    public TrapServiceImpl() {
        traps = new HashSet<>();
    }

    @Override
    public void register(@NonNull final String id, @NonNull final TrapBase trap) {
        if (traps.contains(trap)) {
            Log.warn("Trap with ID " + id + " already register!");
            return;
        }
        traps.add(trap);
    }

    @Override
    public void unregister(@NonNull final TrapBase trap) {
        traps.remove(trap);
    }

    @Override
    public Optional<TrapBase> getTrap(@NonNull final String id) {
        return traps.stream().filter(trap -> trap.getName().equalsIgnoreCase(id)).findFirst();
    }

    @Override
    public Optional<TrapBase> getTrap(@NonNull final Position position) {
        return traps.stream()/*.filter(trap -> trap.getPosition().equals(position))*/.findFirst();
    }


}
