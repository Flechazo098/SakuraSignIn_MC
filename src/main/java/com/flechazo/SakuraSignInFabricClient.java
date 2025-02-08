package com.flechazo;

import com.flechazo.event.ClientEventHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

/**
 * 客户端主类
 * @author Flechazo
 */
@Environment(EnvType.CLIENT)
public class SakuraSignInFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 注册客户端生命周期事件
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            // 创建配置文件目录并初始化配置
            ClientEventHandler.createConfigPath();
            
            // 注册客户端事件处理器
            ClientEventHandler.register();
            
            // 加载主题
            ClientEventHandler.loadThemeTexture();
        });
    }
} 