package com.pepej.gungame.service;

import com.pepej.gungame.npc.GunGameNpc;
import com.pepej.gungame.service.impl.NpcServiceImpl;
import com.pepej.papi.services.Implementor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Optional;

@Implementor(NpcServiceImpl.class)
public interface NpcService {

    void register(@NonNull String id, @NonNull GunGameNpc npc);

    void unregister(@NonNull String id);

    Optional<GunGameNpc> getNpc(@NonNull String id);

    @NonNull Collection<GunGameNpc> getNpcs();

}
