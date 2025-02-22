package com.flechazo;

import com.flechazo.config.ClientConfig;
import com.flechazo.config.ServerConfig;
import com.flechazo.event.ClientEventHandler;
import com.flechazo.network.ModNetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;

/**
 * 客户端主类
 * @author Flechazo
 */
@Environment(EnvType.CLIENT)
public class SakuraSignInFabricClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("sakura_sign_in");
    private final ClientEventHandler clientEventHandler = new ClientEventHandler();

    @Override
    public void onInitializeClient() {
        // 创建配置文件目录并初始化配置
        clientEventHandler.createConfigPath();

        // 注册客户端生命周期事件
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            // 初始化日历当前日期
            SakuraSignInFabric.setCalendarCurrentDate(new Date());
            
            // 加载主题
            clientEventHandler.loadThemeTexture();
            
            // 注册客户端事件处理器
            clientEventHandler.register();
        });

        // 注册客户端网络处理器
        ModNetworkHandler.registerS2CPackets();
        
        LOGGER.info("Client mod initialized.");
    }
} 