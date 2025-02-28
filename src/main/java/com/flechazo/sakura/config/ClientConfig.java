package com.flechazo.sakura.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * 客户端配置
 * @author Flechazo
 */
@Config(name = "sakura_sign_in-client")
public class ClientConfig implements ConfigData {
    private static ClientConfig INSTANCE;

    /**
     * 主题设置
     */
    @ConfigEntry.Gui.Tooltip
    private String THEME = "textures/gui/sign_in_calendar_original.png";

    /**
     * 是否使用内置主题特殊图标
     */
    @ConfigEntry.Gui.Tooltip
    private Boolean SPECIAL_THEME = true;

    /**
     * 签到页面显示上月奖励
     */
    @ConfigEntry.Gui.Tooltip
    private Boolean SHOW_LAST_REWARD = false;

    /**
     * 签到页面显示下月奖励
     */
    @ConfigEntry.Gui.Tooltip
    private Boolean SHOW_NEXT_REWARD = false;

    /**
     * 自动领取
     */
    @ConfigEntry.Gui.Tooltip
    private Boolean AUTO_REWARDED = false;

    /**
     * 背包界面签到按钮坐标
     */
    @ConfigEntry.Gui.Tooltip
    private String INVENTORY_SIGN_IN_BUTTON_COORDINATE = "92,2";

    /**
     * 背包界面奖励配置按钮坐标
     */
    @ConfigEntry.Gui.Tooltip
    private String INVENTORY_REWARD_OPTION_BUTTON_COORDINATE = "72,2";

    /**
     * 显示签到界面提示
     */
    @ConfigEntry.Gui.Tooltip
    private Boolean SHOW_SIGN_IN_SCREEN_TIPS = true;

    public static void init() {
        INSTANCE = AutoConfig.register(ClientConfig.class, me.shedaniel.autoconfig.serializer.JanksonConfigSerializer::new).getConfig();
    }

    public static String getTHEME() {
        return INSTANCE.THEME;
    }

    public static void setTHEME(String theme) {
        INSTANCE.THEME = theme;
    }

    public static Boolean getSPECIAL_THEME() {
        return INSTANCE.SPECIAL_THEME;
    }

    public static void setSPECIAL_THEME(Boolean specialTheme) {
        INSTANCE.SPECIAL_THEME = specialTheme;
    }

    public static Boolean getSHOW_LAST_REWARD() {
        return INSTANCE.SHOW_LAST_REWARD;
    }

    public static void setSHOW_LAST_REWARD(Boolean showLastReward) {
        INSTANCE.SHOW_LAST_REWARD = showLastReward;
    }

    public static Boolean getSHOW_NEXT_REWARD() {
        return INSTANCE.SHOW_NEXT_REWARD;
    }

    public static void setSHOW_NEXT_REWARD(Boolean showNextReward) {
        INSTANCE.SHOW_NEXT_REWARD = showNextReward;
    }

    public static Boolean getAUTO_REWARDED() {
        return INSTANCE.AUTO_REWARDED;
    }

    public static void setAUTO_REWARDED(Boolean autoRewarded) {
        INSTANCE.AUTO_REWARDED = autoRewarded;
    }

    public static String getINVENTORY_SIGN_IN_BUTTON_COORDINATE() {
        return INSTANCE.INVENTORY_SIGN_IN_BUTTON_COORDINATE;
    }

    public static void setINVENTORY_SIGN_IN_BUTTON_COORDINATE(String coordinate) {
        INSTANCE.INVENTORY_SIGN_IN_BUTTON_COORDINATE = coordinate;
    }

    public static String getINVENTORY_REWARD_OPTION_BUTTON_COORDINATE() {
        return INSTANCE.INVENTORY_REWARD_OPTION_BUTTON_COORDINATE;
    }

    public static void setINVENTORY_REWARD_OPTION_BUTTON_COORDINATE(String coordinate) {
        INSTANCE.INVENTORY_REWARD_OPTION_BUTTON_COORDINATE = coordinate;
    }

    public static Boolean getSHOW_SIGN_IN_SCREEN_TIPS() {
        return INSTANCE.SHOW_SIGN_IN_SCREEN_TIPS;
    }

    public static void setSHOW_SIGN_IN_SCREEN_TIPS(Boolean showTips) {
        INSTANCE.SHOW_SIGN_IN_SCREEN_TIPS = showTips;
    }
}
