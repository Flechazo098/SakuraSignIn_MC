package com.flechazo.sakura.event;

import com.flechazo.sakura.SakuraSignInFabric;
import com.flechazo.sakura.config.ClientConfig;
import com.flechazo.sakura.enums.EI18nType;
import com.flechazo.sakura.network.ClientConfigSyncPacket;
import com.flechazo.sakura.network.ModNetworkHandler;
import com.flechazo.sakura.screen.RewardOptionScreen;
import com.flechazo.sakura.screen.SignInScreen;
import com.flechazo.sakura.screen.component.IText;
import com.flechazo.sakura.screen.component.InventoryButton;
import com.flechazo.sakura.screen.component.NotificationManager;
import com.flechazo.sakura.screen.coordinate.TextureCoordinate;
import com.flechazo.sakura.util.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.flechazo.sakura.SakuraSignInFabric.PNG_CHUNK_NAME;


/**
 * 客户端事件处理器
 * @author Flechazo
 */
@Environment(EnvType.CLIENT)
public class ClientEventHandler {
    private static final Logger LOGGER = LogManager.getLogger(SakuraSignInFabric.MOD_ID);
    private static final String CATEGORIES = "key.sakura_sign_in.categories";
    private static boolean keysRegistered = false;

    // 定义按键绑定
    public static final KeyBinding SIGN_IN_SCREEN_KEY = new KeyBinding(
        "key.sakura_sign_in.sign_in",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_H,
        CATEGORIES
    );
    public static final KeyBinding REWARD_OPTION_SCREEN_KEY = new KeyBinding(
        "key.sakura_sign_in.reward_option",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_O,
        CATEGORIES
    );

    /**
     * 签到按钮
     */
    private InventoryButton signInButton;
    /**
     * 奖励选项按钮
     */
    private InventoryButton rewardOptionButton;

    public ClientEventHandler() {
        super();
    }

