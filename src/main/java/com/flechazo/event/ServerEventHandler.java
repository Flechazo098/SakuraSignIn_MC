package com.flechazo.event;

import com.flechazo.SakuraSignInFabric;
import com.flechazo.capability.IPlayerSignInData;
import com.flechazo.capability.PlayerSignInData;
import com.flechazo.config.RewardOptionDataManager;
import com.flechazo.network.*;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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