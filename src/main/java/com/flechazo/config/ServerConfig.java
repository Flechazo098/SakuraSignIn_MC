package com.flechazo.config;

import com.flechazo.enums.ETimeCoolingMethod;
import com.flechazo.util.DateUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.Date;

/**
 * 服务器配置
 * @author Flechazo
 */
@Config(name = "sakura_sign_in-server")
public class ServerConfig implements ConfigData {
    private static ServerConfig INSTANCE;

    /**
     * 自动签到
     */
    @ConfigEntry.Gui.Tooltip
    private Boolean AUTO_SIGN_IN = true;

    /**
     * 签到时间冷却方式
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    private ETimeCoolingMethod TIME_COOLING_METHOD = ETimeCoolingMethod.FIXED_TIME;

    /**
     * 签到冷却刷新时间
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -2359, max = 2359)
    private Double TIME_COOLING_TIME = 0.00;

    /**
     * 签到冷却刷新间隔
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 2359)
    private Double TIME_COOLING_INTERVAL = 12.34;

    /**
     * 是否启用补签卡
     */
    @ConfigEntry.Gui.Tooltip
    private Boolean SIGN_IN_CARD = true;

    /**
     * 最大补签天数
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 365)
    private Integer RE_SIGN_IN_DAYS = 30;

    /**
     * 补签仅基础奖励
     */
    @ConfigEntry.Gui.Tooltip
    private Boolean SIGN_IN_CARD_ONLY_BASE_REWARD = true;

    /**
     * 服务器时间
     */
    @ConfigEntry.Gui.Tooltip
    private String SERVER_TIME = DateUtils.toDateTimeString(new Date());

    /**
     * 实际时间
     */
    @ConfigEntry.Gui.Tooltip
    private String ACTUAL_TIME = DateUtils.toDateTimeString(new Date());

    /**
     * 玩家签到数据同步网络包大小
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 1024)
    private Integer PLAYER_DATA_SYNC_PACKET_SIZE = 100;

    /**
     * 奖励领取是否受玩家幸运/霉运影响
     */
    @ConfigEntry.Gui.Tooltip
    private Boolean REWARD_AFFECTED_BY_LUCK = true;

    /**
     * 连续签到奖励 天数达标后是否允许一直领取该标准奖励
     */
    @ConfigEntry.Gui.Tooltip
    private Boolean CONTINUOUS_REWARDS_REPEATABLE = false;

    /**
     * 签到周期奖励 天数达标后是否允许一直领取该标准奖励
     */
    @ConfigEntry.Gui.Tooltip
    private Boolean CYCLE_REWARDS_REPEATABLE = false;

    public static void init() {
        INSTANCE = AutoConfig.register(ServerConfig.class, me.shedaniel.autoconfig.serializer.JanksonConfigSerializer::new).getConfig();
    }

    public static Boolean getAUTO_SIGN_IN() {
        return INSTANCE.AUTO_SIGN_IN;
    }

    public static void setAUTO_SIGN_IN(Boolean autoSignIn) {
        INSTANCE.AUTO_SIGN_IN = autoSignIn;
    }

    public static ETimeCoolingMethod getTIME_COOLING_METHOD() {
        return INSTANCE.TIME_COOLING_METHOD;
    }

    public static void setTIME_COOLING_METHOD(ETimeCoolingMethod method) {
        INSTANCE.TIME_COOLING_METHOD = method;
    }

    public static Double getTIME_COOLING_TIME() {
        return INSTANCE.TIME_COOLING_TIME;
    }

    public static void setTIME_COOLING_TIME(Double time) {
        INSTANCE.TIME_COOLING_TIME = time;
    }

    public static Double getTIME_COOLING_INTERVAL() {
        return INSTANCE.TIME_COOLING_INTERVAL;
    }

    public static void setTIME_COOLING_INTERVAL(Double interval) {
        INSTANCE.TIME_COOLING_INTERVAL = interval;
    }

    public static Boolean getSIGN_IN_CARD() {
        return INSTANCE.SIGN_IN_CARD;
    }

    public static void setSIGN_IN_CARD(Boolean signInCard) {
        INSTANCE.SIGN_IN_CARD = signInCard;
    }

    public static Integer getRE_SIGN_IN_DAYS() {
        return INSTANCE.RE_SIGN_IN_DAYS;
    }

    public static void setRE_SIGN_IN_DAYS(Integer days) {
        INSTANCE.RE_SIGN_IN_DAYS = days;
    }

    public static Boolean getSIGN_IN_CARD_ONLY_BASE_REWARD() {
        return INSTANCE.SIGN_IN_CARD_ONLY_BASE_REWARD;
    }

    public static void setSIGN_IN_CARD_ONLY_BASE_REWARD(Boolean onlyBaseReward) {
        INSTANCE.SIGN_IN_CARD_ONLY_BASE_REWARD = onlyBaseReward;
    }

    public static String getSERVER_TIME() {
        return INSTANCE.SERVER_TIME;
    }

    public static void setSERVER_TIME(String time) {
        INSTANCE.SERVER_TIME = time;
    }

    public static String getACTUAL_TIME() {
        return INSTANCE.ACTUAL_TIME;
    }

    public static void setACTUAL_TIME(String time) {
        INSTANCE.ACTUAL_TIME = time;
    }

    public static Integer getPLAYER_DATA_SYNC_PACKET_SIZE() {
        return INSTANCE.PLAYER_DATA_SYNC_PACKET_SIZE;
    }

    public static void setPLAYER_DATA_SYNC_PACKET_SIZE(Integer size) {
        INSTANCE.PLAYER_DATA_SYNC_PACKET_SIZE = size;
    }

    public static Boolean getREWARD_AFFECTED_BY_LUCK() {
        return INSTANCE.REWARD_AFFECTED_BY_LUCK;
    }

    public static void setREWARD_AFFECTED_BY_LUCK(Boolean affected) {
        INSTANCE.REWARD_AFFECTED_BY_LUCK = affected;
    }

    public static Boolean getCONTINUOUS_REWARDS_REPEATABLE() {
        return INSTANCE.CONTINUOUS_REWARDS_REPEATABLE;
    }

    public static void setCONTINUOUS_REWARDS_REPEATABLE(Boolean repeatable) {
        INSTANCE.CONTINUOUS_REWARDS_REPEATABLE = repeatable;
    }

    public static Boolean getCYCLE_REWARDS_REPEATABLE() {
        return INSTANCE.CYCLE_REWARDS_REPEATABLE;
    }

    public static void setCYCLE_REWARDS_REPEATABLE(Boolean repeatable) {
        INSTANCE.CYCLE_REWARDS_REPEATABLE = repeatable;
    }
}