package com.flechazo.sakura.rewards.impl;

import com.flechazo.sakura.enums.EI18nType;
import com.flechazo.sakura.enums.ERewardType;
import com.flechazo.sakura.rewards.RewardParser;
import com.flechazo.sakura.util.I18nUtils;
import com.google.gson.JsonObject;
import lombok.NonNull;

public class ExpLevelRewardParser implements RewardParser <Integer> {

    @Override
    public @NonNull Integer deserialize(JsonObject json) {
        try {
            return json.get("expLevel").getAsInt();
        } catch (Exception e) {
            LOGGER.error("Failed to parse exp level reward", e);
            return 0;
        }
    }

    @Override
    public JsonObject serialize(Integer reward) {
        JsonObject json = new JsonObject();
        json.addProperty("expLevel", reward);
        return json;
    }

    @Override
    public @NonNull String getDisplayName(String languageCode, JsonObject json) {
        return getDisplayName(languageCode, json, false);
    }

    @Override
    public @NonNull String getDisplayName(String languageCode, JsonObject json, boolean withNum) {
        int num = deserialize(json);
        String rewardType = I18nUtils.getTranslation(EI18nType.WORD, "reward_type_" + ERewardType.EXP_LEVEL.getCode(), languageCode);
        return rewardType + (withNum ? "x" + num : "");
    }
}