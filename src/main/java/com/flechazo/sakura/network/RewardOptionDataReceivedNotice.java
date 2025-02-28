package com.flechazo.sakura.network;

import com.flechazo.sakura.enums.EI18nType;
import com.flechazo.sakura.screen.component.NotificationManager;
import com.flechazo.sakura.util.Component;
import net.minecraft.network.PacketByteBuf;

/**
 * 奖励选项数据接收通知数据包
 * @author Flechazo
 */
public class RewardOptionDataReceivedNotice {
    private final boolean success;

    public RewardOptionDataReceivedNotice(boolean success) {
        this.success = success;
    }

    public RewardOptionDataReceivedNotice(PacketByteBuf buf) {
        this.success = buf.readBoolean();
    }

    public void toBytes(PacketByteBuf buf) {
        buf.writeBoolean(this.success);
    }

    public static void handle(RewardOptionDataReceivedNotice packet) {
        NotificationManager.Notification notification;
        if (packet.success) {
            notification = NotificationManager.Notification.ofComponentWithBlack(Component.translatable(EI18nType.MESSAGE, "reward_option_upload_success"));
        } else {
            notification = NotificationManager.Notification.ofComponentWithBlack(Component.translatable(EI18nType.MESSAGE, "reward_option_upload_failed")).setBgColor(0x88FF5555);
        }
        NotificationManager.get().addNotification(notification);
    }
}
