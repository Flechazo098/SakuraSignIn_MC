package com.flechazo.sakura;

import com.flechazo.sakura.command.SignInCommand;
import com.flechazo.sakura.config.KeyValue;
import com.flechazo.sakura.config.RewardOptionDataManager;
import com.flechazo.sakura.config.ServerConfig;
import com.flechazo.sakura.event.ClientEventHandler;
import com.flechazo.sakura.event.ModEventHandler;
import com.flechazo.sakura.event.ServerEventHandler;
import com.flechazo.sakura.network.AdvancementData;
import com.flechazo.sakura.network.ModNetworkHandler;
import com.flechazo.sakura.network.SplitPacket;
import com.flechazo.sakura.rewards.RewardList;
import com.flechazo.sakura.screen.coordinate.TextureCoordinate;
import com.flechazo.sakura.util.DateUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;

public class SakuraSignInFabric implements ModInitializer {
	public static final String MOD_ID = "sakura_sign_in";
	public static final String PNG_CHUNK_NAME = "vacb";

	public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * 服务端实例
	 */
	@Getter
	private static MinecraftServer serverInstance;

	/**
	 * 默认语言
	 */
	public static final String DEFAULT_LANGUAGE = "en_us";


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
	private static List<AdvancementData> advancementData = new ArrayList<>();
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
	private static final Map<String, List<? extends SplitPacket>> packetCache = new ConcurrentHashMap<>();

	/**
	 * 玩家能力同步状态
	 */
	@Getter
	private static final Map<String, Boolean> playerCapabilityStatus = new ConcurrentHashMap<>();

	@Getter
	private static final KeyValue<String, String> clientServerTime = new KeyValue<>(DateUtils.toDateTimeString(new Date(0)), DateUtils.toString(new Date(0)));

	@Getter
	private static final RewardList clipboard = new RewardList();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing SakuraSignInFabric mod...");

		// 初始化配置
		ServerConfig.init();
		LOGGER.info("Server config initialized.");

		// 注册事件处理器
		ModEventHandler.register();
		ServerEventHandler.register();
		LOGGER.info("Server event handlers registered.");

		// 注册网络通道
		ModNetworkHandler.registerPackets();
		LOGGER.info("Network packets registered.");

		// 注册服务器生命周期事件
		ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

		// 注册命令
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			SignInCommand.register(dispatcher);
			LOGGER.info("Sign-in command registered.");
		});

		// 玩家登出事件
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			handlePlayerLogout(handler.getPlayer());
			LOGGER.info("Player logged out event registered.");
		});
		LOGGER.info("SakuraSignInFabric mod initialized successfully.");
	}

	private void onServerStarting(MinecraftServer server) {
		RewardOptionDataManager.loadRewardOption();
		LOGGER.debug("SignIn data loaded.");
	}

	private void onServerStopping(MinecraftServer server) {
		RewardOptionDataManager.saveRewardOption();
		LOGGER.debug("SignIn data saved.");
	}

	private void handlePlayerLogout(PlayerEntity player) {
		LOGGER.debug("Player has logged out.");
		if (player != null) {
			playerCapabilityStatus.remove(player.getUuid().toString());
			if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
				handleClientLogout(player);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	private void handleClientLogout(PlayerEntity player) {
		if (MinecraftClient.getInstance().player != null && 
			MinecraftClient.getInstance().player.getUuid().equals(player.getUuid())) {
			enabled = false;
		}
	}

	public static TextureCoordinate getThemeTextureCoordinate(boolean nonNull) {
		if (nonNull && (themeTextureCoordinate == null || themeTexture == null)) {
			new ClientEventHandler().loadThemeTexture();
		}
		return themeTextureCoordinate;
	}

	@NonNull
	public static TextureCoordinate getThemeTextureCoordinate() {
		return getThemeTextureCoordinate(true);
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

	/**
	 * 设置进度数据
	 * @param data 进度数据
	 */
	public static void setAdvancementData(List<AdvancementData> data) {
		LOGGER.debug("Setting advancement data: {} entries", data == null ? 0 : data.size());
		if (data == null) {
			LOGGER.warn("Received null advancement data, using empty list");
			advancementData = new ArrayList<>();
			return;
		}
		advancementData = new ArrayList<>(data);
		LOGGER.debug("Advancement data updated successfully");
	}

	/**
	 * 获取进度数据
	 * @return 进度数据
	 */
	public static List<AdvancementData> getAdvancementData() {
		if (advancementData == null) {
			LOGGER.warn("Advancement data is null, initializing empty list");
			advancementData = new ArrayList<>();
		}
		return advancementData;
	}
}