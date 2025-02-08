package com.flechazo.command;

import com.flechazo.SakuraSignInFabric;
import com.flechazo.config.RewardOptionDataManager;
import com.flechazo.config.ServerConfig;
import com.flechazo.network.SignInPacket;
import com.flechazo.util.AbstractGuiUtils;
import com.flechazo.util.DateUtils;
import com.flechazo.util.StringUtils;
import com.flechazo.capability.IPlayerSignInData;
import com.flechazo.capability.PlayerSignInDataCapability;
import com.flechazo.config.KeyValue;
import com.flechazo.screen.component.IText;
import com.flechazo.enums.ESignInType;
import com.flechazo.enums.ETimeCoolingMethod;
import com.flechazo.rewards.RewardList;
import com.flechazo.rewards.RewardManager;
import com.flechazo.util.CollectionUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lombok.NonNull;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import static com.flechazo.util.I18nUtils.getByZh;
import static com.flechazo.util.I18nUtils.get;

public class SignInCommand {

    public static int HELP_INFO_NUM_PER_PAGE = 5;

    public static final List<KeyValue<String, String>> HELP_MESSAGE = new ArrayList<>() {{
        add(new KeyValue<>("/va help[ <page>]", "va_help"));                                                 // 获取帮助信息
        add(new KeyValue<>("/sign[ <year> <month> <day>]", "sign"));                                         // 签到简洁版本
        add(new KeyValue<>("/reward[ <year> <month> <day>]", "reward"));                                     // 领取今天的奖励简洁版本
        add(new KeyValue<>("/signex[ <year> <month> <day>]", "signex"));                                     // 签到并领取奖励简洁版本
        add(new KeyValue<>("/cdk <key>", "cdk"));                                                            // 领取兑换码奖励
        add(new KeyValue<>("/va sign <year> <month> <day>", "va_sign"));                                     // 签到/补签指定日期
        add(new KeyValue<>("/va reward[ <year> <month> <day>]", "va_reward"));                               // 领取指定日期奖励
        add(new KeyValue<>("/va signex[ <year> <month> <day>]", "va_signex"));                               // 签到/补签并领取指定日期奖励
        add(new KeyValue<>("/va cdk <key>", "va_cdk"));                                                      // 签到/补签并领取指定日期奖励
        add(new KeyValue<>("/va card give <num>[ <player>]", "va_card_give"));                               // 给予玩家补签卡
        add(new KeyValue<>("/va card set <num>[ <player>]", "va_card_set"));                                 // 设置玩家补签卡
        add(new KeyValue<>("/va card get <player>", "va_card_get"));                                         // 获取玩家补签卡
        add(new KeyValue<>("/va config get", "va_config_get"));                                              // 获取服务器配置项信息
        add(new KeyValue<>("/va config set date <year> <month> <day> <hour> <minute> <second>", "va_config_set_date"));    // 设置服务器时间
    }};

    /*
        1：绕过服务器原版的出生点保护系统，可以破坏出生点地形。
        2：使用原版单机一切作弊指令（除了/publish，因为其只能在单机使用，/debug也不能使用）。
        3：可以使用大多数多人游戏指令，例如/op，/ban（/debug属于3级OP使用的指令）。
        4：使用所有命令，可以使用/stop关闭服务器。
    */

