package com.flechazo.network;

import com.flechazo.enums.ERewardRule;
import com.flechazo.rewards.Reward;
import net.minecraft.network.PacketByteBuf;

/**
 * 奖励配置同步数据
 * @author Flechazo
 */
public class RewardOptionSyncData {
    private final ERewardRule rule;
    private final String key;
    private final Reward reward;

    public RewardOptionSyncData(ERewardRule rule, String key, Reward reward) {
        this.rule = rule;
        this.key = key;
        this.reward = reward;
    }

    public static RewardOptionSyncData decode(PacketByteBuf buf) {
        ERewardRule rule = ERewardRule.valueOf(buf.readInt());
        String key = buf.readString();
        Reward reward = Reward.fromJson(buf.readString());
        return new RewardOptionSyncData(rule, key, reward);
    }

    public static void encode(RewardOptionSyncData data, PacketByteBuf buf) {
        buf.writeInt(data.rule.getCode());
        buf.writeString(data.key);
        buf.writeString(data.reward.toJson());
    }

    public ERewardRule getRule() {
        return rule;
    }

    public String getKey() {
        return key;
    }

    public Reward getReward() {
        return reward;
    }
}