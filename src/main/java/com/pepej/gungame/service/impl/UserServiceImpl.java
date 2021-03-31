package com.pepej.gungame.service.impl;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.repository.QuestRepository;
import com.pepej.gungame.repository.UserRepository;
import com.pepej.gungame.rpg.quest.Quest;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.adventure.platform.bukkit.BukkitAudiences;
import com.pepej.papi.adventure.text.Component;
import com.pepej.papi.adventure.text.format.NamedTextColor;
import com.pepej.papi.adventure.title.Title;
import com.pepej.papi.events.Events;
import com.pepej.papi.promise.Promise;
import com.pepej.papi.scheduler.Schedulers;
import com.pepej.papi.services.Service;
import com.pepej.papi.services.Services;
import com.pepej.papi.terminable.TerminableConsumer;
import com.pepej.papi.terminable.module.TerminableModule;
import com.pepej.papi.utils.Log;
import com.pepej.papi.utils.Players;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

@Getter
public class UserServiceImpl implements UserService, TerminableModule {

    private final Set<User> userCache;
    private final UserRepository userRepository;
    private final QuestRepository questRepository;
    @Service
    private BukkitAudiences audiences;

    public UserServiceImpl() {
        this.userRepository = Services.load(UserRepository.class);
        this.questRepository = Services.load(QuestRepository.class);
        this.userCache = new HashSet<>();
        this.loadAllOnlineUsers();
    }


    @Override
    public void loadAllOnlineUsers() {
        Players.all()
               .forEach(player -> registerUser(player.getUniqueId(), player.getName()));
    }

    @Override
    public void broadcastMessage(@NonNull Arena arena, @NonNull final Component message) {
        arena.getContext().getUsers().forEach(user -> sendMessage(user, message));
    }

    @Override
    public void sendMessage(@NonNull final User user, @NonNull final Component message) {
        Component prefix = Component.text()
                                    .color(NamedTextColor.GRAY)
                                    .append(Component.text('['))
                                    .append(Component.text("GG", NamedTextColor.GREEN))
                                    .append(Component.text(']'))
                                    .append(Component.space())
                                    .build();
        if (user.asPlayer() != null) {
            audiences.player(user.asPlayer()).sendMessage(prefix.append(message));
        }
    }

    @Override
    public void sendBossBar(@NonNull final User user, @NonNull final Component message) {
        if (user.asPlayer() != null) {
            audiences.player(user.asPlayer()).sendActionBar(message);

        }
    }

    @Override
    public void sendTitle(@NonNull final User user, @NonNull final Title title) {
        if (user.asPlayer() != null) {
            audiences.player(user.asPlayer()).showTitle(title);
        }
    }


    @Override
    public Promise<User> registerUser(@NonNull final UUID id, final String username) {
        Promise<User> userPromise = Promise.start()
                                           .thenApplySync($ -> userRepository.userExists(id.toString()))
                                           .thenAcceptAsync(exists -> {
                                               if (!exists) {
                                                   userRepository.saveUser(id.toString(), username);
                                               }
                                           })
                                           .thenRunSync(() -> Log.info("Loading user %s...", username))
                                           .thenApplyAsync($ -> userRepository.getUserByUuid(id.toString()));

        userPromise.thenAcceptAsync(user -> Promise.start()
                                                    .thenApplyAsync($ -> questRepository.getUserQuests(id.toString()))
                                                    .thenAcceptAsync(quests -> {
                                                        for (Quest quest : quests) {
                                                            if (user != null) {
                                                                user.getQuests().add(quest);
                                                                quest.setHolder(user);

                                                            }
                                                        }
                                                        Log.info("User %s loaded!", username);
                                                        userCache.add(user);
                                                    }));
        return userPromise;

    }



    @Override
    public void handleJoin(final Player player) {
        registerUser(player.getUniqueId(), player.getName());
//        for (HologramTopService.TopStrategy value : HologramTopService.TopStrategy.values()) {
//            GunGameHologram hologram = hologramLoader.load(user, value);
//            topService.register(user, value, hologram);
//        }
    }

    @Override
    public void unregisterUser(@NonNull final UUID id) {
        getUser(id).ifPresent(user -> Schedulers.async().run(() -> {
            userRepository.updateUser(user.getId().toString(), user.getGamesPlayed(), user.getWins(), user.getKills(), user.getDeaths(), user.getLevelsReached(), user.getExp());
            user.getQuests().forEach(quest -> {
                if (questRepository.questExists(quest.getId().toString())) {
                    questRepository.updateQuest(quest.getId().toString(), quest.isCompleted(), quest.getCompletionTime(), quest.getProgress());
                }
                else {
                    questRepository.saveQuests(quest.getId().toString(), user.getId().toString(), quest.getCreationTime(), quest.isCompleted(), quest.getCompletionTime(), quest.getProgress(), quest.getType());
                }
            });
            userCache.remove(user);
            if (user.getCurrentArena() != null) {
                user.getCurrentArena().leave(user, Arena.ArenaLeaveCause.FORCE);
            }
        }));
    }

    @Override
    public @Nullable User getUserByPlayerNullable(@NonNull final Player player) {
        return getUserNullable(player.getUniqueId());
    }

    @Override
    public @NonNull Optional<User> getUserByPlayer(@NonNull final Player player) {
        return getUser(player.getUniqueId());
    }

    @Override
    public @NonNull Optional<User> getUserByPlayer(@NonNull final Optional<Player> player) {
        return player.map(this::getUserByPlayerNullable);
    }

    @Override
    public @Nullable User getUserNullable(@NonNull final UUID id) {
        return getUser(id).orElse(null);
    }

    @Override
    public @NonNull Optional<User> getUser(@NonNull final UUID id) {
        return userCache.stream()
                        .filter(u -> u.getId().equals(id))
                        .findFirst();
    }

    @Override
    public @NonNull Promise<Optional<User>> getOrLoadUser(final UUID id) {
        if (getUser(id).isPresent()) {
            return Promise.completed(getUser(id));
        }
        else {
            final Promise<@Nullable User> user = Promise.supplyingAsync(() -> userRepository.getUserByUuid(id.toString()));
            user.thenAcceptAsync(u -> {
                if (u != null) {
                    for (final Quest userQuest : questRepository.getUserQuests(u.getId().toString())) {
                        u.getQuests().add(userQuest);
                        userQuest.setHolder(u);
                    }
                }
            });
            return user.thenApplySync(Optional::of);


        }

    }

    @Override
    public @Nullable User getTopUserNullable() {
        return userCache.stream()
                        .filter(u -> u.getCurrentArenaSafe().isPresent())
                        .max(Comparator.comparingInt(User::getLocalLevelsReached))
                        .orElse(null);

    }


    @Override
    public Set<User> getAllUsers() {
        return userCache;
    }

    @Override
    public void setup(@NonNull final TerminableConsumer terminableConsumer) {
        Events.subscribe(PlayerJoinEvent.class)
              .ignoreCancelled(false)
              .handler(event -> {
                  Player player = event.getPlayer();
                  handleJoin(player);
              })
              .bindWith(terminableConsumer);

        Events.merge(Player.class)
              .bindEvent(PlayerQuitEvent.class, PlayerQuitEvent::getPlayer)
              .bindEvent(PlayerKickEvent.class, PlayerKickEvent::getPlayer)
              .handler(event -> {
                  Player player = event.getPlayer();
                  unregisterUser(player.getUniqueId());
              })
              .bindWith(terminableConsumer);
    }
}
