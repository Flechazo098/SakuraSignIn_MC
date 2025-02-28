package com.flechazo.sakura.network;

import com.flechazo.sakura.capability.IPlayerSignInData;
import com.flechazo.sakura.capability.PlayerSignInDataCapability;
import com.flechazo.sakura.config.ClientConfig;
import lombok.Getter;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.client.MinecraftClient;

/**
 * 客户端配置同步数据包
 * @author Flechazo
 */
@Getter
public class ClientConfigSyncPacket {
    /**
     * 自动领取奖励
     */
    private final boolean autoRewarded;

    public ClientConfigSyncPacket() {
        this.autoRewarded = ClientConfig.getAUTO_REWARDED();
    }

    public ClientConfigSyncPacket(PacketByteBuf buf) {
        this.autoRewarded = buf.readBoolean();
    }

    public void toBytes(PacketByteBuf buf) {
        buf.writeBoolean(this.autoRewarded);
    }

    /**
     * 处理服务端接收到的数据包
     */
    public static void handleServer(ClientConfigSyncPacket packet, ServerPlayerEntity player) {
        if (player == null) {
            return;
        }
        IPlayerSignInData signInData = PlayerSignInDataCapability.getData(player);
        signInData.setAutoRewarded(packet.autoRewarded);
        signInData.save(player);
    }

    /**
     * 处理客户端接收到的数据包
     */
    public static void handleClient(ClientConfigSyncPacket packet, MinecraftClient client) {
        if (client.player == null) {
            return;
        }
        ClientConfig.setAUTO_REWARDED(packet.autoRewarded);
    }
}