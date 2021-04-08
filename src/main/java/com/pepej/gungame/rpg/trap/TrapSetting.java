package com.pepej.gungame.rpg.trap;

import com.pepej.papi.serialize.Position;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrapSetting {

    private final TrapType trapType;
    private Position first;
    private Position last;
}
