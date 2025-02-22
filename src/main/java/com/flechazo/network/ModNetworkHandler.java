package com.flechazo.network;

import com.flechazo.SakuraSignInFabric;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
    public static final Identifier PLAYER_SIGN_IN_DATA_SYNC = new Identifier(SakuraSignInFabric.MOD_ID, "player_sign_in_data_sync");
    public static final Identifier REWARD_OPTION_SYNC = new Identifier(SakuraSignInFabric.MOD_ID, "reward_option_sync");
    public static final Identifier ITEM_STACK = new Identifier(SakuraSignInFabric.MOD_ID, "item_stack");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(SIGN_IN, (server, player, handler, buf, responseSender) -> {
            SignInPacket packet = new SignInPacket(buf);
            server.execute(() -> SignInPacket.handle(packet, player));
        });

        ServerPlayNetworking.registerGlobalReceiver(CLIENT_CONFIG_SYNC, (server, player, handler, buf, responseSender) -> {
            ClientConfigSyncPacket packet = new ClientConfigSyncPacket(buf);
            server.execute(() -> ClientConfigSyncPacket.handle(packet, player));
        });

        ServerPlayNetworking.registerGlobalReceiver(ITEM_STACK, (server, player, handler, buf, responseSender) -> {
            ItemStackPacket packet = new ItemStackPacket(buf);
            server.execute(() -> ItemStackPacket.handle(packet, player));
        });
    }

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
            RewardOptionSyncPacket packet = RewardOptionSyncPacket.decode(buf);
            client.execute(() -> RewardOptionSyncPacket.handle(packet));
        });
    }

    /**
     * 注册所有的网络通道
     */
    public static void registerPackets() {
        registerC2SPackets();
    }
}