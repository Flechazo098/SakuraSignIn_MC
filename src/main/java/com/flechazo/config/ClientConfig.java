package com.flechazo.config;

import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.impl.ConfigBuilderImpl;
import net.minecraft.text.Text;
import lombok.Getter;
import lombok.Setter;

/**
 * 客户端配置
 * @author Flechazo
 */
public class ClientConfig {
    private static ConfigBuilder BUILDER = new ConfigBuilderImpl();
    private static ConfigEntryBuilder ENTRY_BUILDER = BUILDER.entryBuilder();
    private static ConfigCategory CATEGORY;

    /**
     * 主题设置
     */
    @Getter
    @Setter
    private static String THEME = "textures/gui/sign_in_calendar_sakura.png";
    
    /**
     * 是否使用内置主题特殊图标
     */
    @Getter
    @Setter
    private static Boolean SPECIAL_THEME = true;
    
    /**
     * 签到页面显示上月奖励
     */
    @Getter
    @Setter
    private static Boolean SHOW_LAST_REWARD = false;
    
    /**
     * 签到页面显示下月奖励
     */
    @Getter
    @Setter
    private static Boolean SHOW_NEXT_REWARD = false;
    
    /**
     * 自动领取
     */
    @Getter
    @Setter
    private static Boolean AUTO_REWARDED = false;

    /**
     * 背包界面签到按钮坐标
     */
    @Getter
    @Setter
    private static String INVENTORY_SIGN_IN_BUTTON_COORDINATE = "92,2";

    /**
     * 背包界面奖励配置按钮坐标
     */
    @Getter
    @Setter
    private static String INVENTORY_REWARD_OPTION_BUTTON_COORDINATE = "72,2";

    /**
     * 显示签到界面提示
     */
    @Getter
    @Setter
    private static Boolean SHOW_SIGN_IN_SCREEN_TIPS = true;

    static {
        BUILDER.setTitle(Text.translatable("config.sakura-sign-in.title"));
        CATEGORY = BUILDER.getOrCreateCategory(Text.translatable("config.sakura-sign-in.category.client"));

        CATEGORY.addEntry(ENTRY_BUILDER.startStrField(Text.translatable("config.sakura-sign-in.theme"), THEME)
                .setDefaultValue("textures/gui/sign_in_calendar_sakura.png")
                .setTooltip(Text.translatable("config.sakura-sign-in.theme.tooltip"))
                .setSaveConsumer(newValue -> THEME = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startBooleanToggle(Text.translatable("config.sakura-sign-in.special_theme"), SPECIAL_THEME)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.sakura-sign-in.special_theme.tooltip"))
                .setSaveConsumer(newValue -> SPECIAL_THEME = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startBooleanToggle(Text.translatable("config.sakura-sign-in.show_last_reward"), SHOW_LAST_REWARD)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("config.sakura-sign-in.show_last_reward.tooltip"))
                .setSaveConsumer(newValue -> SHOW_LAST_REWARD = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startBooleanToggle(Text.translatable("config.sakura-sign-in.show_next_reward"), SHOW_NEXT_REWARD)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("config.sakura-sign-in.show_next_reward.tooltip"))
                .setSaveConsumer(newValue -> SHOW_NEXT_REWARD = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startBooleanToggle(Text.translatable("config.sakura-sign-in.auto_rewarded"), AUTO_REWARDED)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("config.sakura-sign-in.auto_rewarded.tooltip"))
                .setSaveConsumer(newValue -> AUTO_REWARDED = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startStrField(Text.translatable("config.sakura-sign-in.inventory_sign_in_button_coordinate"), INVENTORY_SIGN_IN_BUTTON_COORDINATE)
                .setDefaultValue("92,2")
                .setTooltip(Text.translatable("config.sakura-sign-in.inventory_sign_in_button_coordinate.tooltip"))
                .setSaveConsumer(newValue -> INVENTORY_SIGN_IN_BUTTON_COORDINATE = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startStrField(Text.translatable("config.sakura-sign-in.inventory_reward_option_button_coordinate"), INVENTORY_REWARD_OPTION_BUTTON_COORDINATE)
                .setDefaultValue("72,2")
                .setTooltip(Text.translatable("config.sakura-sign-in.inventory_reward_option_button_coordinate.tooltip"))
                .setSaveConsumer(newValue -> INVENTORY_REWARD_OPTION_BUTTON_COORDINATE = newValue)
                .build());

        CATEGORY.addEntry(ENTRY_BUILDER.startBooleanToggle(Text.translatable("config.sakura-sign-in.show_sign_in_screen_tips"), SHOW_SIGN_IN_SCREEN_TIPS)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.sakura-sign-in.show_sign_in_screen_tips.tooltip"))
                .setSaveConsumer(newValue -> SHOW_SIGN_IN_SCREEN_TIPS = newValue)
                .build());
    }

    public static void init() {
        BUILDER.build();
    }
}
