package com.pepej.gungame.api;


import com.google.inject.AbstractModule;
import com.pepej.gungame.arena.loader.ArenaLoader;
import com.pepej.gungame.arena.loader.SimpleArenaLoader;
import com.pepej.gungame.service.TeamService;
import com.pepej.gungame.service.impl.TeamServiceImpl;

public class GunGameModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TeamService.class).to(TeamServiceImpl.class);
        bind(ArenaLoader.class).to(SimpleArenaLoader.class);
    }
}
