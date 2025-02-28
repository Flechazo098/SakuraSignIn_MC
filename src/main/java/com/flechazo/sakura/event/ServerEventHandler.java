package com.flechazo.sakura.event;

import com.flechazo.sakura.SakuraSignInFabric;
import com.flechazo.sakura.capability.IPlayerSignInData;
import com.flechazo.sakura.capability.PlayerSignInData;
import com.flechazo.sakura.capability.PlayerSignInDataCapability;
import com.flechazo.sakura.config.ClientConfig;
import com.flechazo.sakura.config.RewardOptionDataManager;
import com.flechazo.sakura.config.ServerConfig;
import com.flechazo.sakura.enums.ESignInType;
import com.flechazo.sakura.network.*;
import com.flechazo.sakura.network.AdvancementPacket;
import com.flechazo.sakura.network.ModNetworkHandler;
import com.flechazo.sakura.network.PlayerDataSyncPacket;
import com.flechazo.sakura.network.RewardOptionSyncPacket;
import com.flechazo.sakura.rewards.RewardManager;
import com.flechazo.sakura.util.DateUtils;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

/**
 * 服务端事件处理器
 * @author Flechazo
 */
public class ServerEventHandler implements EntityComponentInitializer {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final ComponentKey<PlayerSignInData> PLAYER_DATA =
            ComponentRegistry.getOrCreate(new Identifier(SakuraSignInFabric.MOD_ID, "player_sign_in_data"), PlayerSignInData.class);

    /**
     * 注册事件处理器
     */
    public static void register() {
        // 注册玩家加入事件
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            if (player != null) {
                // 同步玩家签到数据到客户端
                syncPlayerData(player);
                
                // 检查自动签到
                if (ServerConfig.getAUTO_SIGN_IN()) {
                    IPlayerSignInData data = PlayerSignInDataCapability.getData(player);
                    if (!RewardManager.isSignedIn(data, DateUtils.getServerDate(), true)) {
                        // 执行自动签到
                        RewardManager.signIn(player, new SignInPacket(DateUtils.toDateTimeString(DateUtils.getServerDate()), 
                            data.isAutoRewarded(), ESignInType.SIGN_IN));
                        LOGGER.debug("Auto sign-in executed for player on join: {}", player.getName().getString());
                    }
                }
            }
        });

        // 注册玩家退出事件
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            // 玩家退出服务器时移除mod安装状态
            if (player != null) {
                SakuraSignInFabric.getPlayerCapabilityStatus().remove(player.getUuid().toString());
                LOGGER.debug("Removed capability status for player: {}", player.getName().getString());
            }
        });

        // 服务端Tick事件
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // 获取服务器当前时间
            long currentTime = DateUtils.getServerDate().getTime();

            server.getPlayerManager().getPlayerList().forEach(player -> {
                // 只给安装了mod的玩家发送数据包和执行自动签到
                if (SakuraSignInFabric.getPlayerCapabilityStatus().getOrDefault(player.getUuid().toString(), false)) {
                    // 如果玩家还活着则同步玩家传送数据到客户端
                    if (player.isAlive()) {
                        try {
                            PlayerSignInDataCapability.syncPlayerData(player);
                        } catch (Exception e) {
                            LOGGER.error("Failed to sync player data: ", e);
                        }
                    }

                    // 检查自动签到
                    IPlayerSignInData data = PlayerSignInDataCapability.getData(player);
                    if (ServerConfig.getAUTO_SIGN_IN() && !RewardManager.isSignedIn(data, DateUtils.getServerDate(), true)) {
                        // 执行自动签到
                        RewardManager.signIn(player, new SignInPacket(DateUtils.toDateTimeString(DateUtils.getServerDate()), 
                            data.isAutoRewarded(), ESignInType.SIGN_IN));
                        LOGGER.debug("Auto sign-in executed for player: {}", player.getName().getString());
                    }
                }
            });
        });
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