package com.flechazo.network;

import com.flechazo.SakuraSignInFabric;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ItemStackPacket类用于在网络中传输ItemStack数据
 * 它提供了将ItemStack序列化和反序列化的方法,以便于网络传输
 */
public class ItemStackPacket {
    private static final Logger LOGGER = LogManager.getLogger();
    
    // 存储要传输的ItemStack对象
    private final ItemStack itemStack;

    /**
     * 构造函数,用于创建一个包含指定ItemStack的ItemStackPacket
     *
     * @param itemStack 要传输的ItemStack对象
     */
    public ItemStackPacket(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * 构造函数,用于从PacketByteBuf中读取数据并重构ItemStackPacket
     *
     * @param buf 包含ItemStack数据的PacketByteBuf
     */
    public ItemStackPacket(PacketByteBuf buf) {
        this.itemStack = buf.readItemStack();
    }

    /**
     * 将ItemStack数据写入PacketByteBuf,准备进行网络传输
     *
     * @param buf 用于存储ItemStack数据的PacketByteBuf
     */
    public void toBytes(PacketByteBuf buf) {
        buf.writeItemStack(itemStack);
    }

    /**
     * 处理物品堆数据包的函数
     * 该函数用于处理来自客户端的物品堆数据包,决定物品是否添加到玩家的库存中,或者以物品实体的形式生成在世界上
     *
     * @param player 发送数据包的玩家实体
     */
    public static void handle(ItemStackPacket packet, ServerPlayerEntity player) {
        if (player != null) {
            // 尝试将物品堆添加到玩家的库存中
            boolean added = player.getInventory().insertStack(packet.itemStack);
            // 如果物品堆无法添加到库存,则以物品实体的形式生成在世界上
            if (!added) {
                World world = player.getWorld();
                ItemEntity itemEntity = new ItemEntity(world, player.getX(), player.getY(), player.getZ(), packet.itemStack);
                world.spawnEntity(itemEntity);
            }
        }
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}