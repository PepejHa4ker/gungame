package com.pepej.gungame.service.impl;

import com.google.inject.Singleton;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.events.Events;
import com.pepej.papi.terminable.TerminableConsumer;
import com.pepej.papi.terminable.module.TerminableModule;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

@Singleton
@Getter
public class UserServiceImpl implements UserService, TerminableModule {

    private final Set<User> userCache;

    public UserServiceImpl() {
        this.userCache = new HashSet<>();
    }

    @Override
    public void registerUser(@NonNull final UUID id, final String username) {
        User user = new User(id, username);
        userCache.add(user);
    }

    @Override
    public void unregisterUser(@NonNull final UUID id) {
        getUser(id).ifPresent(this.userCache::remove);
    }

    @Override
    public Optional<User> getUser(@NonNull final UUID id) {
        return userCache.stream().filter(u -> u.getId().equals(id)).findFirst();
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
