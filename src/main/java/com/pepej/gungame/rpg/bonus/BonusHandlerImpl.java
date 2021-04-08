package com.pepej.gungame.rpg.bonus;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.random.RandomSelector;
import com.pepej.papi.serialize.Position;
import com.pepej.papi.services.Service;
import com.pepej.papi.terminable.TerminableConsumer;
import com.pepej.papi.terminable.module.TerminableModule;
import org.bukkit.Material;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BonusHandlerImpl implements BonusHandler, TerminableModule {

    @Service
    private BonusRegistrar registrar;
    @Service
    private UserService userService;


    @Override
    public void spawn(final Arena arena, final Bonus bonus, final Position position) {
        position.toLocation().getWorld().getBlockAt(position.toLocation()).setType(Material.BEACON);
    }

    @Override
    public Bonus selectRandomBonus(Arena arena) {
        return RandomSelector.uniform(registrar.getBonuses().get(arena)).pick();
    }

    @Override
    public void handle(final Arena arena, Bonus bonus, final User user) {
        bonus.onCollect(user);
    }


    @Override
    public void setup(@NonNull final TerminableConsumer consumer) {
//        Events.subscribe(PlayerInteractEvent.class)
//              .filter(e -> e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.BEACON)
//              .handler(e -> {
//                  Position clickPos = Position.of(e.getClickedBlock().getLocation());
//                  userService.getUserByPlayer(e.getPlayer())
//                             .filter(user -> user.getCurrentArena() != null)
//                             .ifPresent(user -> registrar.getBonuses().get(user.getCurrentArena())
//                                                         .stream()
//                                                         .filter(bonus -> bonus.getPosition() != null && bonus.getPosition().equals(clickPos))
//                                                         .findFirst()
//                                                         .ifPresent(b -> handle(b.getArena(), b, user)));
//              })
//              .bindWith(consumer);

    }
}
