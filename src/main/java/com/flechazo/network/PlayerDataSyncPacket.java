package com.flechazo.network;

import com.flechazo.capability.IPlayerSignInData;
import com.flechazo.capability.PlayerSignInData;
import com.flechazo.capability.SignInRecord;
import com.flechazo.config.ServerConfig;
import com.flechazo.util.CollectionUtils;
import net.minecraft.network.PacketByteBuf;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerDataSyncPacket extends SplitPacket {
    private final UUID playerUUID;
    private final int totalSignInDays;
    private final int continuousSignInDays;
    private final Date lastSignInTime;
    private final int signInCard;
    private final boolean autoRewarded;
    private final List<SignInRecord> signInRecords;

    public PlayerDataSyncPacket(UUID playerUUID, IPlayerSignInData data) {
        super();
        this.playerUUID = playerUUID;
        this.totalSignInDays = data.getTotalSignInDays();
        this.continuousSignInDays = data.getContinuousSignInDays();
        this.lastSignInTime = data.getLastSignInTime();
        this.signInCard = data.getSignInCard();
        this.autoRewarded = data.isAutoRewarded();
        this.signInRecords = data.getSignInRecords();
    }

    public PlayerDataSyncPacket(PacketByteBuf buffer) {
        super(buffer);
        playerUUID = buffer.readUuid();
        this.totalSignInDays = buffer.readInt();
        this.continuousSignInDays = buffer.readInt();
        this.lastSignInTime = buffer.readDate();
        this.signInCard = buffer.readInt();
        this.autoRewarded = buffer.readBoolean();
        int size = buffer.readInt();
        this.signInRecords = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.signInRecords.add(SignInRecord.readFromNBT(Objects.requireNonNull(buffer.readNbt())));
        }
    }

    public PlayerDataSyncPacket(List<PlayerDataSyncPacket> packets) {
        super();
        this.playerUUID = packets.get(0).playerUUID;
        this.totalSignInDays = packets.get(0).totalSignInDays;
        this.continuousSignInDays = packets.get(0).continuousSignInDays;
        this.lastSignInTime = packets.get(0).lastSignInTime;
        this.signInCard = packets.get(0).signInCard;
        this.autoRewarded = packets.get(0).autoRewarded;
        this.signInRecords = packets.stream()
                .map(PlayerDataSyncPacket::getSignInRecords)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(SignInRecord::getSignInTime))
                .collect(Collectors.toList());
    }

    private PlayerDataSyncPacket(UUID playerUUID, int totalSignInDays, int continuousSignInDays, Date lastSignInTime, int signInCard, boolean autoRewarded) {
        super();
        this.playerUUID = playerUUID;
        this.totalSignInDays = totalSignInDays;
        this.continuousSignInDays = continuousSignInDays;
        this.lastSignInTime = lastSignInTime;
        this.signInCard = signInCard;
        this.autoRewarded = autoRewarded;
        this.signInRecords = new ArrayList<>();
    }

    public void toBytes(PacketByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeUuid(playerUUID);
        buffer.writeInt(this.totalSignInDays);
        buffer.writeInt(this.continuousSignInDays);
        buffer.writeDate(this.lastSignInTime);
        buffer.writeInt(this.signInCard);
        buffer.writeBoolean(this.autoRewarded);
        buffer.writeInt(this.signInRecords.size());
        for (SignInRecord record : this.signInRecords) {
            buffer.writeNbt(record.writeToNBT());
        }
    }

    public static void handle(PlayerDataSyncPacket packet) {
        List<PlayerDataSyncPacket> packets = SplitPacket.handle(packet);
        if (CollectionUtils.isNotNullOrEmpty(packets)) {
            ClientProxy.handleSynPlayerData(new PlayerDataSyncPacket(packets));
        }
    }

    @Override
    public int getChunkSize() {
        return ServerConfig.PLAYER_DATA_SYNC_PACKET_SIZE.get();
    }

    /**
     * 将数据包拆分为多个小包
     */
    public List<PlayerDataSyncPacket> split() {
        List<PlayerDataSyncPacket> result = new ArrayList<>();
        for (int i = 0, index = 0; i < signInRecords.size() / getChunkSize() + 1; i++) {
            PlayerDataSyncPacket packet = new PlayerDataSyncPacket(this.playerUUID, this.totalSignInDays, this.continuousSignInDays, this.lastSignInTime, this.signInCard, this.autoRewarded);
            for (int j = 0; j < getChunkSize(); j++) {
                if (index >= signInRecords.size()) break;
                packet.signInRecords.add(this.signInRecords.get(index));
                index++;
            }
            packet.setId(this.getId());
            packet.setSort(i);
            result.add(packet);
        }
        result.forEach(packet -> packet.setTotal(result.size()));
        return result;
    }

    public IPlayerSignInData getData() {
        IPlayerSignInData data = new PlayerSignInData ();
        data.setTotalSignInDays(this.totalSignInDays);
        data.setContinuousSignInDays(this.continuousSignInDays);
        data.setLastSignInTime(this.lastSignInTime);
        data.setSignInCard(this.signInCard);
        data.setAutoRewarded(this.autoRewarded);
        data.setSignInRecords(this.signInRecords);
        return data;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getTotalSignInDays() {
        return totalSignInDays;
    }

    public int getContinuousSignInDays() {
        return continuousSignInDays;
    }

    public Date getLastSignInTime() {
        return lastSignInTime;
    }

    public int getSignInCard() {
        return signInCard;
    }

    public boolean isAutoRewarded() {
        return autoRewarded;
    }

    public List<SignInRecord> getSignInRecords() {
        return signInRecords;
    }
}