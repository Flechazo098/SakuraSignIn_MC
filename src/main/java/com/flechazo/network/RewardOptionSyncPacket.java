package com.flechazo.network;

import com.flechazo.rewards.RewardManager;
import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * 奖励配置同步数据包
 * @author Flechazo
 */
public class RewardOptionSyncPacket extends SplitPacket {
    private final List<RewardOptionSyncData> data = new ArrayList<>();

    public RewardOptionSyncPacket() {
        for (int i = 0; i < RewardManager.getRewardOptions().size(); i++) {
            data.add(new RewardOptionSyncData(RewardManager.getRewardOptions().get(i).getRule(), RewardManager.getRewardOptions().get(i).getKey(), RewardManager.getRewardOptions().get(i).getReward()));
        }
    }

    public static void encode(RewardOptionSyncPacket packet, PacketByteBuf buffer) {
        buffer.writeInt(packet.data.size());
        for (RewardOptionSyncData data : packet.data) {
            RewardOptionSyncData.encode(data, buffer);
        }
    }

    public static RewardOptionSyncPacket decode(PacketByteBuf buffer) {
        RewardOptionSyncPacket packet = new RewardOptionSyncPacket();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            packet.data.add(RewardOptionSyncData.decode(buffer));
        }
        return packet;
    }

    public List<RewardOptionSyncData> getData() {
        return data;
    }

    @Override
    public int getChunkSize() {
        return 1024;
    }

    public List<RewardOptionSyncPacket> Chopping () {
        List<RewardOptionSyncPacket> result = new ArrayList<>();
        for (int i = 0, index = 0; i < data.size() / getChunkSize() + 1; i++) {
            RewardOptionSyncPacket packet = new RewardOptionSyncPacket();
            for (int j = 0; j < getChunkSize(); j++) {
                if (index >= data.size()) break;
                packet.data.add(this.data.get(index));
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