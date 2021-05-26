package com.pepej.gungame.npc;

import com.pepej.papi.config.objectmapping.ConfigSerializable;
import com.pepej.papi.config.objectmapping.meta.Matches;
import com.pepej.papi.config.objectmapping.meta.Setting;
import com.pepej.papi.serialize.Point;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigSerializable
public class NpcConfig {


    @Setting
    @Matches(value = "[a-z0-9-]*", failureMessage = "Invalid arena id")
    String id;

    @Setting
    Point position;

    @Setting
    String displayName;

    @Setting
    String texture;

    @Setting
    String signature;

    @Setting
    String command;

}
