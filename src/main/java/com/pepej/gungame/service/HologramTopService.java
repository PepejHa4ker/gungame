package com.pepej.gungame.service;

import com.pepej.gungame.hologram.GunGameHologram;
import lombok.Getter;

import java.util.Map;

public interface HologramTopService {

    void register(TopStrategy strategy, GunGameHologram hologram);

    void updateHolograms();

    Map<TopStrategy, GunGameHologram> getHolograms();




    @Getter
    enum TopStrategy {
        KILLS("Убийство", "Убийства", "Убийств"),
        WINS("Победа", "Победы", "Побед"),
        GAMES("Игра", "Игры", "Игр"),
        LEVELS("Уровень", "Уровня", "Уровней"),
        EXP("Опыт", "Опыта", "Опыта");

        private final String decsSingle;
        private final String decsA;
        private final String decsB;


        TopStrategy(final String decsSingle, final String decsA, final String decsB) {
            this.decsSingle = decsSingle;
            this.decsA = decsA;
            this.decsB = decsB;
        }
    }


}
