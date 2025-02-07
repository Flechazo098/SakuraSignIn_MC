package com.flechazo.rewards.impl;

import com.flechazo.enums.ERewardType;
import com.flechazo.rewards.RewardParser;
import com.flechazo.screen.component.IText;
import com.flechazo.util.AbstractGuiUtils;
import com.flechazo.util.I18nUtils;
import com.google.gson.JsonObject;
import lombok.NonNull;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

public class MessageRewardParser implements RewardParser < MutableText > {

    @Override
    public @NonNull MutableText deserialize(JsonObject json) {
        MutableText message;
        try {
            message = Text.literal(json.get("contents").getAsString());
            JsonObject styleJson = json.getAsJsonObject("style");
            Style style = Style.EMPTY;
            if (styleJson.has("color"))
                style.withColor(TextColor.fromRgb(styleJson.get("color").getAsInt()));
            style.withBold(styleJson.get("bold").getAsBoolean());
            style.withItalic(styleJson.get("italic").getAsBoolean());
            style.withUnderline(styleJson.get("underlined").getAsBoolean());
            style.withStrikethrough(styleJson.get("strikethrough").getAsBoolean());
            style.withObfuscated(styleJson.get("obfuscated").getAsBoolean());
            style.withFont(new Identifier(styleJson.get("font").getAsString()));
            message.setStyle(style);
        } catch (Exception e) {
            LOGGER.error("Failed to parse message reward", e);
            message = AbstractGuiUtils.textToComponent(IText.literal("Failed to parse message reward").setColor(0xFFFF0000));
        }
        return message;
    }

    @Override
    public JsonObject serialize(MutableText reward) {
        JsonObject result = new JsonObject();
        JsonObject styleJson = new JsonObject();
        Style style = reward.getStyle();
        if (style.getColor() != null) {
            styleJson.addProperty("color", style.getColor().getRgb());
        }
        styleJson.addProperty("bold", style.isBold());
        styleJson.addProperty("italic", style.isItalic());
        styleJson.addProperty("underlined", style.isUnderlined());
        styleJson.addProperty("strikethrough", style.isStrikethrough());
        styleJson.addProperty("obfuscated", style.isObfuscated());
        styleJson.addProperty("font", style.getFont().toString());
        result.addProperty("contents", reward.getString());
        result.add("style", styleJson);
        return result;
    }

    @Override
    public @NonNull String getDisplayName(JsonObject json) {
        return getDisplayName(json, false);
    }

    @Override
    public @NonNull String getDisplayName(JsonObject json, boolean withNum) {
        return I18nUtils.get(String.format("reward.sakura_sign_in.reward_type_%s", ERewardType.MESSAGE.getCode()));
    }
}
