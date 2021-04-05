package com.pepej.gungame.equipment;

import com.pepej.gungame.model.Armor;
import com.pepej.gungame.user.User;
import com.pepej.papi.services.Implementor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Implementor(EquipmentResolverImpl.class)
public interface EquipmentResolver {

    Armor resolve(int level);

    void equipUser(@NonNull User user,  @NonNull Armor armor);


}
