package com.pepej.gungame.service.impl;

import com.pepej.gungame.npc.GunGameNpc;
import com.pepej.gungame.service.NpcService;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NpcServiceImpl implements NpcService {

    private final Map<String, GunGameNpc> npcMap;

    public NpcServiceImpl() {
        npcMap = new HashMap<>();
    }

    @Override
    public void register(@NonNull final String id, @NonNull final GunGameNpc npc) {
        npcMap.put(id, npc);
    }

    @Override
    public void unregister(@NonNull final String id) {
        npcMap.remove(id);
    }

    @Override
    public Optional<GunGameNpc> getNpc(@NonNull final String id) {
        return Optional.ofNullable(npcMap.get(id));
    }

    @Override
    public @NonNull Collection<GunGameNpc> getNpcs() {
        return npcMap.values();
    }
}
