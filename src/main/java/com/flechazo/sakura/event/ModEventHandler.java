package com.flechazo.sakura.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mod 事件处理器
 * @author Flechazo
 */
public class ModEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * 注册事件处理器
     */
    public static void register() {
        // 服务器启动完成事件
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.debug("Server Config loaded/reloaded.");
            // 在这里可以处理服务器配置加载完成后的逻辑
        });

        // 服务器停止事件
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.debug("Server is stopping, saving config...");
            // 在这里可以处理服务器关闭前的配置保存等逻辑
        });
    }
}