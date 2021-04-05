package com.pepej.gungame.arena;


import com.pepej.papi.config.objectmapping.ConfigSerializable;
import com.pepej.papi.config.objectmapping.meta.Setting;
import com.pepej.papi.serialize.Point;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Duration;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigSerializable
public class ArenaConfig {

    @Setting
    String arenaId;

    @Setting
    Point startPosition;

    @Setting
    List<Point> positions;

    @Setting
    String arenaName;

    @Setting
    String arenaWorld;

    @Setting
    int maxPlayers;

    @Setting
    int requiredPlayersToStart;

    @Setting
    Duration arenaStartDelay;

    @Setting
    Duration arenaGameTime;
}
