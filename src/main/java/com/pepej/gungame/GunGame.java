package com.pepej.gungame;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.arena.loader.ArenaLoader;
import com.pepej.gungame.arena.loader.SimpleArenaLoader;
import com.pepej.gungame.menu.ArenaSelectorMenu;
import com.pepej.gungame.service.ArenaService;
import com.pepej.gungame.service.TeamService;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.service.impl.ArenaServiceImpl;
import com.pepej.gungame.service.impl.TeamServiceImpl;
import com.pepej.gungame.service.impl.UserServiceImpl;
import com.pepej.gungame.user.User;
import com.pepej.papi.Services;
import com.pepej.papi.adventure.platform.bukkit.BukkitAudiences;
import com.pepej.papi.adventure.text.Component;
import com.pepej.papi.ap.Plugin;
import com.pepej.papi.ap.PluginDependency;
import com.pepej.papi.command.Commands;
import com.pepej.papi.plugin.PapiJavaPlugin;
import lombok.Getter;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

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
        provideService(BukkitAudiences.class, audiences);
        provideService(ArenaService.class, new ArenaServiceImpl());
        provideService(UserService.class, new UserServiceImpl());
        provideService(TeamService.class, new TeamServiceImpl());
        provideService(ArenaLoader.class, new SimpleArenaLoader(getBundledFile("arenas.json")));
        ArenaLoader loader =  Services.load(ArenaLoader.class);
        final Arena arena = loader.loadArena("test-arena-1234");
        Services.load(ArenaService.class).register(arena);
        Commands.create()
                .assertPlayer()
                .handler(context -> new ArenaSelectorMenu(context.sender()).open())
                .registerAndBind(this, "menu");

        Commands.create()
                .assertPlayer()
                .handler(context -> {
                   UserService userService = Services.load(UserService.class);
                    final Optional<User> userByPlayer = userService.getUserByPlayer(context.sender());
                    userByPlayer.ifPresent(user -> {
                       if (user.getCurrentArena() != null) {
                           user.getCurrentArena().leave(user);
                       } else {
                           user.sendMessage(Component.text("no arena present ("));
                       }
                    });
                })
        .registerAndBind(this, "leave-gg");

    }
}
