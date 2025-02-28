package com.flechazo.sakura.network;

import com.flechazo.sakura.SakuraSignInFabric;
import com.flechazo.sakura.util.DateUtils;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

import java.util.Date;

/**
 * 服务器时间同步数据包
 * @author Flechazo
 */
@Getter
public class ServerTimeSyncPacket {
    /**
     * 服务器时间
     */
    private final String serverTime;

    public ServerTimeSyncPacket() {
        this.serverTime = DateUtils.toDateTimeString(DateUtils.getServerDate());
    }

    public ServerTimeSyncPacket(PacketByteBuf buf) {
        this.serverTime = buf.readString();
    }

    public void toBytes(PacketByteBuf buf) {
        buf.writeString(this.serverTime);
    }

    /**
     * 处理服务器时间同步数据包
     * @param packet 数据包
     * @param client Minecraft客户端实例
     * @param responseSender 响应发送器
     */
    @Environment(EnvType.CLIENT)
    public static void handle(ServerTimeSyncPacket packet, MinecraftClient client, PacketSender responseSender) {
        client.execute(() -> {
            String clientTime = DateUtils.toDateTimeString(new Date());
            SakuraSignInFabric.getClientServerTime().setKey(clientTime).setValue(packet.serverTime);
        });
    }
}
