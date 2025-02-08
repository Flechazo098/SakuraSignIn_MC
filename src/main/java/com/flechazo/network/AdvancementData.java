package com.flechazo.network;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Objects;

/**
 * 进度信息
 */
public record AdvancementData(Identifier id, AdvancementDisplay displayInfo) {
    public AdvancementData (Identifier id, AdvancementDisplay displayInfo) {
        this.id = id;
        this.displayInfo = Objects.requireNonNullElseGet (displayInfo, AdvancementData :: emptyDisplayInfo);
    }

    public static AdvancementData fromAdvancement (Advancement advancement) {
        AdvancementDisplay displayInfo = advancement.getDisplay ();
        return new AdvancementData (advancement.getId (), Objects.requireNonNullElseGet (displayInfo, () -> createDisplayInfo (advancement.getId ().toString ())));
    }

    public static AdvancementData readFromBuffer (PacketByteBuf buffer) {
        Identifier id = buffer.readIdentifier ();
        return new AdvancementData (id, AdvancementDisplay.fromPacket (buffer));
    }

    public static AdvancementDisplay emptyDisplayInfo () {
        return createDisplayInfo ("");
    }

    public static AdvancementDisplay createDisplayInfo (String title) {
        return createDisplayInfo (title, "", new ItemStack (Items.AIR));
    }

    public static AdvancementDisplay createDisplayInfo (String title, String description) {
        return createDisplayInfo (title, description, new ItemStack (Items.AIR));
    }

    public static AdvancementDisplay createDisplayInfo (String title, String description, ItemStack itemStack) {
        return new AdvancementDisplay (itemStack
                , Text.literal (title), Text.literal (description)
                , new Identifier (""), AdvancementFrame.TASK
                , false, false, false);
    }

    public void writeToBuffer (PacketByteBuf buffer) {
        buffer.writeIdentifier (id);
        displayInfo.toPacket (buffer);
    }

}