package com.pepej.gungame;

import com.pepej.gungame.api.Arena;
import com.pepej.gungame.menu.ArenaSelectorMenu;
import com.pepej.gungame.menu.QuestSelectorMenu;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.command.Commands;
import com.pepej.papi.promise.Promise;
import com.pepej.papi.services.Services;
import com.pepej.papi.terminable.TerminableConsumer;
import com.pepej.papi.terminable.module.TerminableModule;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class CommandRegister implements TerminableModule {

    private final UserService userService;

    public CommandRegister() {
        userService = Services.load(UserService.class);
    }

    @Override
    public void setup(@NonNull final TerminableConsumer consumer) {
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
                        } else {
                            Promise.start()
                                   .thenRunAsync(() -> context.reply("Loading user.."))
                                   .thenComposeAsync($ -> userService.getOrLoadUser(context.arg(0).parse(OfflinePlayer.class).orElseThrow(IllegalStateException::new).getUniqueId()))
                                   .thenAcceptAsync(us -> {
                                       if (us.isPresent()) {
                                           sendTopMessage(userService, u, us.get());
                                       }
                                       else {
                                           context.replyError(":(");
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
