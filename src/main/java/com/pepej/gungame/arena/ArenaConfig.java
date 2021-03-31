package com.pepej.gungame.arena;


import com.pepej.papi.config.objectmapping.ConfigSerializable;
import com.pepej.papi.config.objectmapping.meta.Required;
import com.pepej.papi.config.objectmapping.meta.Setting;
import com.pepej.papi.serialize.Position;
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
    @Required
    Position lobby;

    @Setting
    @Required
    Position startPosition;

    @Setting
    @Required
    List<Position> positions;

//    @Setting
//    @Required
//    List<Position> possibleBonusSpawns;

    @Setting
    @Required
    String arenaName;

    @Setting
    @Required
    String arenaWorld;

    @Setting
    @Required
    int maxPlayers;

    @Setting
    @Required
    int requiredPlayersToStart;

    @Setting
    @Required
    Duration arenaStartDelay;

    @Setting
    @Required
    Duration arenaGameTime;
}
