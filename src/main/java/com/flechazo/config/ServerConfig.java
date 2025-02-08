package com.flechazo.config;

import com.flechazo.enums.ETimeCoolingMethod;
import com.flechazo.util.DateUtils;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.ConfigBuilderImpl;
import net.minecraft.text.Text;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 服务器配置
 * @author Flechazo
 */
public class ServerConfig {
    private static ConfigBuilder BUILDER = new ConfigBuilderImpl();
    private static ConfigEntryBuilder ENTRY_BUILDER = BUILDER.entryBuilder();
    private static ConfigCategory CATEGORY;

    /**
     * 自动签到
     */
    @Getter
    @Setter
    private static Boolean AUTO_SIGN_IN = true;

    /**
     * 签到时间冷却方式
     */
    @Getter
    @Setter
    private static ETimeCoolingMethod TIME_COOLING_METHOD = ETimeCoolingMethod.FIXED_TIME;

    /**
     * 签到冷却刷新时间
     */
    @Getter
    @Setter
    private static Double TIME_COOLING_TIME = 0.00;

    /**
     * 签到冷却刷新间隔
     */
    @Getter
    @Setter
    private static Double TIME_COOLING_INTERVAL = 12.34;

    /**
     * 是否启用补签卡
     */
    @Getter
    @Setter
    private static Boolean SIGN_IN_CARD = true;

    /**
     * 最大补签天数
     */
    @Getter
    @Setter
    private static Integer RE_SIGN_IN_DAYS = 30;

    /**
     * 补签仅基础奖励
     */
    @Getter
    @Setter
    private static Boolean SIGN_IN_CARD_ONLY_BASE_REWARD = true;

    /**
     * 服务器时间
     */
    @Getter
    @Setter
    private static String SERVER_TIME = DateUtils.toDateTimeString(new Date());

    /**
     * 实际时间
     */
    @Getter
    @Setter
    private static String ACTUAL_TIME = DateUtils.toDateTimeString(new Date());

    /**
     * 玩家签到数据同步网络包大小
     */
    @Getter
    @Setter
    private static Integer PLAYER_DATA_SYNC_PACKET_SIZE = 100;

    /**
     * 奖励领取是否受玩家幸运/霉运影响
     */
    @Getter
    @Setter
    private static Boolean REWARD_AFFECTED_BY_LUCK = true;

    /**
     * 连续签到奖励 天数达标后是否允许一直领取该标准奖励
     */
    @Getter
    @Setter
    private static Boolean CONTINUOUS_REWARDS_REPEATABLE = false;

    /**
     * 签到周期奖励 天数达标后是否允许一直领取该标准奖励
     */
    @Getter
    @Setter
    private static Boolean CYCLE_REWARDS_REPEATABLE = false;

    static {
        BUILDER.setTitle(Text.translatable("config.sakura-sign-in.title"));
        CATEGORY = BUILDER.getOrCreateCategory(Text.translatable("config.sakura-sign-in.category.server"));

        CATEGORY.addEntry(ENTRY_BUILDER.startBooleanToggle(Text.translatable("config.sakura-sign-in.auto_sign_in"), AUTO_SIGN_IN)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.sakura-sign-in.auto_sign_in.tooltip"))
                .setSaveConsumer(newValue -> AUTO_SIGN_IN = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startEnumSelector(Text.translatable("config.sakura-sign-in.time_cooling_method"), ETimeCoolingMethod.class, TIME_COOLING_METHOD)
                .setDefaultValue(ETimeCoolingMethod.FIXED_TIME)
                .setTooltip(Text.translatable("config.sakura-sign-in.time_cooling_method.tooltip"))
                .setSaveConsumer(newValue -> TIME_COOLING_METHOD = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startDoubleField(Text.translatable("config.sakura-sign-in.time_cooling_time"), TIME_COOLING_TIME)
                .setDefaultValue(0.00)
                .setMin(-23.59)
                .setMax(23.59)
                .setTooltip(Text.translatable("config.sakura-sign-in.time_cooling_time.tooltip"))
                .setSaveConsumer(newValue -> TIME_COOLING_TIME = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startDoubleField(Text.translatable("config.sakura-sign-in.time_cooling_interval"), TIME_COOLING_INTERVAL)
                .setDefaultValue(12.34)
                .setMin(0.00)
                .setMax(23.59)
                .setTooltip(Text.translatable("config.sakura-sign-in.time_cooling_interval.tooltip"))
                .setSaveConsumer(newValue -> TIME_COOLING_INTERVAL = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startBooleanToggle(Text.translatable("config.sakura-sign-in.sign_in_card"), SIGN_IN_CARD)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.sakura-sign-in.sign_in_card.tooltip"))
                .setSaveConsumer(newValue -> SIGN_IN_CARD = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startIntField(Text.translatable("config.sakura-sign-in.re_sign_in_days"), RE_SIGN_IN_DAYS)
                .setDefaultValue(30)
                .setMin(1)
                .setMax(365)
                .setTooltip(Text.translatable("config.sakura-sign-in.re_sign_in_days.tooltip"))
                .setSaveConsumer(newValue -> RE_SIGN_IN_DAYS = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startBooleanToggle(Text.translatable("config.sakura-sign-in.sign_in_card_only_base_reward"), SIGN_IN_CARD_ONLY_BASE_REWARD)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.sakura-sign-in.sign_in_card_only_base_reward.tooltip"))
                .setSaveConsumer(newValue -> SIGN_IN_CARD_ONLY_BASE_REWARD = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startStrField(Text.translatable("config.sakura-sign-in.server_time"), SERVER_TIME)
                .setDefaultValue(DateUtils.toDateTimeString(new Date()))
                .setTooltip(Text.translatable("config.sakura-sign-in.server_time.tooltip"))
                .setSaveConsumer(newValue -> SERVER_TIME = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startStrField(Text.translatable("config.sakura-sign-in.actual_time"), ACTUAL_TIME)
                .setDefaultValue(DateUtils.toDateTimeString(new Date()))
                .setTooltip(Text.translatable("config.sakura-sign-in.actual_time.tooltip"))
                .setSaveConsumer(newValue -> ACTUAL_TIME = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startIntField(Text.translatable("config.sakura-sign-in.player_data_sync_packet_size"), PLAYER_DATA_SYNC_PACKET_SIZE)
                .setDefaultValue(100)
                .setMin(1)
                .setMax(1024)
                .setTooltip(Text.translatable("config.sakura-sign-in.player_data_sync_packet_size.tooltip"))
                .setSaveConsumer(newValue -> PLAYER_DATA_SYNC_PACKET_SIZE = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startBooleanToggle(Text.translatable("config.sakura-sign-in.reward_affected_by_luck"), REWARD_AFFECTED_BY_LUCK)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.sakura-sign-in.reward_affected_by_luck.tooltip"))
                .setSaveConsumer(newValue -> REWARD_AFFECTED_BY_LUCK = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startBooleanToggle(Text.translatable("config.sakura-sign-in.continuous_rewards_repeatable"), CONTINUOUS_REWARDS_REPEATABLE)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("config.sakura-sign-in.continuous_rewards_repeatable.tooltip"))
                .setSaveConsumer(newValue -> CONTINUOUS_REWARDS_REPEATABLE = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startBooleanToggle(Text.translatable("config.sakura-sign-in.cycle_rewards_repeatable"), CYCLE_REWARDS_REPEATABLE)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("config.sakura-sign-in.cycle_rewards_repeatable.tooltip"))
                .setSaveConsumer(newValue -> CYCLE_REWARDS_REPEATABLE = newValue)
                .build());
    }

    public static void init() {
        BUILDER.build();
    }
}