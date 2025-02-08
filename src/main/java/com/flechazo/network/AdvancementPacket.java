package com.flechazo.network;

import com.flechazo.util.CollectionUtils;
import lombok.Getter;
import net.minecraft.advancement.Advancement;
import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class AdvancementPacket extends SplitPacket {
    // 存储要传输的AdvancementData对象
    private final List<AdvancementData> advancements;

    public AdvancementPacket(Collection<Advancement> advancements) {
        super();
        this.advancements = advancements.stream()
                .map(AdvancementData::fromAdvancement)
                .collect(Collectors.toList());
    }

    public AdvancementPacket(PacketByteBuf buf) {
        super(buf);
        int size = buf.readVarInt();
        List<AdvancementData> advancements = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            advancements.add(AdvancementData.readFromBuffer(buf));
        }
        this.advancements = advancements;
    }

    private AdvancementPacket(List<AdvancementPacket> packets) {
        super();
        this.advancements = new ArrayList<>();
        this.advancements.addAll(packets.stream().flatMap(packet -> packet.getAdvancements().stream()).toList());
    }

    public static void handle(AdvancementPacket packet) {
        List<AdvancementPacket> packets = SplitPacket.handle(packet);
        if (CollectionUtils.isNotNullOrEmpty(packets)) {
            ClientProxy.handleAdvancement(new AdvancementPacket(packets));
        }
    }

    public void toBytes(PacketByteBuf buf) {
        super.toBytes(buf);
        buf.writeVarInt(this.advancements.size());
        for (AdvancementData data : this.advancements) {
            data.writeToBuffer(buf);
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
        for (int i = 0, index = 0; i < advancements.size() / getChunkSize() + 1; i++) {
            AdvancementPacket packet = new AdvancementPacket(new ArrayList<Advancement>());
            for (int j = 0; j < getChunkSize(); j++) {
                if (index >= advancements.size()) break;
                packet.advancements.add(this.advancements.get(index));
                index++;
            }
            packet.setId(this.getId());
            packet.setSort(i);
            result.add(packet);
        }
        result.forEach(packet -> packet.setTotal(result.size()));
        return result;
    }

}