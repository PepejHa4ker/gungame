package com.pepej.gungame.arena;


import com.google.common.reflect.TypeToken;
import com.pepej.papi.serialize.Point;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConfigSerializable
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArenaConfig {

    public static final TypeToken<ArenaConfig> TOKEN = TypeToken.of(ArenaConfig.class);

    @Setting("lobby")
    @NonNull
    Point lobby;

    @Setting("arena-world")
    String arenaWorld;

    @Setting("arena-name")
    String arenaName;


}
