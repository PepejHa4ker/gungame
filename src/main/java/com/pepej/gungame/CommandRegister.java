package com.pepej.gungame;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.pepej.gungame.api.Arena;
import com.pepej.gungame.arena.ArenaConfig;
import com.pepej.gungame.arena.loader.ArenaLoader;
import com.pepej.gungame.menu.ArenaSelectorMenu;
import com.pepej.gungame.menu.QuestSelectorMenu;
import com.pepej.gungame.rpg.trap.GunGameTrap;
import com.pepej.gungame.rpg.trap.TrapSetting;
import com.pepej.gungame.rpg.trap.TrapType;
import com.pepej.gungame.service.ArenaService;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.command.CommandInterruptException;
import com.pepej.papi.command.Commands;
import com.pepej.papi.config.ConfigFactory;
import com.pepej.papi.config.ConfigurationNode;
import com.pepej.papi.config.serialize.SerializationException;
import com.pepej.papi.metadata.Metadata;
import com.pepej.papi.metadata.MetadataMap;
import com.pepej.papi.promise.Promise;
import com.pepej.papi.protocol.Protocol;
import com.pepej.papi.scheduler.Schedulers;
import com.pepej.papi.serialize.Point;
import com.pepej.papi.serialize.Position;
import com.pepej.papi.serialize.Region;
import com.pepej.papi.services.Services;
import com.pepej.papi.terminable.TerminableConsumer;
import com.pepej.papi.terminable.module.TerminableModule;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class CommandRegister implements TerminableModule {

    private final UserService userService;
    private final ArenaService arenaService;
    private final ArenaLoader arenaLoader;

    public CommandRegister() {
        userService = Services.load(UserService.class);
        arenaService = Services.load(ArenaService.class);
        arenaLoader = Services.load(ArenaLoader.class);
    }

    @Override
    public void setup(@NonNull final TerminableConsumer consumer) {
        Commands.create()
                .assertPlayer()
                .assertUsage("<arena>")
                .description("Add spawn to arena")
                .assertPermission("gungame.admin")
                .tabHandler(context -> arenaService.getArenas().stream().map(a -> a.getContext().getConfig().getArenaId()).collect(toList()))
                .handler(context -> {
                    Arena arena = arenaService.getArenaNullable(context.arg(0).parseOrFail(String.class));
                    if (arena == null) {
                        context.replyError("Арена не найдена");
                        return;
                    }
                    final ArenaConfig config = arena.getContext().getConfig();
                    config.getPositions().add(Point.of(context.sender().getLocation()));
                    arena.getContext().setConfig(config);
                    try {
                        final File file = GunGame.getInstance().getBundledFile("arenas.json");
                        final ConfigurationNode node = ConfigFactory.gson().load(file);
                        final List<ArenaConfig> configs = node.getList(ArenaConfig.class);
                        if (configs == null) {
                            return;
                        }
                        configs.stream().filter(c -> c.getArenaId().equals(config.getArenaId())).findFirst().ifPresent(configs::remove);
                        configs.add(config);
                        node.setList(ArenaConfig.class, configs);
                        ConfigFactory.gson().save(file, node);
                    } catch (SerializationException e) {
                        context.replyError("Ошибка во время сохранения файла..");
                    }
                    context.replyAnnouncement("Спавн был успешно добавлен");
                })
                .registerAndBind(consumer, "addspawn");
        Commands.create()
                .assertPlayer()
                .assertUsage("<arena>")
                .description("View arena spawns")
                .assertPermission("gungame.admin")
                .assertCooldown(10, TimeUnit.SECONDS)
                .tabHandler(context -> arenaService.getArenas().stream().map(a -> a.getContext().getConfig().getArenaId()).collect(toList()))
                .handler(context -> {
                    Arena arena = arenaService.getArenaNullable(context.arg(0).parseOrFail(String.class));
                    if (arena == null) {
                        context.replyError("Арена не найдена");
                        return;
                    }
                    final ArenaConfig config = arena.getContext().getConfig();
                    AtomicInteger counter = new AtomicInteger(40);
                    Schedulers.async()
                              .runRepeating(task -> {
                                  counter.getAndDecrement();
                                  if (counter.get() <= 0) {
                                      task.close();
                                  }
                                  for (Point point : config.getPositions()) {
                                      Location location = point.toLocation();
                                      PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.WORLD_PARTICLES);
                                      packetContainer.getModifier().writeDefaults();
                                      packetContainer.getParticles().write(0, EnumWrappers.Particle.BARRIER);
                                      packetContainer.getFloat().write(0, (float) location.getX()).write(1, (float) location.getY()).write(2, (float) location.getZ());
                                      Protocol.sendPacket(context.sender(), packetContainer);
                                  }
                              }, 5,5)
                              .bindWith(consumer);


                })
                .registerAndBind(consumer, "spawns");
        Commands.create()
                .assertPlayer()
                .assertUsage("<arena>")
                .description("Create new arena")
                .assertPermission("gungame.admin")
                .tabHandler(context -> arenaService.getArenas().stream().map(a -> a.getContext().getConfig().getArenaId()).collect(toList()))
                .handler(context -> {
                    String arena = context.arg(0).parseOrFail(String.class);
                    final MetadataMap metadataMap = Metadata.provideForPlayer(context.sender());
                    if (metadataMap.getOrNull(Metadatas.CREATED_ARENA) != null) {
                        context.replyError("Вы уже настраиваете одну арену");
                        return;
                    }

                    final ArenaConfig.ArenaConfigBuilder builder = ArenaConfig.builder()
                                                                              .arenaWorld(context.sender().getWorld().getName())
                                                                              .startPosition(Point.of(context.sender().getLocation()))
                                                                              .arenaGameTime(Duration.ofMinutes(5))
                                                                              .maxPlayers(16)
                                                                              .requiredPlayersToStart(4)
                                                                              .arenaStartDelay(Duration.ofSeconds(20))
                                                                              .positions(new ArrayList<>())
                                                                              .traps(new ArrayList<>())
                                                                              .arenaId(arena);
                    metadataMap.put(Metadatas.CREATED_ARENA, builder);

                    userService.getUserByPlayer(context.sender()).ifPresent(u -> userService.sendMessage(u, "&aВведите имя арены"));
                })
                .registerAndBind(consumer, "create");
        Commands.create()
                .assertUsage("<arena> <trap>")
                .assertPlayer()
                .description("Add new trap")
                .assertPermission("gungame.admin")
                .tabHandler(context -> {
                    if (context.args().size() <= 1) {
                        return arenaService.getArenas().stream().map(a -> a.getContext().getConfig().getArenaId()).collect(toList());
                    } else {
                        return Arrays.stream(TrapType.values()).map(TrapType::name).map(String::toLowerCase).collect(toList());
                    }
                })
                .handler(context -> {
                    String arenaId = context.arg(0).parseOrFail(String.class);
                    final Arena arena = arenaService.getArenaNullable(arenaId);
                    if (arena == null) {
                        context.replyError("Арена с текущим id не найдена");
                        return;
                    }
                    final MetadataMap metadataMap = Metadata.provideForPlayer(context.sender());
                    final TrapType trapType = TrapType.valueOf(context.arg(1).parseOrFail(String.class).toUpperCase(Locale.ROOT));
                    final Position position = Position.of(context.sender().getLocation());
                    final TrapSetting trapSetting = metadataMap.getOrDefault(Metadatas.TRAP_SETTING, new TrapSetting(trapType, null, null));
                    if (trapSetting.getFirst() != null) {
                        final Position firstPosition = trapSetting.getFirst();
                        final ArenaConfig config = arena.getContext().getConfig();
                        final Region region = Region.of(firstPosition, position);
                        config.getTraps().add(new GunGameTrap(trapType, region));
                        arenaLoader.loadAndSaveArenaFromConfig(config);
                        context.replyAnnouncement("Вы успешно добавили ловушку на арену");
                        metadataMap.remove(Metadatas.TRAP_SETTING);
                    } else {
                        trapSetting.setFirst(position);
                        metadataMap.put(Metadatas.TRAP_SETTING, trapSetting);
                        context.replyAnnouncement("Первая точка успешно добавлена! Встаньте в другую точку и пропишите эту команду еще раз");
                    }


                })
                .registerAndBind(consumer, "addtrap");
        Commands.create()
                .assertPlayer()
                .assertUsage("<arena>")
                .description("Remove arena")
                .assertPermission("gungame.admin")
                .tabHandler(context -> arenaService.getArenas().stream().map(a -> a.getContext().getConfig().getArenaId()).collect(toList()))
                .handler(context -> {
                    String arena = context.arg(0).parseOrFail(String.class);
                    if (arenaService.getArenaNullable(arena) == null) {
                        context.replyError("Арена с текущим id не найдена");
                        return;
                    }
                    arenaLoader.removeArena(arena);
                   context.replyAnnouncement("Арена была успешно удалена");

                })
                .registerAndBind(consumer, "remove");
        Commands.create()
                .assertPlayer()
                .description("Quest command")
                .handler(context -> new QuestSelectorMenu(context.sender()).open())
                .registerAndBind(consumer, "quests");


        Commands.create()
                .assertPlayer()
                .handler(context -> new ArenaSelectorMenu(context.sender()).open())
                .registerAndBind(consumer, "arenas");
        Commands.create()
                .assertPlayer()
                .handler(context -> {
                    userService.getUserByPlayer(context.sender())
                               .ifPresent(u -> {
                                   u.getCurrentArenaSafe().ifPresent(a -> a.leave(u, Arena.ArenaLeaveCause.FORCE));
                               });
                })
                .register("leave", "лив", "выйти");

        Commands.create()
                .assertPlayer()
                .assertUsage("[ник]")
                .assertCooldown(5, TimeUnit.SECONDS)
                .handler(context -> {
                    userService.getUserByPlayer(context.sender()).ifPresent(u -> {
                        if (context.args().isEmpty()) {
                            sendTopMessage(userService, u, null);
                        }
                        else {
                            Promise.start()
                                   .thenRunAsync(() -> context.reply("&7Загружаю данные.."))
                                   .thenRunAsync(() -> {
                                       try {
                                           final Promise<Optional<User>> user = userService.getOrLoadUser(context.arg(0).parse(OfflinePlayer.class).orElseThrow(() -> new CommandInterruptException("Игрок не найден")).getUniqueId());
                                           user.thenAcceptAsync(user1 -> {
                                               if (user1.isPresent()) {
                                                   sendTopMessage(userService, u, user1.get());

                                               }
                                               else {
                                                   context.replyError("Игрок не найден");
                                               }
                                           });
                                       } catch (CommandInterruptException e) {
                                           e.printStackTrace();
                                       }
                                   });
                        }

                    });
                })
                .register("stats", "стата", "statistics", "статистика", "данные");
    }


    private static void sendTopMessage(UserService userService, User who, User about) {
        if (about == null) {
            userService.sendMessage(who, "&a----- Ваша статистика ----");
            about = who;
        }
        else {
            userService.sendMessage(who, format("&a----- Статистика игрока &c%s ----", about.getUsername()));
        }
        userService.sendMessage(who, format("&aУбийств: &c%s", about.getKills()));
        userService.sendMessage(who, format("&aСмертей : &c%s", about.getDeaths()));
        userService.sendMessage(who, format("&aИгр сыграно: &c%s", about.getGamesPlayed()));
        userService.sendMessage(who, format("&aПобед: &c%s", about.getWins()));
        userService.sendMessage(who, format("&aУровней набрано: &c%s", about.getLevelsReached()));
        userService.sendMessage(who, format("&aОпыта заработано: &c%s", about.getExp()));
        userService.sendMessage(who, "&a--------------------------");
    }
}
