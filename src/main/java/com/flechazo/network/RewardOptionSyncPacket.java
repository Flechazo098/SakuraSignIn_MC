package com.flechazo.network;

import com.flechazo.config.RewardOptionDataManager;
import com.flechazo.enums.ERewardRule;
import com.flechazo.rewards.Reward;
import com.flechazo.rewards.RewardList;
import lombok.Getter;
import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 奖励配置同步数据包
 * @author Flechazo
 */
@Getter
public class RewardOptionSyncPacket extends SplitPacket {
    private final List<RewardOptionSyncData> data = new ArrayList<>();

    public RewardOptionSyncPacket() {
        super();
        // 基础奖励
        RewardList baseRewards = RewardOptionDataManager.getRewardOptionData().getBaseRewards();
        if (baseRewards != null) {
            for (Reward reward : baseRewards) {
                data.add(new RewardOptionSyncData(ERewardRule.BASE_REWARD, "base", reward));
            }
        }

        // 连续签到奖励
        Map<String, RewardList> continuousRewards = RewardOptionDataManager.getRewardOptionData().getContinuousRewards();
        if (continuousRewards != null) {
            for (Map.Entry<String, RewardList> entry : continuousRewards.entrySet()) {
                for (Reward reward : entry.getValue()) {
                    data.add(new RewardOptionSyncData(ERewardRule.CONTINUOUS_REWARD, entry.getKey(), reward));
                }
            }
        }

        // 周期签到奖励
        Map<String, RewardList> cycleRewards = RewardOptionDataManager.getRewardOptionData().getCycleRewards();
        if (cycleRewards != null) {
            for (Map.Entry<String, RewardList> entry : cycleRewards.entrySet()) {
                for (Reward reward : entry.getValue()) {
                    data.add(new RewardOptionSyncData(ERewardRule.CYCLE_REWARD, entry.getKey(), reward));
                }
            }
        }

        // 年度签到奖励
        Map<String, RewardList> yearRewards = RewardOptionDataManager.getRewardOptionData().getYearRewards();
        if (yearRewards != null) {
            for (Map.Entry<String, RewardList> entry : yearRewards.entrySet()) {
                for (Reward reward : entry.getValue()) {
                    data.add(new RewardOptionSyncData(ERewardRule.YEAR_REWARD, entry.getKey(), reward));
                }
            }
        }

        // 月度签到奖励
        Map<String, RewardList> monthRewards = RewardOptionDataManager.getRewardOptionData().getMonthRewards();
        if (monthRewards != null) {
            for (Map.Entry<String, RewardList> entry : monthRewards.entrySet()) {
                for (Reward reward : entry.getValue()) {
                    data.add(new RewardOptionSyncData(ERewardRule.MONTH_REWARD, entry.getKey(), reward));
                }
            }
        }

        // 周度签到奖励
        Map<String, RewardList> weekRewards = RewardOptionDataManager.getRewardOptionData().getWeekRewards();
        if (weekRewards != null) {
            for (Map.Entry<String, RewardList> entry : weekRewards.entrySet()) {
                for (Reward reward : entry.getValue()) {
                    data.add(new RewardOptionSyncData(ERewardRule.WEEK_REWARD, entry.getKey(), reward));
                }
            }
        }

        // 日期时间签到奖励
        Map<String, RewardList> dateTimeRewards = RewardOptionDataManager.getRewardOptionData().getDateTimeRewards();
        if (dateTimeRewards != null) {
            for (Map.Entry<String, RewardList> entry : dateTimeRewards.entrySet()) {
                for (Reward reward : entry.getValue()) {
                    data.add(new RewardOptionSyncData(ERewardRule.DATE_TIME_REWARD, entry.getKey(), reward));
                }
            }
        }

        // 累计签到奖励
        Map<String, RewardList> cumulativeRewards = RewardOptionDataManager.getRewardOptionData().getCumulativeRewards();
        if (cumulativeRewards != null) {
            for (Map.Entry<String, RewardList> entry : cumulativeRewards.entrySet()) {
                for (Reward reward : entry.getValue()) {
                    data.add(new RewardOptionSyncData(ERewardRule.CUMULATIVE_REWARD, entry.getKey(), reward));
                }
            }
        }

        // 随机签到奖励
        Map<String, RewardList> randomRewards = RewardOptionDataManager.getRewardOptionData().getRandomRewards();
        if (randomRewards != null) {
            for (Map.Entry<String, RewardList> entry : randomRewards.entrySet()) {
                for (Reward reward : entry.getValue()) {
                    data.add(new RewardOptionSyncData(ERewardRule.RANDOM_REWARD, entry.getKey(), reward));
                }
            }
        }
    }

    public RewardOptionSyncPacket(PacketByteBuf buf) {
        super(buf);
    }

    public static void encode(RewardOptionSyncPacket packet, PacketByteBuf buffer) {
        packet.toBytes(buffer);
        buffer.writeInt(packet.data.size());
        for (RewardOptionSyncData data : packet.data) {
            RewardOptionSyncData.encode(data, buffer);
        }
    }

    public static RewardOptionSyncPacket decode(PacketByteBuf buffer) {
        RewardOptionSyncPacket packet = new RewardOptionSyncPacket(buffer);
        if (buffer.readableBytes() > 0) {
            int size = buffer.readInt();
            for (int i = 0; i < size; i++) {
                packet.data.add(RewardOptionSyncData.decode(buffer));
            }
        }
        return packet;
    }

    @Override
    public void toBytes(PacketByteBuf buf) {
        super.toBytes(buf);
    }

    @Override
    public int getChunkSize() {
        return 128;
    }

    public List<RewardOptionSyncPacket> Chopping() {
        List<RewardOptionSyncPacket> result = new ArrayList<>();
        int totalChunks = (data.size() + getChunkSize() - 1) / getChunkSize();

        for (int i = 0; i < totalChunks; i++) {
            RewardOptionSyncPacket packet = new RewardOptionSyncPacket();
            int startIndex = i * getChunkSize();
            int endIndex = Math.min(startIndex + getChunkSize(), data.size());
            
            for (int j = startIndex; j < endIndex; j++) {
                packet.data.add(this.data.get(j));
            }
            
            packet.setId(this.getId());
            packet.setSort(i);
            packet.setTotal(totalChunks);
            result.add(packet);
        }
        
        return result;
    }
}