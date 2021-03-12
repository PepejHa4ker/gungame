package com.pepej.gungame.equipment;

import com.pepej.gungame.model.Armor;
import com.pepej.gungame.user.User;
import com.pepej.papi.item.ItemStackBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EquipmentResolverImpl implements EquipmentResolver {

    @Override
    public Armor resolve(final int level) {
        return Armor.getValues().stream()
                .filter(a -> a.getLevel() == level)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void equipUser(@NonNull final User user, @NonNull final Armor armor) {
        Player player = user.asPlayer();
        final PlayerInventory inventory = player.getInventory();
        ItemStack helmet = ItemStackBuilder.of(armor.getHelmet())
                .breakable(false)
                .hideAttributes()
                .build();
        ItemStack chestPlate = ItemStackBuilder.of(armor.getChestPlate())
                                           .breakable(false)
                                           .hideAttributes()
                                           .build();
        ItemStack leggings = ItemStackBuilder.of(armor.getLeggings())
                                           .breakable(false)
                                           .hideAttributes()
                                           .build();
        ItemStack boots = ItemStackBuilder.of(armor.getBoots())
                                           .breakable(false)
                                           .hideAttributes()
                                           .build();

        ItemStack sword = ItemStackBuilder.of(armor.getSword())
                .breakable(false)
                .hideAttributes()
                .build();
        inventory.setArmorContents(new ItemStack[]{boots, leggings, chestPlate, helmet});
        inventory.setItem(0, sword);
        inventory.setHeldItemSlot(0);
    }


}


