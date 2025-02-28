package com.flechazo.sakura.rewards.impl;

import com.flechazo.sakura.enums.EI18nType;
import com.flechazo.sakura.enums.ERewardType;
import com.flechazo.sakura.rewards.RewardParser;
import com.flechazo.sakura.screen.component.IText;
import com.flechazo.sakura.util.AbstractGuiUtils;
import com.flechazo.sakura.util.Component;
import com.flechazo.sakura.util.I18nUtils;
import com.google.gson.JsonObject;
import lombok.NonNull;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

public class MessageRewardParser implements RewardParser < Component > {

    @Override
    public @NonNull Component deserialize(JsonObject json) {
        // 检查并设置必需字段的默认值
        if (!json.has("text")) {
            json.addProperty("text", "");
        }
        if (!json.has("i18nType")) {
            json.addProperty("i18nType", EI18nType.MESSAGE.name());
        }
        if (!json.has("languageCode")) {
            json.addProperty("languageCode", "en_us");
        }
        
        // 设置可选字段的默认值
        if (!json.has("color")) {
            json.addProperty("color", 0xFFFFFF);
        }
        if (!json.has("bgColor")) {
            json.addProperty("bgColor", 0);
        }
        if (!json.has("shadow")) {
            json.addProperty("shadow", false);
        }
        if (!json.has("bold")) {
            json.addProperty("bold", false);
        }
        if (!json.has("italic")) {
            json.addProperty("italic", false);
        }
        if (!json.has("underlined")) {
            json.addProperty("underlined", false);
        }
        if (!json.has("strikethrough")) {
            json.addProperty("strikethrough", false);
        }
        if (!json.has("obfuscated")) {
            json.addProperty("obfuscated", false);
        }
        
        return Component.deserialize(json);
    }

    @Override
    public JsonObject serialize(Component reward) {
        return Component.serialize(reward);
    }

    @Override
    public @NonNull String getDisplayName(String languageCode, JsonObject json) {
        return getDisplayName(languageCode, json, false);
    }

    @Override
    public @NonNull String getDisplayName(String languageCode, JsonObject json, boolean withNum) {
        return I18nUtils.getTranslation(EI18nType.WORD, "reward_type_" + ERewardType.MESSAGE.getCode(), languageCode);
    }
}