    /**
     * 注册事件处理器
     * <p>
     * 包含以下功能:
     * <p>
     * 1. 注册按键绑定
     * <p>
     * 2. 注册屏幕初始化事件,用于创建和管理按钮
     * <p>
     * 3. 注册渲染事件,用于按钮的显示
     * <p>
     * 4. 注册鼠标事件,处理按钮的交互
     * <p>
     * 5. 注册客户端Tick事件,处理按键和屏幕状态
     */
    public void register() {
        // 注册客户端登录事件
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LOGGER.debug("Client: Player logged in.");
            // 同步客户端配置到服务器
            PacketByteBuf buf = PacketByteBufs.create();
            ClientConfigSyncPacket packet = new ClientConfigSyncPacket();
            packet.toBytes(buf);
            ClientPlayNetworking.send(ModNetworkHandler.CLIENT_CONFIG_SYNC, buf);
        });

        // 注册客户端登出事件
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LOGGER.debug("Client: Player logged out.");
            SakuraSignInFabric.setEnabled(false);
        });

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

        // 注册屏幕初始化事件
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            // 清理旧的按钮实例
            signInButton = null;
            rewardOptionButton = null;

            if (screen instanceof HandledScreen) {
                // 创建签到按钮
                signInButton = new InventoryButton(0, 0, 20, 20, I18nUtils.getTranslationClient(EI18nType.KEY, "sign_in"))
                        .setUV(SakuraSignInFabric.getThemeTextureCoordinate().getSignInBtnUV(),
                              SakuraSignInFabric.getThemeTextureCoordinate().getTotalWidth(),
                              SakuraSignInFabric.getThemeTextureCoordinate().getTotalHeight())
                        .setOnClick(button -> openSignInScreen(screen))
                        .setOnDragEnd(coordinate -> ClientConfig.setINVENTORY_SIGN_IN_BUTTON_COORDINATE(coordinate.toString()));

                // 创建奖励选项按钮
                rewardOptionButton = new InventoryButton(0, 0, 20, 20, I18nUtils.getTranslationClient(EI18nType.KEY, "reward_option"))
                        .setUV(SakuraSignInFabric.getThemeTextureCoordinate().getRewardOptionBtnUV(),
                              SakuraSignInFabric.getThemeTextureCoordinate().getTotalWidth(),
                              SakuraSignInFabric.getThemeTextureCoordinate().getTotalHeight())
                        .setOnClick(button -> openRewardOptionScreen(screen))
                        .setOnDragEnd(coordinate -> ClientConfig.setINVENTORY_REWARD_OPTION_BUTTON_COORDINATE(coordinate.toString()));

                // 设置按钮位置
                String[] signInCoords = ClientConfig.getINVENTORY_SIGN_IN_BUTTON_COORDINATE().split(",");
                String[] rewardCoords = ClientConfig.getINVENTORY_REWARD_OPTION_BUTTON_COORDINATE().split(",");

                signInButton.setX((int)InventoryButton.getValidX(Integer.parseInt(signInCoords[0].trim()), 20));
                signInButton.setY((int)InventoryButton.getValidY(Integer.parseInt(signInCoords[1].trim()), 20));
                rewardOptionButton.setX((int)InventoryButton.getValidX(Integer.parseInt(rewardCoords[0].trim()), 20));
                rewardOptionButton.setY((int)InventoryButton.getValidY(Integer.parseInt(rewardCoords[1].trim()), 20));

                // 注册屏幕渲染事件（带通知渲染）
                ScreenEvents.afterRender(screen).register((screen1, context, mouseX, mouseY, delta) -> {
                    if (signInButton != null) {
                        signInButton.render(context, mouseX, mouseY, delta);
                    }
                    if (rewardOptionButton != null) {
                        rewardOptionButton.render(context, mouseX, mouseY, delta);
                        NotificationManager.get().render(context);
                    }
                });

                // 注册鼠标点击事件
                ScreenMouseEvents.allowMouseClick(screen).register((screen1, mouseX, mouseY, button) -> {
                    if (signInButton != null && signInButton.isMouseOver(mouseX, mouseY)) {
                        signInButton.mouseClicked(mouseX, mouseY, button);
                        return true;
                    }
                    if (rewardOptionButton != null && rewardOptionButton.isMouseOver(mouseX, mouseY)) {
                        rewardOptionButton.mouseClicked(mouseX, mouseY, button);
                        return true;
                    }
                    return false;
                });

                // 注册鼠标释放事件
                ScreenMouseEvents.allowMouseRelease(screen).register((screen1, mouseX, mouseY, button) -> {
                    if (signInButton != null) {
                        signInButton.mouseReleased(mouseX, mouseY, button);
                        return true;
                    }
                    if (rewardOptionButton != null) {
                        rewardOptionButton.mouseReleased(mouseX, mouseY, button);
                        return true;
                    }
                    return false;
                });

                // 注册鼠标拖动事件 - 通过mouseMoved方法实现
                ScreenMouseEvents.beforeMouseClick(screen).register((screen1, mouseX, mouseY, button) -> {
                    if (signInButton != null) {
                        signInButton.mouseMoved(mouseX, mouseY);
                    }
                    if (rewardOptionButton != null) {
                        rewardOptionButton.mouseMoved(mouseX, mouseY);
                    }
                });

                // 注册鼠标拖动事件 - 通过mouseDragged方法实现
                ScreenMouseEvents.beforeMouseRelease(screen).register((screen1, mouseX, mouseY, button) -> {
                    if (signInButton != null && signInButton.isMouseOver(mouseX, mouseY)) {
                        double deltaX = mouseX - signInButton.getMouseClickX();
                        double deltaY = mouseY - signInButton.getMouseClickY();
                        signInButton.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
                    }
                    if (rewardOptionButton != null && rewardOptionButton.isMouseOver(mouseX, mouseY)) {
                        double deltaX = mouseX - rewardOptionButton.getMouseClickX();
                        double deltaY = mouseY - rewardOptionButton.getMouseClickY();
                        rewardOptionButton.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
                    }
                });
            }
        });

        // 注册客户端Tick事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 检查屏幕变化
            Screen currentScreen = client.currentScreen;
            if (!(currentScreen instanceof HandledScreen)) {
                signInButton = null;
                rewardOptionButton = null;
            }


            // 处理按键事件
            if (SIGN_IN_SCREEN_KEY.wasPressed() && SakuraSignInFabric.isEnabled()) {
                openSignInScreen(currentScreen);
            }
            if (REWARD_OPTION_SCREEN_KEY.wasPressed() && SakuraSignInFabric.isEnabled()) {
                openRewardOptionScreen(currentScreen);
            }
        });

        // 注册HUD渲染事件（无屏幕时通知）
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (MinecraftClient.getInstance().currentScreen == null){
                NotificationManager.get().render(context);
            }
        });
    }

    public static void registerKeyBindings() {
        if (!keysRegistered) {
            KeyBindingHelper.registerKeyBinding(SIGN_IN_SCREEN_KEY);
            KeyBindingHelper.registerKeyBinding(REWARD_OPTION_SCREEN_KEY);
            keysRegistered = true; // 标记为已注册
        }
    }

    /**
     * 创建配置文件目录
     */
    public void createConfigPath() {
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
    public void loadThemeTexture() {
        LOGGER.debug("Loading theme texture...");
        try {
            SakuraSignInFabric.setThemeTexture(TextureUtils.loadCustomTexture(ClientConfig.getTHEME()));
            SakuraSignInFabric.setSpecialVersionTheme(Boolean.TRUE.equals(ClientConfig.getSPECIAL_THEME()));
            InputStream inputStream = MinecraftClient.getInstance().getResourceManager().getResourceOrThrow(SakuraSignInFabric.getThemeTexture()).getInputStream();
            SakuraSignInFabric.setThemeTextureCoordinate(PNGUtils.readLastPrivateChunk(inputStream, PNG_CHUNK_NAME));
        } catch (IOException | ClassNotFoundException ignored) {
            LOGGER.warn("Failed to load theme texture coordinates, using default");
        }
        if (SakuraSignInFabric.getThemeTextureCoordinate(false) == null) {
            // 使用默认配置
            SakuraSignInFabric.setThemeTextureCoordinate(TextureCoordinate.getDefault());
        }
        // 设置内置主题特殊图标UV的偏移量
        if (SakuraSignInFabric.isSpecialVersionTheme() && SakuraSignInFabric.getThemeTextureCoordinate().isSpecial()) {
            LOGGER.debug("Setting special theme UV offset");
            SakuraSignInFabric.getThemeTextureCoordinate().getNotSignedInUV().setX(320);
            SakuraSignInFabric.getThemeTextureCoordinate().getSignedInUV().setX(320);
            SakuraSignInFabric.getThemeTextureCoordinate().getThemeUV().setX(320);
            SakuraSignInFabric.getThemeTextureCoordinate().getThemeHoverUV().setX(320);
            SakuraSignInFabric.getThemeTextureCoordinate().getThemeTapUV().setX(320);
        } else {
            LOGGER.debug("Using normal theme UV offset");
            SakuraSignInFabric.getThemeTextureCoordinate().getNotSignedInUV().setX(0);
            SakuraSignInFabric.getThemeTextureCoordinate().getSignedInUV().setX(0);
            SakuraSignInFabric.getThemeTextureCoordinate().getThemeUV().setX(0);
            SakuraSignInFabric.getThemeTextureCoordinate().getThemeHoverUV().setX(0);
            SakuraSignInFabric.getThemeTextureCoordinate().getThemeTapUV().setX(0);
        }
        LOGGER.debug("Theme texture loaded. Special version: {}", SakuraSignInFabric.isSpecialVersionTheme());
    }

    /**
     * 打开签到界面
     * @param previousScreen 上一个界面
     */
    public static void openSignInScreen(Screen previousScreen) {
        if (SakuraSignInFabric.isEnabled()) {
            LOGGER.info("Opening sign in screen...");
            SakuraSignInFabric.setCalendarCurrentDate(DateUtils.getServerDate());
            MinecraftClient.getInstance().setScreen(new SignInScreen().setPreviousScreen(previousScreen));
        } else {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                Component component = Component.translatableClient(EI18nType.MESSAGE, "sakura_is_offline");
                NotificationManager.get().addNotification(NotificationManager.Notification.ofComponentWithBlack(component).setBgColor(0x88FF5555));
            }
        }
    }

    public static void openRewardOptionScreen(Screen previousScreen) {
        if (SakuraSignInFabric.isEnabled()) {
            LOGGER.info("Opening reward option screen...");
            MinecraftClient.getInstance().setScreen(new RewardOptionScreen().setPreviousScreen(previousScreen));
        }
    }
}