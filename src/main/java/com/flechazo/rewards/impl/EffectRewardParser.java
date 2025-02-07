package com.flechazo.rewards.impl;

import com.flechazo.enums.ERewardType;
import com.flechazo.rewards.RewardParser;
import com.flechazo.util.I18nUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.NonNull;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class EffectRewardParser implements RewardParser < StatusEffectInstance > {
    @Override
    public @NonNull StatusEffectInstance deserialize(JsonObject json) {
        StatusEffectInstance StatusEffectInstance;
        try {
            String effectId = json.get("effect").getAsString();
            int duration = json.get("duration").getAsInt();
            int amplifier = json.get("amplifier").getAsInt();

            StatusEffect effect = Registries.STATUS_EFFECT.get(new Identifier (effectId));

            if (effect == null) {
                throw new JsonParseException ("Unknown potion effect ID: " + effectId);
            }
            StatusEffectInstance = new StatusEffectInstance(effect, duration, amplifier);
        } catch (Exception e) {
            LOGGER.error("Failed to parse effect reward", e);
            StatusEffectInstance = new StatusEffectInstance(StatusEffects.LUCK, 0, 0);
        }
        return StatusEffectInstance;
    }

    @Override
    public JsonObject serialize(StatusEffectInstance reward) {
        JsonObject json = new JsonObject();
        json.addProperty("effect", Registries.STATUS_EFFECT.getKey(reward.getEffectType()).toString());
        json.addProperty("duration", reward.getDuration());
        json.addProperty("amplifier", reward.getAmplifier());
        return json;
    }

    @Override
    public @NonNull String getDisplayName(JsonObject json) {
        return getDisplayName(json, false);
    }

    @Override
    public @NonNull String getDisplayName(JsonObject json, boolean withNum) {
        return String.format("%s: %s", I18nUtils.get(String.format("reward.sakura_sign_in.reward_type_%s", ERewardType.EFFECT.getCode()))
                , this.deserialize(json).getEffectType().getName().getString());
    }

    public static @NonNull String getDisplayName(StatusEffectInstance instance) {
        return getDisplayName(instance.getEffectType());
    }

    public static @NonNull String getDisplayName(StatusEffect effect) {
        return effect.getName().getString().replaceAll("\\[(.*)]", "$1");
    }

    public static String getId(StatusEffectInstance instance) {
        return getId(instance.getEffectType()) + " " + instance.getDuration() + " " + instance.getAmplifier();
    }

    public static String getId(StatusEffect effect) {
        Identifier resource = Registries.STATUS_EFFECT.getKey(effect)
                .map(RegistryKey ::getValue)  // 提取 RegistryKey 中的 Identifier
                .orElseThrow(() -> new IllegalArgumentException("Effect not found in the registry."));  // 如果为空，抛出异常
        if (resource == null) return "minecraft:luck";
        else return resource.toString();
    }

    public static StatusEffect getEffect(String id) {
        String resourceId = id;
        if (id.contains(" ") && id.split(" ").length == 3) resourceId = resourceId.substring(0, id.indexOf(" "));
        return Registries.STATUS_EFFECT.get(new Identifier(resourceId));
    }

    public static StatusEffectInstance getMobEffectInstance(String id, int duration, int amplifier) {
        id = id.split(" ")[0] + " " + duration + " " + amplifier;
        return getMobEffectInstance(id);
    }

    public static StatusEffectInstance getMobEffectInstance(String id) {
        StatusEffectInstance result = new StatusEffectInstance(StatusEffects.LUCK);
        try {
            result = getMobEffectInstance(id, false);
        } catch (CommandSyntaxException ignored) {
        }
        return result;
    }

    public static StatusEffectInstance getMobEffectInstance(String id, boolean throwException) throws CommandSyntaxException {
        StatusEffect effect = getEffect(id);
        if (effect == null) {
            throw new RuntimeException("Unknown effect ID: " + id);
        }
        int amplifier = 0;
        int duration = 0;
        if (id.contains(" ") && id.split(" ").length == 3) {
            try {
                String[] split = id.split(" ");
                amplifier = Integer.parseInt(split[1]);
                duration = Integer.parseInt(split[2]);
            } catch (Exception e) {
                if (throwException) throw e;
                LOGGER.error("Failed to parse Effect data", e);
            }
        }
        return new StatusEffectInstance(effect, duration, amplifier);
    }
}
