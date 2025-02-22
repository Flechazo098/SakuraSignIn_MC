package com.flechazo.config;

import com.flechazo.enums.ERewardType;
import com.flechazo.rewards.Reward;
import com.flechazo.rewards.RewardList;
import com.flechazo.rewards.impl.*;
import com.flechazo.util.CollectionUtils;
import com.flechazo.util.DateUtils;
import com.flechazo.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.NonNull;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class RewardOptionData implements Serializable {
    public static final String DATE_RANGE_REGEX1 = "(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})(?:[T ](\\d{1,2}):(\\d{1,2}):(\\d{1,2})|[ ](\\d{1,2}):(\\d{1,2}))?";
    public static final String DATE_RANGE_REGEX2 = "(\\d{4})(?:~(\\d+))?[-/](\\d{1,2})(?:~(\\d+))?[-/](\\d{1,2})(?:~(\\d+))?";
    public static final String REWARD_RULE_KEY_REGEX = "(?:(?:" + DATE_RANGE_REGEX1 + ")|(?:" + DATE_RANGE_REGEX2 + ")|(?:" + "-?\\d{4}" + "))";

    /**
     * 每次签到基础奖励
     */
    @NonNull
    private RewardList baseRewards;
    /**
     * 连续签到奖励<p>
     * 天数超过 maxDay=max(cycleRewards.keySet()) 时按maxDay计算奖励<p>
     * 例
     * 1 : 苹果
     * 2 : 西瓜
     * 4 : 面包
     * 8 : 蛋糕<p>
     * 连续签到第4天时奖励为面包<p>
     * 连续签到第8,9,10,...天时奖励为蛋糕<p>
     */
    @NonNull
    private Map <String, RewardList> continuousRewards;

    /**
     * 连续签到奖励映射关系
     */
    @NonNull
    @Expose (deserialize = false)
    private Map<String, String> continuousRewardsRelation;

    /**
     * 签到周期奖励<p>
     * 每 maxDay=max(cycleRewards.keySet()) 一个循环周期<p>
     * 例
     * 1 : 苹果
     * 2 : 西瓜
     * 4 : 面包
     * 8 : 蛋糕<p>
     * 连续签到第1,9,1+maxDay*n天时奖励为苹果(maxDay=8天一个周期)<p>
     * 连续签到第3,11,3+maxDay*n天时奖励为西瓜(大于2天但不足下一个奖励天)
     */
    @NonNull
    private Map<String, RewardList> cycleRewards;

    /**
     * 签到周期奖励映射关系
     */
    @NonNull
    @Expose(deserialize = false)
    private Map<String, String> cycleRewardsRelation;

    /**
     * 年度签到奖励<p>
     * 可以设置每年第几天奖励, 正数为正数第几天, 负数为倒数第几天<p>
     * 例
     * 1 : 苹果
     * 2 : 西瓜
     * 59 : 面包
     * 60 : 蛋糕<p>
     * 每年第1天(1月1号)奖励为苹果,每年第2天(1月2号)奖励为西瓜,每年第59天(2月28号)奖励为面包,每年第60天(2月29号(闰年)或3月1号)奖励为蛋糕
     */
    @NonNull
    private Map<String, RewardList> yearRewards;
    /**
     * 月度签到奖励<p>
     * 可以设置每月第几天奖励, 正数为正数第几天, 负数为倒数第几天<p>
     * 例
     * 1 : 苹果
     * 2 : 西瓜
     * 4 : 面包
     * 8 : 蛋糕<p>
     * 每月1号奖励为苹果,2号奖励为西瓜,4号奖励为面包,8号奖励为蛋糕
     */
    @NonNull
    private Map<String, RewardList> monthRewards;
    /**
     * 周度签到奖励<p>
     * 可以设置每周几奖励, 与<strong>签到周期奖励</strong>类似, 但不需要<strong>连续签到</strong><p>
     * 例
     * 1 : 苹果
     * 2 : 西瓜
     * 4 : 面包
     * 7 : 蛋糕<p>
     * 每周一奖励为苹果,周二奖励为西瓜,周四奖励为面包,周日奖励为蛋糕
     */
    @NonNull
    private Map<String, RewardList> weekRewards;
    /**
     * 指定日期/日期范围签到奖励<p>
     * 日期格式支持 yyyy-MM-dd, 0000-MM-dd, yyyy-MM-00, 0000-MM-00<p>
     * yyyy~n-MM~n-dd, 0000-MM~n-dd~n, yyyy-MM~n-00, 0000-MM~n-00<p>
     * yyyy-MM-ddTHH:mm, yyyy-MM-ddTHH~n:mm~n<p>
     * 指定具体时间时, 日期与时间需要'T'分隔<p>
     * ~n表示区间, 例 2024-10-05~5 表示 2024年10月05日到10日的5天<p>
     * 0000(yyyy) 表示不限年份, 00(MM) 表示不限月份, 00(dd) 表示不限日期
     */
    @NonNull
    private Map<String, RewardList> dateTimeRewards;

    /**
     * 日期时间奖励映射关系
     */
    @NonNull
    @Expose(deserialize = false)
    private Map<String, String> dateTimeRewardsRelation;

    /**
     * 累计签到奖励<p>
     * 累计签到达到一定天数时，会获得相应的奖励
     */
    @NonNull
    private Map<String, RewardList> cumulativeRewards;

    /**
     * 随机奖励<p>
     * 在每次签到时随机获得一组奖励
     */
    @NonNull
    private Map<String, RewardList> randomRewards;

    /**
     * 兑换码奖励<p>
     * 输入正确的兑换码进行兑换，每个兑换码仅可使用一次<p>
     * key : expiration date : rewards
     */
    private List <KeyValue<String, KeyValue<String, RewardList>>> cdkRewards;

    public RewardOptionData() {
        this.baseRewards = new RewardList();
        this.continuousRewards = new LinkedHashMap <> ();
        this.continuousRewardsRelation = new LinkedHashMap<>();
        this.cycleRewards = new LinkedHashMap<>();
        this.cycleRewardsRelation = new LinkedHashMap<>();
        this.yearRewards = new LinkedHashMap<>();
        this.monthRewards = new LinkedHashMap<>();
        this.weekRewards = new LinkedHashMap<>();
        this.dateTimeRewards = new LinkedHashMap<>();
        this.dateTimeRewardsRelation = new LinkedHashMap<>();
        this.cumulativeRewards = new LinkedHashMap<>();
        this.randomRewards = new LinkedHashMap<>();
        this.cdkRewards = new ArrayList <> ();
    }

    /**
     * 设置连续签到奖励
     */
    public void setContinuousRewards(@NonNull Map<String, RewardList> continuousRewards) {
        // 只有键为有效正整数字符串时才会被添加到映射中，以确保数据的合法性
        this.continuousRewards = new LinkedHashMap<>();
        continuousRewards.forEach((key, value) -> {
            if (StringUtils.isNotNullOrEmpty(key)) {
                this.addContinuousRewards(key, value);
            }
        });
    }

    /**
     * 添加连续签到奖励
     */
    @SuppressWarnings("ConstantConditions")
    public void addContinuousRewards(@NonNull String key, @NonNull RewardList value) {
        if (this.continuousRewards == null) this.continuousRewards = new LinkedHashMap<>();
        int keyInt = StringUtils.toInt(key);
        if (keyInt > 0) {
            if (this.cumulativeRewards.containsKey(String.valueOf(keyInt))) {
                this.cumulativeRewards.get(String.valueOf(keyInt)).addAll(value);
            } else {
                this.continuousRewards.put(String.valueOf(keyInt), value);
            }
        }
        this.refreshContinuousRewardsRelation();
    }

    public void refreshContinuousRewardsRelation() {
        // 处理映射关系
        if (!this.continuousRewards.isEmpty()) {
            this.continuousRewardsRelation = new LinkedHashMap<>();
            List<Integer> keyList = this.continuousRewards.keySet().stream().map(Integer::parseInt).sorted().toList();
            if (ServerConfig.getCONTINUOUS_REWARDS_REPEATABLE()) {
                int max = keyList.stream().max(Comparator.naturalOrder()).orElse(0);
                int cur = keyList.get(0);
                for (int i = 1; i <= max; i++) {
                    if (keyList.contains(i)) cur = i;
                    this.continuousRewardsRelation.put(String.valueOf(i), String.valueOf(cur));
                }
            } else {
                this.continuousRewardsRelation.put(String.valueOf(keyList.get(0)), String.valueOf(keyList.get(0)));
            }
        }
    }

    /**
     * 设置周期签到奖励
     */
    public void setCycleRewards(@NonNull Map<String, RewardList> cycleRewards) {
        // 只有键为有效正整数字符串时才会被添加到映射中，以确保数据的合法性
        this.cycleRewards = new LinkedHashMap<>();
        cycleRewards.forEach((key, value) -> {
            if (StringUtils.isNotNullOrEmpty(key)) {
                this.addCycleRewards(key, value);
            }
        });
    }

    /**
     * 添加周期签到奖励
     */
    @SuppressWarnings("ConstantConditions")
    public void addCycleRewards(@NonNull String key, @NonNull RewardList value) {
        if (this.cycleRewards == null) this.cycleRewards = new LinkedHashMap<>();
        int keyInt = StringUtils.toInt(key);
        if (keyInt > 0) {
            if (this.cycleRewards.containsKey(String.valueOf(keyInt))) {
                this.cycleRewards.get(String.valueOf(keyInt)).addAll(value);
            } else {
                this.cycleRewards.put(String.valueOf(keyInt), value);
            }
        }
        this.refreshCycleRewardsRelation();
    }

    public void refreshCycleRewardsRelation() {
        // 处理映射关系
        if (!this.cycleRewards.isEmpty()) {
            this.cycleRewardsRelation = new LinkedHashMap<>();
            List<Integer> keyList = this.cycleRewards.keySet().stream().map(Integer::parseInt).sorted().toList();
            if (ServerConfig.getCYCLE_REWARDS_REPEATABLE()) {
                int max = keyList.stream().max(Comparator.naturalOrder()).orElse(0);
                int cur = keyList.get(0);
                for (int i = 1; i <= max; i++) {
                    if (keyList.contains(i)) cur = i;
                    this.cycleRewardsRelation.put(String.valueOf(i), String.valueOf(cur));
                }
            } else {
                this.cycleRewardsRelation.put(String.valueOf(keyList.get(0)), String.valueOf(keyList.get(0)));
            }
        }
    }

    /**
     * 设置年度签到奖励
     */
    public void setYearRewards(@NonNull Map<String, RewardList> yearRewards) {
        // 只有键为有效整数字符串时才会被添加到映射中，以确保数据的合法性
        this.yearRewards = new LinkedHashMap<>();
        yearRewards.forEach((key, value) -> {
            if (StringUtils.isNotNullOrEmpty(key)) {
                this.addYearRewards(key, value);
            }
        });
    }

    /**
     * 添加年度签到奖励
     */
    @SuppressWarnings("ConstantConditions")
    public void addYearRewards(@NonNull String key, @NonNull RewardList value) {
        if (this.yearRewards == null) this.yearRewards = new LinkedHashMap<>();
        int keyInt = StringUtils.toInt(key);
        if (keyInt > -366 && keyInt <= 366 && keyInt != 0) {
            if (this.yearRewards.containsKey(String.valueOf(keyInt))) {
                this.yearRewards.get(String.valueOf(keyInt)).addAll(value);
            } else {
                this.yearRewards.put(String.valueOf(keyInt), value);
            }
        }
    }

    /**
     * 设置月度签到奖励
     */
    public void setMonthRewards(@NonNull Map<String, RewardList> monthRewards) {
        // 只有键为有效整数字符串时才会被添加到映射中，以确保数据的合法性
        this.monthRewards = new LinkedHashMap<>();
        monthRewards.forEach((key, value) -> {
            if (StringUtils.isNotNullOrEmpty(key)) {
                this.addMonthRewards(key, value);
            }
        });
    }

    /**
     * 添加月度签到奖励
     */
    @SuppressWarnings("ConstantConditions")
    public void addMonthRewards(@NonNull String key, @NonNull RewardList value) {
        if (this.monthRewards == null) this.monthRewards = new LinkedHashMap<>();
        int keyInt = StringUtils.toInt(key);
        if (keyInt > -31 && keyInt <= 31 && keyInt != 0) {
            if (this.monthRewards.containsKey(String.valueOf(keyInt))) {
                this.monthRewards.get(String.valueOf(keyInt)).addAll(value);
            } else {
                this.monthRewards.put(String.valueOf(keyInt), value);
            }
        }
    }

    /**
     * 设置周度签到奖励
     */
    public void setWeekRewards(@NonNull Map<String, RewardList> weekRewards) {
        // 只有键为有效正整数字符串时才会被添加到映射中，以确保数据的合法性
        this.weekRewards = new LinkedHashMap<>();
        weekRewards.forEach((key, value) -> {
            if (StringUtils.isNotNullOrEmpty(key)) {
                this.addWeekRewards(key, value);
            }
        });
    }

    /**
     * 添加周度签到奖励
     */
    @SuppressWarnings("ConstantConditions")
    public void addWeekRewards(@NonNull String key, @NonNull RewardList value) {
        if (this.weekRewards == null) this.weekRewards = new LinkedHashMap<>();
        int keyInt = StringUtils.toInt(key);
        if (keyInt > 0 && keyInt <= 7) {
            if (this.weekRewards.containsKey(String.valueOf(keyInt))) {
                this.weekRewards.get(String.valueOf(keyInt)).addAll(value);
            } else {
                this.weekRewards.put(String.valueOf(keyInt), value);
            }
        }
    }

    /**
     * 设置日期时间奖励映射
     * <p>
     * 本方法用于接收一个日期时间与奖励列表的映射，并根据日期范围解析生成一个更详细的日期与奖励关系映射
     * 这有助于在给定特定日期时，能够快速确定该日期所属的日期范围及其对应的奖励
     */
    public void setDateTimeRewards(@NonNull Map<String, RewardList> dateTimeRewards) {
        this.dateTimeRewards = new LinkedHashMap<>();
        dateTimeRewards.forEach((key, rewardList) -> {
            if (CollectionUtils.isNotNullOrEmpty(rewardList)) {
                // 解析日期范围并生成具体日期
                this.addDateTimeRewards(key, rewardList);
            }
        });
    }

    /**
     * 添加日期时间奖励
     */
    @SuppressWarnings("ConstantConditions")
    public void addDateTimeRewards(@NonNull String key, @NonNull RewardList value) {
        if (this.dateTimeRewards == null) this.dateTimeRewards = new LinkedHashMap<>();
        if (!RewardOptionData.parseDateRange(key).isEmpty()) {
            if (this.dateTimeRewards.containsKey(key)) {
                this.dateTimeRewards.get(key).addAll(value);
            } else {
                this.dateTimeRewards.put(key, value);
            }
            this.dateTimeRewardsRelation = new LinkedHashMap<>();
            for (String dateTimeKey : this.dateTimeRewards.keySet()) {
                List<String> parsedDates = RewardOptionData.parseDateRange(dateTimeKey);
                for (String date : parsedDates) {
                    this.dateTimeRewardsRelation.put(date, dateTimeKey);
                }
            }
        }
    }

    /**
     * 设置累计签到奖励
     */
    public void setCumulativeRewards(@NonNull Map<String, RewardList> cumulativeRewards) {
        // 只有键为有效正整数字符串时才会被添加到映射中，以确保数据的合法性
        this.cumulativeRewards = new LinkedHashMap<>();
        cumulativeRewards.forEach((key, value) -> {
            if (StringUtils.isNotNullOrEmpty(key)) {
                this.addCumulativeReward(key, value);
            }
        });
    }

    /**
     * 添加累计签到奖励
     */
    @SuppressWarnings("ConstantConditions")
    public void addCumulativeReward(@NonNull String key, @NonNull RewardList value) {
        if (this.cumulativeRewards == null) this.cumulativeRewards = new LinkedHashMap<>();
        int keyInt = StringUtils.toInt(key);
        if (keyInt > 0) {
            if (this.cumulativeRewards.containsKey(String.valueOf(keyInt))) {
                this.cumulativeRewards.get(String.valueOf(keyInt)).addAll(value);
            } else {
                this.cumulativeRewards.put(String.valueOf(keyInt), value);
            }
        }
    }

    /**
     * 设置随机签到奖励
     */
    public void setRandomRewards(@NonNull Map<String, RewardList> randomRewards) {
        this.randomRewards = new LinkedHashMap<>();
        randomRewards.forEach((key, value) -> {
            if (StringUtils.isNotNullOrEmpty(key)) {
                this.addRandomReward(key, value);
            }
        });
    }

    /**
     * 添加随机签到奖励
     */
    @SuppressWarnings("ConstantConditions")
    public void addRandomReward(@NonNull String key, @NonNull RewardList value) {
        if (this.randomRewards == null) this.randomRewards = new LinkedHashMap<>();
        BigDecimal probability = StringUtils.toBigDecimal(key);
        if (probability.compareTo(BigDecimal.ZERO) > 0 && probability.compareTo(BigDecimal.ONE) <= 0) {
            String fixedKey = StringUtils.toFixedEx(probability, 10);
            if (this.randomRewards.containsKey(fixedKey)) {
                this.randomRewards.get(fixedKey).addAll(value);
            } else {
                this.randomRewards.put(fixedKey, value);
            }
        }
    }

    /**
     * 设置兑换码签到奖励
     */
    public void setCdkRewards(@NonNull List<KeyValue<String, KeyValue<String, RewardList>>> cdkRewards) {
        this.cdkRewards = new ArrayList<>();
        cdkRewards.forEach((keyValue) -> {
            if (StringUtils.isNotNullOrEmpty(keyValue.getKey())) {
                this.addCdkReward(keyValue);
            }
        });
    }

    /**
     * 添加兑换码签到奖励
     */
    @SuppressWarnings("ConstantConditions")
    public void addCdkReward(@NonNull KeyValue<String, KeyValue<String, RewardList>> keyValue) {
        if (this.cdkRewards == null) this.cdkRewards = new ArrayList<>();
        if (StringUtils.isNotNullOrEmpty(keyValue.getKey())) {
            if (StringUtils.isNullOrEmptyEx(keyValue.getValue().getKey())) {
                String year = StringUtils.getString("9", String.valueOf(DateUtils.getYearPart(new Date())).length());
                keyValue.getValue().setKey(year + "-12-31");
            }
            this.cdkRewards.add(keyValue);
        }
    }

    public static RewardOptionData getDefault() {
        return new RewardOptionData() {{
            setBaseRewards(new RewardList() {{
                add(new Reward () {{
                    setContent(new ItemRewardParser ().serialize(new ItemStack (Items.APPLE, 1)));
                    setType(ERewardType.ITEM);
                }});
            }});
            setContinuousRewards(new LinkedHashMap<>() {{
                put("1", new RewardList() {{
                    add(new Reward() {{
                        setContent(new ExpPointRewardParser ().serialize(5));
                        setType(ERewardType.EXP_POINT);
                    }});
                }});
                put("4", new RewardList() {{
                    add(new Reward() {{
                        setContent(new ItemRewardParser().serialize(new ItemStack(Items.CAKE, 1)));
                        setType(ERewardType.ITEM);
                    }});
                }});
                put("8", new RewardList() {{
                    add(new Reward() {{
                        setContent(new SignInCardRewardParser ().serialize(1));
                        setType(ERewardType.SIGN_IN_CARD);
                    }});
                }});
            }});
            setCycleRewards(new LinkedHashMap<>() {{
                put("2", new RewardList() {{
                    add(new Reward() {{
                        setContent(new ExpPointRewardParser().serialize(3));
                        setType(ERewardType.EXP_POINT);
                    }});
                }});
                put("5", new RewardList() {{
                    add(new Reward() {{
                        setContent(new ExpLevelRewardParser ().serialize(1));
                        setType(ERewardType.EXP_LEVEL);
                    }});
                }});
            }});
            setYearRewards(new LinkedHashMap<>());
            setMonthRewards(new LinkedHashMap<>());
            setWeekRewards(new LinkedHashMap<>() {{
                put("6", new RewardList() {{
                    add(new Reward() {{
                        setContent(new EffectRewardParser ().serialize(new StatusEffectInstance(StatusEffects.LUCK, 6000, 1)));
                        setType(ERewardType.EFFECT);
                    }});
                }});
                put("7", new RewardList() {{
                    add(new Reward() {{
                        // 急促
                        setContent(new EffectRewardParser().serialize(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 6000, 0)));
                        setType(ERewardType.EFFECT);
                    }});
                    add(new Reward() {{
                        setContent(new EffectRewardParser().serialize(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 6000, 0)));
                        setType(ERewardType.EFFECT);
                    }});
                    add(new Reward() {{
                        setContent(new ItemRewardParser().serialize(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1)));
                        setType(ERewardType.ITEM);
                    }});
                }});
            }});
            setDateTimeRewards(new LinkedHashMap<>() {{
                put("0000-10-06~1", new RewardList() {{
                    add(new Reward() {{
                        setContent(new ItemRewardParser().serialize(new ItemStack(Items.EXPERIENCE_BOTTLE, 1)));
                        setType(ERewardType.ITEM);
                    }});
                    add(new Reward() {{
                        setContent(new EffectRewardParser().serialize(new StatusEffectInstance(StatusEffects.RESISTANCE, 300, 1)));
                        setType(ERewardType.EFFECT);
                    }});
                }});
            }});
            setCumulativeRewards(new LinkedHashMap<>() {{
                put("100", new RewardList() {{
                    add(new Reward() {{
                        setContent(new EffectRewardParser().serialize(new StatusEffectInstance(StatusEffects.LUCK, 99999, 2)));
                        setType(ERewardType.EFFECT);
                    }});
                }});
            }});
        }};
    }

    /**
     * 解析日期范围
     */
    public static List<String> parseDateRange(String dateRange) {
        List<String> result = new ArrayList<>();

        // 先尝试匹配固定的日期格式 yyyy-MM-dd 或 yyyy-MM-ddTHH:mm:ss
        if (tryParseFixedDate(dateRange, result)) {
            return result;
        }

        // 如果没有匹配上，再尝试匹配日期范围格式 yyyy-MM-dd~n 或 yyyy-MM~n-dd~n
        return tryParseDateRange(dateRange, result);
    }

    private static boolean tryParseFixedDate(String dateRange, List<String> result) {
        Pattern fixedDatePattern = Pattern.compile(DATE_RANGE_REGEX1);
        Matcher matcher = fixedDatePattern.matcher(dateRange);

        if (matcher.matches()) {
            String yearString = matcher.group(1);
            String monthString = matcher.group(2);
            String dayString = matcher.group(3);
            String hourString = matcher.group(4);
            String minuteString = matcher.group(5);
            String secondString = matcher.group(6);

            // 对于无效的日期，直接返回原样字符串
            if (isInvalidDate(yearString, monthString, dayString)) {
                result.add(dateRange);
                return true;
            }

            // 解析日期
            Date parsedDate = DateUtils.getDate(yearString, monthString, dayString, hourString, minuteString, secondString);
            String formattedDate = formatDateTime(parsedDate, hourString, minuteString, secondString);
            result.add(formattedDate);
            return true;
        }

        return false;
    }

    private static boolean isInvalidDate(String year, String month, String day) {
        return "0000".equals(year) || "00".equals(month) || "0".equals(month) || "00".equals(day) || "0".equals(day);
    }

    private static String formatDateTime(Date date, String hourString, String minuteString, String secondString) {
        if (StringUtils.isNotNullOrEmpty(hourString) && StringUtils.isNotNullOrEmpty(minuteString) && StringUtils.isNotNullOrEmpty(secondString)) {
            return DateUtils.toDateTimeString(date);
        } else {
            return DateUtils.toString(date);
        }
    }

    private static List<String> tryParseDateRange(String dateRange, List<String> result) {
        Pattern rangePattern = Pattern.compile(DATE_RANGE_REGEX2);
        Matcher matcher = rangePattern.matcher(dateRange);

        if (matcher.matches()) {
            String startYear = matcher.group(1);
            String yearRange = matcher.group(2);  // 可能为空
            String startMonth = matcher.group(3);
            String monthRange = matcher.group(4); // 可能为空
            String startDay = matcher.group(5);
            String dayRange = matcher.group(6);   // 可能为空

            // 默认值为0
            int yearDiff = parseIntOrDefault(yearRange, 0);
            int monthDiff = parseIntOrDefault(monthRange, 0);
            int dayDiff = parseIntOrDefault(dayRange, 0);

            Date startDate = DateUtils.getDate(getValidYear(startYear), getValidMonth(startMonth), getValidDay(startDay));
            generateDateRange(startDate, yearDiff, monthDiff, dayDiff, result);

            return result;
        }

        return result;
    }

    private static int parseIntOrDefault(String str, int defaultValue) {
        return StringUtils.isNullOrEmpty(str) ? defaultValue : Integer.parseInt(str);
    }

    private static String getValidYear(String year) {
        return "0000".equals(year) ? "2020" : year;
    }

    private static String getValidMonth(String month) {
        return "00".equals(month) ? "01" : month;
    }

    private static String getValidDay(String day) {
        return "00".equals(day) ? "01" : day;
    }

    private static void generateDateRange(Date startDate, int yearDiff, int monthDiff, int dayDiff, List<String> result) {
        // 生成日期范围
        for (int i = 0; i <= yearDiff && i < 10; i++) {
            for (int j = 0; j <= monthDiff && j < 12; j++) {
                for (int k = 0; k <= dayDiff && k < 31; k++) {
                    startDate = DateUtils.addYear(startDate, i);
                    startDate = DateUtils.addMonth(startDate, j);
                    startDate = DateUtils.addDay(startDate, k);

                    String year = String.valueOf(DateUtils.getYearPart(startDate));
                    String month = String.valueOf(DateUtils.getMonthOfDate(startDate));
                    String day = String.valueOf(DateUtils.getDayOfMonth(startDate));

                    result.add(year + "-" + month + "-" + day);
                }
            }
        }
    }

    public JsonObject toJsonObject() {
        JsonObject json = new JsonObject();
        json.add("baseRewards", baseRewards.toJsonArray());

        JsonObject continuousRewardsJson = new JsonObject();
        for (Map.Entry<String, RewardList> entry : continuousRewards.entrySet()) {
            continuousRewardsJson.add(entry.getKey(), entry.getValue().toJsonArray());
        }
        json.add("continuousRewards", continuousRewardsJson);

        JsonObject continuousRewardsRelationJson = new JsonObject();
        for (Map.Entry<String, String> entry : continuousRewardsRelation.entrySet()) {
            continuousRewardsRelationJson.addProperty(entry.getKey(), entry.getValue());
        }
        json.add("continuousRewardsRelation", continuousRewardsRelationJson);

        JsonObject cycleRewardsJson = new JsonObject();
        for (Map.Entry<String, RewardList> entry : cycleRewards.entrySet()) {
            cycleRewardsJson.add(entry.getKey(), entry.getValue().toJsonArray());
        }
        json.add("cycleRewards", cycleRewardsJson);

        JsonObject cycleRewardsRelationJson = new JsonObject();
        for (Map.Entry<String, String> entry : cycleRewardsRelation.entrySet()) {
            cycleRewardsRelationJson.addProperty(entry.getKey(), entry.getValue());
        }
        json.add("cycleRewardsRelation", cycleRewardsRelationJson);

        JsonObject yearRewardsJson = new JsonObject();
        for (Map.Entry<String, RewardList> entry : yearRewards.entrySet()) {
            yearRewardsJson.add(entry.getKey(), entry.getValue().toJsonArray());
        }
        json.add("yearRewards", yearRewardsJson);

        JsonObject monthRewardsJson = new JsonObject();
        for (Map.Entry<String, RewardList> entry : monthRewards.entrySet()) {
            monthRewardsJson.add(entry.getKey(), entry.getValue().toJsonArray());
        }
        json.add("monthRewards", monthRewardsJson);

        JsonObject weekRewardsJson = new JsonObject();
        for (Map.Entry<String, RewardList> entry : weekRewards.entrySet()) {
            weekRewardsJson.add(entry.getKey(), entry.getValue().toJsonArray());
        }
        json.add("weekRewards", weekRewardsJson);

        JsonObject dateTimeRewardsJson = new JsonObject();
        for (Map.Entry<String, RewardList> entry : dateTimeRewards.entrySet()) {
            dateTimeRewardsJson.add(entry.getKey(), entry.getValue().toJsonArray());
        }
        json.add("dateTimeRewards", dateTimeRewardsJson);

        JsonObject dateTimeRewardsRelationJson = new JsonObject();
        for (Map.Entry<String, String> entry : dateTimeRewardsRelation.entrySet()) {
            dateTimeRewardsRelationJson.addProperty(entry.getKey(), entry.getValue());
        }
        json.add("dateTimeRewardsRelation", dateTimeRewardsRelationJson);

        JsonObject cumulativeRewardJson = new JsonObject();
        for (Map.Entry<String, RewardList> entry : cumulativeRewards.entrySet()) {
            cumulativeRewardJson.add(entry.getKey(), entry.getValue().toJsonArray());
        }
        json.add("cumulativeRewards", cumulativeRewardJson);

        JsonObject randomRewardsJson = new JsonObject();
        for (Map.Entry<String, RewardList> entry : randomRewards.entrySet()) {
            randomRewardsJson.add(entry.getKey(), entry.getValue().toJsonArray());
        }
        json.add("randomRewards", randomRewardsJson);

        JsonArray cdkRewardsArray = new JsonArray();
        for (KeyValue<String, KeyValue<String, RewardList>> entry : cdkRewards) {
            JsonObject keyValueJson = new JsonObject();
            keyValueJson.addProperty("key", entry.getKey());
            keyValueJson.addProperty("date", entry.getValue().getKey());
            keyValueJson.add("value", entry.getValue().getValue().toJsonArray());
            cdkRewardsArray.add(keyValueJson);
        }
        json.add("cdkRewards", cdkRewardsArray);
        return json;
    }
}
