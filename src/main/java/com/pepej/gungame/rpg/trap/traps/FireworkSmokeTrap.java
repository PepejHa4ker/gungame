package com.pepej.gungame.rpg.trap.traps;


import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.pepej.gungame.api.Arena;
import com.pepej.gungame.rpg.trap.TrapBase;
import com.pepej.gungame.rpg.trap.TrapType;
import com.pepej.gungame.user.User;
import com.pepej.papi.protocol.Protocol;
import com.pepej.papi.scheduler.Schedulers;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

public class FireworkSmokeTrap extends TrapBase {
    public FireworkSmokeTrap() {
        super("firework smoke", TrapType.FIREWORK_SMOKE);
    }

    @Override
    public void onActivate(@NonNull final User user, @NonNull final Arena arena) {
        Player player = user.asPlayer();
        AtomicInteger counter = new AtomicInteger(300);
        getUserService().broadcastMessage(arena, format("&e\"Да кто тут кинул этот смок? Я ничерта не вижу\" &7- пробормотал &a%s,&7 задыхаясь в угарном газе", user.getUsername()));
        Schedulers.async()
                  .runRepeating(task -> {
                      if (counter.getAndDecrement() <= 0) {
                          task.close();
                      } else {
                          if (player != null && player.isOnline()) {
                              PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.WORLD_PARTICLES);
                              packetContainer.getModifier().writeDefaults();
                              packetContainer.getParticles().write(0, EnumWrappers.Particle.EXPLOSION_HUGE);
                              final Location location = player.getEyeLocation();
                              packetContainer.getFloat()
                                             .write(0, (float) location.getX())
                                             .write(1, (float) location.getY())
                                             .write(2, (float) location.getZ()-0.5f)
                                             .write(3, 0.5f) //x offset
                                             .write(4, 0.5f) //y offset
                                             .write(5, 0.5f);//z offset
                              Protocol.broadcastPacket(packetContainer);
                          } else {
                              task.stop();
                          }

                      }
                  }, 0, 1)
                  .bindWith(this);

    }
}
