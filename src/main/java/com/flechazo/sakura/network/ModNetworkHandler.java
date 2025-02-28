package com.flechazo.sakura.network;

import com.flechazo.sakura.SakuraSignInFabric;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * 网络处理器
 * @author Flechazo
 */
public class ModNetworkHandler {
    public static final Identifier SIGN_IN = new Identifier(SakuraSignInFabric.MOD_ID, "sign_in");
    public static final Identifier ADVANCEMENT = new Identifier(SakuraSignInFabric.MOD_ID, "advancement");
    public static final Identifier CLIENT_CONFIG_SYNC = new Identifier(SakuraSignInFabric.MOD_ID, "client_config_sync");
    public static final Identifier PLAYER_SIGN_IN_DATA_SYNC = new Identifier(SakuraSignInFabric.MOD_ID, "player_sign_in_data_sync");
    public static final Identifier REWARD_OPTION_SYNC = new Identifier(SakuraSignInFabric.MOD_ID, "reward_option_sync");
    public static final Identifier ITEM_STACK = new Identifier(SakuraSignInFabric.MOD_ID, "item_stack");
    public static final Identifier SERVER_TIME_SYNC = new Identifier(SakuraSignInFabric.MOD_ID, "server_time_sync");
    public static final Identifier REWARD_OPTION_RECEIVED = new Identifier(SakuraSignInFabric.MOD_ID, "reward_option_received");

    /**
     * 注册客户端到服务端(C2S)的数据包
     */
    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(SIGN_IN, (server, player, handler, buf, responseSender) -> {
            SignInPacket packet = new SignInPacket(buf);
            server.execute(() -> SignInPacket.handle(packet, player));
        });

        ServerPlayNetworking.registerGlobalReceiver(CLIENT_CONFIG_SYNC, (server, player, handler, buf, responseSender) -> {
            ClientConfigSyncPacket packet = new ClientConfigSyncPacket(buf);
            server.execute(() -> ClientConfigSyncPacket.handleServer(packet, player));
        });

        ServerPlayNetworking.registerGlobalReceiver(ITEM_STACK, (server, player, handler, buf, responseSender) -> {
            ItemStackPacket packet = new ItemStackPacket(buf);
            server.execute(() -> ItemStackPacket.handle(packet, player));
        });
    }

    /**
     * 注册服务端到客户端(S2C)的数据包
     */
    @Environment(EnvType.CLIENT)
    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ADVANCEMENT, (client, handler, buf, responseSender) -> {
            AdvancementPacket packet = new AdvancementPacket(buf);
            client.execute(() -> AdvancementPacket.handle(packet));
        });

        ClientPlayNetworking.registerGlobalReceiver(PLAYER_SIGN_IN_DATA_SYNC, (client, handler, buf, responseSender) -> {
            PlayerDataSyncPacket packet = new PlayerDataSyncPacket(buf);
            client.execute(() -> PlayerDataSyncPacket.handle(packet));
        });

        ClientPlayNetworking.registerGlobalReceiver(REWARD_OPTION_SYNC, (client, handler, buf, responseSender) -> {
            RewardOptionSyncPacket packet = new RewardOptionSyncPacket(buf);
            client.execute(() -> RewardOptionSyncPacket.handleClient(client, packet));
        });

        // 注册服务器时间同步数据包接收器
        ClientPlayNetworking.registerGlobalReceiver(SERVER_TIME_SYNC, (client, handler, buf, responseSender) -> {
            ServerTimeSyncPacket packet = new ServerTimeSyncPacket(buf);
            ServerTimeSyncPacket.handle(packet, client, responseSender);
        });

        // 注册奖励选项数据接收通知数据包接收器
        ClientPlayNetworking.registerGlobalReceiver(REWARD_OPTION_RECEIVED, (client, handler, buf, responseSender) -> {
            RewardOptionDataReceivedNotice packet = new RewardOptionDataReceivedNotice(buf);
            client.execute(() -> RewardOptionDataReceivedNotice.handle(packet));
        });

        ClientPlayNetworking.registerGlobalReceiver(CLIENT_CONFIG_SYNC, (client, handler, buf, responseSender) -> {
            ClientConfigSyncPacket packet = new ClientConfigSyncPacket(buf);
            client.execute(() -> ClientConfigSyncPacket.handleClient(packet, client));
        });
    }

    /**
     * 发送服务器时间同步数据包到客户端
     * @param player 目标玩家
     */
    public static void sendServerTimeSyncPacket(ServerPlayerEntity player) {
        ServerTimeSyncPacket packet = new ServerTimeSyncPacket();
        PacketByteBuf buf = PacketByteBufs.create();
        packet.toBytes(buf);
        ServerPlayNetworking.send(player, SERVER_TIME_SYNC, buf);
    }

    /**
     * 注册所有的网络通道
     */
    public static void registerPackets() {
        registerC2SPackets();
    }
}