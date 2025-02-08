package com.flechazo.event;

import com.flechazo.SakuraSignInFabric;
import com.flechazo.capability.IPlayerSignInData;
import com.flechazo.capability.PlayerSignInData;
import com.flechazo.config.ClientConfig;
import com.flechazo.config.RewardOptionDataManager;
import com.flechazo.config.ServerConfig;
import com.flechazo.enums.ESignInType;
import com.flechazo.network.*;
import com.flechazo.rewards.RewardManager;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

/**
 * 服务端事件处理器
 * @author Flechazo
 */
public class ServerEventHandler implements EntityComponentInitializer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean isPlayerLoggedIn = false;
    private static boolean hasTriggeredLoadComplete = false;

    public static final ComponentKey<PlayerSignInData> PLAYER_DATA =
            ComponentRegistry.getOrCreate(new Identifier(SakuraSignInFabric.MOD_ID, "player_sign_in_data"), PlayerSignInData.class);

    /**
     * 注册事件处理器
     */
    public static void register() {
        // 客户端登录事件
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LOGGER.debug("Client: Player logged in.");
            isPlayerLoggedIn = true;
            // 同步客户端配置到服务器
            PacketByteBuf buf = PacketByteBufs.create();
            ClientConfigSyncPacket packet = new ClientConfigSyncPacket();
            packet.toBytes(buf);
            ClientPlayNetworking.send(ModNetworkHandler.CLIENT_CONFIG_SYNC, buf);
        });

        // 客户端登出事件
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LOGGER.debug("Client: Player logged out.");
            isPlayerLoggedIn = false;
            hasTriggeredLoadComplete = false;
        });

        // 服务端玩家加入事件
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (SakuraSignInFabric.getPlayerCapabilityStatus().containsKey(player.getUuid().toString())) {
                SakuraSignInFabric.getPlayerCapabilityStatus().put(player.getUuid().toString(), false);
            }

            // 同步玩家签到数据到客户端
            syncPlayerData(player);

            // 同步签到奖励配置到客户端  
            for (RewardOptionSyncPacket rewardOptionSyncPacket : RewardOptionDataManager.toSyncPacket(player.hasPermissionLevel(3)).Chopping()) {
                PacketByteBuf buf = PacketByteBufs.create();
                rewardOptionSyncPacket.toBytes(buf);
                ServerPlayNetworking.send(player, ModNetworkHandler.REWARD_OPTION_SYNC, buf);
            }

            // 同步进度列表到客户端
            for (AdvancementPacket advancementPacket : new AdvancementPacket(player.server.getAdvancementLoader().getAdvancements()).split()) {
                PacketByteBuf buf = PacketByteBufs.create();
                advancementPacket.toBytes(buf);
                ServerPlayNetworking.send(player, ModNetworkHandler.ADVANCEMENT, buf);
            }
        });

        // 客户端Tick事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isPlayerLoggedIn) {
                if (client.player != null && client.world != null && client.currentScreen == null && !hasTriggeredLoadComplete) {
                    LOGGER.debug("Client: Player load complete.");
                    hasTriggeredLoadComplete = true;
                    // 获取玩家的自定义数据
                    IPlayerSignInData data = client.player.getComponent(PLAYER_DATA);
                    // 服务器是否启用自动签到, 且玩家未签到
                    if (ServerConfig.getAUTO_SIGN_IN() && !RewardManager.isSignedIn(data, new Date(), true)) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        SignInPacket packet = new SignInPacket(new Date(), ClientConfig.getAUTO_REWARDED(), ESignInType.SIGN_IN);
                        packet.toBytes(buf);
                        ClientPlayNetworking.send(ModNetworkHandler.SIGN_IN, buf);
                    }
                }
            }
        });

        // 服务端Tick事件
        ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerManager().getPlayerList().forEach(player -> {
            // 不用给未安装mod的玩家发送数据包
            if (!SakuraSignInFabric.getPlayerCapabilityStatus().getOrDefault(player.getUuid().toString(), true)) {
                // 同步玩家签到数据到客户端
                syncPlayerData(player);
            }
        }));
    }

    /**
     * 同步玩家数据到客户端
     * @param player 玩家
     */
    public static void syncPlayerData(ServerPlayerEntity player) {
        IPlayerSignInData data = player.getComponent(PLAYER_DATA);
        PacketByteBuf buf = PacketByteBufs.create();
        PlayerDataSyncPacket packet = new PlayerDataSyncPacket(player.getUuid(), data);
        packet.toBytes(buf);
        ServerPlayNetworking.send(player, ModNetworkHandler.PLAYER_SIGN_IN_DATA_SYNC, buf);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PLAYER_DATA, player -> new PlayerSignInData());
    }
}