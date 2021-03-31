package com.pepej.gungame;


import com.pepej.papi.config.objectmapping.ConfigSerializable;
import com.pepej.papi.config.objectmapping.meta.Setting;
import com.pepej.papi.serialize.Position;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigSerializable
public class GlobalConfig {

    @Setting
    Position lobbyPosition;

    @Setting
    long hologramUpdateTicks;

    @Setting
    Duration questDelay;
}
