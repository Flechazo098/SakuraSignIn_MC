package com.flechazo.network;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * 进度信息
 */
public class AdvancementData {
    private final Identifier id;
    private final AdvancementDisplay displayInfo;

    public AdvancementData(Identifier id, AdvancementDisplay displayInfo) {
        this.id = id;
        if (displayInfo == null) {
            this.displayInfo = emptyDisplayInfo();
        } else {
            this.displayInfo = displayInfo;
        }
    }

    public static AdvancementData fromAdvancement(Advancement advancement) {
        AdvancementDisplay displayInfo = advancement.getDisplay();
        if (displayInfo == null) {
            return new AdvancementData(advancement.getId(), createDisplayInfo(advancement.getId().toString()));
        }
        return new AdvancementData(advancement.getId(), displayInfo);
    }

    public static AdvancementData readFromBuffer(PacketByteBuf buffer) {
        Identifier id = buffer.readIdentifier();
        return new AdvancementData(id, AdvancementDisplay.fromPacket(buffer));
    }

    public static AdvancementDisplay emptyDisplayInfo() {
        return createDisplayInfo("");
    }

    public static AdvancementDisplay createDisplayInfo(String title) {
        return createDisplayInfo(title, "", new ItemStack(Items.AIR));
    }

    public static AdvancementDisplay createDisplayInfo(String title, String description) {
        return createDisplayInfo(title, description, new ItemStack(Items.AIR));
    }

    public static AdvancementDisplay createDisplayInfo(String title, String description, ItemStack itemStack) {
        return new AdvancementDisplay(itemStack
                , Text.literal(title), Text.literal(description)
                , new Identifier(""), AdvancementFrame.TASK
                , false, false, false);
    }

    public void writeToBuffer(PacketByteBuf buffer) {
        buffer.writeIdentifier(id);
        displayInfo.toPacket(buffer);
    }

    public Identifier getId() {
        return id;
    }

    public AdvancementDisplay getDisplayInfo() {
        return displayInfo;
    }
}