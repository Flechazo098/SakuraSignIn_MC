package com.flechazo.sakura.network;

import com.flechazo.sakura.SakuraSignInFabric;
import com.flechazo.sakura.event.ServerEventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientProxy {
    public static final Logger LOGGER = LogManager.getLogger();

    public static void handleSynPlayerData(PlayerDataSyncPacket packet) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            try {
                player.getComponent(ServerEventHandler.PLAYER_DATA).copyFrom(packet.getData());
                LOGGER.debug("Client: Player data received successfully.");
            } catch (Exception ignored) {
                LOGGER.debug("Client: Player data received failed.");
            }
            SakuraSignInFabric.setEnabled(true);
        }
    }

    public static void handleAdvancement(AdvancementPacket packet) {
        LOGGER.info("Client: Received {} advancements", packet.getAdvancements().size());
        SakuraSignInFabric.setAdvancementData(packet.getAdvancements());
        LOGGER.info("Client: Total advancements after update: {}", SakuraSignInFabric.getAdvancementData().size());
    }
} 