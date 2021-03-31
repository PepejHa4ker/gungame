package com.pepej.gungame.hologram;

import com.pepej.papi.hologram.individual.IndividualHologram;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Data
public class GunGameHologram {

    double x;
    double y;
    double z;
    String world;
    HologramType type;
    long updateInterval;
    final IndividualHologram source;

}
