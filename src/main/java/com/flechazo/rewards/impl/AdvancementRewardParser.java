package com.flechazo.rewards.impl;

import com.flechazo.SakuraSignInFabric;
import com.flechazo.enums.ERewardType;
import com.flechazo.network.AdvancementData;
import com.flechazo.rewards.RewardParser;
import com.flechazo.util.I18nUtils;
import com.google.gson.JsonObject;
import lombok.NonNull;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.util.Identifier;

public class AdvancementRewardParser implements RewardParser < Identifier > {

    @Override
    public @NonNull Identifier deserialize(JsonObject json) {
        String advancementId;
        try {
            advancementId = json.get("advancement").getAsString();
        } catch (Exception e) {
            LOGGER.error("Failed to parse advancement reward", e);
            advancementId = SakuraSignInFabric.MOD_ID + ":unknownAdvancement";
        }
        return new Identifier(advancementId);
    }

    @Override
    public JsonObject serialize(Identifier reward) {
        JsonObject json = new JsonObject();
        json.addProperty("advancement", reward.toString());
        return json;
    }

    public static AdvancementData getAdvancementData(String id) {
        return SakuraSignInFabric.getAdvancementData().stream()
                .filter(data -> data.id ().toString().equalsIgnoreCase(id))
                .findFirst().orElse(new AdvancementData(new Identifier(id), null));
    }

    public static String getId(AdvancementData advancementData) {
        return getId(advancementData.id ());
    }

    public static String getId(Advancement advancement) {
        return getId(advancement.getId());
    }

    public static String getId(Identifier resourceLocation) {
        return resourceLocation.toString();
    }

    public static AdvancementData getAdvancementData(Identifier resourceLocation) {
        return getAdvancementData(resourceLocation.toString());
    }

    public static @NonNull String getDisplayName(AdvancementData advancementData) {
        return advancementData.displayInfo ().getTitle().getString();
    }

    public static @NonNull String getDescription(AdvancementData advancementData) {
        return advancementData.displayInfo ().getDescription().getString();
    }

    public static @NonNull String getDisplayName(Advancement advancement) {
        String result = "";
        AdvancementDisplay display = advancement.getDisplay();
        if (display != null)
            result = display.getTitle().getString();
        return result;
    }

    @Override
    public @NonNull String getDisplayName(JsonObject json) {
        return getDisplayName(json, false);
    }

    @Override
    public @NonNull String getDisplayName(JsonObject json, boolean withNum) {
        Identifier deserialize = deserialize(json);
        return String.format("%s: %s", I18nUtils.get(String.format("reward.sakura_sign_in.reward_type_%s", ERewardType.ADVANCEMENT.getCode()))
                , SakuraSignInFabric.getAdvancementData().stream()
                        .filter(data -> data.id ().equals(deserialize))
                        .findFirst().orElse(new AdvancementData(deserialize, null))
                        .displayInfo ().getTitle().getString());
    }

    public @NonNull
    static String getDescription(Advancement advancement) {
        String result = "";
        AdvancementDisplay display = advancement.getDisplay();
        if (display != null)
            result = advancement.getDisplay().getDescription().getString();
        return result;
    }
}

