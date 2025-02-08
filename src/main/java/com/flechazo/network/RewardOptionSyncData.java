package com.flechazo.network;

import com.flechazo.enums.ERewardRule;
import com.flechazo.enums.ERewardType;
import com.flechazo.rewards.Reward;
import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;

import static com.flechazo.config.RewardOptionDataManager.GSON;

/**
 * 奖励配置同步数据
 *
 * @author Flechazo
 */
public record RewardOptionSyncData(ERewardRule rule, String key, Reward reward) {

    public static RewardOptionSyncData decode (PacketByteBuf buf) {
        ERewardRule rule = ERewardRule.valueOf (buf.readInt ());
        String key = buf.readString ();
        JsonObject jsonObject = GSON.fromJson (buf.readString (), JsonObject.class);
        Reward reward = new Reward ();
        reward.setRewarded (jsonObject.get ("rewarded").getAsBoolean ());
        reward.setDisabled (jsonObject.get ("disabled").getAsBoolean ());
        reward.setType (ERewardType.valueOf (jsonObject.get ("type").getAsString ()));
        reward.setProbability (jsonObject.get ("probability").getAsBigDecimal ());
        reward.setContent (jsonObject.getAsJsonObject ("content"));
        return new RewardOptionSyncData (rule, key, reward);
    }

    public static void encode (RewardOptionSyncData data, PacketByteBuf buf) {
        buf.writeInt (data.rule.getCode ());
        buf.writeString (data.key);
        buf.writeString (GSON.toJson (data.reward.toJsonObject ()));
    }

}