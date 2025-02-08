package com.flechazo.event;

import com.flechazo.SakuraSignInFabric;
import com.flechazo.config.ClientConfig;
import com.flechazo.rewards.RewardManager;
import com.flechazo.screen.RewardOptionScreen;
import com.flechazo.screen.SignInScreen;
import com.flechazo.screen.component.IText;
import com.flechazo.screen.component.InventoryButton;
import com.flechazo.screen.coordinate.TextureCoordinate;
import com.flechazo.util.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;


/**
 * 客户端事件处理器
 * @author Flechazo
 */
@Environment(EnvType.CLIENT)
public class ClientEventHandler {
    private static final Logger LOGGER = LogManager.getLogger(SakuraSignInFabric.MOD_ID);
    private static final String CATEGORIES = "category.sakura-sign-in.general";

    // 定义按键绑定
    public static final KeyBinding SIGN_IN_SCREEN_KEY = new KeyBinding(
        "key.sakura-sign-in.open_sign_in_screen",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_K,
        CATEGORIES
    );
    public static final KeyBinding REWARD_OPTION_SCREEN_KEY = new KeyBinding(
        "key.sakura-sign-in.open_reward_option",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_O,
        CATEGORIES
    );

    /**
     * 注册事件处理器
     */
    public static void register() {
        // 注册按键绑定
        registerKeyBindings();

        // 注册客户端Tick事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (SIGN_IN_SCREEN_KEY.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new SignInScreen());
                }
            }
            while (REWARD_OPTION_SCREEN_KEY.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new RewardOptionScreen());
                }
            }
        });

        // 注册屏幕事件
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractInventoryScreen) {
                if (SakuraSignInFabric.getThemeTexture() == null) loadThemeTexture();
                
                // 创建按钮并添加到界面
                String[] signInCoordinate = ClientConfig.getINVENTORY_SIGN_IN_BUTTON_COORDINATE().split(",");
                String[] rewardOptionCoordinate = ClientConfig.getINVENTORY_REWARD_OPTION_BUTTON_COORDINATE().split(",");
                double signInX_ = signInCoordinate.length == 2 ? StringUtils.toFloat(signInCoordinate[0]) : 0;
                double signInY_ = signInCoordinate.length == 2 ? StringUtils.toFloat(signInCoordinate[1]) : 0;
                double rewardOptionX_ = rewardOptionCoordinate.length == 2 ? StringUtils.toFloat(rewardOptionCoordinate[0]) : 0;
                double rewardOptionY_ = rewardOptionCoordinate.length == 2 ? StringUtils.toFloat(rewardOptionCoordinate[1]) : 0;

                double signInX = signInX_;
                double signInY = signInY_;
                double rewardOptionX = rewardOptionX_;
                double rewardOptionY = rewardOptionY_;

                // 如果坐标为0则设置默认坐标
                if (signInX == 0) signInX = 2;
                if (signInY == 0) signInY = 2;
                if (rewardOptionX == 0) rewardOptionX = 20;
                if (rewardOptionY == 0) rewardOptionY = 2;

                // 如果坐标发生变化则保存到配置文件
                if (signInX_ != signInX || signInY_ != signInY) {
                    ClientConfig.setINVENTORY_SIGN_IN_BUTTON_COORDINATE(String.format("%.6f,%.6f", signInX, signInY));
                }
                if (rewardOptionX_ != rewardOptionX || rewardOptionY_ != rewardOptionY) {
                    ClientConfig.setINVENTORY_REWARD_OPTION_BUTTON_COORDINATE(String.format("%.6f,%.6f", rewardOptionX, rewardOptionY));
                }

                // 如果坐标为百分比则转换为像素坐标
                if (signInX > 0 && signInX <= 1) signInX *= screen.width;
                if (signInY > 0 && signInY <= 1) signInY *= screen.height;
                if (rewardOptionX > 0 && rewardOptionX <= 1) rewardOptionX *= screen.width;
                if (rewardOptionY > 0 && rewardOptionY <= 1) rewardOptionY *= screen.height;

                // 转换为有效坐标
                signInX = InventoryButton.getValidX(signInX, AbstractGuiUtils.ITEM_ICON_SIZE);
                signInY = InventoryButton.getValidY(signInY, AbstractGuiUtils.ITEM_ICON_SIZE);
                rewardOptionX = InventoryButton.getValidX(rewardOptionX, AbstractGuiUtils.ITEM_ICON_SIZE);
                rewardOptionY = InventoryButton.getValidY(rewardOptionY, AbstractGuiUtils.ITEM_ICON_SIZE);

                InventoryButton signInButton = new InventoryButton((int) signInX, (int) signInY,
                        AbstractGuiUtils.ITEM_ICON_SIZE,
                        AbstractGuiUtils.ITEM_ICON_SIZE,
                        IText.translatable("key.sakura_sign_in.sign_in"))
                        .setUV(SakuraSignInFabric.getThemeTextureCoordinate().getSignInBtnUV(), SakuraSignInFabric.getThemeTextureCoordinate().getTotalWidth(), SakuraSignInFabric.getThemeTextureCoordinate().getTotalHeight())
                        .setOnClick((button) -> openSignInScreen(screen))
                        .setOnDragEnd((coordinate) -> ClientConfig.setINVENTORY_SIGN_IN_BUTTON_COORDINATE(String.format("%.6f,%.6f", coordinate.getX(), coordinate.getY())));
                InventoryButton rewardOptionButton = new InventoryButton((int) rewardOptionX, (int) rewardOptionY,
                        AbstractGuiUtils.ITEM_ICON_SIZE,
                        AbstractGuiUtils.ITEM_ICON_SIZE,
                        IText.translatable("key.sakura_sign_in.reward_option"))
                        .setUV(SakuraSignInFabric.getThemeTextureCoordinate().getRewardOptionBtnUV(), SakuraSignInFabric.getThemeTextureCoordinate().getTotalWidth(), SakuraSignInFabric.getThemeTextureCoordinate().getTotalHeight())
                        .setOnClick((button) -> MinecraftClient.getInstance().setScreen(new RewardOptionScreen().setPreviousScreen(screen)))
                        .setOnDragEnd((coordinate) -> ClientConfig.setINVENTORY_REWARD_OPTION_BUTTON_COORDINATE(String.format("%.6f,%.6f", coordinate.getX(), coordinate.getY())));

                // 注册按钮事件
                ScreenEvents.beforeTick(screen).register(screen1 -> {
                    signInButton.tick();
                    rewardOptionButton.tick();
                });

                ScreenEvents.afterRender(screen).register((screen1, context, mouseX, mouseY, delta) -> {
                    signInButton.render(context, mouseX, mouseY, delta);
                    rewardOptionButton.render(context, mouseX, mouseY, delta);
                });
            }
        });
    }

    /**
     * 注册按键绑定
     */
    public static void registerKeyBindings() {
        KeyBindingHelper.registerKeyBinding(SIGN_IN_SCREEN_KEY);
        KeyBindingHelper.registerKeyBinding(REWARD_OPTION_SCREEN_KEY);
    }

    /**
     * 创建配置文件目录
     */
    public static void createConfigPath() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir().resolve(SakuraSignInFabric.MOD_ID);
            Path themesDir = configDir.resolve("themes");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            if (!Files.exists(themesDir)) {
                Files.createDirectories(themesDir);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to create config directories", e);
        }
    }

    /**
     * 加载主题纹理
     */
    public static void loadThemeTexture() {
        String themePath = ClientConfig.getTHEME();
        if (themePath != null && !themePath.isEmpty()) {
            SakuraSignInFabric.setThemeTexture(new Identifier(SakuraSignInFabric.MOD_ID, themePath));
        }
    }

    /**
     * 打开签到界面
     * @param previousScreen 上一个界面
     */
    public static void openSignInScreen(Screen previousScreen) {
        if (SakuraSignInFabric.isEnabled()) {
            SakuraSignInFabric.setCalendarCurrentDate(RewardManager.getCompensateDate(new Date()));
            MinecraftClient.getInstance().setScreen(new SignInScreen().setPreviousScreen(previousScreen));
        } else {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                player.sendMessage(AbstractGuiUtils.textToComponent(IText.translatable("sakura_sign_in.message.server_offline")));
            }
        }
    }
} 