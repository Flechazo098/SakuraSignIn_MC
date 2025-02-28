package com.flechazo.sakura.config;

import com.flechazo.sakura.enums.ETimeCoolingMethod;
import com.flechazo.sakura.util.DateUtils;
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

    /**
     * 编辑奖励配置所需的权限
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
    private Integer PERMISSION_EDIT_REWARD = 3;

    /**
     * 查看基础奖励配置所需的权限
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
    private Integer PERMISSION_BASE_REWARD = 0;

    /**
     * 查看连续奖励配置所需的权限
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
    private Integer PERMISSION_CONTINUOUS_REWARD = 0;

    /**
     * 查看循环奖励配置所需的权限
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
    private Integer PERMISSION_CYCLE_REWARD = 0;

    /**
     * 查看年度奖励配置所需的权限
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
    private Integer PERMISSION_YEAR_REWARD = 0;

    /**
     * 查看月度奖励配置所需的权限
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
    private Integer PERMISSION_MONTH_REWARD = 0;

    /**
     * 查看周度奖励配置所需的权限
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
    private Integer PERMISSION_WEEK_REWARD = 0;

    /**
     * 查看具体时间奖励配置所需的权限
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
    private Integer PERMISSION_DATE_TIME_REWARD = 0;

    /**
     * 查看累计奖励配置所需的权限
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
    private Integer PERMISSION_CUMULATIVE_REWARD = 0;

    /**
     * 查看随机奖励池配置所需的权限
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
    private Integer PERMISSION_RANDOM_REWARD = 0;

    /**
     * 查看兑换码奖励池配置所需的权限
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
    private Integer PERMISSION_CDK_REWARD = 3;

    /**
     * 客户端显示奖励概率所需的权限
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
    private Integer PERMISSION_REWARD_PROBABILITY = 0;

    /**
     * 客户端显示奖励详情所需的权限
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
    private Integer PERMISSION_REWARD_DETAIL = 0;

    /**
     * 显示领取失败的奖励提示所需的权限
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
    private Integer PERMISSION_REWARD_FAILED_TIPS = 0;

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

    public static Integer getPERMISSION_EDIT_REWARD() {
        return INSTANCE.PERMISSION_EDIT_REWARD;
    }

    public static void setPERMISSION_EDIT_REWARD(Integer permission) {
        INSTANCE.PERMISSION_EDIT_REWARD = permission;
    }

    public static Integer getPERMISSION_BASE_REWARD() {
        return INSTANCE.PERMISSION_BASE_REWARD;
    }

    public static void setPERMISSION_BASE_REWARD(Integer permission) {
        INSTANCE.PERMISSION_BASE_REWARD = permission;
    }

    public static Integer getPERMISSION_CONTINUOUS_REWARD() {
        return INSTANCE.PERMISSION_CONTINUOUS_REWARD;
    }

    public static void setPERMISSION_CONTINUOUS_REWARD(Integer permission) {
        INSTANCE.PERMISSION_CONTINUOUS_REWARD = permission;
    }

    public static Integer getPERMISSION_CYCLE_REWARD() {
        return INSTANCE.PERMISSION_CYCLE_REWARD;
    }

    public static void setPERMISSION_CYCLE_REWARD(Integer permission) {
        INSTANCE.PERMISSION_CYCLE_REWARD = permission;
    }

    public static Integer getPERMISSION_YEAR_REWARD() {
        return INSTANCE.PERMISSION_YEAR_REWARD;
    }

    public static void setPERMISSION_YEAR_REWARD(Integer permission) {
        INSTANCE.PERMISSION_YEAR_REWARD = permission;
    }

    public static Integer getPERMISSION_MONTH_REWARD() {
        return INSTANCE.PERMISSION_MONTH_REWARD;
    }

    public static void setPERMISSION_MONTH_REWARD(Integer permission) {
        INSTANCE.PERMISSION_MONTH_REWARD = permission;
    }

    public static Integer getPERMISSION_WEEK_REWARD() {
        return INSTANCE.PERMISSION_WEEK_REWARD;
    }

    public static void setPERMISSION_WEEK_REWARD(Integer permission) {
        INSTANCE.PERMISSION_WEEK_REWARD = permission;
    }

    public static Integer getPERMISSION_DATE_TIME_REWARD() {
        return INSTANCE.PERMISSION_DATE_TIME_REWARD;
    }

    public static void setPERMISSION_DATE_TIME_REWARD(Integer permission) {
        INSTANCE.PERMISSION_DATE_TIME_REWARD = permission;
    }

    public static Integer getPERMISSION_CUMULATIVE_REWARD() {
        return INSTANCE.PERMISSION_CUMULATIVE_REWARD;
    }

    public static void setPERMISSION_CUMULATIVE_REWARD(Integer permission) {
        INSTANCE.PERMISSION_CUMULATIVE_REWARD = permission;
    }

    public static Integer getPERMISSION_RANDOM_REWARD() {
        return INSTANCE.PERMISSION_RANDOM_REWARD;
    }

    public static void setPERMISSION_RANDOM_REWARD(Integer permission) {
        INSTANCE.PERMISSION_RANDOM_REWARD = permission;
    }

    public static Integer getPERMISSION_CDK_REWARD() {
        return INSTANCE.PERMISSION_CDK_REWARD;
    }

    public static void setPERMISSION_CDK_REWARD(Integer permission) {
        INSTANCE.PERMISSION_CDK_REWARD = permission;
    }

    public static Integer getPERMISSION_REWARD_PROBABILITY() {
        return INSTANCE.PERMISSION_REWARD_PROBABILITY;
    }

    public static void setPERMISSION_REWARD_PROBABILITY(Integer permission) {
        INSTANCE.PERMISSION_REWARD_PROBABILITY = permission;
    }

    public static Integer getPERMISSION_REWARD_DETAIL() {
        return INSTANCE.PERMISSION_REWARD_DETAIL;
    }

    public static void setPERMISSION_REWARD_DETAIL(Integer permission) {
        INSTANCE.PERMISSION_REWARD_DETAIL = permission;
    }

    public static Integer getPERMISSION_REWARD_FAILED_TIPS() {
        return INSTANCE.PERMISSION_REWARD_FAILED_TIPS;
    }

    public static void setPERMISSION_REWARD_FAILED_TIPS(Integer permission) {
        INSTANCE.PERMISSION_REWARD_FAILED_TIPS = permission;
    }
}