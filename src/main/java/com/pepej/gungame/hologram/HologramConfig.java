package com.pepej.gungame.hologram;

import com.pepej.gungame.service.HologramTopService;
import com.pepej.papi.config.objectmapping.ConfigSerializable;
import com.pepej.papi.config.objectmapping.meta.Setting;
import com.pepej.papi.serialize.Point;
import com.pepej.papi.serialize.Position;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigSerializable
public class HologramConfig {

    @Setting
    Position position;

    @Setting
    Point npcPosition;

    @Setting
    HologramTopService.TopStrategy strategy;

}
