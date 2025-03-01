package com.flechazo.sakura.capability;

import com.flechazo.sakura.config.KeyValue;
import com.flechazo.sakura.event.ServerEventHandler;
import com.flechazo.sakura.util.CollectionUtils;
import com.flechazo.sakura.util.DateUtils;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import lombok.NonNull;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 玩家签到数据
 * @author Flechazo
 */
public class PlayerSignInData implements IPlayerSignInData, Component, AutoSyncedComponent, PlayerComponent<PlayerSignInData> {
    private final AtomicInteger totalSignInDays = new AtomicInteger();
    private final AtomicInteger continuousSignInDays = new AtomicInteger();
    private Date lastSignInTime;
    private final AtomicInteger signInCard = new AtomicInteger();
    private boolean autoRewarded;
    private List<SignInRecord> signInRecords;
    private List<KeyValue<String, KeyValue<Date, Boolean>>> cdkRecords;

    @Override
    public int getTotalSignInDays() {
        return this.totalSignInDays.get();
    }

    @Override
    public void setTotalSignInDays(int days) {
        this.totalSignInDays.set(days);
    }

    @Override
    public int plusTotalSignInDays() {
        return this.totalSignInDays.incrementAndGet();
    }

    @Override
    public int getContinuousSignInDays() {
        return this.continuousSignInDays.get();
    }

    @Override
    public void setContinuousSignInDays(int days) {
        this.continuousSignInDays.set(days);
    }

    @Override
    public int plusContinuousSignInDays() {
        return this.continuousSignInDays.incrementAndGet();
    }

    @Override
    public void resetContinuousSignInDays() {
        this.continuousSignInDays.set(1);
    }

    @Override
    public @NonNull Date getLastSignInTime() {
        return this.lastSignInTime = this.lastSignInTime == null ? DateUtils.getDate(0, 1, 1) : this.lastSignInTime;
    }

    @Override
    public void setLastSignInTime(Date time) {
        this.lastSignInTime = time;
    }

    @Override
    public int getSignInCard() {
        return this.signInCard.get();
    }

    @Override
    public int plusSignInCard() {
        return this.signInCard.incrementAndGet();
    }

    @Override
    public int plusSignInCard(int num) {
        return this.signInCard.addAndGet(num);
    }

    @Override
    public int subSignInCard() {
        return this.signInCard.decrementAndGet();
    }

    @Override
    public int subSignInCard(int num) {
        return this.signInCard.addAndGet(-num);
    }

    @Override
    public void setSignInCard(int num) {
        this.signInCard.set(num);
    }

    @Override
    public boolean isAutoRewarded() {
        return this.autoRewarded;
    }

    @Override
    public void setAutoRewarded(boolean autoRewarded) {
        this.autoRewarded = autoRewarded;
    }

    @Override
    public @NonNull List<SignInRecord> getSignInRecords() {
       if (this.signInRecords == null) {
           this.signInRecords = new ArrayList<>();
       } else {
           this.signInRecords.removeIf(Objects::isNull);
       }
       return this.signInRecords;
    }

    @Override
    public void setSignInRecords(List<SignInRecord> records) {
        if (records == null) {
            records = new ArrayList<>();
        } else {
            records = new ArrayList<>(records);
            records.removeIf(Objects::isNull);
        }
        this.signInRecords = records;
    }

    @Override
    public @NonNull List<KeyValue<String, KeyValue<Date, Boolean>>> getCdkErrorRecords() {
        if (this.cdkRecords == null) {
            this.cdkRecords = new ArrayList<>();
        } else {
            this.cdkRecords.removeIf(Objects::isNull);
        }
        return this.cdkRecords;
    }

    @Override
    public void setCdkErrorRecords(List<KeyValue<String, KeyValue<Date, Boolean>>> cdkRecords) {
        if (cdkRecords == null) {
            cdkRecords = new ArrayList<>();
        } else {
            cdkRecords = new ArrayList<>(cdkRecords);
            cdkRecords.removeIf(Objects::isNull);
        }
        this.cdkRecords = cdkRecords;
    }

    public void writeToBuffer(PacketByteBuf buffer) {
        buffer.writeInt(this.getTotalSignInDays());
        buffer.writeInt(this.getContinuousSignInDays());
        buffer.writeString(DateUtils.toDateTimeString(this.getLastSignInTime()));
        buffer.writeInt(this.getSignInCard());
        buffer.writeBoolean(this.isAutoRewarded());
        buffer.writeInt(this.getSignInRecords().size());
        for (SignInRecord record : this.getSignInRecords()) {
            buffer.writeNbt(record.writeToNBT());
        }
        buffer.writeInt(this.getCdkErrorRecords().size());
        for (KeyValue<String, KeyValue<Date, Boolean>> record : this.getCdkErrorRecords()) {
            buffer.writeString(record.getKey());
            buffer.writeString(DateUtils.toDateTimeString(record.getValue().getKey()));
            buffer.writeBoolean(record.getValue().getValue());
        }
    }

