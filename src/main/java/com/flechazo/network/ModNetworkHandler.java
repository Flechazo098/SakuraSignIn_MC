package com.flechazo.network;

import com.flechazo.SakuraSignInFabric;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

/**
 * 网络处理器
 * @author Flechazo
 */
public class ModNetworkHandler {
    public static final Identifier SIGN_IN = new Identifier(SakuraSignInFabric.MOD_ID, "sign_in");
    public static final Identifier ADVANCEMENT = new Identifier(SakuraSignInFabric.MOD_ID, "advancement");
    public static final Identifier CLIENT_CONFIG_SYNC = new Identifier(SakuraSignInFabric.MOD_ID, "client_config_sync");
    public static final Identifier CLIENT_MOD_LOADED = new Identifier(SakuraSignInFabric.MOD_ID, "client_mod_loaded");
    public static final Identifier DOWNLOAD_REWARD_OPTION = new Identifier(SakuraSignInFabric.MOD_ID, "download_reward_option");
    public static final Identifier PLAYER_SIGN_IN_DATA_SYNC = new Identifier(SakuraSignInFabric.MOD_ID, "player_sign_in_data_sync");
    public static final Identifier REWARD_OPTION_SYNC = new Identifier(SakuraSignInFabric.MOD_ID, "reward_option_sync");
    public static final Identifier ITEM_STACK = new Identifier(SakuraSignInFabric.MOD_ID, "item_stack");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(SIGN_IN, (server, player, handler, buf, responseSender) -> {
            SignInPacket packet = SignInPacket.decode(buf);
            server.execute(() -> SignInPacketHandler.handle(packet, player));
        });

        ServerPlayNetworking.registerGlobalReceiver(CLIENT_CONFIG_SYNC, (server, player, handler, buf, responseSender) -> {
            ClientConfigSyncPacket packet = ClientConfigSyncPacket.decode(buf);
            server.execute(() -> ClientConfigSyncPacketHandler.handle(packet, player));
        });

        ServerPlayNetworking.registerGlobalReceiver(CLIENT_MOD_LOADED, (server, player, handler, buf, responseSender) -> {
            ClientModLoadedNotice packet = ClientModLoadedNotice.decode(buf);
            server.execute(() -> ClientModLoadedNoticeHandler.handle(packet, player));
        });

        ServerPlayNetworking.registerGlobalReceiver(ITEM_STACK, (server, player, handler, buf, responseSender) -> {
            ItemStackPacket packet = new ItemStackPacket(buf);
            server.execute(() -> ItemStackPacket.handle(packet, player));
        });
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ADVANCEMENT, (client, handler, buf, responseSender) -> {
            AdvancementPacket packet = AdvancementPacket.decode(buf);
            client.execute(() -> AdvancementPacketHandler.handle(packet));
        });

        ClientPlayNetworking.registerGlobalReceiver(DOWNLOAD_REWARD_OPTION, (client, handler, buf, responseSender) -> {
            DownloadRewardOptionNotice packet = DownloadRewardOptionNotice.decode(buf);
            client.execute(() -> DownloadRewardOptionNoticeHandler.handle(packet));
        });

        ClientPlayNetworking.registerGlobalReceiver(PLAYER_SIGN_IN_DATA_SYNC, (client, handler, buf, responseSender) -> {
            PlayerDataSyncPacket packet = PlayerDataSyncPacket.decode(buf);
            client.execute(() -> PlayerDataSyncPacketHandler.handle(packet));
        });

        ClientPlayNetworking.registerGlobalReceiver(REWARD_OPTION_SYNC, (client, handler, buf, responseSender) -> {
            RewardOptionSyncPacket packet = RewardOptionSyncPacket.decode(buf);
            client.execute(() -> RewardOptionSyncPacketHandler.handle(packet));
        });
    }
}