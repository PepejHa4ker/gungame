package com.pepej.gungame.npc;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface NpcLoader {

    void loadAngRegisterAllNpcs();

    @Nullable
    GunGameNpc loadNpc(String id);



}
