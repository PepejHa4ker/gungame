package com.pepej.gungame.hologram;

import com.pepej.papi.hologram.Hologram;
import com.pepej.papi.npc.CitizensNpc;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Data
public class GunGameHologram {

   Hologram hologram;
   HologramConfig config;
   CitizensNpc currentNpc;

}
