package com.pepej.gungame.menu;

import com.google.inject.Inject;
import com.pepej.gungame.api.Arena;
import com.pepej.gungame.service.ArenaService;
import com.pepej.gungame.service.UserService;
import com.pepej.papi.item.ItemStackBuilder;
import com.pepej.papi.menu.Menu;
import com.pepej.papi.menu.scheme.MenuPopulator;
import com.pepej.papi.menu.scheme.MenuScheme;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static java.lang.System.out;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArenaSelectorMenu extends Menu {

    ArenaService arenaService;
    UserService userService;


    private static final MenuScheme ARENAS_SCHEME = new MenuScheme()
            .mask("111101111");
    private static final MenuScheme RANDOM_JOIN_SCHEME = new MenuScheme()
            .mask("000010000");

    @Inject
    public ArenaSelectorMenu(final Player player, final ArenaService arenaService, final UserService userService) {
        super(player, 1, "Выбоо арены");
        this.arenaService = arenaService;
        this.userService = userService;
    }

    @Override
    public void redraw() {
        MenuPopulator arenaPopulator = ARENAS_SCHEME.newPopulator(this);
        MenuPopulator rjPopulator = RANDOM_JOIN_SCHEME.newPopulator(this);
        rjPopulator.accept(ItemStackBuilder.of(Material.BONE)
                                            .buildConsumer(e -> {
                                               arenaService.getMostRelevantArena().ifPresent(a -> {
                                                   userService.getUser(e.getWhoClicked().getUniqueId()).ifPresent(a::join);
                                               });
                                            })
        );

        for (Arena a : arenaService.getArenas()) {
            out.println(a.getContext().getName());
            arenaPopulator.accept(
                    ItemStackBuilder.of(Material.BEDROCK)
                                    .name(a.getContext().getName())
                                    .buildConsumer(e -> {
//                                        a.join();
                                    })
            );
        }

    }
}
