package com.pepej.gungame.service.impl;

import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.events.Events;
import com.pepej.papi.terminable.TerminableConsumer;
import com.pepej.papi.terminable.module.TerminableModule;
import com.pepej.papi.utils.Log;
import com.pepej.papi.utils.Players;
import lombok.Getter;
import org.bukkit.entity.Player;
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

    public UserServiceImpl() {
        this.userCache = new HashSet<>();
        this.loadAllOnlineUsers();
    }

    @Override
    public void loadAllOnlineUsers() {
        Players.all()
               .forEach(player -> registerUser(player.getUniqueId(), player.getName()));
    }

    @Override
    public void registerUser(@NonNull final UUID id, final String username) {
        User user = new User(id, username);
        Log.info("loading user %s...", user);
        userCache.add(user);

    }

    @Override
    public void unregisterUser(@NonNull final UUID id) {
        getUser(id).ifPresent(this.userCache::remove);
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

        Events.subscribe(PlayerQuitEvent.class)
              .ignoreCancelled(false)
              .handler(event -> {
                  Player player = event.getPlayer();
                  unregisterUser(player.getUniqueId());
              })
              .bindWith(terminableConsumer);
    }
}
