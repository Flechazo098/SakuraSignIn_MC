package com.flechazo.capability;

import com.flechazo.network.ModNetworkHandler;
import com.flechazo.network.PlayerDataSyncPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 玩家签到数据能力
 */
public class PlayerSignInDataCapability {
    // 定义 Capability 实例
    public static Capability<IPlayerSignInData> PLAYER_DATA = CapabilityManager.get(new CapabilityToken<>() {
    });

    /**
     * 获取玩家签到数据
     *
     * @param player 玩家实体
     * @return 玩家的签到数据
     */
    public static IPlayerSignInData getData(PlayerEntity player) {
        return player.getCapability(PLAYER_DATA).orElseThrow(() -> new IllegalArgumentException("Player data capability is missing."));
    }

    public static LazyOptional<IPlayerSignInData> getDataOptional(ServerPlayer player) {
        return player.getCapability(PLAYER_DATA);
    }

    /**
     * 设置玩家签到数据
     *
     * @param player 玩家实体
     * @param data   玩家签到数据
     */
    public static void setData(PlayerEntity player, IPlayerSignInData data) {
        LazyOptional<IPlayerSignInData> optional = player.getCapability(PLAYER_DATA);
        if (optional.isPresent()) {
            optional.ifPresent(capability -> capability.copyFrom(data));
        } else {
            throw new IllegalArgumentException("Player data capability is missing.");
        }
    }

    /**
     * 同步玩家签到数据到客户端
     */
    public static void syncPlayerData(ServerPlayerEntity player) {
        // 创建自定义包并发送到客户端
        PlayerDataSyncPacket packet = new PlayerDataSyncPacket(player.getUuid(), PlayerSignInDataCapability.getData(player));
        for (PlayerDataSyncPacket syncPacket : packet.split()) {
            ModNetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), syncPacket);
        }
    }
}