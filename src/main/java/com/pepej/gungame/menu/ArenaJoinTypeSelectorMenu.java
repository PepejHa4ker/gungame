package com.pepej.gungame.menu;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.service.UserService;
import com.pepej.papi.item.ItemStackBuilder;
import com.pepej.papi.menu.Menu;
import com.pepej.papi.menu.scheme.MenuPopulator;
import com.pepej.papi.menu.scheme.MenuScheme;
import com.pepej.papi.services.Services;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ArenaJoinTypeSelectorMenu extends Menu {

    private final Arena arena;
    private final UserService userService;

    private static final MenuScheme MEMBER_SCHEME = new MenuScheme()
            .mask("000000000")
            .mask("011100000")
            .mask("011100000")
            .mask("011100000")
            .mask("000000000");

    private static final MenuScheme SPECTATOR_SCHEME = new MenuScheme()
            .mask("000000000")
            .mask("000001110")
            .mask("000001110")
            .mask("000001110")
            .mask("000000000");



    public ArenaJoinTypeSelectorMenu(final Player player, final Arena arena) {
        super(player, 5, "Выбор режима");
        this.arena = arena;
        this.userService = Services.load(UserService.class);
    }


    @Override
    public void redraw() {
        final MenuPopulator memberPop = MEMBER_SCHEME.newPopulator(this);
        final MenuPopulator spectatorPop = SPECTATOR_SCHEME.newPopulator(this);
        for (int i = 0; i < 9; i++) {
            memberPop.accept(ItemStackBuilder.of(Material.DIAMOND)
            .nameClickable("&aПрисоединиться в качестве участника")
            .loreClickable("присоединиться к арене")
                    .buildConsumer(e -> {
                        Player player = (Player) e.getWhoClicked();
                        userService.getUserByPlayer(player).ifPresent(user -> arena.join(user, Arena.ArenaJoinType.MEMBER));

                    }));

            spectatorPop.accept(ItemStackBuilder.of(Material.EMERALD)
                                             .nameClickable("&aПрисоединиться в качестве наблюдателя")
                                             .loreClickable("присоединиться к арене")
                                             .buildConsumer(e -> {
                                                 Player player = (Player) e.getWhoClicked();
                                                 userService.getUserByPlayer(player).ifPresent(user -> arena.join(user, Arena.ArenaJoinType.SPECTATOR));

                                             }));
        }
    }
}
