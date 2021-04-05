package com.pepej.gungame.service.impl;

import com.pepej.gungame.GlobalConfig;
import com.pepej.gungame.GunGame;
import com.pepej.gungame.hologram.GunGameHologram;
import com.pepej.gungame.repository.UserRepository;
import com.pepej.gungame.service.HologramTopService;
import com.pepej.gungame.user.User;
import com.pepej.gungame.utils.SquarelandApiUtils;
import com.pepej.papi.Papi;
import com.pepej.papi.scheduler.Schedulers;
import com.pepej.papi.services.Services;
import com.pepej.papi.terminable.TerminableConsumer;
import com.pepej.papi.terminable.module.TerminableModule;
import com.pepej.papi.utils.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HologramTopServiceImpl implements HologramTopService, TerminableModule {

    @Getter
    Map<TopStrategy, GunGameHologram> holograms;
    UserRepository userRepository;


    public HologramTopServiceImpl() {
        this.holograms = new HashMap<>();
        this.userRepository = Services.load(UserRepository.class);
    }


    @Override
    public void register(final TopStrategy strategy, GunGameHologram hologram) {
        holograms.put(strategy, hologram);
    }

    @Override
    public void updateHolograms() {
        for (Map.Entry<TopStrategy, GunGameHologram> entry : holograms.entrySet()) {
            TopStrategy key = entry.getKey();
            GunGameHologram value = entry.getValue();
            List<User> usersToUpdate = getTopByStrategy(entry.getKey(), userRepository, 10);
            List<String> lines = new LinkedList<>();
            lines.add(format("&cТоп %s", key.getDecsB()));
            AtomicInteger position = new AtomicInteger(1);
            final User user = usersToUpdate.get(0);
            if (value.getConfig().getNpcPosition() != null) {
                SquarelandApiUtils.getUserNpc(user.getUsername(), value.getConfig().getNpcPosition()).thenAcceptAsync(npc -> {

                    final Consumer<Player> clickCallback = player -> {
                        Papi.server().dispatchCommand(player, format("stats %s", user.getUsername()));
                    };
                    if (value.getCurrentNpc() == null) {
                        value.setCurrentNpc(npc);
                        npc.setClickCallback(clickCallback);

                    }
                    else {
                        npc.setClickCallback(clickCallback);
                        Schedulers.sync().run(() -> { //remove npcs in main bukkit thread
                        value.getCurrentNpc().getNpc().despawn();
                        value.getCurrentNpc().getNpc().destroy();
                    });
                        value.setCurrentNpc(npc);
                    }
                });
            }

            lines.addAll(usersToUpdate.stream().map(u -> {
                final int statByStrategy = getStatByStrategy(key, u);
                return format("&d#%s &a%s &7- &c%s &7%s",
                        position.getAndIncrement(),
                        u.getUsername(),
                        statByStrategy,
                        StringUtils.plural(statByStrategy, key.getDecsSingle(), key.getDecsA(), key.getDecsB()));
            }).collect(toList()));


            value.getHologram().updateLines(lines);
            value.getHologram().delete();
            value.getHologram().spawn();
        }
    }


    private static int getStatByStrategy(TopStrategy strategy, User user) {
        switch (strategy) {
            case KILLS:
                return user.getKills();
            case WINS:
                return user.getWins();
            case GAMES:
                return user.getGamesPlayed();
            case LEVELS:
                return user.getLevelsReached();
            case EXP:
                return user.getExp();

        }
        return 0;
    }

    private static List<User> getTopByStrategy(TopStrategy strategy, UserRepository userRepository, int size) {
        switch (strategy) {

            case KILLS:
                return userRepository.getTopUsersByKills(size);
            case WINS:
                return userRepository.getTopUsersByWins(size);
            case GAMES:
                return userRepository.getTopUsersByGames(size);
            case LEVELS:
                return userRepository.getTopUsersByLevels(size);
            case EXP:
                return userRepository.getTopUsersByExp(size);
            default:
                throw new AssertionError();

        }
    }


    @Override
    public void setup(@NonNull final TerminableConsumer consumer) {
        GlobalConfig config = GunGame.getInstance().getGlobalConfig();
        Schedulers.builder()
                  .sync()
                  .every(config.getHologramUpdateTicks())
                  .run(this::updateHolograms)
                  .bindWith(consumer);
    }
}
