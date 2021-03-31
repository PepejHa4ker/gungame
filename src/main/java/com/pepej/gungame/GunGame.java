package com.pepej.gungame;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.arena.loader.ArenaLoader;
import com.pepej.gungame.arena.loader.SimpleArenaLoader;
import com.pepej.gungame.hologram.loader.HologramLoader;
import com.pepej.gungame.hologram.loader.HologramLoaderImpl;
import com.pepej.gungame.listener.Listener;
import com.pepej.gungame.repository.DatabaseService;
import com.pepej.gungame.repository.QuestRepository;
import com.pepej.gungame.repository.UserRepository;
import com.pepej.gungame.repository.mysql.MySQLQuestRepository;
import com.pepej.gungame.repository.mysql.MySQLUserRepository;
import com.pepej.gungame.rpg.bonus.BonusRegistrar;
import com.pepej.gungame.service.*;
import com.pepej.gungame.service.impl.HologramTopServiceImpl;
import com.pepej.gungame.service.impl.UserServiceImpl;
import com.pepej.papi.adventure.platform.bukkit.BukkitAudiences;
import com.pepej.papi.ap.Plugin;
import com.pepej.papi.ap.PluginDependency;
import com.pepej.papi.config.ConfigFactory;
import com.pepej.papi.dependency.Dependency;
import com.pepej.papi.plugin.PapiJavaPlugin;
import com.pepej.papi.services.Services;
import com.pepej.papi.utils.Players;
import lombok.Getter;
import lombok.SneakyThrows;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

@Plugin(name = "GunGame", version = "1.0.0", authors = "pepej", description = "GunGame plugin", depends = {@PluginDependency("papi"), @PluginDependency("LuckPerms")})
@Dependency("com.zaxxer:HikariCP:3.4.5")
@Getter
public class GunGame extends PapiJavaPlugin {

    private static GunGame instance;

    @NonNull
    public static GunGame getInstance() {
        return instance;
    }

    private GlobalConfig globalConfig;

    @Override
    public void onPluginLoad() {
        instance = this;
    }


    @SneakyThrows
    @Override
    public void onPluginEnable() {
        this.globalConfig = ConfigFactory.gson().load(getBundledFile("config.json")).get(GlobalConfig.class);
        provideService(QuestService.class);
        DatabaseService databaseService = new DatabaseService(getBundledFile("database.json"));
        provideService(DatabaseService.class, databaseService);
        provideService(QuestRepository.class, databaseService.getJdbi().onDemand(MySQLQuestRepository.class));
        provideService(UserRepository.class, databaseService.getJdbi().onDemand(MySQLUserRepository.class));
        provideService(BonusRegistrar.class, new BonusRegistrar());
        provideService(LuckPerms.class, LuckPermsProvider.get());
        provideService(ArenaService.class);
        provideService(BukkitAudiences.class, bind(BukkitAudiences.create(this)));
        provideService(ScoreboardService.class);
        provideService(TrapService.class);
        provideService(UserService.class, bindModule(new UserServiceImpl()));
//        provideService(BonusHandler.class, bindModule(new BonusHandlerImpl()));
        provideService(HologramTopService.class, bindModule(new HologramTopServiceImpl()));
        provideService(ArenaLoader.class, new SimpleArenaLoader(getBundledFile("arenas.json")));
        provideService(HologramLoader.class, new HologramLoaderImpl(getBundledFile("holograms.json")));
//        provideService(TrapLoader.class, new SimpleTrapLoader(getBundledFile("traps.json")));
        bindModule(new Listener());
        bindModule(new CommandRegister());
        ArenaLoader loader = Services.load(ArenaLoader.class);
        ArenaService arenaService = Services.load(ArenaService.class);
        final Arena arena = loader.loadArena("test-arena-1234");
        arenaService.register(arena);
        getService(HologramLoader.class)
                .load();
    }

    @Override
    public void onPluginDisable() {
        UserService userService = Services.load(UserService.class);
        for (final Player player : Players.all()) {
            userService.unregisterUser(player.getUniqueId());
        }
    }
}
