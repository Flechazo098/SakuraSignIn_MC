package com.flechazo.event;

import com.flechazo.SakuraSignInFabric;
import com.flechazo.capability.IPlayerSignInData;
import com.flechazo.config.ClientConfig;
import com.flechazo.config.ServerConfig;
import com.flechazo.enums.ESignInType;
import com.flechazo.network.ClientConfigSyncPacket;
import com.flechazo.network.ModNetworkHandler;
import com.flechazo.network.SignInPacket;
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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
import net.minecraft.text.Text;
import net.minecraft.network.PacketByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import static com.flechazo.SakuraSignInFabric.PNG_CHUNK_NAME;


/**
 * 客户端事件处理器
 * @author Flechazo
 */
@Environment(EnvType.CLIENT)
public class ClientEventHandler extends Screen {
    private static final Logger LOGGER = LogManager.getLogger(SakuraSignInFabric.MOD_ID);
    private static final String CATEGORIES = "key.sakura_sign_in.categories";
    private static boolean keysRegistered = false;

    // 定义按键绑定
    private static final KeyBinding SIGN_IN_SCREEN_KEY = new KeyBinding(
        "key.sakura_sign_in.sign_in",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_K,
        CATEGORIES
    );
    private static final KeyBinding REWARD_OPTION_SCREEN_KEY = new KeyBinding(
        "key.sakura_sign_in.reward_option",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_L,
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
        super(Text.empty());
    }

//    /**
//     * 获取签到界面按键绑定
//     */
//    public KeyBinding getSignInScreenKey() {
//        return SIGN_IN_SCREEN_KEY;
//    }
//
//    /**
//     * 获取奖励选项界面按键绑定
//     */
//    public KeyBinding getRewardOptionScreenKey() {
//        return REWARD_OPTION_SCREEN_KEY;
//    }
//
//    /**
//     * 注册按键绑定
//     */
//    private void registerKeyBindings() {
//        if (!keysRegistered) {
//            KeyBindingHelper.registerKeyBinding(SIGN_IN_SCREEN_KEY);
//            KeyBindingHelper.registerKeyBinding(REWARD_OPTION_SCREEN_KEY);
//            keysRegistered = true;
//        }
//    }

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
        // 注册按键绑定
        registerKeyBindings();

        // 注册客户端登录事件
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LOGGER.debug("Client: Player logged in.");
            // 同步客户端配置到服务器
            PacketByteBuf buf = PacketByteBufs.create();
            ClientConfigSyncPacket packet = new ClientConfigSyncPacket ();
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
            // 处理自动签到
            if (SakuraSignInFabric.isEnabled() && client.player != null && client.world != null) {
                IPlayerSignInData data = client.player.getComponent(ServerEventHandler.PLAYER_DATA);
                // 服务器是否启用自动签到, 且玩家未签到
                if (ServerConfig.getAUTO_SIGN_IN() && !RewardManager.isSignedIn(data, new Date(), true)) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    SignInPacket packet = new SignInPacket(new Date(), ClientConfig.getAUTO_REWARDED(), ESignInType.SIGN_IN);
                    packet.toBytes(buf);
                    ClientPlayNetworking.send(ModNetworkHandler.SIGN_IN, buf);
                }
            }
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
                signInButton = new InventoryButton(0, 0, 20, 20, IText.translatable("key.sakura_sign_in.sign_in"))
                        .setUV(SakuraSignInFabric.getThemeTextureCoordinate().getSignInBtnUV(),
                              SakuraSignInFabric.getThemeTextureCoordinate().getTotalWidth(),
                              SakuraSignInFabric.getThemeTextureCoordinate().getTotalHeight())
                        .setOnClick(button -> openSignInScreen(screen))
                        .setOnDragEnd(coordinate -> ClientConfig.setINVENTORY_SIGN_IN_BUTTON_COORDINATE(coordinate.toString()));

                // 创建奖励选项按钮
                rewardOptionButton = new InventoryButton(0, 0, 20, 20, IText.translatable("key.sakura_sign_in.sign_in"))
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

                // 注册渲染事件
                ScreenEvents.afterRender(screen).register((screen1, context, mouseX, mouseY, delta) -> {
                    if (signInButton != null) {
                        signInButton.render(context, mouseX, mouseY, delta);
                    }
                    if (rewardOptionButton != null) {
                        rewardOptionButton.render(context, mouseX, mouseY, delta);
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
        LOGGER.debug("Theme texture loaded.");
    }

    /**
     * 打开签到界面
     * @param previousScreen 上一个界面
     */
    public static void openSignInScreen(Screen previousScreen) {
        if (SakuraSignInFabric.isEnabled()) {
            LOGGER.info("Opening sign in screen...");
            SakuraSignInFabric.setCalendarCurrentDate(RewardManager.getCompensateDate(new Date()));
            MinecraftClient.getInstance().setScreen(new SignInScreen().setPreviousScreen(previousScreen));
        } else {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                player.sendMessage(AbstractGuiUtils.textToComponent(IText.translatable("sakura_sign_in.message.server_offline")));
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