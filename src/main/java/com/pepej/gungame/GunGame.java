package com.pepej.gungame;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.pepej.gungame.api.Arena;
import com.pepej.gungame.api.GunGameModule;
import com.pepej.gungame.api.annotations.ArenaFile;
import com.pepej.gungame.arena.loader.ArenaLoader;
import com.pepej.gungame.menu.ArenaSelectorMenu;
import com.pepej.gungame.service.ArenaService;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.service.impl.UserServiceImpl;
import com.pepej.papi.adventure.platform.bukkit.BukkitAudiences;
import com.pepej.papi.ap.Plugin;
import com.pepej.papi.ap.PluginDependency;
import com.pepej.papi.command.Commands;
import com.pepej.papi.plugin.PapiJavaPlugin;
import lombok.Getter;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;

@Plugin(name = "GunGame", version = "1.0.0", authors = "pepej", description = "GunGame plugin", depends = @PluginDependency("papi"))
@Getter
public class GunGame extends PapiJavaPlugin {

    private static GunGame instance;


    @NonNull
    public static GunGame getInstance() {
        return instance;
    }

    private Injector injector;
    private BukkitAudiences audiences;

    @Override
    public void onPluginLoad() {
        instance = this;
    }

    @SneakyThrows
    @Override
    public void onPluginEnable() {
        audiences = bind(BukkitAudiences.create(this));
        injector = Guice.createInjector(new GunGameModule(), binder -> {
            binder.bind(BukkitAudiences.class)
                  .toInstance(audiences);
            binder.bind(UserService.class)
                  .toInstance(bindModule(new UserServiceImpl()));

            binder.bind(File.class)
                  .annotatedWith(ArenaFile.class)
                  .toInstance(getBundledFile("arenas.json"));
        });

        ArenaLoader arenaLoader = injector.getInstance(ArenaLoader.class);
        ArenaService arenaService = injector.getInstance(ArenaService.class);
        UserService userService = injector.getInstance(UserService.class);
        final Arena arena = arenaLoader.loadArena("test-arena-1234");
        arenaService.register(arena);
        Commands.create()
                .assertPlayer()
                .handler(context -> new ArenaSelectorMenu(context.sender(), arenaService, userService).open())
                .registerAndBind(this, "menu");
        arena.enable();
        arena.getContext().getLobby();

    }
}
