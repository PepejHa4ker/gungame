package com.pepej.gungame.menu;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.service.ArenaService;
import com.pepej.gungame.service.UserService;
import com.pepej.papi.item.ItemStackBuilder;
import com.pepej.papi.menu.Menu;
import com.pepej.papi.menu.scheme.MenuPopulator;
import com.pepej.papi.menu.scheme.MenuScheme;
import com.pepej.papi.services.Services;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static com.pepej.gungame.api.Arena.ArenaState.WAITING;
import static java.lang.String.format;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArenaSelectorMenu extends Menu {

    ArenaService arenaService;
    UserService userService;


    private static final MenuScheme ARENAS_SCHEME = new MenuScheme()
            .mask("111101111");
    private static final MenuScheme RANDOM_JOIN_SCHEME = new MenuScheme()
            .mask("000010000");

    public ArenaSelectorMenu(final Player player) {
        super(player, 1, "Выбор арены");
        this.arenaService = Services.load(ArenaService.class);
        this.userService = Services.load(UserService.class);
    }

    @Override
    public void redraw() {
        MenuPopulator arenaPopulator = ARENAS_SCHEME.newPopulator(this);
        MenuPopulator rjPopulator = RANDOM_JOIN_SCHEME.newPopulator(this);
        rjPopulator.accept(ItemStackBuilder.of(Material.COMPASS)
                                           .nameClickable("&aСлучайная арена")
                                           .buildConsumer(e -> {
                                                   e.getWhoClicked().closeInventory();
                                                   arenaService.getMostRelevantArena()
                                                               .ifPresent(a -> userService
                                                                       .getUserByPlayer((Player) e.getWhoClicked())
                                                                       .ifPresent(a::join));

                                           })

        );

        for (Arena a : arenaService.getArenas()) {
            String color = a.getState() == WAITING ? "&a" : "&c" + a.getContext().getConfig().getArenaName();
            arenaPopulator.accept(
                    ItemStackBuilder.of(a.getState() == WAITING ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK)

                                    .nameClickable(format("%s%s", color, a.getContext().getConfig().getArenaName()))
                                    .lore(
                                            format("&7Игроков: &e[%s/%s]", a.getContext().getUsersCount(), a.getContext().getConfig().getMaxPlayers()),
                                            "",
                                            format("%s%s", color, a.getState().getDesc()),
                                            format("&7Макс. уровень: %s/12", a.getContext().getMaxUserLevel())
                                    )
                                    .buildConsumer(e -> {
                                        e.getWhoClicked().closeInventory();
                                        userService.getUser(e.getWhoClicked().getUniqueId()).ifPresent(a::join);
                                    })
            );
        }

    }
}
