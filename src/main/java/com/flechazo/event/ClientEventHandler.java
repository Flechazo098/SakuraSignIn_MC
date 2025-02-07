package com.flechazo.event;

import com.flechazo.SakuraSignInFabric;
import com.flechazo.config.ClientConfig;
import com.flechazo.rewards.RewardManager;
import com.flechazo.screen.RewardOptionScreen;
import com.flechazo.screen.SignInScreen;
import com.flechazo.screen.component.InventoryButton;
import com.flechazo.screen.coordinate.TextureCoordinate;
import com.flechazo.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static com.flechazo.SakuraSignInFabric.PNG_CHUNK_NAME;
import static com.flechazo.util.I18nUtils.getI18nKey;

/**
 * 客户端事件处理器
 * @author Flechazo
 */
public class ClientEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String CATEGORIES = "key.sakura_sign_in.categories";

    // 定义按键绑定
    public static final KeyBinding SIGN_IN_SCREEN_KEY = new KeyBinding ("key.sakura_sign_in.sign_in",
            GLFW.GLFW_KEY_H, CATEGORIES);
    public static final KeyBinding REWARD_OPTION_SCREEN_KEY = new KeyBinding("key.sakura_sign_in.reward_option",
            GLFW.GLFW_KEY_O, CATEGORIES);

    /**
     * 注册事件处理器
     */
    public static void register() {
        // 注册按键绑定
        KeyBindingHelper.registerKeyBinding(SIGN_IN_SCREEN_KEY);
        KeyBindingHelper.registerKeyBinding(REWARD_OPTION_SCREEN_KEY);

        // 注册客户端Tick事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 检测并消费点击事件
            if (SIGN_IN_SCREEN_KEY.wasPressed()) {
                // 打开签到界面
                openSignInScreen(null);
            } else if (REWARD_OPTION_SCREEN_KEY.wasPressed()) {
                // 打开奖励配置界面
                MinecraftClient.getInstance().setScreen(new RewardOptionScreen());
            }
        });

        // 注册屏幕事件
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractInventoryScreen) {
                if (SakuraSignInFabric.getThemeTexture() == null) loadThemeTexture();
                
                // 创建按钮并添加到界面
                String[] signInCoordinate = ClientConfig.INVENTORY_SIGN_IN_BUTTON_COORDINATE.get().split(",");
                String[] rewardOptionCoordinate = ClientConfig.INVENTORY_REWARD_OPTION_BUTTON_COORDINATE.get().split(",");
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
                    ClientConfig.INVENTORY_SIGN_IN_BUTTON_COORDINATE.set(String.format("%.6f,%.6f", signInX, signInY));
                }
                if (rewardOptionX_ != rewardOptionX || rewardOptionY_ != rewardOptionY) {
                    ClientConfig.INVENTORY_REWARD_OPTION_BUTTON_COORDINATE.set(String.format("%.6f,%.6f", rewardOptionX, rewardOptionY));
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
                        I18nUtils.get("key.sakura_sign_in.sign_in"))
                        .setUV(SakuraSignInFabric.getThemeTextureCoordinate().getSignInBtnUV(), SakuraSignInFabric.getThemeTextureCoordinate().getTotalWidth(), SakuraSignInFabric.getThemeTextureCoordinate().getTotalHeight())
                        .setOnClick((button) -> openSignInScreen(screen))
                        .setOnDragEnd((coordinate) -> ClientConfig.INVENTORY_SIGN_IN_BUTTON_COORDINATE.set(String.format("%.6f,%.6f", coordinate.getX(), coordinate.getY())));
                InventoryButton rewardOptionButton = new InventoryButton((int) rewardOptionX, (int) rewardOptionY,
                        AbstractGuiUtils.ITEM_ICON_SIZE,
                        AbstractGuiUtils.ITEM_ICON_SIZE,
                        I18nUtils.get("key.sakura_sign_in.reward_option"))
                        .setUV(SakuraSignInFabric.getThemeTextureCoordinate().getRewardOptionBtnUV(), SakuraSignInFabric.getThemeTextureCoordinate().getTotalWidth(), SakuraSignInFabric.getThemeTextureCoordinate().getTotalHeight())
                        .setOnClick((button) -> MinecraftClient.getInstance().setScreen(new RewardOptionScreen().setPreviousScreen(screen)))
                        .setOnDragEnd((coordinate) -> ClientConfig.INVENTORY_REWARD_OPTION_BUTTON_COORDINATE.set(String.format("%.6f,%.6f", coordinate.getX(), coordinate.getY())));

                // 注册按钮事件
                ScreenEvents.beforeKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                    signInButton.keyPressed_(key, scancode, modifiers);
                    rewardOptionButton.keyPressed_(key, scancode, modifiers);
                });

                ScreenEvents.beforeKeyRelease(screen).register((screen1, key, scancode, modifiers) -> {
                    signInButton.keyReleased_(key, scancode, modifiers);
                    rewardOptionButton.keyReleased_(key, scancode, modifiers);
                });

                ScreenEvents.beforeMouseClick(screen).register((screen1, mouseX, mouseY, button) -> {
                    signInButton.mouseClicked_(mouseX, mouseY, button);
                    rewardOptionButton.mouseClicked_(mouseX, mouseY, button);
                });

                ScreenEvents.beforeMouseRelease(screen).register((screen1, mouseX, mouseY, button) -> {
                    signInButton.mouseReleased_(mouseX, mouseY, button);
                    rewardOptionButton.mouseReleased_(mouseX, mouseY, button);
                });

                ScreenEvents.afterRender(screen).register((screen1, matrices, mouseX, mouseY, tickDelta) -> {
                    signInButton.render_(matrices, mouseX, mouseY, tickDelta);
                    rewardOptionButton.render_(matrices, mouseX, mouseY, tickDelta);
                });
            }
        });
    }

    /**
     * 创建配置文件目录
     */
    public static void createConfigPath() {
        File themesPath = new File(FMLPaths.CONFIGDIR.get().resolve(SakuraSignInFabric.MOD_ID).toFile(), "themes");
        if (!themesPath.exists()) {
            themesPath.mkdirs();
        }
    }

    /**
     * 加载主题纹理
     */
    public static void loadThemeTexture() {
        try {
            SakuraSignInFabric.setThemeTexture(TextureUtils.loadCustomTexture(ClientConfig.THEME.get()));
            SakuraSignInFabric.setSpecialVersionTheme(Boolean.TRUE.equals(ClientConfig.SPECIAL_THEME.get()));
            InputStream inputStream = MinecraftClient.getInstance().getResourceManager().getResourceOrThrow(SakuraSignInFabric.getThemeTexture()).getInputStream();
            SakuraSignInFabric.setThemeTextureCoordinate(PNGUtils.readLastPrivateChunk(inputStream, PNG_CHUNK_NAME));
        } catch (IOException | ClassNotFoundException ignored) {
        }
        if (SakuraSignInFabric.getThemeTextureCoordinate(false) == null) {
            // 使用默认配置
            SakuraSignInFabric.setThemeTextureCoordinate(TextureCoordinate.getDefault());
        }
        // 设置内置主题特殊图标UV的偏移量
        if (SakuraSignInFabric.isSpecialVersionTheme() && SakuraSignInFabric.getThemeTextureCoordinate().isSpecial()) {
            SakuraSignInFabric.getThemeTextureCoordinate().getNotSignedInUV().setX(320);
            SakuraSignInFabric.getThemeTextureCoordinate().getSignedInUV().setX(320);
        } else {
            SakuraSignInFabric.getThemeTextureCoordinate().getNotSignedInUV().setX(0);
            SakuraSignInFabric.getThemeTextureCoordinate().getSignedInUV().setX(0);
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
                player.sendMessage(Text.translatable(getI18nKey("SakuraSignIn server is offline!")));
            }
        }
    }
} 