package com.pepej.gungame.menu;

import com.pepej.gungame.GunGame;
import com.pepej.gungame.rpg.quest.Quest;
import com.pepej.gungame.rpg.quest.QuestType;
import com.pepej.gungame.service.QuestService;
import com.pepej.gungame.service.UserService;
import com.pepej.gungame.user.User;
import com.pepej.papi.item.ItemStackBuilder;
import com.pepej.papi.menu.Item;
import com.pepej.papi.menu.paginated.PaginatedMenu;
import com.pepej.papi.menu.paginated.PaginatedMenuBuilder;
import com.pepej.papi.menu.scheme.MenuPopulator;
import com.pepej.papi.menu.scheme.MenuScheme;
import com.pepej.papi.menu.scheme.StandardSchemeMappings;
import com.pepej.papi.scheduler.Schedulers;
import com.pepej.papi.services.Services;
import com.pepej.papi.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class QuestSelectorMenu extends PaginatedMenu {


    private static final MenuScheme QUEST_SCHEME = new MenuScheme()
            .maskEmpty(1)
            .mask("011111110")
            .mask("011111110");

    private static final MenuScheme ACTIVE_QUEST_SCHEME = new MenuScheme()
            .maskEmpty(2)
            .mask("011111110");

    private static final List<Integer> COMPLETED_QUEST_ITEM_SLOTS = new MenuScheme()
            .maskEmpty(4)
            .mask("011111110")
            .maskEmpty(1)
            .getMaskedIndexes();

    private static final MenuScheme COMPLETED_QUEST_SCHEME = new MenuScheme(StandardSchemeMappings.STAINED_GLASS)
            .mask("100000001")
            .mask("100000001")
            .mask("100000001")
            .mask("100000001")
            .mask("100000001")
            .mask("100000001")
            .scheme(7, 7)
            .scheme(7, 7)
            .scheme(7, 7)
            .scheme(7, 7)
            .scheme(7, 7)
            .scheme(7, 7)
            .scheme(7, 7);

    private static final int NEXT_PAGE_SLOT = new MenuScheme()
            .maskEmpty(5)
            .mask("000000010")
            .getMaskedIndexes()
            .get(0);

    private static final int PREVIOUS_PAGE_SLOT = new MenuScheme()
            .maskEmpty(5)
            .mask("010000000")
            .getMaskedIndexes()
            .get(0);


    private static final MenuScheme QUEST_INFO_SCHEME = new MenuScheme()
            .mask("000010000");

    public QuestSelectorMenu(final Player player) {
        super($ -> {
            List<Item> ret = new LinkedList<>();
            UserService userService = Services.load(UserService.class);

            User user = userService.getUserByPlayer(player).orElseThrow(IllegalStateException::new);
            user.getQuests().stream().filter(q -> !q.isActive()).sorted(Comparator.comparingLong(Quest::getCreationTime).reversed()).forEach(quest -> {
                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.ROOT);
                final ItemStackBuilder builder = ItemStackBuilder.of(Material.BOOK)
                                                                 .name("&d" + quest.getType().getDisplayName())
                                                                 .lore(format("&7Квест взят (&e%s&7)", dateFormat.format(new Date(quest.getCreationTime()))));
                if (quest.isCompleted()) {
                    builder.lore(format("&aКвест выполнен &7(&e%s&7)", dateFormat.format(new Date(quest.getCompletionTime()))));

                }
                else {
                    builder.lore("&cКвест провален");
                }
                builder.lore(format("&a%s &7(%s/%s)",
                        getQuestProgress(quest),
                        quest.getProgress(),
                        quest.getType().getCount()));
                ret.add(builder.buildItem().build());


            });
            return ret;
        }, player, PaginatedMenuBuilder.create().scheme(COMPLETED_QUEST_SCHEME).itemSlots(COMPLETED_QUEST_ITEM_SLOTS).lines(6).nextPageSlot(NEXT_PAGE_SLOT).previousPageSlot(PREVIOUS_PAGE_SLOT).title("Квесты"));
        Schedulers.builder()
                  .async()
                  .afterAndEvery(1, TimeUnit.SECONDS)
                  .run(this::redraw)
                  .bindWith(this);

    }


    @Override
    public void redraw() {
        super.redraw();
        MenuPopulator infoPop = QUEST_INFO_SCHEME.newPopulator(this);
        MenuPopulator pop = QUEST_SCHEME.newPopulator(this);
        MenuPopulator activePop = ACTIVE_QUEST_SCHEME.newPopulator(this);
        UserService userService = Services.load(UserService.class);
        QuestService questService = Services.load(QuestService.class);
        User user = userService.getUserByPlayer(getPlayer()).orElseThrow(IllegalStateException::new);
        List<Quest> activeQuests = user.getQuests()
                                       .stream()
                                       .filter(Quest::isActive)
                                       .sorted(Comparator.comparingLong(Quest::getCreationTime)
                                                         .reversed())
                                       .collect(Collectors.toList());
        for (Quest active : activeQuests) {
            final long expireTime = active.getCreationTime() + active.getExpireTime();

            activePop.accept(ItemStackBuilder.of(Material.EYE_OF_ENDER)
                                             .name(format("&a%s &7(Активен)", active.getType().getDisplayName()))
                                             .lore(format("&a%s &7(%s/%s)",
                                                     getQuestProgress(active),
                                                     active.getProgress(),
                                                     active.getType().getCount()))
                                             .lore(format("&eДо завершения: &c%s", formatTime(expireTime - System.currentTimeMillis())))
                                             .buildItem()
                                             .build()
            );

        }

        ItemStackBuilder builder = ItemStackBuilder.of(Material.PAPER)
                                                   .name("&7Информация о квестах")
                                                   .lore(format(" &7- &aВсего квестов: &d%s", user.getQuests().size()))
                                                   .lore(format(" &7- &aНевыполненны квестов: &d%s", user.getQuests().stream().filter(Quest::isExpired).count()))
                                                   .lore(format(" &7- &aАктивных квестов: &d%s", activeQuests.size()))
                                                   .lore(" &7Список активных квестов:");
        activeQuests.stream().filter(Quest::isActive).forEach(q -> builder.lore("  &6- " + q.getType().getDisplayName()));
        infoPop.accept(builder.build(this::redraw));


        for (QuestType type : QuestType.values()) {
            if (!questService.canUserTakeQuest(user, type)) {
                Quest lastCreated = questService.getLastCreationQuestTime(user, type);
                if (lastCreated != null) {
                    final long expireTime = lastCreated.getCreationTime() + GunGame.getInstance().getGlobalConfig().getQuestDelay().toMillis();

                    pop.accept(ItemStackBuilder.of(Material.COMPASS)
                                               .name(format("&c%s &7(Взят)", type.getDisplayName()))
                                               .lore(format("&7Квест будет недоступен ещё &c%s", formatTime(expireTime - System.currentTimeMillis())))
                                               .buildItem().build()

                    );
                }
            } else {
                pop.accept(ItemStackBuilder.of(Material.COMPASS)
                                           .nameClickable("&a" + type.getDisplayName())
                                           .lore(format("&7Время на выполнение: &e%s", formatTime(type.getExpiryTimeMs())))
                                           .lore(format("&7Награда за квест: &e%s", type.getReward()))
                                           .lore("&6Вы не сможете взять другой квест этого типа пока он активен!")
                                           .buildConsumer(e -> {
                                                       if (!getPlayer().hasPermission(type.getPermission())) {
                                                           userService.sendMessage(user, "&cВы не можете взять этот квест :(");
                                                           return;
                                                       }
                                                       if (activeQuests.size() >= 1 && !getPlayer().hasPermission("gungame.vip")) {
                                                           userService.sendMessage(user, "&cЧтобы взять больше одного квеста нужна привелегия &aVip");
                                                           return;
                                                       }
                                                       if (activeQuests.size() >= 2 && !getPlayer().hasPermission("gungame.premium")) {
                                                           userService.sendMessage(user, "&cЧтобы взять больше двух квестов нужна привелегия &bPremium");
                                                           return;
                                                       }
                                                       if (activeQuests.size() >= 3 && !getPlayer().hasPermission("gungame.grand")) {
                                                           userService.sendMessage(user, "&cЧтобы взять больше трех квестов нужна привелегия &6Grand");
                                                           return;
                                                       }
                                                       if (!questService.canUserTakeQuest(user, type)) {
                                                           return;
                                                       }
                                                       Quest quest = questService.getQuestByType(type, null);
                                                       user.getQuests().add(quest);
                                                       quest.setHolder(user);
                                                       userService.sendMessage(user, format("&aВы успешно взяли квест &6%s!", type.getDisplayName()));
                                                       redraw();
                                                   }
                                           ));
            }
        }

    }


    private static String getQuestProgress(Quest quest) {
        return StringUtils.getProgressBar(quest.getProgress(), quest.getType().getCount(), 10, '|', ChatColor.GREEN, ChatColor.GRAY);
    }

    private static String formatTime(long millis) {
        long secs = millis / 1000;
        return String.format("%02d:%02d:%02d", secs / 3600, (secs % 3600) / 60, secs % 60);
    }
}
