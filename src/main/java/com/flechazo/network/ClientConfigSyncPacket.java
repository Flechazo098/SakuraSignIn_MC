package com.flechazo.network;

import com.flechazo.capability.IPlayerSignInData;
import com.flechazo.capability.PlayerSignInDataCapability;
import com.flechazo.config.ClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 客户端配置同步数据包
 * @author Flechazo
 */
@Environment(EnvType.CLIENT)
public class ClientConfigSyncPacket {
    /**
     * 自动领取奖励
     */
    private final boolean autoRewarded;

    public ClientConfigSyncPacket() {
        this.autoRewarded = ClientConfig.AUTO_REWARDED.get();
    }

    public ClientConfigSyncPacket(PacketByteBuf buf) {
        this.autoRewarded = buf.readBoolean();
    }

    public void toBytes(PacketByteBuf buf) {
        buf.writeBoolean(this.autoRewarded);
    }

    public static void handle(ClientConfigSyncPacket packet, ServerPlayerEntity player) {
        if (player == null) {
            return;
        }
        IPlayerSignInData signInData = PlayerSignInDataCapability.getData(player);
        signInData.setAutoRewarded(packet.autoRewarded);
        signInData.save(player);
    }

    public boolean isAutoRewarded() {
        return autoRewarded;
    }
}