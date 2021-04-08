package com.pepej.gungame.rpg.trap;

import com.pepej.papi.config.objectmapping.ConfigSerializable;
import com.pepej.papi.serialize.Region;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigSerializable
public class GunGameTrap {

    TrapType type;
    Region region;
}
