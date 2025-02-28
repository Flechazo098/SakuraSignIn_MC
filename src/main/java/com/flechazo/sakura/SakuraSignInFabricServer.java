package com.flechazo.sakura;

import com.flechazo.sakura.config.RewardOptionDataManager;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * 服务端主类
 * @author Flechazo
 */
@Environment(EnvType.SERVER)
public class SakuraSignInFabricServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        // 加载奖励配置数据
        RewardOptionDataManager.loadRewardOption();
        SakuraSignInFabric.LOGGER.info("Server-side initialization completed.");
    }
} 