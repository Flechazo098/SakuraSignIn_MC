package com.flechazo.rewards.impl;

import com.flechazo.enums.ERewardType;
import com.flechazo.rewards.RewardParser;
import com.flechazo.util.I18nUtils;
import com.google.gson.JsonObject;
import lombok.NonNull;

public class CommandRewardParser implements RewardParser <String> {

    @Override
    public @NonNull String deserialize(JsonObject json) {
        try {
            return json.get("command").getAsString();
        } catch (Exception e) {
            LOGGER.error("Failed to parse command reward", e);
            return "";
        }
    }

    @Override
    public JsonObject serialize(String reward) {
        JsonObject json = new JsonObject();
        json.addProperty("command", reward);
        return json;
    }

    @Override
    public @NonNull String getDisplayName(JsonObject json) {
        return getDisplayName(json, false);
    }

    @Override
    public @NonNull String getDisplayName(JsonObject json, boolean withNum) {
        return I18nUtils.get(String.format("reward.sakura_sign_in.reward_type_%s", ERewardType.COMMAND.getCode()));
    }
}
