package com.flechazo.capability;

import com.flechazo.event.ServerEventHandler;
import com.flechazo.network.PlayerDataSyncPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import com.flechazo.network.ModNetworkHandler;

/**
 * 玩家签到数据能力
 * @author Flechazo
 */
public class PlayerSignInDataCapability {
    /**
     * 获取玩家签到数据
     *
     * @param player 玩家实体
     * @return 玩家的签到数据
     */
    public static IPlayerSignInData getData(PlayerEntity player) {
        return player.getComponent(ServerEventHandler.PLAYER_DATA);
    }

    /**
     * 设置玩家签到数据
     *
     * @param player 玩家实体
     * @param data   玩家签到数据
     */
    public static void setData(PlayerEntity player, IPlayerSignInData data) {
        PlayerSignInData playerData = player.getComponent(ServerEventHandler.PLAYER_DATA);
        if (playerData != null) {
            playerData.copyFrom(data);
        } else {
            throw new IllegalArgumentException("Player data capability is missing.");
        }
    }

    /**
     * 同步玩家签到数据到客户端
     */
    public static void syncPlayerData(ServerPlayerEntity player) {
        // 创建自定义包并发送到客户端
        for (PlayerDataSyncPacket syncPacket : new PlayerDataSyncPacket(player.getUuid(), getData(player)).split()) {
            PacketByteBuf buf = PacketByteBufs.create();
            syncPacket.toBytes(buf);
            ServerPlayNetworking.send(player, ModNetworkHandler.PLAYER_SIGN_IN_DATA_SYNC, buf);
        }
    }
}