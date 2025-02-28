package com.flechazo.sakura;

import com.flechazo.sakura.config.ClientConfig;
import com.flechazo.sakura.event.ClientEventHandler;
import com.flechazo.sakura.network.ModNetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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
        // 初始化客户端配置
        ClientConfig.init();
        LOGGER.info("Client config initialized.");

        // 创建配置文件目录
        clientEventHandler.createConfigPath();
        LOGGER.info("Config path created.");

        // 注册按键绑定
        KeyBindingHelper.registerKeyBinding(ClientEventHandler.SIGN_IN_SCREEN_KEY);
        KeyBindingHelper.registerKeyBinding(ClientEventHandler.REWARD_OPTION_SCREEN_KEY);
        LOGGER.info("Key bindings registered.");

        // 注册客户端网络处理器
        ModNetworkHandler.registerS2CPackets();
        LOGGER.info("Client network handlers registered.");

        // 注册客户端生命周期事件
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            // 初始化日历当前日期
            SakuraSignInFabric.setCalendarCurrentDate(new Date());
            
            // 加载主题
            clientEventHandler.loadThemeTexture();
            
            // 注册客户端事件处理器
            clientEventHandler.register();
            
            LOGGER.info("Client lifecycle events registered.");
        });
        
        LOGGER.info("Client mod initialized successfully.");
    }
} 