package com.flechazo;

import com.flechazo.command.SignInCommand;
import com.flechazo.config.ClientConfig;
import com.flechazo.config.RewardOptionDataManager;
import com.flechazo.config.ServerConfig;
import com.flechazo.event.ClientEventHandler;
import com.flechazo.network.AdvancementData;
import com.flechazo.network.ModNetworkHandler;
import com.flechazo.network.SplitPacket;
import com.flechazo.rewards.RewardList;
import com.flechazo.screen.coordinate.TextureCoordinate;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SakuraSignInFabric implements ModInitializer {
	public static final String MOD_ID = "sakura-sign-in";
	public static final String PNG_CHUNK_NAME = "vacb";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	/**
	 * 是否有对应的服务端
	 */
	@Getter
	@Setter
	private static boolean enabled;
	/**
	 * 奖励配置页面侧边栏是否开启
	 */
	@Getter
	@Setter
	private static boolean rewardOptionBarOpened = false;
	/**
	 * 签到页面当前显示的日期
	 */
	@Getter
	@Setter
	private static Date calendarCurrentDate;

	/**
	 * 背景材质
	 */
	@Getter
	@Setter
	private static Identifier themeTexture = null;
	/**
	 * 背景材质坐标
	 */
	@Setter
	public static TextureCoordinate themeTextureCoordinate = null;
	/**
	 * 是否使用内置主题特殊图标
	 */
	@Getter
	@Setter
	private static boolean specialVersionTheme = false;
	/**
	 * 奖励配置数据
	 */
	@Getter
	@Setter
	private static List < AdvancementData > advancementData;
	/**
	 * 玩家权限等级
	 */
	@Getter
	@Setter
	private static int permissionLevel;

	/**
	 * 分片网络包缓存
	 */
	@Getter
	private static final Map <String, List<? extends SplitPacket >> packetCache = new ConcurrentHashMap <> ();

	/**
	 * 玩家能力同步状态
	 */
	@Getter
	private static final Map<String, Boolean> playerCapabilityStatus = new ConcurrentHashMap<>();

	@Getter
	private static final RewardList clipboard = new RewardList();

	@Override
	public void onInitialize(){

		// 注册网络通道
		ModNetworkHandler.registerPackets();

		// 注册服务器生命周期事件
		ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

		// 注册命令
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			SignInCommand.register(dispatcher);
		});

		// 玩家登出事件
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			handlePlayerLogout(handler.getPlayer());
		});
	}

	private void onServerStarting(MinecraftServer server) {
		RewardOptionDataManager.loadRewardOption();
		LOGGER.debug("SignIn data loaded.");
	}

	private void onServerStopping(MinecraftServer server) {
		// RewardOptionDataManager.saveRewardOption();
	}

	private void handlePlayerLogout(PlayerEntity player) {
		LOGGER.debug("Player has logged out.");
		// 处理玩家登出逻辑
		}

	public static TextureCoordinate getThemeTextureCoordinate(boolean nonNull) {
		if (nonNull && (themeTextureCoordinate == null || themeTexture == null)) ClientEventHandler.loadThemeTexture();
		return themeTextureCoordinate;
	}

	@NonNull
	public static TextureCoordinate getThemeTextureCoordinate() {
		return getThemeTextureCoordinate(true);
	}

	/**
	 * 在客户端设置阶段触发的事件处理方法
	 * 此方法主要用于接收 FML 客户端设置事件，并执行相应的初始化操作
	 */
	@SubscribeEvent
	public void onClientSetup(final FMLClientSetupEvent event) {
		// 创建配置文件目录
		ClientEventHandler.createConfigPath();
	}


	/**
	 * 玩家注销事件
	 *
	 * @param event 玩家注销事件对象，通过该对象可以获取到注销的玩家对象
	 */
	@SubscribeEvent
	public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		LOGGER.debug("Player has logged out.");
		// 获取退出的玩家对象
		PlayerEntity player = player.get.getEntity();
		// 判断是否在客户端并且退出的玩家是客户端的当前玩家
		if (player.getCommandSenderWorld().isClientSide) {
            if (MinecraftClient.getInstance ().player != null && MinecraftClient.getInstance ().player.getUuid ().equals (player.getUuid ())) {
                LOGGER.debug ("Current player has logged out.");
                // 当前客户端玩家与退出的玩家相同
                enabled = false;
            }
        }
	}

	/**
	 * 打开指定路径的文件夹
	 */
	@Environment  ( EnvType.CLIENT)
	public static void openFileInFolder(Path path) {
		try {
			if (Files.isDirectory(path)) {
				// 如果是文件夹，直接打开文件夹
				openFolder(path);
			} else if (Files.isRegularFile(path)) {
				// 如果是文件，打开文件所在的文件夹，并选中文件
				openFolderAndSelectFile(path);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to open file/folder: ", e);
		}
	}

	private static void openFolder(Path path) {
		try {
			// Windows
			if (System.getProperty("os.name").toLowerCase().contains("win")) {
				new ProcessBuilder("explorer.exe", path.toString()).start();
			}
			// macOS
			else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
				new ProcessBuilder("open", path.toString()).start();
			}
			// Linux
			else {
				new ProcessBuilder("xdg-open", path.toString()).start();
			}
		} catch (IOException e) {
			LOGGER.error("Failed to open folder: ", e);
		}
	}

	private static void openFolderAndSelectFile(Path file) {
		try {
			// Windows
			if (System.getProperty("os.name").toLowerCase().contains("win")) {
				new ProcessBuilder("explorer.exe", "/select,", file.toString()).start();
			}
			// macOS
			else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
				new ProcessBuilder("open", "-R", file.toString()).start();
			}
			// Linux
			else {
				new ProcessBuilder("xdg-open", "--select", file.toString()).start();
			}
		} catch (IOException e) {
			LOGGER.error("Failed to open folder and select file: ", e);
		}
	}
}