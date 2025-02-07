package com.flechazo.capability;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;

/**
 * 玩家签到数据提供者类，实现了Component和AutoSyncedComponent接口，
 * 用于管理和序列化玩家的签到数据
 * @author Flechazo
 */
public class PlayerSignInDataProvider implements Component, AutoSyncedComponent {

    // 玩家签到数据实例，使用PlayerSignInData类进行管理
    private IPlayerSignInData playerData;

    /**
     * 获取或创建玩家签到数据实例
     *
     * @return 返回玩家签到数据实例
     */
    public IPlayerSignInData getOrCreateData() {
        if (playerData == null) {
            this.playerData = new PlayerSignInData();
        }
        return this.playerData;
    }

    /**
     * 序列化玩家签到数据为NBT格式
     *
     */
    @Override
    public void writeToNbt(NbtCompound nbt) {
        NbtCompound data = this.getOrCreateData().serializeNBT();
        nbt.put("SignInData", data);
    }

    /**
     * 从NBT格式的数据中反序列化玩家签到数据
     *
     * @param nbt 包含玩家签到数据的NbtCompound对象
     * <p>
     * 该方法实现了玩家签到数据的反序列化，从提供的NBT数据中恢复玩家签到信息
     */
    @Override
    public void readFromNbt(NbtCompound nbt) {
        if (nbt.contains("SignInData")) {
            this.getOrCreateData().deserializeNBT(nbt.getCompound("SignInData"));
        }
    }

    /**
     * 获取玩家签到数据
     *
     * @return 返回玩家签到数据实例
     */
    public IPlayerSignInData getPlayerData() {
        return getOrCreateData();
    }

    /**
     * 设置玩家签到数据
     *
     * @param data 要设置的玩家签到数据
     */
    public void setPlayerData(IPlayerSignInData data) {
        this.playerData = data;
    }
}