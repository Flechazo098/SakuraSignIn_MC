package com.flechazo.sakura.network;

import com.flechazo.sakura.util.CollectionUtils;
import lombok.Getter;
import net.minecraft.advancement.Advancement;
import net.minecraft.network.PacketByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class AdvancementPacket extends SplitPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdvancementPacket.class);

    // 存储要传输的AdvancementData对象
    private final List<AdvancementData> advancements;

    public AdvancementPacket(Collection<Advancement> advancements) {
        super();
        LOGGER.debug("Creating advancement packet from {} advancements", advancements.size());
        this.advancements = new ArrayList<>();
        for (Advancement advancement : advancements) {
            if (advancement != null) {
                try {
                    this.advancements.add(AdvancementData.fromAdvancement(advancement));
                } catch (Exception e) {
                    LOGGER.error("Failed to convert advancement: {}", advancement.getId(), e);
                }
            }
        }
        LOGGER.debug("Created advancement packet with {} valid advancements", this.advancements.size());
    }

    public AdvancementPacket(PacketByteBuf buf) {
        super(buf);
        int size = buf.readVarInt();
        LOGGER.debug("Reading advancement packet with {} advancements", size);
        List<AdvancementData> advancements = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            try {
                advancements.add(AdvancementData.readFromBuffer(buf));
            } catch (Exception e) {
                LOGGER.error("Failed to read advancement data at index {}", i, e);
            }
        }
        this.advancements = advancements;
        LOGGER.debug("Read advancement packet with {} valid advancements", this.advancements.size());
    }

    private AdvancementPacket(List<AdvancementPacket> packets) {
        super();
        LOGGER.debug("Merging {} advancement packets", packets.size());
        this.advancements = new ArrayList<>();
        for (AdvancementPacket packet : packets) {
            if (packet != null && packet.getAdvancements() != null) {
                this.advancements.addAll(packet.getAdvancements());
            }
        }
        LOGGER.debug("Merged advancement packet with {} total advancements", this.advancements.size());
    }

    public static void handle(AdvancementPacket packet) {
        LOGGER.debug("Handling advancement packet with {} advancements", packet.getAdvancements().size());
        List<AdvancementPacket> packets = SplitPacket.handle(packet);
        if (CollectionUtils.isNotNullOrEmpty(packets)) {
            LOGGER.debug("Merging {} advancement packets", packets.size());
            ClientProxy.handleAdvancement(new AdvancementPacket(packets));
        } else {
            LOGGER.debug("No complete advancement packets available yet");
        }
    }

    public void toBytes(PacketByteBuf buf) {
        super.toBytes(buf);
        LOGGER.debug("Writing advancement packet with {} advancements", this.advancements.size());
        buf.writeVarInt(this.advancements.size());
        for (AdvancementData data : this.advancements) {
            try {
                data.writeToBuffer(buf);
            } catch (Exception e) {
                LOGGER.error("Failed to write advancement data: {}", data.id(), e);
            }
        }
    }

    @Override
    public int getChunkSize() {
        return 1024;
    }

    /**
     * 将数据包拆分为多个小包
     */
    public List<AdvancementPacket> split() {
        List<AdvancementPacket> result = new ArrayList<>();
        if (advancements.isEmpty()) {
            LOGGER.warn("Attempting to split empty advancement packet");
            return result;
        }

        int totalChunks = (advancements.size() + getChunkSize() - 1) / getChunkSize(); // 向上取整
        LOGGER.debug("Splitting {} advancements into {} chunks (chunk size: {})", advancements.size(), totalChunks, getChunkSize());
        
        for (int i = 0, index = 0; i < totalChunks; i++) {
            AdvancementPacket packet = new AdvancementPacket(new ArrayList<Advancement>());
            int chunkSize = Math.min(getChunkSize(), advancements.size() - index);
            for (int j = 0; j < chunkSize; j++) {
                packet.advancements.add(this.advancements.get(index));
                index++;
            }
            packet.setId(this.getId());
            packet.setSort(i);
            result.add(packet);
            LOGGER.debug("Created chunk {}/{} with {} advancements", i + 1, totalChunks, packet.advancements.size());
        }
        result.forEach(packet -> packet.setTotal(result.size()));
        return result;
    }

}