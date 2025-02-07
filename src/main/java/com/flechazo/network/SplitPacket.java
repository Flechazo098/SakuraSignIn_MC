package com.flechazo.network;

import com.flechazo.SakuraSignInFabric;
import net.minecraft.network.PacketByteBuf;

import java.util.*;
import java.util.stream.Collectors;

public abstract class SplitPacket {
    private static final long CACHE_EXPIRE_TIME = 1000 * 60 * 5; // 缓存过期时间为5分钟

    /**
     * 分包ID
     */
    private String id;
    /**
     * 总包数
     */
    private int total;
    /**
     * 当前包序号
     */
    private int sort;

    protected SplitPacket() {
        this.id = String.format("%d.%d", System.currentTimeMillis(), new Random().nextInt(999999999));
    }

    protected SplitPacket(PacketByteBuf buf) {
        this.id = buf.readString();
        this.total = buf.readInt();
        this.sort = buf.readInt();
    }

    public static <T extends SplitPacket> List<T> handle(T packet) {
        List<T> result = new ArrayList<>();
        Map<String, List<? extends SplitPacket>> packetCache = SakuraSignInFabric.getPacketCache();
        // 确保键存在，并初始化为空列表
        @SuppressWarnings("unchecked")
        List<T> splitPackets = (List<T>) packetCache.computeIfAbsent(packet.getId(), k -> new ArrayList<>());
        splitPackets.add(packet);
        if (splitPackets.size() == packet.getTotal()) {
            // 对列表进行排序
            result = splitPackets.stream()
                    .sorted(Comparator.comparingInt(SplitPacket::getSort))
                    .collect(Collectors.toList());
            // 清理缓存
            packetCache.remove(packet.getId());
            // 清理过期缓存
            cleanExpiredCache(packetCache);
        }
        return result;
    }

    private static void cleanExpiredCache(Map<String, List<? extends SplitPacket>> packetCache) {
        packetCache.keySet().stream()
                .filter(key -> isExpired(key))
                .forEach(packetCache::remove);
    }

    private static boolean isExpired(String key) {
        return Math.abs(System.currentTimeMillis() - Long.parseLong(key.split("\\.")[0])) > CACHE_EXPIRE_TIME;
    }

    protected void toBytes(PacketByteBuf buf) {
        buf.writeString(id);
        buf.writeInt(total);
        buf.writeInt(sort);
    }

    public abstract int getChunkSize();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }
}