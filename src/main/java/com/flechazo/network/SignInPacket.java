package com.flechazo.network;

import com.flechazo.capability.IPlayerSignInData;
import com.flechazo.capability.PlayerSignInDataComponent;
import com.flechazo.enums.ESignInType;
import com.flechazo.event.ServerEventHandler;
import com.flechazo.rewards.RewardManager;
import com.flechazo.util.DateUtils;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Date;

/**
 * 签到数据包
 * @author Flechazo
 */
public class SignInPacket {
    private final Date signInTime;
    private final boolean autoRewarded;
    private final ESignInType signInType;

    public SignInPacket(Date signInTime, boolean autoRewarded, ESignInType signInType) {
        this.signInTime = signInTime;
        this.autoRewarded = signInType.equals(ESignInType.REWARD) || autoRewarded;
        this.signInType = signInType;
    }

    public SignInPacket(PacketByteBuf buf) {
        this.signInTime = DateUtils.format(buf.readString());
        this.autoRewarded = buf.readBoolean();
        this.signInType = ESignInType.valueOf(buf.readInt());
    }

    public void toBytes(PacketByteBuf buf) {
        buf.writeString(DateUtils.toDateTimeString(signInTime));
        buf.writeBoolean(autoRewarded);
        buf.writeInt(signInType.getCode());
    }

    public static void handle(SignInPacket packet, ServerPlayerEntity player) {
        if (player != null) {
            // 获取玩家的签到数据组件
            PlayerSignInDataComponent component = player.getComponent(ServerEventHandler.PLAYER_DATA);
            // 获取玩家的签到数据
            IPlayerSignInData data = component.getPlayerData();
            // 设置是否自动领取奖励
            data.setAutoRewarded(packet.autoRewarded);
            // 如果是补签,则进行补签处理
            if (packet.signInType == ESignInType.RE_SIGN_IN) {
                RewardManager.compensate(player, packet);
            } else {
                // 否则进行普通签到
                RewardManager.signIn(player, packet);
            }
        }
    }

    public Date getSignInTime() {
        return signInTime;
    }

    public boolean isAutoRewarded() {
        return autoRewarded;
    }

    public ESignInType getSignInType() {
        return signInType;
    }
}