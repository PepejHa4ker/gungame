package com.pepej.gungame;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.arena.loader.ArenaLoader;
import com.pepej.gungame.arena.loader.SimpleArenaLoader;
import com.pepej.gungame.listener.Listener;
import com.pepej.gungame.menu.ArenaSelectorMenu;
import com.pepej.gungame.service.ArenaService;
import com.pepej.gungame.service.ScoreboardService;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.service.impl.UserServiceImpl;
import com.pepej.gungame.user.User;
import com.pepej.papi.adventure.platform.bukkit.BukkitAudiences;
import com.pepej.papi.ap.Plugin;
import com.pepej.papi.ap.PluginDependency;
import com.pepej.papi.command.Commands;
import com.pepej.papi.plugin.PapiJavaPlugin;
import com.pepej.papi.services.Services;
import lombok.Getter;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;

@Plugin(name = "GunGame", version = "1.0.0", authors = "pepej", description = "GunGame plugin", depends = @PluginDependency("papi"))
@Getter
public class GunGame extends PapiJavaPlugin {

    private static GunGame instance;

    @NonNull
    public static GunGame getInstance() {
        return instance;
    }

    private BukkitAudiences audiences;

    @Override
    public void onPluginLoad() {
        instance = this;
    }

    @SneakyThrows
    @Override
    public void onPluginEnable() {

        audiences = bind(BukkitAudiences.create(this));
        provideService(ArenaService.class);
        provideService(BukkitAudiences.class, audiences);
        provideService(ScoreboardService.class);
        provideService(ArenaLoader.class, new SimpleArenaLoader(getBundledFile("arenas.json")));
        provideService(UserService.class, bindModule(new UserServiceImpl()));
        bindModule(new Listener());
        ArenaLoader loader = Services.load(ArenaLoader.class);
        ArenaService arenaService = Services.load(ArenaService.class);
        final Arena arena = loader.loadArena("test-arena-1234");
        arenaService.register(arena);

        Commands.create()
                .assertPlayer()
                .handler(context -> new ArenaSelectorMenu(context.sender()).open())
                .registerAndBind(this, "arenas");


    }

    @Override
    public void onPluginDisable() {
        UserService userService = Services.load(UserService.class);
        for (final User user : userService.getAllUsers()) {
            user.getCurrentArenaSafe().ifPresent(arena -> arena.leave(user, Arena.ArenaLeaveCause.END_OF_GAME));
        }
    }
}
