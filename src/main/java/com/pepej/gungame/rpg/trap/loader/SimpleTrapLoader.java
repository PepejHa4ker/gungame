package com.pepej.gungame.rpg.trap.loader;

import com.pepej.gungame.api.trap.TrapLoader;
import com.pepej.gungame.rpg.trap.TrapBase;
import com.pepej.gungame.service.TrapService;
import com.pepej.papi.serialize.Region;
import com.pepej.papi.services.Service;
import lombok.Getter;

import java.io.File;

@Getter
public class SimpleTrapLoader implements TrapLoader {

    @Service
    private TrapService trapService;
    private final File file;

    public SimpleTrapLoader(File file) {
        this.file = file;
    }


    @Override
    public TrapBase load(final Region region, final String name) {

        return null;
    }
}