    /**
     * 注册命令到命令调度器
     *
     * @param dispatcher 命令调度器，用于管理服务器中的所有命令
     */
    public static void register(CommandDispatcher< ServerCommandSource > dispatcher) {

        // 提供日期建议的 SuggestionProvider
        SuggestionProvider<ServerCommandSource> dateSuggestions = (context, builder) -> {
            LocalDateTime localDateTime = DateUtils.getLocalDateTime(DateUtils.getServerDate());
            builder.suggest(localDateTime.getYear() + " " + localDateTime.getMonthValue() + " " + localDateTime.getDayOfMonth());
            builder.suggest("~ ~ ~");
            builder.suggest("~ ~ ~-1");
            builder.suggest("all");
            return builder.buildFuture();
        };
        SuggestionProvider<ServerCommandSource> datetimeSuggestions = (context, builder) -> {
            LocalDateTime localDateTime = DateUtils.getLocalDateTime(DateUtils.getServerDate());
            builder.suggest(localDateTime.getYear() + " " + localDateTime.getMonthValue() + " " + localDateTime.getDayOfMonth()
                    + " " + localDateTime.getHour() + " " + localDateTime.getMinute() + " " + localDateTime.getSecond());
            builder.suggest("~ ~ ~ ~ ~ ~");
            builder.suggest("~ ~ ~ ~ ~ ~-1");
            return builder.buildFuture();
        };
        // 提供布尔值建议的 SuggestionProvider
        SuggestionProvider<ServerCommandSource> booleanSuggestions = (context, builder) -> {
            builder.suggest("true");
            builder.suggest("false");
            return builder.buildFuture();
        };

        Command<ServerCommandSource> signInCommand = context -> {
            List<KeyValue<Date, ESignInType>> signInTimeList = new ArrayList<>();
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            IPlayerSignInData signInData = PlayerSignInDataCapability.getData(player);
            try {
                String string = StringArgumentType.getString(context, "date");
                if (ServerConfig.getSIGN_IN_CARD() && "all".equalsIgnoreCase(string)) {
                    int days = 0;
                    for (int i = 1; i <= ServerConfig.getRE_SIGN_IN_DAYS() && days < signInData.getSignInCard(); i++) {
                        Date date = DateUtils.addDay(DateUtils.getServerDate(), -i);
                        if (signInData.getSignInRecords().stream().noneMatch(data -> DateUtils.toDateInt(data.getCompensateTime()) == DateUtils.toDateInt(date))) {
                            signInTimeList.add(new KeyValue<>(DateUtils.format(DateUtils.toString(date)), ESignInType.RE_SIGN_IN));
                            days++;
                        }
                    }
                } else {
                    long date = getRelativeLong(string, "date");
                    signInTimeList.add(new KeyValue<>(DateUtils.getDate(date), ESignInType.RE_SIGN_IN));
                }
            } catch (IllegalArgumentException ignored) {
                signInTimeList.add(new KeyValue<>(DateUtils.getServerDate(), ESignInType.SIGN_IN));
            }
            for (KeyValue<Date, ESignInType> keyValue : signInTimeList) {
                RewardManager.signIn(player, new SignInPacket(keyValue.getKey(), signInData.isAutoRewarded(), keyValue.getValue()));
            }
            return 1;
        };
        Command<ServerCommandSource> rewardCommand = context -> {
            List<Date> rewardTimeList = new ArrayList<>();
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            IPlayerSignInData signInData = PlayerSignInDataCapability.getData(player);
            try {
                String string = StringArgumentType.getString(context, "date");
                if ("all".equalsIgnoreCase(string)) {
                    signInData.getSignInRecords().stream()
                            .filter(data -> !data.isRewarded())
                            .forEach(data -> rewardTimeList.add(DateUtils.format(DateUtils.toString(data.getCompensateTime()))));
                } else {
                    long date = getRelativeLong(string, "date");
                    rewardTimeList.add(DateUtils.getDate(date));
                }
            } catch (IllegalArgumentException ignored) {
                rewardTimeList.add(DateUtils.getServerDate());
            }
            for (Date date : rewardTimeList) {
                RewardManager.signIn(player, new SignInPacket (date, true, ESignInType.REWARD));
            }
            return 1;
        };
        Command<ServerCommandSource> signAndRewardCommand = context -> {
            List<KeyValue<Date, ESignInType>> signInTimeList = new ArrayList<>();
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            IPlayerSignInData signInData = PlayerSignInDataCapability.getData(player);
            try {
                String string = StringArgumentType.getString(context, "date");
                if (ServerConfig.getSIGN_IN_CARD() && "all".equalsIgnoreCase(string)) {
                    int days = 0;
                    for (int i = 1; i <= ServerConfig.getRE_SIGN_IN_DAYS() && days < signInData.getSignInCard(); i++) {
                        Date date = DateUtils.addDay(DateUtils.getServerDate(), -i);
                        if (signInData.getSignInRecords().stream().noneMatch(data -> DateUtils.toDateInt(data.getCompensateTime()) == DateUtils.toDateInt(date))) {
                            signInTimeList.add(new KeyValue<>(date, ESignInType.RE_SIGN_IN));
                            days++;
                        }
                    }
                } else {
                    long date = getRelativeLong(string, "date");
                    signInTimeList.add(new KeyValue<>(DateUtils.getDate(date), ESignInType.RE_SIGN_IN));
                }
            } catch (IllegalArgumentException ignored) {
                signInTimeList.add(new KeyValue<>(DateUtils.getServerDate(), ESignInType.SIGN_IN));
            }
            for (KeyValue<Date, ESignInType> keyValue : signInTimeList) {
                RewardManager.signIn(player, new SignInPacket(keyValue.getKey(), true, keyValue.getValue()));
            }
            return 1;
        };
        Command<ServerCommandSource> cdkCommand = context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            IPlayerSignInData signInData = PlayerSignInDataCapability.getData(player);
            String string = StringArgumentType.getString(context, "key");
            if (signInData.getCdkErrorRecords().stream()
                    .filter(keyValue -> DateUtils.toDateInt(keyValue.getValue().getKey()) == DateUtils.toDateInt(DateUtils.getServerDate()))
                    .filter(keyValue -> !keyValue.getValue().getValue())
                    .count() >= 5) {
                player.sendMessage(AbstractGuiUtils.textToComponent(IText.i18n("今日CDK输入错误次数过多，请明日再试").setColor(0xFFFF0000)));
            } else if (signInData.getCdkErrorRecords().stream()
                    .filter(keyValue -> keyValue.getKey().equals(string))
                    .anyMatch(keyValue -> keyValue.getValue().getValue())) {
                player.sendMessage(AbstractGuiUtils.textToComponent(IText.i18n("阁下已领取过当前CDK的奖励，请勿重复领取").setColor(0xFFFFFF00)));
            } else {
                List<KeyValue<String, KeyValue<String, RewardList>>> cdkRewards = RewardOptionDataManager.getRewardOptionData().getCdkRewards();
                if (CollectionUtils.isNotNullOrEmpty(cdkRewards)) {
                    // 找到第一个匹配的项的下标
                    int indexToRemove = IntStream.range(0, cdkRewards.size())
                            .filter(i -> cdkRewards.get(i).getKey().equals(string))
                            .findFirst()
                            .orElse(-1);
                    boolean error = true;
                    if (indexToRemove < 0) {
                        player.sendMessage(AbstractGuiUtils.textToComponent(IText.i18n("输入的CDK不存在或已被领取").setColor(0xFFFF0000)));
                    } else {
                        KeyValue<String, KeyValue<String, RewardList>> rewardKeyValue = RewardOptionDataManager.getRewardOptionData().getCdkRewards().remove(indexToRemove);
                        RewardOptionDataManager.saveRewardOption();
                        Date format = DateUtils.format(rewardKeyValue.getValue().getKey());
                        if (format.before(DateUtils.getServerDate())) {
                            player.sendMessage(AbstractGuiUtils.textToComponent(IText.i18n("输入的CDK已过期").setColor(0xFFFF0000)));
                        } else {
                            MutableText msg = Text.literal(getByZh("奖励领取详情:"));
                            rewardKeyValue.getValue().getValue().forEach(reward -> {
                                MutableText detail = Text.literal(reward.getName(true));
                                if (RewardManager.giveRewardToPlayer(player, signInData, reward)) {
                                    detail.styled(style -> style.withColor(Color.GREEN.getRGB()));
                                } else {
                                    detail.styled(style -> style.withColor(Color.RED.getRGB()));
                                }
                                msg.append(", ").append(detail);
                            });
                            player.sendMessage(msg);
                            error = false;
                        }
                    }
                    signInData.getCdkErrorRecords().add(new KeyValue<>(string, new KeyValue<>(DateUtils.getServerDate(), !error)));
                }
            }
            return 1;
        };
        Command<ServerCommandSource> helpCommand = context -> {
            int page = 1;
            try {
                page = IntegerArgumentType.getInteger(context, "page");
            } catch (IllegalArgumentException ignored) {
            }
            int pages = (int) Math.ceil((double) HELP_MESSAGE.size() / HELP_INFO_NUM_PER_PAGE);
            if (page < 1 || page > pages) {
                throw new IllegalArgumentException("page must be between 1 and " + (HELP_MESSAGE.size() / HELP_INFO_NUM_PER_PAGE));
            }
            MutableText helpInfo = Text.literal("-----==== Sakura Sign In Help (" + page + "/" + pages + ") ====-----\n");
            for (int i = 0; (page - 1) * HELP_INFO_NUM_PER_PAGE + i < HELP_MESSAGE.size() && i < HELP_INFO_NUM_PER_PAGE; i++) {
                KeyValue<String, String> keyValue = HELP_MESSAGE.get((page - 1) * HELP_INFO_NUM_PER_PAGE + i);
                MutableText commandTips = Text.translatable("command." + SakuraSignInFabric.MOD_ID + "." + keyValue.getValue());
                commandTips.fillStyle(Style.EMPTY.withColor(Formatting.GRAY));
                helpInfo.append(keyValue.getKey())
                        .append(Text.literal(" -> ").fillStyle(Style.EMPTY.withColor(Formatting.YELLOW)))
                        .append(commandTips);
                if (i != HELP_MESSAGE.size() - 1) {
                    helpInfo.append("\n");
                }
            }
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            player.sendMessage(helpInfo);
            return 1;
        };

