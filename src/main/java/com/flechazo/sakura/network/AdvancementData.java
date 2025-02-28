package com.flechazo.sakura.network;

import com.flechazo.sakura.util.Component;
import lombok.NonNull;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * 进度信息
 */
public record AdvancementData(@NonNull Identifier id, @NonNull AdvancementDisplay displayInfo) {
    private static final Logger LOGGER = LogManager.getLogger();

    public AdvancementData(Identifier id, AdvancementDisplay displayInfo) {
        this.id = Objects.requireNonNull(id, "Advancement ID cannot be null");
        this.displayInfo = Objects.requireNonNullElseGet(displayInfo, () -> {
            LOGGER.debug("Creating empty display info for advancement: {}", id);
            return emptyDisplayInfo();
        });
        LOGGER.debug("Created advancement data: id={}, title={}", id, this.displayInfo.getTitle().getString());
    }

    public static AdvancementData fromAdvancement(Advancement advancement) {
        if (advancement == null) {
            LOGGER.error("Attempted to create AdvancementData from null advancement");
            return new AdvancementData(new Identifier("unknown"), emptyDisplayInfo());
        }

        Identifier id = advancement.getId();
        AdvancementDisplay displayInfo = advancement.getDisplay();
        LOGGER.debug("Converting advancement to data: id={}", id);

        if (displayInfo == null) {
            LOGGER.debug("Creating default display info for advancement: {}", id);
            displayInfo = createDisplayInfo(id.toString());
        }

        return new AdvancementData(id, displayInfo);
    }

    public static AdvancementData readFromBuffer(PacketByteBuf buffer) {
        try {
            Identifier id = buffer.readIdentifier();
            LOGGER.debug("Reading advancement data from buffer: id={}", id);
            AdvancementDisplay displayInfo = AdvancementDisplay.fromPacket(buffer);
            return new AdvancementData(id, displayInfo);
        } catch (Exception e) {
            LOGGER.error("Failed to read advancement data from buffer", e);
            return new AdvancementData(new Identifier("unknown"), emptyDisplayInfo());
        }
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
        return new AdvancementDisplay(
            Objects.requireNonNullElse(itemStack, new ItemStack(Items.AIR)),
            Component.literal(title).toTextComponent(),
            Component.literal(description).toTextComponent(),
            new Identifier(""),
            AdvancementFrame.TASK,
            false,
            false,
            false
        );
    }

    public void writeToBuffer(PacketByteBuf buffer) {
        try {
            LOGGER.debug("Writing advancement data to buffer: id={}", id);
            buffer.writeIdentifier(id);
            displayInfo.toPacket(buffer);
        } catch (Exception e) {
            LOGGER.error("Failed to write advancement data to buffer: id={}", id, e);
        }
    }
}