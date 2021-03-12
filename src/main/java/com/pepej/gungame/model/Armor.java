package com.pepej.gungame.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;
import java.util.TreeSet;

import static org.bukkit.Material.*;


@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NonNull
public class Armor implements Comparable<Armor> {

    @Getter
    private static Set<Armor> values = new TreeSet<>();
    int level;
    Material helmet;
    Material chestPlate;
    Material leggings;
    Material boots;
    Material sword;


    static {
        values.add(Armor.builder().level(1).helmet(LEATHER_HELMET).chestPlate(LEATHER_CHESTPLATE).leggings(LEATHER_LEGGINGS).boots(LEATHER_BOOTS).sword(WOOD_SWORD).build());
        values.add(Armor.builder().level(2).helmet(CHAINMAIL_HELMET).chestPlate(LEATHER_CHESTPLATE).leggings(LEATHER_LEGGINGS).boots(LEATHER_BOOTS).sword(STONE_SWORD).build());
        values.add(Armor.builder().level(3).helmet(CHAINMAIL_HELMET).chestPlate(CHAINMAIL_CHESTPLATE).leggings(LEATHER_LEGGINGS).boots(LEATHER_BOOTS).sword(STONE_SWORD).build());
        values.add(Armor.builder().level(4).helmet(CHAINMAIL_HELMET).chestPlate(CHAINMAIL_CHESTPLATE).leggings(CHAINMAIL_LEGGINGS).boots(LEATHER_BOOTS).sword(STONE_SWORD).build());
        values.add(Armor.builder().level(5).helmet(CHAINMAIL_HELMET).chestPlate(CHAINMAIL_CHESTPLATE).leggings(CHAINMAIL_LEGGINGS).boots(CHAINMAIL_LEGGINGS).sword(STONE_SWORD).build());
        values.add(Armor.builder().level(6).helmet(IRON_HELMET).chestPlate(CHAINMAIL_CHESTPLATE).leggings(CHAINMAIL_LEGGINGS).boots(CHAINMAIL_LEGGINGS).sword(IRON_SWORD).build());
        values.add(Armor.builder().level(7).helmet(IRON_HELMET).chestPlate(IRON_CHESTPLATE).leggings(CHAINMAIL_LEGGINGS).boots(CHAINMAIL_LEGGINGS).sword(IRON_SWORD).build());
        values.add(Armor.builder().level(8).helmet(IRON_HELMET).chestPlate(IRON_CHESTPLATE).leggings(IRON_LEGGINGS).boots(CHAINMAIL_LEGGINGS).sword(IRON_SWORD).build());
        values.add(Armor.builder().level(9).helmet(IRON_HELMET).chestPlate(IRON_CHESTPLATE).leggings(IRON_LEGGINGS).boots(IRON_LEGGINGS).sword(IRON_SWORD).build());
        values.add(Armor.builder().level(10).helmet(IRON_HELMET).chestPlate(IRON_CHESTPLATE).leggings(IRON_LEGGINGS).boots(IRON_LEGGINGS).sword(DIAMOND_SWORD).build());
        values.add(Armor.builder().level(11).helmet(AIR).chestPlate(AIR).leggings(AIR).boots(AIR).sword(WOOD_SWORD).build());

    }
    @Override
    public int compareTo(@NonNull final Armor o) {
        if (level == o.level) {
            return 0;
        } else if (level < o.level) {
            return -1;
        }
        return 1;
    }
}