        // 签到 /sign
        dispatcher.register(CommandManager.literal("sign").executes(signInCommand)
                // 带有日期参数 -> 补签
                .then(CommandManager.argument("date", StringArgumentType.greedyString())
                        .suggests(dateSuggestions)
                        .executes(signInCommand)
                )
        );

        // 领取奖励 /reward
        dispatcher.register(CommandManager.literal("reward").executes(rewardCommand)
                // 带有日期参数 -> 补签
                .then(CommandManager.argument("date", StringArgumentType.greedyString())
                        .suggests(dateSuggestions)
                        .executes(rewardCommand)
                )
        );

        // 签到并领取奖励 /signex
        dispatcher.register(CommandManager.literal("signex").executes(signAndRewardCommand)
                // 带有日期参数 -> 补签
                .then(CommandManager.argument("date", StringArgumentType.greedyString())
                        .suggests(dateSuggestions)
                        .executes(signAndRewardCommand)
                )
        );

        // 領取CDK獎勵 /cdk
        dispatcher.register(CommandManager.literal("cdk")
                // 带有日期参数 -> 补签
                .then(CommandManager.argument("key", StringArgumentType.word())
                        .executes(cdkCommand)
                )
        );

        // 注册有前缀的指令
        dispatcher.register(CommandManager.literal("va")
                .executes(helpCommand)
                .then(CommandManager.literal("help")
                        .executes(helpCommand)
                        .then(CommandManager.argument("page", IntegerArgumentType.integer(1, (int) Math.ceil((double) HELP_MESSAGE.size() / HELP_INFO_NUM_PER_PAGE)))
                                .suggests((context, builder) -> {
                                    int totalPages = (int) Math.ceil((double) HELP_MESSAGE.size() / HELP_INFO_NUM_PER_PAGE);
                                    for (int i = 0; i < totalPages; i++) {
                                        builder.suggest(i + 1);
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(helpCommand)
                        )
                )
                // 签到 /va sign
                .then(CommandManager.literal("sign").executes(signInCommand)
                        // 补签 /va sign <year> <month> <day>
                        .then(CommandManager.argument("date", StringArgumentType.greedyString())
                                .suggests(dateSuggestions)
                                .executes(signInCommand)
                        )
                )
                // 奖励 /va reward
                .then(CommandManager.literal("reward").executes(rewardCommand)
                        // 补签 /va sign <year> <month> <day>
                        .then(CommandManager.argument("date", StringArgumentType.greedyString())
                                .suggests(dateSuggestions)
                                .executes(rewardCommand)
                        )
                )
                // 签到并领取奖励 /va signex
                .then(CommandManager.literal("signex").executes(signAndRewardCommand)
                        // 补签 /va signex <year> <month> <day>
                        .then(CommandManager.argument("date", StringArgumentType.greedyString())
                                .suggests(dateSuggestions)
                                .executes(signAndRewardCommand)
                        )
                )
                // 領取CDK獎勵 /va cdk
                .then(CommandManager.literal("cdk")
                        // 补签 /va signex <year> <month> <day>
                        .then(CommandManager.argument("key", StringArgumentType.greedyString())
                                .executes(cdkCommand)
                        )
                )
                // 获取补签卡数量 /va card
                .then(CommandManager.literal("card")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            if (!ServerConfig.getSIGN_IN_CARD()) {
                                player.sendMessage(Text.translatable(get("服务器补签功能被禁用了哦。")));
                            } else {
                                player.sendMessage(Text.translatable(get("当前拥有%d张补签卡"), PlayerSignInDataCapability.getData(player).getSignInCard()));
                            }
                            return 1;
                        })
                        // 增加/减少补签卡 /va card give <num> [<players>]
                        .then(CommandManager.literal("give")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.argument("num", IntegerArgumentType.integer())
                                        .suggests((context, builder) -> {
                                            builder.suggest(1);
                                            builder.suggest(10);
                                            builder.suggest(50);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            int num = IntegerArgumentType.getInteger(context, "num");
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            IPlayerSignInData signInData = PlayerSignInDataCapability.getData(player);
                                            signInData.setSignInCard(signInData.getSignInCard() + num);
                                            player.sendMessage(Text.translatable(get("给予%d张补签卡"), num));
                                            PlayerSignInDataCapability.syncPlayerData(player);
                                            return 1;
                                        })
                                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                                .executes(context -> {
                                                    int num = IntegerArgumentType.getInteger(context, "num");
                                                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
                                                    for (ServerPlayerEntity player : players) {
                                                        IPlayerSignInData signInData = PlayerSignInDataCapability.getData(player);
                                                        signInData.setSignInCard(signInData.getSignInCard() + num);
                                                        player.sendMessage(Text.translatable(get("获得%d张补签卡"), num));
                                                        PlayerSignInDataCapability.syncPlayerData(player);
                                                    }
                                                    return 1;
                                                })
                                        )

                                )
                        )
                        // 设置补签卡数量 /va card set <num> [<players>]
                        .then(CommandManager.literal("set")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.argument("num", IntegerArgumentType.integer())
                                        .suggests((context, builder) -> {
                                            builder.suggest(0);
                                            builder.suggest(1);
                                            builder.suggest(10);
                                            builder.suggest(50);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            int num = IntegerArgumentType.getInteger(context, "num");
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            IPlayerSignInData signInData = PlayerSignInDataCapability.getData(player);
                                            signInData.setSignInCard(num);
                                            player.sendMessage(Text.translatable(get("补签卡被设置为了%d张"), num));
                                            PlayerSignInDataCapability.syncPlayerData(player);
                                            return 1;
                                        })
                                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                                .executes(context -> {
                                                    int num = IntegerArgumentType.getInteger(context, "num");
                                                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
                                                    for (ServerPlayerEntity player : players) {
                                                        IPlayerSignInData signInData = PlayerSignInDataCapability.getData(player);
                                                        signInData.setSignInCard(num);
                                                        player.sendMessage(Text.translatable(get("补签卡被设置为了%d张"), num));
                                                        PlayerSignInDataCapability.syncPlayerData(player);
                                                    }
                                                    return 1;
                                                })
                                        )
                                )

                        )
                        // 获取补签卡数量 /va card get [<player>]
                        .then(CommandManager.literal("get")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            IPlayerSignInData signInData = PlayerSignInDataCapability.getData(target);
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            player.sendMessage(Text.translatable(get("玩家[%s]拥有%d张补签卡"), target.getDisplayName().getString(), signInData.getSignInCard()));
                                            PlayerSignInDataCapability.syncPlayerData(target);
                                            return 1;
                                        })
                                )

                        )
                )
                // 获取服务器配置 /va config get
                .then(CommandManager.literal("config")
                        .then(CommandManager.literal("get")
                                .then(CommandManager.literal("autoSignIn")
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            player.sendMessage(Text.translatable(get(String.format("服务器已%s自动签到", ServerConfig.getAUTO_SIGN_IN() ? "启用" : "禁用"))));
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("timeCoolingMethod")
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            ETimeCoolingMethod coolingMethod = ServerConfig.getTIME_COOLING_METHOD();
                                            player.sendMessage(Text.translatable(get("服务器签到时间冷却方式为: %s"), coolingMethod.name()));
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("timeCoolingTime")
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            Double time = ServerConfig.getTIME_COOLING_TIME();
                                            player.sendMessage(Text.translatable(get("服务器签到冷却刷新时间为: %05.2f"), time));
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("timeCoolingInterval")
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            Double time = ServerConfig.getTIME_COOLING_INTERVAL();
                                            player.sendMessage(Text.translatable(get("服务器签到冷却刷新间隔为: %05.2f"), time));
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("signInCard")
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            player.sendMessage(Text.translatable(get(String.format("服务器已%s补签卡", ServerConfig.getSIGN_IN_CARD() ? "启用" : "禁用"))));
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("reSignInDays")
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            int time = ServerConfig.getRE_SIGN_IN_DAYS();
                                            player.sendMessage(Text.translatable(get("服务器最大补签天数为: %d"), time));
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("signInCardOnlyBaseReward")
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            player.sendMessage(Text.translatable(get(String.format("服务器已%s补签仅获得基础奖励", ServerConfig.getSIGN_IN_CARD_ONLY_BASE_REWARD() ? "启用" : "禁用"))));
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("date")
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            player.sendMessage(Text.translatable(get("服务器当前时间: %s"), DateUtils.toDateTimeString(DateUtils.getServerDate())));
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("playerDataSyncPacketSize")
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            player.sendMessage(Text.translatable(get("玩家签到数据同步网络包大小为: %d"), ServerConfig.getPLAYER_DATA_SYNC_PACKET_SIZE()));
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("rewardAffectedByLuck")
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            player.sendMessage(Text.translatable(get(String.format("服务器已%s奖励领取受幸运影响", ServerConfig.getREWARD_AFFECTED_BY_LUCK() ? "启用" : "禁用"))));
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("continuousRewardsRepeatable")
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            player.sendMessage(Text.translatable(get(String.format("服务器已%s连续签到奖励持续领取", ServerConfig.getCONTINUOUS_REWARDS_REPEATABLE() ? "启用" : "禁用"))));
                                            return 1;
                                        })
                                )
                                .then(CommandManager.literal("cycleRewardsRepeatable")
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            player.sendMessage(Text.translatable(get(String.format("服务器已%s循环签到奖励持续领取", ServerConfig.getCYCLE_REWARDS_REPEATABLE() ? "启用" : "禁用"))));
                                            return 1;
                                        })
                                )
                        )
                        // 设置服务器时间 /va config set date <year> <month> <day> <hour> <minute> <second>
                        .then(CommandManager.literal("set")
                                .requires(source -> source.hasPermissionLevel(3))
                                .then(CommandManager.literal("date")
                                        .then(CommandManager.argument("datetime", StringArgumentType.greedyString())
                                                .suggests(datetimeSuggestions)
                                                .executes(context -> {
                                                    String string = StringArgumentType.getString(context, "datetime");
                                                    long datetime = getRelativeLong(string, "datetime");
                                                    Date date = DateUtils.getDate(datetime);
                                                    ServerConfig.setSERVER_TIME(DateUtils.toDateTimeString(new Date()));
                                                    ServerConfig.setACTUAL_TIME(DateUtils.toDateTimeString(date));
                                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                    broadcastMessage(player, Text.translatable(get("服务器时间已设置为: %s"), DateUtils.toDateTimeString(date)));
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("autoSignIn")
                                        .then(CommandManager.argument("bool", StringArgumentType.word())
                                                .suggests(booleanSuggestions)
                                                .executes(context -> {
                                                    String boolString = StringArgumentType.getString(context, "bool");
                                                    boolean bool = StringUtils.stringToBoolean(boolString);
                                                    ServerConfig.setAUTO_SIGN_IN(bool);
                                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                    broadcastMessage(player, Text.translatable(get("服务器已%s自动签到"), bool ? "启用" : "禁用"));
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("signInCard")
                                        .then(CommandManager.argument("bool", StringArgumentType.word())
                                                .suggests(booleanSuggestions)
                                                .executes(context -> {
                                                    String boolString = StringArgumentType.getString(context, "bool");
                                                    boolean bool = StringUtils.stringToBoolean(boolString);
                                                    ServerConfig.setSIGN_IN_CARD(bool);
                                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                    broadcastMessage(player, Text.translatable(get("服务器已%s补签卡"), bool ? "启用" : "禁用"));
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("reSignInDays")
                                        .then(CommandManager.argument("days", IntegerArgumentType.integer(1, 365))
                                                .suggests((context, builder) -> {
                                                    builder.suggest(1);
                                                    builder.suggest(7);
                                                    builder.suggest(30);
                                                    builder.suggest(365);
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    int days = IntegerArgumentType.getInteger(context, "days");
                                                    ServerConfig.setRE_SIGN_IN_DAYS(days);
                                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                    broadcastMessage(player, Text.translatable(get("服务器最大补签天数已被设置为: %d"), days));
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("signInCardOnlyBaseReward")
                                        .then(CommandManager.argument("bool", StringArgumentType.word())
                                                .suggests(booleanSuggestions)
                                                .executes(context -> {
                                                    String boolString = StringArgumentType.getString(context, "bool");
                                                    boolean bool = StringUtils.stringToBoolean(boolString);
                                                    ServerConfig.setSIGN_IN_CARD_ONLY_BASE_REWARD(bool);
                                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                    broadcastMessage(player, Text.translatable(get("服务器已%s补签仅获得基础奖励"), bool ? "启用" : "禁用"));
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("timeCoolingMethod")
                                        .then(CommandManager.argument("method", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    for (ETimeCoolingMethod value : ETimeCoolingMethod.values()) {
                                                        builder.suggest(value.name());
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    String method = StringArgumentType.getString(context, "method");
                                                    ServerConfig.setTIME_COOLING_METHOD(ETimeCoolingMethod.valueOf(method));
                                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                    broadcastMessage(player, Text.translatable(get("服务器签到时间冷却方式已被设置为: %s"), method));
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("timeCoolingTime")
                                        .then(CommandManager.argument("time", DoubleArgumentType.doubleArg(-23.59, 23.59))
                                                .suggests((context, builder) -> {
                                                    builder.suggest("0.00");
                                                    builder.suggest("4.00");
                                                    builder.suggest("12.00");
                                                    builder.suggest("23.59");
                                                    builder.suggest("-23.59");
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    double time = DoubleArgumentType.getDouble(context, "time");
                                                    SignInCommand.checkTime(time);
                                                    ServerConfig.setTIME_COOLING_TIME(time);
                                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                    broadcastMessage(player, Text.translatable(get("服务器签到冷却刷新时间已被设置为: %05.2f"), time));
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("timeCoolingInterval")
                                        .then(CommandManager.argument("time", DoubleArgumentType.doubleArg(0, 23.59f))
                                                .suggests((context, builder) -> {
                                                    builder.suggest("0.00");
                                                    builder.suggest("6.00");
                                                    builder.suggest("12.34");
                                                    builder.suggest("23.59");
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    double time = DoubleArgumentType.getDouble(context, "time");
                                                    SignInCommand.checkTime(time);
                                                    ServerConfig.setTIME_COOLING_INTERVAL(time);
                                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                    broadcastMessage(player, Text.translatable(get("服务器签到冷却刷新间隔已被设置为: %05.2f"), time));
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("playerDataSyncPacketSize")
                                        .then(CommandManager.argument("size", IntegerArgumentType.integer(1, 1024))
                                                .suggests((context, builder) -> {
                                                    builder.suggest(1);
                                                    builder.suggest(10);
                                                    builder.suggest(100);
                                                    builder.suggest(1024);
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    int size = IntegerArgumentType.getInteger(context, "size");
                                                    ServerConfig.setPLAYER_DATA_SYNC_PACKET_SIZE(size);
                                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                    broadcastMessage(player, Text.translatable(get("玩家签到数据同步网络包大小已被设置为: %d"), size));
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("rewardAffectedByLuck")
                                        .then(CommandManager.argument("bool", StringArgumentType.word())
                                                .suggests(booleanSuggestions)
                                                .executes(context -> {
                                                    String boolString = StringArgumentType.getString(context, "bool");
                                                    boolean bool = StringUtils.stringToBoolean(boolString);
                                                    ServerConfig.setREWARD_AFFECTED_BY_LUCK(bool);
                                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                    broadcastMessage(player, Text.translatable(get("服务器已%s奖励领取受幸运影响"), bool ? "启用" : "禁用"));
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("continuousRewardsRepeatable")
                                        .then(CommandManager.argument("bool", StringArgumentType.word())
                                                .suggests(booleanSuggestions)
                                                .executes(context -> {
                                                    String boolString = StringArgumentType.getString(context, "bool");
                                                    boolean bool = StringUtils.stringToBoolean(boolString);
                                                    ServerConfig.setCONTINUOUS_REWARDS_REPEATABLE(bool);
                                                    RewardOptionDataManager.getRewardOptionData().refreshContinuousRewardsRelation();
                                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                    broadcastMessage(player, Text.translatable(get("服务器已%s连续签到奖励持续领取"), bool ? "启用" : "禁用"));
                                                    return 1;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("cycleRewardsRepeatable")
                                        .then(CommandManager.argument("bool", StringArgumentType.word())
                                                .suggests(booleanSuggestions)
                                                .executes(context -> {
                                                    String boolString = StringArgumentType.getString(context, "bool");
                                                    boolean bool = StringUtils.stringToBoolean(boolString);
                                                    ServerConfig.setCYCLE_REWARDS_REPEATABLE(bool);
                                                    RewardOptionDataManager.getRewardOptionData().refreshCycleRewardsRelation();
                                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                    broadcastMessage(player, Text.translatable(get("服务器已%s循环签到奖励持续领取"), bool ? "启用" : "禁用"));
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
        );
    }

    // 广播消息
    private static void broadcastMessage(ServerPlayerEntity player, Text message) {
        player.server.getPlayerManager().broadcast(Text.translatable("chat.type.announcement", player.getDisplayName(), message), false);
    }

    // 校验时间是否合法
    private static void checkTime(double time) throws CommandSyntaxException {
        boolean throwException = false;
        if (time < -23.59 || time > 23.59) {
            throwException = true;
        } else {
            String format = String.format("%05.2f", time);
            String[] split = format.split("\\.");
            if (split.length != 2) {
                throwException = true;
            } else {
                int hour = StringUtils.toInt(split[0]);
                int minute = StringUtils.toInt(split[1]);
                if (hour < -23 || hour > 23) {
                    throwException = true;
                } else if (minute < 0 || minute > 59) {
                    throwException = true;
                }
            }
        }
        if (throwException) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create(time);
        }
    }

    private static long getRelativeLong(String string, @NonNull String name) throws CommandSyntaxException {
        if (StringUtils.isNullOrEmptyEx(string)) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().create(string);
        }
        String[] split = string.split(" ");
        String[] units;
        if ((name.equalsIgnoreCase("date") && split.length == 3)) {
            units = new String[]{"year", "month", "day"};
        } else if ((name.equalsIgnoreCase("time") && split.length == 3)) {
            units = new String[]{"hour", "minute", "second"};
        } else if (name.equalsIgnoreCase("datetime") && split.length == 6) {
            units = new String[]{"year", "month", "day", "hour", "minute", "second"};
        } else {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().create(string);
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            int input;
            int offset;
            String inputString = split[i];
            if (inputString.startsWith("_") || inputString.startsWith("~")) {
                offset = switch (units[i]) {
                    case "year" -> DateUtils.getLocalDateTime(DateUtils.getServerDate()).getYear();
                    case "month" -> DateUtils.getLocalDateTime(DateUtils.getServerDate()).getMonthValue();
                    case "day" -> DateUtils.getLocalDateTime(DateUtils.getServerDate()).getDayOfMonth();
                    case "hour" -> DateUtils.getLocalDateTime(DateUtils.getServerDate()).getHour();
                    case "minute" -> DateUtils.getLocalDateTime(DateUtils.getServerDate()).getMinute();
                    case "second" -> DateUtils.getLocalDateTime(DateUtils.getServerDate()).getSecond();
                    default -> 0;
                };
                if (inputString.equalsIgnoreCase("_") || inputString.equalsIgnoreCase("~")) {
                    inputString = "0";
                } else {
                    inputString = inputString.substring(1);
                }
            } else {
                offset = 0;
            }
            try {
                input = Integer.parseInt(inputString);
            } catch (NumberFormatException e) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().create(inputString);
            }
            if (units[i].equalsIgnoreCase("year")) {
                result.append(String.format("%04d", offset + input));
            } else {
                result.append(String.format("%02d", offset + input));
            }
        }
        return Long.parseLong(result.toString());
    }
}