    public void readFromBuffer(PacketByteBuf buffer) {
        this.totalSignInDays.set(buffer.readInt());
        this.continuousSignInDays.set(buffer.readInt());
        this.lastSignInTime = DateUtils.format(buffer.readString());
        this.signInCard.set(buffer.readInt());
        this.autoRewarded = buffer.readBoolean();
        this.signInRecords = new ArrayList<>();
        for (int i = 0; i < buffer.readInt(); i++) {
            this.signInRecords.add(SignInRecord.readFromNBT(Objects.requireNonNull(buffer.readNbt())));
        }
        this.cdkRecords = new ArrayList<>();
        for (int i = 0; i < buffer.readInt(); i++) {
            this.cdkRecords.add(new KeyValue<>(buffer.readString(), new KeyValue<>(DateUtils.format(buffer.readString()), buffer.readBoolean())));
        }
    }

    public void copyFrom(IPlayerSignInData capability) {
        this.totalSignInDays.set(capability.getTotalSignInDays());
        this.continuousSignInDays.set(capability.getContinuousSignInDays());
        this.lastSignInTime = capability.getLastSignInTime();
        this.signInCard.set(capability.getSignInCard());
        this.autoRewarded = capability.isAutoRewarded();
        this.setSignInRecords(capability.getSignInRecords());
        this.setCdkErrorRecords(capability.getCdkErrorRecords());
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("totalSignInDays", this.getTotalSignInDays());
        tag.putInt("continuousSignInDays", this.getContinuousSignInDays());
        tag.putString("lastSignInTime", DateUtils.toDateTimeString(this.getLastSignInTime()));
        tag.putInt("signInCard", this.getSignInCard());
        tag.putBoolean("autoRewarded", this.isAutoRewarded());
        // 序列化签到记录
        NbtList recordsNBT = new NbtList();
        for (SignInRecord record : this.getSignInRecords()) {
            recordsNBT.add(record.writeToNBT());
        }
        tag.put("signInRecords", recordsNBT);
        // 序列化CDK输错记录
        NbtList cdkRecordsNBT = new NbtList();
        for (KeyValue<String, KeyValue<Date, Boolean>> record : this.getCdkErrorRecords()) {
            NbtCompound cdkErrorRecordNBT = new NbtCompound();
            cdkErrorRecordNBT.putString("key", record.getKey());
            cdkErrorRecordNBT.putString("date", DateUtils.toDateTimeString(record.getValue().getKey()));
            cdkErrorRecordNBT.putBoolean("value", record.getValue().getValue());
            cdkRecordsNBT.add(cdkErrorRecordNBT);
        }
        tag.put("cdkRecords", cdkRecordsNBT);
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        this.setTotalSignInDays(nbt.getInt("totalSignInDays"));
        this.setContinuousSignInDays(nbt.getInt("continuousSignInDays"));
        this.setLastSignInTime(DateUtils.format(nbt.getString("lastSignInTime")));
        this.setSignInCard(nbt.getInt("signInCard"));
        this.setAutoRewarded(nbt.getBoolean("autoRewarded"));
        // 反序列化签到记录
        NbtList recordsNBT = nbt.getList("signInRecords", 10); // 10 是 CompoundNBT 的类型ID
        List<SignInRecord> records = new ArrayList<>();
        for (int i = 0; i < recordsNBT.size(); i++) {
            records.add(SignInRecord.readFromNBT(recordsNBT.getCompound(i)));
        }
        this.setSignInRecords(records);
        NbtList cdkRecordsNBT = nbt.getList("cdkRecords", 10); // 10 是 CompoundNBT 的类型ID
        List<KeyValue<String, KeyValue<Date, Boolean>>> cdkRecords = new ArrayList<>();
        for (int i = 0; i < cdkRecordsNBT.size(); i++) {
            NbtCompound cdkErrorRecordNBT = cdkRecordsNBT.getCompound(i);
            cdkRecords.add(new KeyValue<>(cdkErrorRecordNBT.getString("key"), new KeyValue<>(DateUtils.format(cdkErrorRecordNBT.getString("date")), cdkErrorRecordNBT.getBoolean("value"))));
        }
        this.setCdkErrorRecords(cdkRecords);
    }

    @Override
    public NbtCompound serializeNBT() {
        NbtCompound tag = new NbtCompound();
        writeToNbt(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(NbtCompound nbt) {
        readFromNbt(nbt);
    }

    @Override
    public void save(ServerPlayerEntity player) {
        if (player instanceof ComponentProvider provider) {
            provider.getComponent(ServerEventHandler.PLAYER_DATA).copyFrom(this);
        }
    }

    @Override
    public boolean shouldCopyForRespawn(boolean lossless, boolean keepInventory, boolean sameCharacter) {
        return true; // 玩家重生时保留签到数据
    }

    @Override
    public void copyForRespawn(@NotNull PlayerSignInData original, boolean lossless, boolean keepInventory, boolean sameCharacter) {
        this.copyFrom(original);
    }
}
