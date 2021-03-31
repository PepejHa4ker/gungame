package com.pepej.gungame.hologram.loader;

import com.pepej.gungame.hologram.HologramConfig;
import com.pepej.gungame.service.HologramTopService;
import com.pepej.papi.config.ConfigFactory;
import com.pepej.papi.hologram.Hologram;
import com.pepej.papi.hologram.HologramFactory;
import com.pepej.papi.services.Service;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.util.List;


@FieldDefaults(level = AccessLevel.PRIVATE)
public class HologramLoaderImpl implements HologramLoader {

    @Service
    HologramTopService hologramTopService;
    @Service
    HologramFactory factory;
    final File file;

    public HologramLoaderImpl(final File file) {
        this.file = file;
    }


    @Override
    @SneakyThrows
    public void load() {
        List<HologramConfig> configs = ConfigFactory.gson().load(file).getList(HologramConfig.class);
        if (configs == null) {
            throw new IllegalStateException();
        }
        for (HologramConfig config : configs) {
            Hologram hologram = factory.newHologram(config.getPosition(), "");
            hologram.spawn();
            hologramTopService.register(config.getStrategy(), hologram);

        }




    }
}
