package com.pepej.gungame.api.trap;

import com.pepej.gungame.rpg.trap.TrapBase;
import com.pepej.papi.serialize.Region;

public interface TrapLoader {

    TrapBase load(Region region, String name);
}
