package com.pepej.gungame.rpg.trap;

import com.pepej.gungame.Metadatas;
import com.pepej.gungame.api.Arena;
import com.pepej.gungame.user.User;
import com.pepej.papi.item.ItemStackBuilder;
import com.pepej.papi.metadata.Metadata;
import com.pepej.papi.metadata.MetadataMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SuperBowTrap extends TrapBase {

    private static final ItemStack SUPER_BOW = ItemStackBuilder.of(Material.BOW)
                                                               .name("&dСупер лук")
                                                               .lore("&eСтреляет супер стрелами, которые моментально убивают жертву")
                                                               .breakable(false)
                                                               .build();
    public static final ItemStack SUPER_ARROW = ItemStackBuilder.of(Material.ARROW)
                                                                .name("&dСупер стрела для супер лука")
                                                                .lore("&7Эта стреля обладает разумом")
                                                                .lore("&7Она будет возвращаться к Вам до тех пор, пока не настигнет цели")
                                                                .build();

    public SuperBowTrap() {
        super("super bow", TrapType.SUPER_BOW);
    }

    @Override
    public void onActivate(@NonNull final User user, @NonNull final Arena arena) {
        final Player player = user.asPlayer();
        final MetadataMap metadataMap = Metadata.provideForPlayer(player);
        final int superShots = metadataMap.getOrDefault(Metadatas.SUPER_SHOTS, 0);
        metadataMap.put(Metadatas.SUPER_SHOTS, superShots + 1);
        player.getInventory().addItem(SUPER_BOW);
        player.getInventory().addItem(SUPER_ARROW);


    }
}
