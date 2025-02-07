package com.flechazo.rewards.impl;

import com.flechazo.rewards.RewardParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.NonNull;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class ItemRewardParser implements RewardParser < ItemStack > {

    @Override
    public @NonNull ItemStack deserialize(JsonObject json) {
        ItemStack itemStack;
        try {
            String itemId = json.get("item").getAsString();
            int count = json.get("count").getAsInt();
            count = Math.max(count, 1);
            Item item = Registries.ITEM.get(new Identifier(itemId));
            if (item == null) {
                throw new JsonParseException ("Unknown item ID: " + itemId);
            }
            itemStack = new ItemStack(item, count);

            // 如果存在NBT数据，则解析
            if (json.has("nbt")) {
                try {
                    NbtCompound nbt = StringNbtReader.parse(json.get("nbt").getAsString());
                    itemStack.setNbt(nbt);
                } catch (CommandSyntaxException e) {
                    throw new JsonParseException("Failed to parse NBT data", e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize item reward", e);
            itemStack = new ItemStack(Items.AIR);
        }
        return itemStack;
    }

    @Override
    public JsonObject serialize(ItemStack reward) {
        JsonObject json = new JsonObject();
        try {
            json.addProperty("item", getId(reward.getItem()));
            json.addProperty("count", reward.getCount());

            // 如果物品有NBT数据，则序列化
            if (reward.hasNbt()) {
                if (reward.getNbt() != null) {
                    json.addProperty("nbt", getNbtString(reward));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to serialize item reward", e);
            json.addProperty("item", getId(Items.AIR));
            json.addProperty("count", 0);
        }
        return json;
    }

    @Override
    public @NonNull String getDisplayName(JsonObject json) {
        return getDisplayName(json, false);
    }

    @Override
    public @NonNull String getDisplayName(JsonObject json, boolean withNum) {
        ItemStack itemStack = this.deserialize(json);
        return itemStack.toHoverableText().getString().replaceAll("\\[(.*)]", "$1") + (withNum ? "x" + itemStack.getCount() : "");
    }

    public @NonNull
    static String getDisplayName(ItemStack itemStack) {
        return itemStack.toHoverableText().getString().replaceAll("\\[(.*)]", "$1");
    }

    public @NonNull
    static String getDisplayName(Item item) {
        return new ItemStack(item).toHoverableText().getString().replaceAll("\\[(.*)]", "$1");
    }

    public static String getNbtString(ItemStack itemStack) {
        // Map<String, INBT> nbtMap = new HashMap<>();
        // try {
        //     if (itemStack.hasTag()) {
        //         if (itemStack.getTag() != null) {
        //             for (String key : itemStack.getTag().getAllKeys()) {
        //                 INBT inbt = itemStack.getTag().get(key);
        //                 if (inbt != null) {
        //                     nbtMap.put(key, inbt);
        //                 }
        //             }
        //         }
        //     }
        // } catch (Exception e) {
        //     LOGGER.error("Failed to get nbt string", e);
        // }
        // return GSON.toJson(nbtMap);
        String json = "";
        if (itemStack.hasNbt() && itemStack.getNbt() != null) {
            json = itemStack.getNbt().toString();
        }
        return json;
    }

    public static String getId(Item item) {
        Identifier resource = Registries.ITEM.getKey(item)
                .map (RegistryKey ::getValue)
                .orElseThrow (() -> new IllegalArgumentException ("ITEM not found in the registry."));
        if (resource == null) return "minecraft:air";
        else return resource.toString();
    }

    public static String getId(ItemStack itemStack) {
        return getId(itemStack.getItem()) + getNbtString(itemStack);
    }

    public static Item getItem(String id) {
        String resourceId = id;
        if (id.contains("{") && id.endsWith("}")) resourceId = resourceId.substring(0, id.indexOf("{"));
        return Registries.ITEM.get(new Identifier(resourceId));
    }

    public static ItemStack getItemStack(String id) {
        ItemStack result = new ItemStack(Items.AIR);
        try {
            result = getItemStack(id, false);
        } catch (CommandSyntaxException ignored) {
        }
        return result;
    }

    public static ItemStack getItemStack(String id, boolean throwException) throws CommandSyntaxException {
        Item item = getItem(id);
        if (item == null) {
            throw new RuntimeException("Unknown item ID: " + id);
        }
        ItemStack itemStack = new ItemStack(item);
        if (id.contains("{") && id.endsWith("}") && !id.endsWith("{}")) {
            try {
                String nbtString = id.substring(id.indexOf("{"));
                NbtCompound nbt = StringNbtReader.parse(nbtString);
                itemStack.setNbt(nbt);
            } catch (Exception e) {
                if (throwException) throw e;
                LOGGER.error("Failed to parse NBT data", e);
            }
        }
        return itemStack;
    }
}
