package com.flechazo.sakura.network;

import com.flechazo.sakura.SakuraSignInFabric;
import com.flechazo.sakura.config.RewardOptionDataManager;
import com.flechazo.sakura.config.ServerConfig;
import com.flechazo.sakura.enums.EI18nType;
import com.flechazo.sakura.screen.component.NotificationManager;
import com.flechazo.sakura.util.CollectionUtils;
import com.flechazo.sakura.util.Component;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 奖励配置同步数据包
 * @author Flechazo
 */
@Getter
@Setter
public class RewardOptionSyncPacket {
    private static final ConcurrentHashMap<Integer, List<RewardOptionSyncPacket>> PACKET_CACHE = new ConcurrentHashMap<>();

    private final List<RewardOptionSyncData> rewardOptionData;
    private int id;
    private int sort;
    private int total;

    public RewardOptionSyncPacket() {
        this.rewardOptionData = new ArrayList<>();
    }

    public RewardOptionSyncPacket(List<RewardOptionSyncData> rewardOptionData) {
        this.rewardOptionData = rewardOptionData;
    }

    public RewardOptionSyncPacket(PacketByteBuf buf) {
        this.id = buf.readInt();
        this.sort = buf.readInt();
        this.total = buf.readInt();
        this.rewardOptionData = new ArrayList<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            this.rewardOptionData.add(RewardOptionSyncData.decode(buf));
        }
    }

    public void write(PacketByteBuf buf) {
        buf.writeInt(this.id);
        buf.writeInt(this.sort);
        buf.writeInt(this.total);
        buf.writeInt(this.rewardOptionData.size());
        for (RewardOptionSyncData data : this.rewardOptionData) {
            RewardOptionSyncData.encode(data, buf);
        }
    }

    public List<RewardOptionSyncData> getData() {
        return this.rewardOptionData;
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        RewardOptionSyncPacket packet = new RewardOptionSyncPacket(buf);
        server.execute(() -> {
            List<RewardOptionSyncPacket> packets = handleSplitPackets(packet);
            if (CollectionUtils.isNotNullOrEmpty(packets)) {
                try {
                    // 判断是否拥有修改权限
                    if (player.hasPermissionLevel(ServerConfig.getPERMISSION_EDIT_REWARD())) {
                        // 备份 RewardOption
                        RewardOptionDataManager.backupRewardOption(false);
                        // 更新 RewardOption
                        RewardOptionDataManager.setRewardOptionData(RewardOptionDataManager.fromSyncPacketList(packets));
                        RewardOptionDataManager.saveRewardOption();

                        // 同步 RewardOption 至所有在线玩家
                        for (ServerPlayerEntity onlinePlayer : server.getPlayerManager().getPlayerList()) {
                            if (onlinePlayer.getUuid().equals(player.getUuid()))
                                continue;
                            // 仅给客户端已安装mod的玩家同步数据
                            if (!SakuraSignInFabric.getPlayerCapabilityStatus().containsKey(onlinePlayer.getUuid().toString()))
                                continue;
                            RewardOptionSyncPacket syncPacket = RewardOptionDataManager.toSyncPacket(onlinePlayer);
                            for (RewardOptionSyncPacket splitPacket : syncPacket.split()) {
                                PacketByteBuf responseBuf = PacketByteBufs.create();
                                splitPacket.write(responseBuf);
                                ServerPlayNetworking.send(onlinePlayer, ModNetworkHandler.REWARD_OPTION_SYNC, responseBuf);
                            }
                        }

                        // 发送成功通知
                        sendSuccessNotification(player);
                    }
                } catch (Exception e) {
                    // 发送失败通知
                    sendFailureNotification(player);
                    throw e;
                }
            }
        });
    }

    public static void handleClient(MinecraftClient client, RewardOptionSyncPacket packet) {
        try {
            List<RewardOptionSyncPacket> packets = handleSplitPackets(packet);
            if (CollectionUtils.isNotNullOrEmpty(packets)) {
                // 备份 RewardOption
                RewardOptionDataManager.backupRewardOption();
                // 更新 RewardOption
                RewardOptionDataManager.setRewardOptionData(RewardOptionDataManager.fromSyncPacketList(packets));
                RewardOptionDataManager.setRewardOptionDataChanged(true);
                RewardOptionDataManager.saveRewardOption();

                Component component = Component.translatable(EI18nType.MESSAGE, "reward_option_download_success");
                NotificationManager.get().addNotification(NotificationManager.Notification.ofComponentWithBlack(component));
            }
        } catch (Exception e) {
            Component component = Component.translatable(EI18nType.MESSAGE, "reward_option_download_failed");
            NotificationManager.get().addNotification(NotificationManager.Notification.ofComponentWithBlack(component).setBgColor(0x88FF5555));
            throw e;
        }
    }

    private static void sendSuccessNotification(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(true);
        ServerPlayNetworking.send(player, new Identifier(SakuraSignInFabric.MOD_ID, "reward_option_sync_result"), buf);
    }

    private static void sendFailureNotification(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(false);
        ServerPlayNetworking.send(player, new Identifier(SakuraSignInFabric.MOD_ID, "reward_option_sync_result"), buf);
    }

    public static List<RewardOptionSyncPacket> handleSplitPackets(RewardOptionSyncPacket packet) {
        List<RewardOptionSyncPacket> result = new ArrayList<>();
        if (packet.getSort() == 0) {
            result.add(packet);
        } else {
            // 获取或创建缓存列表
            List<RewardOptionSyncPacket> cache = PACKET_CACHE.computeIfAbsent(packet.getId(), k -> new ArrayList<>());
            synchronized (cache) {
                // 添加当前包到缓存
                cache.add(packet);

                // 检查是否收到所有分包
                if (cache.size() == packet.getTotal()) {
                    // 按sort排序
                    cache.sort((p1, p2) -> Integer.compare(p1.getSort(), p2.getSort()));
                    result.addAll(cache);
                    // 清理缓存
                    PACKET_CACHE.remove(packet.getId());
                }
            }
        }
        return result;
    }

    public List<RewardOptionSyncPacket> split() {
        List<RewardOptionSyncPacket> result = new ArrayList<>();
        if (CollectionUtils.isNotNullOrEmpty(this.rewardOptionData)) {
            int chunkSize = 1024;
            for (int i = 0, index = 0; i < this.rewardOptionData.size() / chunkSize + 1; i++) {
                List<RewardOptionSyncData> chunkData = new ArrayList<>();
                for (int j = 0; j < chunkSize; j++) {
                    if (index >= this.rewardOptionData.size()) break;
                    chunkData.add(this.rewardOptionData.get(index));
                    index++;
                }
                RewardOptionSyncPacket packet = new RewardOptionSyncPacket(chunkData);
                packet.id = this.id;
                packet.sort = i;
                result.add(packet);
            }
            int total = result.size();
            result.forEach(packet -> packet.total = total);
        }
        return result;
    }
}