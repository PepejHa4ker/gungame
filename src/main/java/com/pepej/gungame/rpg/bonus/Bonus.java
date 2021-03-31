package com.pepej.gungame.rpg.bonus;


import com.pepej.gungame.api.Arena;
import com.pepej.gungame.user.User;
import com.pepej.papi.serialize.Position;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.potion.PotionEffect;

@Getter
@EqualsAndHashCode
@Data
public abstract class Bonus {

    private final String bonusName;
    private final BonusType bonusType;
    private final Arena arena;

    private Position position;
    private PotionEffect effect;
    private int exp;
    private int hp;

    protected Bonus(final BonusType bonusType, final String bonusName, final Arena arena) {
        this.bonusType = bonusType;
        this.bonusName = bonusName;
        this.arena = arena;
    }



    public abstract void onCollect(User user);
}