package com.flechazo.sakura.capability;

import com.flechazo.sakura.rewards.RewardList;
import com.flechazo.sakura.util.CollectionUtils;
import com.flechazo.sakura.util.DateUtils;
import lombok.Data;
import lombok.NonNull;
import net.minecraft.nbt.NbtCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;

import static com.flechazo.sakura.config.RewardOptionDataManager.GSON;

@Data
public class SignInRecord implements Serializable, Cloneable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SignInRecord.class);

    /**
     * 补偿后时间(签到时间+签到冷却刷新时间)
     */
    @NonNull
    private Date compensateTime;
    /**
     * 签到时间
     */
    @NonNull
    private Date signInTime;
    /**
     * 签到玩家
     */
    @NonNull
    private String signInUUID;
    /**
     * 奖励是否领取
     */
    private boolean rewarded;
    /**
     * 签到物品奖励
     */
    @NonNull
    private RewardList rewardList;

    public SignInRecord() {
        this.compensateTime = new Date();
        this.signInTime = new Date();
        this.signInUUID = "";
        this.rewarded = false;
        this.rewardList = new RewardList();
    }

    // 序列化到 NBT
    public NbtCompound writeToNBT() {
        NbtCompound tag = new NbtCompound();
        tag.putString("compensateTime", DateUtils.toDateTimeString(compensateTime));
        tag.putString("signInTime", DateUtils.toDateTimeString(signInTime));
        tag.putString("signInUUID", signInUUID);
        tag.putBoolean("rewarded", rewarded);
        tag.putString("rewardList", GSON.toJson(rewardList.toJsonArray()));
        return tag;
    }

    // 反序列化方法
    public static SignInRecord readFromNBT(NbtCompound tag) {
        SignInRecord record = new SignInRecord();
        // 读取简单字段
        record.compensateTime = DateUtils.format(tag.getString("compensateTime"));
        record.signInTime = DateUtils.format(tag.getString("signInTime"));
        record.signInUUID = tag.getString("signInUUID");
        record.rewarded = tag.getBoolean("rewarded");

        // 反序列化奖励列表
        String rewardListString = tag.getString("rewardList");
        try {
            record.rewardList = GSON.fromJson(rewardListString, RewardList.class);
            if (record.rewardList == null) {
                record.rewardList = new RewardList();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize reward list: {}", e.getMessage());
            record.rewardList = new RewardList();
        }
        return record;
    }

    @Override
    public SignInRecord clone() {
        try {
            SignInRecord cloned = (SignInRecord) super.clone();
            cloned.compensateTime = (Date) this.compensateTime.clone();
            cloned.signInTime = (Date) this.signInTime.clone();
            if (CollectionUtils.isNotNullOrEmpty(this.rewardList))
                cloned.rewardList = this.rewardList.clone();
            else
                cloned.rewardList = new RewardList();
            return cloned;
        } catch (Exception e) {
            return new SignInRecord();
        }
    }
}
