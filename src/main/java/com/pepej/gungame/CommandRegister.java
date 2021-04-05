package com.pepej.gungame;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.arena.ArenaConfig;
import com.pepej.gungame.menu.ArenaSelectorMenu;
import com.pepej.gungame.menu.QuestSelectorMenu;
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
import com.pepej.papi.serialize.Point;
import com.pepej.papi.services.Services;
import com.pepej.papi.terminable.TerminableConsumer;
import com.pepej.papi.terminable.module.TerminableModule;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class CommandRegister implements TerminableModule {

    private final UserService userService;
    private final ArenaService arenaService;

    public CommandRegister() {
        userService = Services.load(UserService.class);
        arenaService = Services.load(ArenaService.class);
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
                                                                              .arenaId(arena);
                    metadataMap.put(Metadatas.CREATED_ARENA, builder);

                    userService.getUserByPlayer(context.sender()).ifPresent(u -> userService.sendMessage(u, "&aВведите имя арены"));
                })
                .registerAndBind(consumer, "create");
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
