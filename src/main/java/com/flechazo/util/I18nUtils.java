package com.flechazo.util;

import net.minecraft.client.resource.language.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 国际化工具类，提供中英键映射和翻译功能
 */
public class I18nUtils {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, String> ZH_CN_KEY_MAP = new HashMap<>();
    private static final String FALLBACK_PREFIX = "§c§l[Missing I18n]§r ";

    static {
        // 初始化中英键映射
        initZhCnMapping();
    }

    private static void initZhCnMapping() {
        // 使用链式put保持可读性
        ZH_CN_KEY_MAP.put("奖励规则类型", "title.sakura_sign_in.reward_rule_type");
        ZH_CN_KEY_MAP.put("基础奖励", "title.sakura_sign_in.base_reward");
        ZH_CN_KEY_MAP.put("第%s天", "title.sakura_sign_in.day_s");
        ZH_CN_KEY_MAP.put("年度第%s天", "title.sakura_sign_in.year_day_s");
        ZH_CN_KEY_MAP.put("月度第%s天", "title.sakura_sign_in.month_day_s");
        ZH_CN_KEY_MAP.put("%s, 有效期至: %s", "title.sakura_sign_in.s_valid_until_s");
        ZH_CN_KEY_MAP.put("周1", "title.sakura_sign_in.week_1");
        ZH_CN_KEY_MAP.put("周2", "title.sakura_sign_in.week_2");
        ZH_CN_KEY_MAP.put("周3", "title.sakura_sign_in.week_3");
        ZH_CN_KEY_MAP.put("周4", "title.sakura_sign_in.week_4");
        ZH_CN_KEY_MAP.put("周5", "title.sakura_sign_in.week_5");
        ZH_CN_KEY_MAP.put("周6", "title.sakura_sign_in.week_6");
        ZH_CN_KEY_MAP.put("周7", "title.sakura_sign_in.week_7");
        ZH_CN_KEY_MAP.put("签到基础奖励", "button.sakura_sign_in.reward_base");
        ZH_CN_KEY_MAP.put("连续签到奖励", "button.sakura_sign_in.reward_continuous");
        ZH_CN_KEY_MAP.put("签到周期奖励", "button.sakura_sign_in.reward_cycle");
        ZH_CN_KEY_MAP.put("年度签到奖励", "button.sakura_sign_in.reward_year");
        ZH_CN_KEY_MAP.put("月度签到奖励", "button.sakura_sign_in.reward_month");
        ZH_CN_KEY_MAP.put("周度签到奖励", "button.sakura_sign_in.reward_week");
        ZH_CN_KEY_MAP.put("具体时间奖励", "button.sakura_sign_in.reward_time");
        ZH_CN_KEY_MAP.put("累计签到奖励", "button.sakura_sign_in.reward_cumulative");
        ZH_CN_KEY_MAP.put("随机奖励池", "button.sakura_sign_in.reward_random");
        ZH_CN_KEY_MAP.put("兑换码奖励池", "button.sakura_sign_in.reward_cdk");
        ZH_CN_KEY_MAP.put("编辑", "option.sakura_sign_in.edit");
        ZH_CN_KEY_MAP.put("复制", "option.sakura_sign_in.copy");
        ZH_CN_KEY_MAP.put("剪切", "option.sakura_sign_in.cut");
        ZH_CN_KEY_MAP.put("粘贴", "option.sakura_sign_in.paste");
        ZH_CN_KEY_MAP.put("删除", "option.sakura_sign_in.delete");
        ZH_CN_KEY_MAP.put("清空", "option.sakura_sign_in.clear");
        ZH_CN_KEY_MAP.put("取消", "option.sakura_sign_in.cancel");
        ZH_CN_KEY_MAP.put("提交", "option.sakura_sign_in.submit");
        ZH_CN_KEY_MAP.put("确认", "option.sakura_sign_in.confirm");
        ZH_CN_KEY_MAP.put("不再提醒", "option.sakura_sign_in.no_remind");
        ZH_CN_KEY_MAP.put("请输入", "tips.sakura_sign_in.enter_something");
        ZH_CN_KEY_MAP.put("请输入规则名称", "tips.sakura_sign_in.enter_reward_rule_key");
        ZH_CN_KEY_MAP.put("请输入物品Json", "tips.sakura_sign_in.enter_item_json");
        ZH_CN_KEY_MAP.put("请输入物品数量", "tips.sakura_sign_in.enter_item_count");
        ZH_CN_KEY_MAP.put("请输入物品NBT", "tips.sakura_sign_in.enter_item_nbt");
        ZH_CN_KEY_MAP.put("请输入效果Json", "tips.sakura_sign_in.enter_effect_json");
        ZH_CN_KEY_MAP.put("请输入持续时间", "tips.sakura_sign_in.enter_effect_duration");
        ZH_CN_KEY_MAP.put("请输入效果等级", "tips.sakura_sign_in.enter_effect_amplifier");
        ZH_CN_KEY_MAP.put("请输入经验点值", "tips.sakura_sign_in.enter_exp_point");
        ZH_CN_KEY_MAP.put("请输入经验等级", "tips.sakura_sign_in.enter_exp_level");
        ZH_CN_KEY_MAP.put("请输入补签卡数量", "tips.sakura_sign_in.enter_sign_in_card");
        ZH_CN_KEY_MAP.put("请输入进度Json", "tips.sakura_sign_in.enter_advancement_json");
        ZH_CN_KEY_MAP.put("请输入消息", "tips.sakura_sign_in.enter_message");
        ZH_CN_KEY_MAP.put("请输入指令", "tips.sakura_sign_in.enter_command");
        ZH_CN_KEY_MAP.put("请输入奖励概率", "tips.sakura_sign_in.enter_reward_probability");
        ZH_CN_KEY_MAP.put("请输入有效期", "tips.sakura_sign_in.enter_valid_until");
        ZH_CN_KEY_MAP.put("规则名称[%s]输入有误", "tips.sakura_sign_in.reward_rule_s_error");
        ZH_CN_KEY_MAP.put("物品Json[%s]输入有误", "tips.sakura_sign_in.item_json_s_error");
        ZH_CN_KEY_MAP.put("物品数量[%s]输入有误", "tips.sakura_sign_in.item_count_s_error");
        ZH_CN_KEY_MAP.put("物品NBT[%s]输入有误", "tips.sakura_sign_in.item_nbt_s_error");
        ZH_CN_KEY_MAP.put("效果Json[%s]输入有误", "tips.sakura_sign_in.effect_json_s_error");
        ZH_CN_KEY_MAP.put("持续时间[%s]输入有误", "tips.sakura_sign_in.effect_duration_s_error");
        ZH_CN_KEY_MAP.put("效果等级[%s]输入有误", "tips.sakura_sign_in.effect_amplifier_s_error");
        ZH_CN_KEY_MAP.put("进度Json[%s]输入有误", "tips.sakura_sign_in.advancement_json_s_error");
        ZH_CN_KEY_MAP.put("奖励概率[%s]输入有误", "tips.sakura_sign_in.reward_probability_s_error");
        ZH_CN_KEY_MAP.put("有效期[%s]输入有误", "tips.sakura_sign_in.valid_until_s_error");
        ZH_CN_KEY_MAP.put("输入值[%s]有误", "tips.sakura_sign_in.enter_value_s_error");
        ZH_CN_KEY_MAP.put("展开侧边栏", "tips.sakura_sign_in.open_sidebar");
        ZH_CN_KEY_MAP.put("收起侧边栏", "tips.sakura_sign_in.close_sidebar");
        ZH_CN_KEY_MAP.put("Y轴偏移:\n%.1f\n点击重置", "tips.sakura_sign_in.y_offset");
        ZH_CN_KEY_MAP.put("Ctrl + 鼠标右键确认", "tips.sakura_sign_in.cancel_or_confirm");
        ZH_CN_KEY_MAP.put("列出模式\n物品栏 (%s)", "tips.sakura_sign_in.item_select_list_inventory_mode");
        ZH_CN_KEY_MAP.put("列出模式\n所有物品 (%s)", "tips.sakura_sign_in.item_select_list_all_mode");
        ZH_CN_KEY_MAP.put("列出模式\n所有效果 (%s)", "tips.sakura_sign_in.effect_select_list_all_mode");
        ZH_CN_KEY_MAP.put("列出模式\n玩家拥有 (%s)", "tips.sakura_sign_in.effect_select_list_player_mode");
        ZH_CN_KEY_MAP.put("列出模式\n所有进度 (%s)", "tips.sakura_sign_in.advancement_select_list_all_mode");
        ZH_CN_KEY_MAP.put("列出模式\n有图标的 (%s)", "tips.sakura_sign_in.advancement_select_list_icon_mode");
        ZH_CN_KEY_MAP.put("设置数量\n当前 %s", "tips.sakura_sign_in.set_count_s");
        ZH_CN_KEY_MAP.put("设置持续时间\n当前 %s", "tips.sakura_sign_in.set_duration_s");
        ZH_CN_KEY_MAP.put("设置效果等级\n当前 %s", "tips.sakura_sign_in.set_amplifier_s");
        ZH_CN_KEY_MAP.put("设置概率\n当前 %.3f%%", "tips.sakura_sign_in.set_probability_f");
        ZH_CN_KEY_MAP.put("编辑NBT", "tips.sakura_sign_in.edit_nbt");
        ZH_CN_KEY_MAP.put("页面上部分元素\n按住Shift键可查看帮助信息", "tips.sakura_sign_in.help_button");
        ZH_CN_KEY_MAP.put("比如红色字体按钮, 按住Shift时会给予帮助信息:\n按住Control键 并且 鼠标右键点击以确认\n直接点击是取消哦", "tips.sakura_sign_in.help_button_shift");
        ZH_CN_KEY_MAP.put("从服务器同步配置文件", "tips.sakura_sign_in.download_reward_config");
        ZH_CN_KEY_MAP.put("将配置文件同步至服务器", "tips.sakura_sign_in.upload_reward_config");
        ZH_CN_KEY_MAP.put("将配置文件同步至服务器\n权限不足", "tips.sakura_sign_in.upload_reward_config_no_permission");
        ZH_CN_KEY_MAP.put("打开配置文件夹", "tips.sakura_sign_in.open_config_folder");
        ZH_CN_KEY_MAP.put("奖励规则排序", "tips.sakura_sign_in.reward_rule_sort");
        ZH_CN_KEY_MAP.put("使用键盘%s键也可以哦", "tips.sakura_sign_in.use_s_key");
        ZH_CN_KEY_MAP.put("点击切换主题", "tips.sakura_sign_in.click_to_change_theme");
        ZH_CN_KEY_MAP.put("左键点击切换主题\n右键点击选择外部主题", "tips.sakura_sign_in.click_to_change_theme_or_select_external_theme");
        ZH_CN_KEY_MAP.put("按住Ctrl或Alt键可拖动按钮\nCtrl: 绝对位置坐标\nAlt: 屏幕百分比位置", "tips.sakura_sign_in.drag_inventory_button");
        ZH_CN_KEY_MAP.put("鼠标左键签到\n右键补签/领取奖励", "tips.sakura_sign_in.how_to_sign_in");
        ZH_CN_KEY_MAP.put("补签卡: %s\n连续签到: %sd\n累计签到: %sd", "tips.sakura_sign_in.sign_in_info");
        ZH_CN_KEY_MAP.put("签到页面开屏提示", "tips.sakura_sign_in.sign_in_screen_tips");
        ZH_CN_KEY_MAP.put("奖励配置页面开屏提示", "tips.sakura_sign_in.reward_option_screen_tips");
        ZH_CN_KEY_MAP.put("已选择主题文件: %s", "message.sakura_sign_in.selected_theme_file_s");
        ZH_CN_KEY_MAP.put("前面的的日期以后再来探索吧。", "message.sakura_sign_in.next_day_cannot_operate");
        ZH_CN_KEY_MAP.put("已经签过到了哦。", "message.sakura_sign_in.already_signed");
        ZH_CN_KEY_MAP.put("不论怎么点也不会获取俩次奖励吧。", "message.sakura_sign_in.already_get_reward");
        ZH_CN_KEY_MAP.put("服务器补签功能被禁用了哦。", "message.sakura_sign_in.server_not_enable_sign_in_card");
        ZH_CN_KEY_MAP.put("补签卡不足了哦。", "message.sakura_sign_in.not_enough_sign_in_card");
        ZH_CN_KEY_MAP.put("过去的的日期怎么想也回不去了吧。", "message.sakura_sign_in.past_day_cannot_operate");

        ZH_CN_KEY_MAP.put("SakuraSignIn server is offline!", "message.sakura_sign_in.sakurasignin_server_is_offline");
        ZH_CN_KEY_MAP.put("当前拥有%d张补签卡", "message.sakura_sign_in.has_sign_in_card_d");
        ZH_CN_KEY_MAP.put("给予%d张补签卡", "message.sakura_sign_in.give_sign_in_card_d");
        ZH_CN_KEY_MAP.put("获得%d张补签卡", "message.sakura_sign_in.get_sign_in_card_d");
        ZH_CN_KEY_MAP.put("补签卡被设置为了%d张", "message.sakura_sign_in.set_sign_in_card_d");
        ZH_CN_KEY_MAP.put("玩家[%s]拥有%d张补签卡", "message.sakura_sign_in.set_player_s_sign_in_card_d");
        ZH_CN_KEY_MAP.put("服务器已启用自动签到", "message.sakura_sign_in.server_enabled_auto_sign");
        ZH_CN_KEY_MAP.put("服务器已禁用自动签到", "message.sakura_sign_in.server_disabled_auto_sign");
        ZH_CN_KEY_MAP.put("服务器签到时间冷却方式为: %s", "message.sakura_sign_in.sign_in_time_cool_down_mode_s");
        ZH_CN_KEY_MAP.put("服务器签到冷却刷新时间为: %05.2f", "message.sakura_sign_in.sign_in_time_cool_down_refresh_time_f");
        ZH_CN_KEY_MAP.put("服务器签到冷却刷新间隔为: %05.2f", "message.sakura_sign_in.sign_in_time_cool_down_refresh_interval_f");
        ZH_CN_KEY_MAP.put("服务器已启用补签卡", "message.sakura_sign_in.server_enabled_sign_in_card");
        ZH_CN_KEY_MAP.put("服务器已禁用补签卡", "message.sakura_sign_in.server_disabled_sign_in_card");
        ZH_CN_KEY_MAP.put("服务器最大补签天数为: %d", "message.sakura_sign_in.max_sign_in_day_d");
        ZH_CN_KEY_MAP.put("服务器已启用补签仅获得基础奖励", "message.sakura_sign_in.server_enabled_sign_in_card_only_basic_reward");
        ZH_CN_KEY_MAP.put("服务器已禁用补签仅获得基础奖励", "message.sakura_sign_in.server_disabled_sign_in_card_only_basic_reward");
        ZH_CN_KEY_MAP.put("服务器当前时间: %s", "message.sakura_sign_in.server_current_time_s");
        ZH_CN_KEY_MAP.put("玩家签到数据同步网络包大小为: %d", "message.sakura_sign_in.player_data_sync_packet_size_d");
        ZH_CN_KEY_MAP.put("服务器已启用奖励领取受幸运影响", "message.sakura_sign_in.server_enabled_reward_affected_by_luck");
        ZH_CN_KEY_MAP.put("服务器已禁用奖励领取受幸运影响", "message.sakura_sign_in.server_disabled_reward_affected_by_luck");
        ZH_CN_KEY_MAP.put("服务器已启用连续签到奖励持续领取", "message.sakura_sign_in.server_enabled_continuous_rewards_repeatable");
        ZH_CN_KEY_MAP.put("服务器已禁用连续签到奖励持续领取", "message.sakura_sign_in.server_disabled_continuous_rewards_repeatable");
        ZH_CN_KEY_MAP.put("服务器已启用循环签到奖励持续领取", "message.sakura_sign_in.server_enabled_cycle_rewards_repeatable");
        ZH_CN_KEY_MAP.put("服务器已禁用循环签到奖励持续领取", "message.sakura_sign_in.server_disabled_cycle_rewards_repeatable");

        ZH_CN_KEY_MAP.put("服务器时间已设置为: %s", "message.sakura_sign_in.set_server_time_s");
        ZH_CN_KEY_MAP.put("服务器最大补签天数已被设置为: %d", "message.sakura_sign_in.set_max_sign_in_day_d");
        ZH_CN_KEY_MAP.put("服务器签到时间冷却方式已被设置为: %s", "message.sakura_sign_in.set_sign_in_time_cool_down_mode_s");
        ZH_CN_KEY_MAP.put("服务器签到冷却刷新时间已被设置为: %05.2f", "message.sakura_sign_in.set_sign_in_time_cool_down_refresh_time_f");
        ZH_CN_KEY_MAP.put("服务器签到冷却刷新间隔已被设置为: %05.2f", "message.sakura_sign_in.set_sign_in_time_cool_down_refresh_interval_f");
        ZH_CN_KEY_MAP.put("玩家签到数据同步网络包大小已被设置为: %d", "message.sakura_sign_in.set_player_data_sync_packet_size_d");

        ZH_CN_KEY_MAP.put("要到今天的%05.2f后才能签到哦", "message.sakura_sign_in.next_sign_in_time_f");
        ZH_CN_KEY_MAP.put("签到日期晚于服务器当前日期，签到失败", "message.sakura_sign_in.sign_in_date_late_server_current_date_fail");
        ZH_CN_KEY_MAP.put("签到日期早于服务器当前日期，签到失败", "message.sakura_sign_in.sign_in_date_early_server_current_date_fail");
        ZH_CN_KEY_MAP.put("补签日期需早于服务器当前日期，补签失败", "message.sakura_sign_in.compensate_date_not_early_server_current_date_fail");
        ZH_CN_KEY_MAP.put("签到冷却中，签到失败，请稍后再试", "message.sakura_sign_in.sign_in_cool_down_fail");
        ZH_CN_KEY_MAP.put("服务器补签功能被禁用了哦，补签失败", "message.sakura_sign_in.server_not_enable_sign_in_card_fail");
        ZH_CN_KEY_MAP.put("补签卡不足，补签失败", "message.sakura_sign_in.not_enough_sign_in_card_fail");
        ZH_CN_KEY_MAP.put("%s的奖励已经领取过啦", "message.sakura_sign_in.already_receive_reward_s");
        ZH_CN_KEY_MAP.put("没有查询到[%s]的签到记录哦，鉴定为阁下没有签到！", "message.sakura_sign_in.not_sign_in");
        ZH_CN_KEY_MAP.put("奖励领取详情:", "message.sakura_sign_in.receive_reward_success");
        ZH_CN_KEY_MAP.put("%s 签到成功, %s/%s", "message.sakura_sign_in.sign_in_success_s");
        ZH_CN_KEY_MAP.put("今日CDK输入错误次数过多，请明日再试", "message.sakura_sign_in.cdk_error_too_many_times");
        ZH_CN_KEY_MAP.put("阁下已领取过当前CDK的奖励，请勿重复领取", "message.sakura_sign_in.cdk_already_received");
        ZH_CN_KEY_MAP.put("输入的CDK不存在或已被领取", "message.sakura_sign_in.cdk_not_exist_or_already_received");
        ZH_CN_KEY_MAP.put("输入的CDK已过期", "message.sakura_sign_in.cdk_expired");

    }

