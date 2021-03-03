package com.pepej.gungame;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.pepej.gungame.api.Arena;
import com.pepej.gungame.api.GunGameModule;
import com.pepej.gungame.api.annotations.ArenaFile;
import com.pepej.gungame.arena.loader.ArenaLoader;
import com.pepej.papi.ap.Plugin;
import com.pepej.papi.ap.PluginDependency;
import com.pepej.papi.config.ConfigFactory;
import com.pepej.papi.plugin.PapiJavaPlugin;
import lombok.SneakyThrows;
import ninja.leaping.configurate.ConfigurationNode;

import java.io.File;

@Plugin(name = "GunGame", version = "1.0.0", authors = "pepej", description = "GunGame plugin", depends = @PluginDependency("papi"))
public class GunGame extends PapiJavaPlugin {

    private Injector injector;

    @SneakyThrows
    @Override
    public void onPluginEnable() {
        injector = Guice.createInjector(new GunGameModule(), binder -> {
            binder.bind(File.class)
                  .annotatedWith(ArenaFile.class)
                  .toInstance(getBundledFile("arenas.json"));
        });

        ArenaLoader arenaLoader = injector.getInstance(ArenaLoader.class);
        ConfigurationNode config = ConfigFactory.gson().load(getBundledFile("arenas.json"));
        final Arena arena = arenaLoader.loadArena("test-arena-1234");
//        arena.enable();
//        arena.getContext().getLobby()

    }
}
