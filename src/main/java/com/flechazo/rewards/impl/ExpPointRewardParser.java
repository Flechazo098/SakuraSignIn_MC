package com.flechazo.rewards.impl;

import com.flechazo.enums.ERewardType;
import com.flechazo.rewards.RewardParser;
import com.flechazo.util.I18nUtils;
import com.google.gson.JsonObject;
import lombok.NonNull;

public class ExpPointRewardParser implements RewardParser <Integer> {

    @Override
    public @NonNull Integer deserialize(JsonObject json) {
        try {
            return json.get("expPoint").getAsInt();
        } catch (Exception e) {
            LOGGER.error("Failed to parse exp point reward", e);
            return 0;
        }
    }

    @Override
    public JsonObject serialize(Integer reward) {
        JsonObject json = new JsonObject();
        json.addProperty("expPoint", reward);
        return json;
    }

    @Override
    public @NonNull String getDisplayName(JsonObject json) {
        return getDisplayName(json, false);
    }

    @Override
    public @NonNull String getDisplayName(JsonObject json, boolean withNum) {
        int num = deserialize(json);
        return I18nUtils.get(String.format("reward.sakura_sign_in.reward_type_%s", ERewardType.EXP_POINT.getCode())) + (withNum ? "x" + num : "");
    }
}
