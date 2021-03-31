package com.pepej.gungame.service.impl;

import com.pepej.gungame.GlobalConfig;
import com.pepej.gungame.GunGame;
import com.pepej.gungame.repository.UserRepository;
import com.pepej.gungame.service.HologramTopService;
import com.pepej.gungame.user.User;
import com.pepej.papi.hologram.Hologram;
import com.pepej.papi.scheduler.Schedulers;
import com.pepej.papi.services.Services;
import com.pepej.papi.terminable.TerminableConsumer;
import com.pepej.papi.terminable.module.TerminableModule;
import com.pepej.papi.utils.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HologramTopServiceImpl implements HologramTopService, TerminableModule {

    @Getter
    Map<TopStrategy, Hologram> holograms;
    UserRepository userRepository;


    public HologramTopServiceImpl() {
        this.holograms = new HashMap<>();
        this.userRepository = Services.load(UserRepository.class);
    }


    @Override
    public void register(final TopStrategy strategy, Hologram hologram) {
        holograms.put(strategy, hologram);
    }

    @Override
    public void updateHolograms() {
        for (Map.Entry<TopStrategy, Hologram> entry : holograms.entrySet()) {
            TopStrategy key = entry.getKey();
            Hologram value = entry.getValue();
            List<User> usersToUpdate = getTopByStrategy(entry.getKey(), userRepository, 10);
            List<String> lines = new LinkedList<>();
            lines.add(format("&cТоп %s", key.getDecsB()));
            AtomicInteger position = new AtomicInteger(1);
            lines.addAll(usersToUpdate.stream().map(u -> {
                final int statByStrategy = getStatByStrategy(key, u);
                return format("&d#%s &a%s &7- &c%s &7%s",
                        position.getAndIncrement(),
                        u.getUsername(),
                        statByStrategy,
                        StringUtils.plural(statByStrategy, key.getDecsSingle(), key.getDecsA(), key.getDecsB()));
            }).collect(toList()));


            value.updateLines(lines);
            value.delete();
            value.spawn();
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
