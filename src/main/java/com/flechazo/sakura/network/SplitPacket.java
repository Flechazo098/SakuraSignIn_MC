package com.flechazo.sakura.network;

import com.flechazo.sakura.SakuraSignInFabric;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.PacketByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
public abstract class SplitPacket {
    private static final Logger LOGGER = LogManager.getLogger();
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
        LOGGER.debug("Received split packet: id={}, total={}, sort={}", this.id, this.total, this.sort);
    }

    public static <T extends SplitPacket> List<T> handle(T packet) {
        List<T> result = new ArrayList<>();
        Map<String, List<? extends SplitPacket>> packetCache = SakuraSignInFabric.getPacketCache();
        LOGGER.debug("Handling split packet: id={}, total={}, sort={}, cache size={}", packet.getId(), packet.getTotal(), packet.getSort(), packetCache.size());
        
        // 确保键存在，并初始化为空列表
        @SuppressWarnings("unchecked")
        List<T> splitPackets = (List<T>) packetCache.computeIfAbsent(packet.getId(), k -> new ArrayList<>());
        splitPackets.add(packet);
        LOGGER.debug("Added packet to cache: id={}, received={}, total={}", packet.getId(), splitPackets.size(), packet.getTotal());
        
        if (splitPackets.size() == packet.getTotal()) {
            LOGGER.debug("All packets received for id={}, sorting {} packets", packet.getId(), splitPackets.size());
            // 对列表进行排序
            result = splitPackets.stream()
                    .sorted(Comparator.comparingInt(SplitPacket::getSort))
                    .collect(Collectors.toList());
            // 清理缓存
            packetCache.remove(packet.getId());
            // 清理过期缓存
            cleanExpiredCache(packetCache);
            LOGGER.debug("Processed all packets for id={}, remaining cache size={}", packet.getId(), packetCache.size());
        }
        return result;
    }

    private static void cleanExpiredCache(Map<String, List<? extends SplitPacket>> packetCache) {
        int beforeSize = packetCache.size();
        packetCache.keySet().stream()
                .filter(SplitPacket :: isExpired)
                .forEach(packetCache::remove);
        int afterSize = packetCache.size();
        if (beforeSize != afterSize) {
            LOGGER.debug("Cleaned {} expired cache entries", beforeSize - afterSize);
        }
    }

    private static boolean isExpired(String key) {
        return Math.abs(System.currentTimeMillis() - Long.parseLong(key.split("\\.")[0])) > CACHE_EXPIRE_TIME;
    }

    public void toBytes(PacketByteBuf buf) {
        buf.writeString(id);
        buf.writeInt(total);
        buf.writeInt(sort);
        LOGGER.debug("Writing split packet: id={}, total={}, sort={}", this.id, this.total, this.sort);
    }

    public abstract int getChunkSize();
}