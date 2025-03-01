package com.flechazo.sakura.util;

import com.flechazo.sakura.SakuraSignInFabric;
import com.flechazo.sakura.config.ServerConfig;
import com.flechazo.sakura.enums.ERewardRule;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.version.VersionInterval;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class SakuraUtils {

    // region 玩家与玩家背包

    /**
     * 获取随机玩家
     */
    public static ServerPlayerEntity getRandomPlayer() {
        try {
            List<ServerPlayerEntity> players = SakuraSignInFabric.getServerInstance().getPlayerManager().getPlayerList();
            return players.get(new Random().nextInt(players.size()));
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 获取随机玩家UUID
     */
    public static UUID getRandomPlayerUUID() {
        PlayerEntity randomPlayer = getRandomPlayer();
        return randomPlayer != null ? randomPlayer.getUuid() : null;
    }

    /**
     * 通过UUID获取对应的玩家
     *
     * @param uuid 玩家UUID
     */
    public static ServerPlayerEntity getPlayer(UUID uuid) {
        try {
            if (MinecraftClient.getInstance().world != null) {
                return Objects.requireNonNull(MinecraftClient.getInstance().world.getServer()).getPlayerManager().getPlayer(uuid);
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 移除玩家背包中的指定物品
     *
     * @param player       玩家
     * @param itemToRemove 要移除的物品
     * @return 是否全部移除成功
     */
    public static boolean removeItemFromPlayerInventory(ServerPlayerEntity player, ItemStack itemToRemove) {
        PlayerInventory inventory = player.getInventory();

        // 剩余要移除的数量
        int remainingAmount = itemToRemove.getCount();
        // 记录成功移除的物品数量，以便失败时进行回滚
        int successfullyRemoved = 0;

        // 遍历玩家背包的所有插槽
        for (int i = 0; i < inventory.size(); i++) {
            // 获取背包中的物品
            ItemStack stack = inventory.getStack(i);
            ItemStack copy = itemToRemove.copy();
            copy.setCount(stack.getCount());

            // 如果插槽中的物品是目标物品
            if (ItemStack.areEqual(stack, copy)) {
                // 获取当前物品堆叠的数量
                int stackSize = stack.getCount();

                // 如果堆叠数量大于或等于剩余需要移除的数量
                if (stackSize >= remainingAmount) {
                    // 移除指定数量的物品
                    stack.decrement(remainingAmount);
                    // 记录成功移除的数量
                    successfullyRemoved += remainingAmount;
                    // 移除完毕
                    remainingAmount = 0;
                    break;
                } else {
                    // 移除该堆所有物品
                    stack.setCount(0);
                    // 记录成功移除的数量
                    successfullyRemoved += stackSize;
                    // 减少剩余需要移除的数量
                    remainingAmount -= stackSize;
                }
            }
        }

        // 如果没有成功移除所有物品，撤销已移除的部分
        if (remainingAmount > 0) {
            // 创建副本并还回成功移除的物品
            ItemStack copy = itemToRemove.copy();
            copy.setCount(successfullyRemoved);
            // 将已移除的物品添加回背包
            player.getInventory().insertStack(copy);
        }

        // 是否成功移除所有物品
        return remainingAmount == 0;
    }

    public static List<ItemStack> getPlayerItemList(ServerPlayerEntity player) {
        List<ItemStack> result = new ArrayList<>();
        if (player != null) {
            result.addAll(player.getInventory().main);
            result.addAll(player.getInventory().armor);
            result.addAll(player.getInventory().offHand);
            result = result.stream().filter(itemStack -> !itemStack.isEmpty() && itemStack.getItem() != Items.AIR).collect(Collectors.toList());
        }
        return result;
    }

    // endregion 玩家与玩家背包

    // region 消息相关

    /**
     * 广播消息
     *
     * @param player  发送者
     * @param message 消息
     */
    public static void broadcastMessage(ServerPlayerEntity player, Component message) {
        player.server.getPlayerManager().broadcast(Text.translatable("chat.type.announcement", player.getDisplayName(), message.toChatComponent()), false);
    }

    /**
     * 广播消息
     *
     * @param server  发送者
     * @param message 消息
     */
    public static void broadcastMessage(MinecraftServer server, Component message) {
        server.getPlayerManager().broadcast(Text.translatable("chat.type.announcement", "Server", message.toChatComponent()), false);
    }

    /**
     * 发送消息
     *
     * @param player  玩家
     * @param message 消息
     */
    public static void sendMessage(ServerPlayerEntity player, Component message) {
        // 发送翻译键,让客户端处理翻译
        player.sendMessageToClient(message.toTranslatedTextComponent(), false);
    }

    /**
     * 发送消息
     *
     * @param player  玩家
     * @param message 消息
     */
    public static void sendMessage(ClientPlayerEntity player, Component message) {
        player.sendMessage(message.toChatComponent(getClientLanguage()));
    }

    /**
     * 发送消息
     *
     * @param player  玩家
     * @param message 消息
     */
    public static void sendMessage(ServerPlayerEntity player, String message) {
        player.sendMessageToClient(Component.literal(message).toTranslatedTextComponent(), false);
    }

    /**
     * 发送翻译消息
     *
     * @param player 玩家
     * @param key    翻译键
     * @param args   参数
     */
    public static void sendTranslatableMessage(ServerPlayerEntity player, String key, Object... args) {
        // 发送翻译键,让客户端处理翻译
        player.sendMessageToClient(Component.translatable(key, args).toTranslatedTextComponent(), false);
    }

    // endregion 消息相关

    // region 权限相关

    public static int getRewardPermissionLevel(ERewardRule rule) {
        return switch (rule) {
            case BASE_REWARD -> ServerConfig.getPERMISSION_BASE_REWARD();
            case CONTINUOUS_REWARD -> ServerConfig.getPERMISSION_CONTINUOUS_REWARD();
            case CYCLE_REWARD -> ServerConfig.getPERMISSION_CYCLE_REWARD();
            case YEAR_REWARD -> ServerConfig.getPERMISSION_YEAR_REWARD();
            case MONTH_REWARD -> ServerConfig.getPERMISSION_MONTH_REWARD();
            case WEEK_REWARD -> ServerConfig.getPERMISSION_WEEK_REWARD();
            case DATE_TIME_REWARD -> ServerConfig.getPERMISSION_DATE_TIME_REWARD();
            case CUMULATIVE_REWARD -> ServerConfig.getPERMISSION_CUMULATIVE_REWARD();
            case RANDOM_REWARD -> ServerConfig.getPERMISSION_RANDOM_REWARD();
            case CDK_REWARD -> ServerConfig.getPERMISSION_CDK_REWARD();
        };
    }

    // endregion 权限相关

    // region 杂项

//    public static String getPlayerLanguage(ServerPlayerEntity player) {
//        return player.getWorld().getServer().getLanguage();
//    }

    /**
     * 获取语言设置
     * 在客户端返回客户端语言,在服务端返回默认语言
     */
    public static String getClientLanguage() {
        try {
            // 尝试获取客户端语言
            return MinecraftClient.getInstance().getLanguageManager().getLanguage();
        } catch (Exception e) {
            // 在服务端环境下返回默认语言
            return "en_us";
        }
    }


    /**
     * 获取当前mod支持的mc版本
     *
     * @return 主版本*1000000+次版本*1000+修订版本， 如 1.16.5 -> 1 * 1000000 + 16 * 1000 + 5 = 10016005
     */
    public static int getMcVersion() {
        int version = 0;
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(SakuraSignInFabric.MOD_ID);
        if (container.isPresent()) {
            ModMetadata metadata = container.get().getMetadata();
            List<ModDependency> deps = metadata.getDependencies().stream()
                    .filter(dep -> dep.getModId().equals("minecraft"))
                    .toList();
            
            if (!deps.isEmpty()) {
                ModDependency mcDep = deps.get(0);
                List<VersionInterval> intervals = mcDep.getVersionIntervals();
                if (!intervals.isEmpty()) {
                    Version minVersion = intervals.get(0).getMin();
                    if (minVersion != null) {
                        String[] parts = minVersion.getFriendlyString().split("\\.");
                        if (parts.length >= 3) {
                            try {
                                int majorVersion = Integer.parseInt(parts[0]);
                                int minorVersion = Integer.parseInt(parts[1]);
                                int incrementalVersion = Integer.parseInt(parts[2]);
                                version = majorVersion * 1000000 + minorVersion * 1000 + incrementalVersion;
                            } catch (NumberFormatException e) {
                                // 版本解析失败时使用默认值
                            }
                        }
                    }
                }
            }
        }
        return version;
    }

    /**
     * 获取玩家当前位置的环境亮度
     *
     * @param player 当前玩家实体
     * @return 当前环境亮度（范围0-15）
     */
    public static int getEnvironmentBrightness(PlayerEntity player) {
        int result = 0;
        if (player != null) {
            World world = player.getWorld();
            BlockPos pos = player.getBlockPos();
            // 获取基础的天空光亮度和方块光亮度
            int skyLight = world.getLightLevel(LightType.SKY, pos);
            int blockLight = world.getLightLevel(LightType.BLOCK, pos);
            // 获取世界时间、天气和维度的影响
            boolean isDay = world.isDay();
            boolean isRaining = world.isRaining();
            boolean isThundering = world.isThundering();
            boolean isUnderground = !world.isSkyVisible(pos);
            // 判断世界维度（地表、下界、末地）
            if (world.getRegistryKey() == World.OVERWORLD) {
                // 如果在地表
                if (!isUnderground) {
                    if (isDay) {
                        // 白天地表：最高亮度
                        result = isThundering ? 6 : isRaining ? 9 : 15;
                    } else {
                        // 夜晚地表
                        // 获取月相，0表示满月，4表示新月
                        int moonPhase = world.getMoonPhase();
                        result = getMoonBrightness(moonPhase, isThundering, isRaining);
                    }
                } else {
                    // 地下环境
                    // 没有光源时最黑，有光源则受距离影响
                    result = Math.max(Math.min(blockLight, 12), 0);
                }
            } else if (world.getRegistryKey() == World.NETHER) {
                // 下界亮度较暗，但部分地方有熔岩光源
                // 近光源则亮度提升，但不会超过10
                result = Math.min(7 + blockLight / 2, 10);
            } else if (world.getRegistryKey() == World.END) {
                // 末地亮度通常较暗
                // 即使贴近光源，末地的亮度上限设为10
                result = Math.min(6 + blockLight / 2, 10);
            } else {
                result = Math.max(skyLight, blockLight);
            }
        }
        // 其他维度或者无法判断的情况，返回环境和方块光的综合值
        return result;
    }

    /**
     * 根据月相、天气等条件获取夜间月光亮度
     *
     * @param moonPhase    月相（0到7，0为满月，4为新月）
     * @param isThundering 是否雷暴
     * @param isRaining    是否下雨
     * @return 夜间月光亮度
     */
    private static int getMoonBrightness(int moonPhase, boolean isThundering, boolean isRaining) {
        if (moonPhase == 0) {
            // 满月
            return isThundering ? 3 : isRaining ? 5 : 9;
        } else if (moonPhase == 4) {
            // 新月（最暗）
            return isThundering ? 1 : 2;
        } else {
            // 其他月相，亮度随月相变化逐渐减小
            int moonLight = 9 - moonPhase;
            return isThundering ? Math.max(moonLight - 3, 1) : isRaining ? Math.max(moonLight - 2, 1) : moonLight;
        }
    }

    public static String getRewardRuleI18nKeyName(ERewardRule rule) {
        return switch (rule) {
            case BASE_REWARD -> "reward_base";
            case CONTINUOUS_REWARD -> "reward_continuous";
            case CYCLE_REWARD -> "reward_cycle";
            case YEAR_REWARD -> "reward_year";
            case MONTH_REWARD -> "reward_month";
            case WEEK_REWARD -> "reward_week";
            case DATE_TIME_REWARD -> "reward_time";
            case CUMULATIVE_REWARD -> "reward_cumulative";
            case RANDOM_REWARD -> "reward_random";
            case CDK_REWARD -> "reward_cdk";
        };
    }


    // endregion 杂项
}