    /**
     * 通过中文键获取翻译（带自动回退机制）
     * @param zhKey 中文键
     * @param args 格式化参数
     * @return 本地化文本
     */
    public static String getByZh(String zhKey, Object... args) {
        if (zhKey == null || zhKey.isEmpty()) {
            LOGGER.warn("Attempted to translate empty key");
            return FALLBACK_PREFIX + "EMPTY_KEY";
        }

        String i18nKey = ZH_CN_KEY_MAP.getOrDefault(zhKey, zhKey);
        String translation;

        try {
            translation = I18n.translate(i18nKey, args);
            if (translation.equals(i18nKey)) { // 处理未找到翻译的情况
                LOGGER.debug("Missing translation for key: {}", i18nKey);
                translation = FALLBACK_PREFIX + String.format(zhKey, args);
            }
        } catch (Exception e) {
            LOGGER.error("Translation error for key: {}", i18nKey, e);
            translation = FALLBACK_PREFIX + String.format(zhKey, args);
        }

        return translation;
    }

    /**
     * 直接通过国际化键获取翻译
     * @param i18nKey 国际化键
     * @param args 格式化参数
     * @return 本地化文本
     */
    public static String get(String i18nKey, Object... args) {
        try {
            return I18n.translate(i18nKey, args);
        } catch (Exception e) {
            LOGGER.error("Translation error for key: {}", i18nKey, e);
            return FALLBACK_PREFIX + i18nKey;
        }
    }

    /**
     * 带参数字段校验的翻译方法（用于需要严格校验的场景）
     * @param zhKey 中文键
     * @param args 格式化参数
     * @return 本地化文本
     * @throws IllegalArgumentException 当参数不匹配时抛出
     */
    public static String strictGetByZh(String zhKey, Object... args) {
        String i18nKey = ZH_CN_KEY_MAP.get(zhKey);
        if (i18nKey == null) {
            throw new IllegalArgumentException("Invalid translation key: " + zhKey);
        }

        try {
            String pattern = I18n.translate(i18nKey);
            validateArgs(pattern, args.length);
            return I18n.translate(i18nKey, args);
        } catch (Exception e) {
            LOGGER.error("Strict translation failed for key: {}", i18nKey, e);
            throw new RuntimeException("Translation failure", e);
        }
    }

    private static void validateArgs(String pattern, int argCount) {
        int expected = pattern.split("%(?!%)").length - 1;
        if (expected != argCount) {
            throw new IllegalArgumentException(
                    String.format("Argument count mismatch. Expected %d, got %d", expected, argCount)
            );
        }
    }
}