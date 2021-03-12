package com.pepej.gungame.service.impl;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.adventure.platform.bukkit.BukkitAudiences;
import com.pepej.papi.adventure.text.Component;
import com.pepej.papi.adventure.title.Title;
import com.pepej.papi.events.Events;
import com.pepej.papi.services.Services;
import com.pepej.papi.terminable.TerminableConsumer;
import com.pepej.papi.terminable.module.TerminableModule;
import com.pepej.papi.utils.Log;
import com.pepej.papi.utils.Players;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
public class UserServiceImpl implements UserService, TerminableModule {

    private final Set<User> userCache;
    private final BukkitAudiences audiences;

    public UserServiceImpl() {
        this.userCache = new HashSet<>();
        this.loadAllOnlineUsers();
        this.audiences = Services.load(BukkitAudiences.class);
    }

    @Override
    public void loadAllOnlineUsers() {
        Players.all()
               .forEach(player -> registerUser(player.getUniqueId(), player.getName()));
    }

    @Override
    public void broadcastMessage(@NonNull final Component message) {
        for (User user : userCache) {
            audiences.player(user.asPlayer()).sendMessage(message);
        }
    }

    @Override
    public void sendMessage(@NonNull final User user, @NonNull final Component message) {
        audiences.player(user.asPlayer()).sendMessage(message);
    }

    @Override
    public void sendBossBar(@NonNull final User user, @NonNull final Component message) {
        audiences.player(user.asPlayer()).sendActionBar(message);

    }

    @Override
    public void sendTitle(@NonNull final User user, @NonNull final Title title) {
        audiences.player(user.asPlayer()).showTitle(title);
    }

    @Override
    public void registerUser(@NonNull final UUID id, final String username) {
        User user = new User(id, username);
        Log.info("loading user %s...", user);
        userCache.add(user);

    }

    @Override
    public void unregisterUser(@NonNull final UUID id) {
        getUser(id).ifPresent(user -> {
                userCache.remove(user);
                if (user.getCurrentArena() != null) {
                    user.getCurrentArena().leave(user, Arena.ArenaLeaveCause.FORCE);
                }
        });
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
    public Optional<User> getUser(@NonNull final UUID id) {
        return userCache.stream()
                        .filter(u -> u.getId().equals(id))
                        .findFirst();
    }

    @Override
    public @Nullable User getTopUserNullable() {
        return userCache.stream()
                        .filter(u -> u.getCurrentArenaSafe().isPresent())
                        .filter(u -> !u.isDied())
                        .max((u1, u2) -> u2.getLocalLevelsReached() - u1.getLocalLevelsReached())
                        .orElse(null);
    }


    @Override
    public Set<User> getAllUsers() {
        return userCache;
    }

    @Override
    public void setup(@NonNull final TerminableConsumer terminableConsumer) {
        Events.subscribe(PlayerLoginEvent.class)
              .ignoreCancelled(false)
              .handler(event -> {
                  Player player = event.getPlayer();
                  registerUser(player.getUniqueId(), player.getName());
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
