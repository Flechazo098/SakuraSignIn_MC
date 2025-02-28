package com.flechazo.sakura.rewards.impl;

import com.flechazo.sakura.SakuraSignInFabric;
import com.flechazo.sakura.enums.EI18nType;
import com.flechazo.sakura.enums.ERewardType;
import com.flechazo.sakura.network.AdvancementData;
import com.flechazo.sakura.rewards.RewardParser;
import com.flechazo.sakura.util.I18nUtils;
import com.google.gson.JsonObject;
import lombok.NonNull;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.util.Identifier;
import java.util.List;

/**
 * 进度奖励解析器
 * @author Flechazo
 */
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
        try {
            if (reward != null) {
                json.addProperty("advancement", reward.toString());
            } else {
                LOGGER.error("Attempted to serialize null advancement ID");
                json.addProperty("advancement", SakuraSignInFabric.MOD_ID + ":unknownAdvancement");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to serialize advancement reward", e);
            json.addProperty("advancement", SakuraSignInFabric.MOD_ID + ":unknownAdvancement");
        }
        return json;
    }

    public static AdvancementData getAdvancementData(String id) {
        List<AdvancementData> advancementData = SakuraSignInFabric.getAdvancementData();
        LOGGER.info("Looking for advancement {} in {} advancements", id, advancementData == null ? 0 : advancementData.size());
        return advancementData.stream()
                .filter(data -> data.id().toString().equalsIgnoreCase(id))
                .findFirst().orElse(new AdvancementData(new Identifier(id), null));
    }

    public static String getId(AdvancementData advancementData) {
        return getId(advancementData.id());
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
        return advancementData.displayInfo().getTitle().getString();
    }

    public static @NonNull String getDescription(AdvancementData advancementData) {
        return advancementData.displayInfo().getDescription().getString();
    }

    public static @NonNull String getDisplayName(Advancement advancement) {
        String result = "";
        AdvancementDisplay display = advancement.getDisplay();
        if (display != null)
            result = display.getTitle().getString();
        return result;
    }

    @Override
    public @NonNull String getDisplayName(String languageCode, JsonObject json) {
        return getDisplayName(languageCode, json, false);
    }

    @Override
    public @NonNull String getDisplayName(String languageCode, JsonObject json, boolean withNum) {
        Identifier deserialize = deserialize(json);
        String rewardType = I18nUtils.getTranslation(EI18nType.WORD, "reward_type_" + ERewardType.ADVANCEMENT.getCode(), languageCode);
        return String.format("%s: %s", rewardType
                , SakuraSignInFabric.getAdvancementData().stream()
                        .filter(data -> data.id().equals(deserialize))
                        .findFirst().orElse(new AdvancementData(deserialize, null))
                        .displayInfo().getTitle().getString());
    }

    public @NonNull static String getDescription(Advancement advancement) {
        String result = "";
        AdvancementDisplay display = advancement.getDisplay();
        if (display != null)
            result = advancement.getDisplay().getDescription().getString();
        return result;
    }
}

