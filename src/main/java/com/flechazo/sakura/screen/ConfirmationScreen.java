package com.flechazo.sakura.screen;

import com.flechazo.sakura.enums.EI18nType;
import com.flechazo.sakura.screen.component.IText;
import com.flechazo.sakura.util.AbstractGuiUtils;
import com.flechazo.sakura.util.Component;
import com.flechazo.sakura.util.I18nUtils;
import lombok.NonNull;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

/**
 * 操作确认 Screen
 */
public class ConfirmationScreen extends Screen {

    private final static Component TITLE = Component.literal("ConfirmationScreen");

    /**
     * 父级 Screen
     */
    private final Screen previousScreen;
    /**
     * 标题
     */
    private final IText titleText;
    /**
     * 回调
     */
    private final Runnable onConfirm;
    /**
     * 是否要显示该界面, 若为false则直接关闭当前界面并返回到调用者的 Screen
     */
    private final Supplier <Boolean> shouldClose;


    public ConfirmationScreen(Screen callbackScreen, IText titleText, @NonNull Runnable onConfirm) {
        super(TITLE.toTextComponent());
        this.previousScreen = callbackScreen;
        this.onConfirm = onConfirm;
        this.titleText = titleText;
        this.shouldClose = null;
    }

    public ConfirmationScreen(Screen callbackScreen, IText titleText, @NonNull Runnable onConfirm, Supplier<Boolean> shouldClose) {
        super(TITLE.toTextComponent());
        this.previousScreen = callbackScreen;
        this.onConfirm = onConfirm;
        this.titleText = titleText;
        this.shouldClose = shouldClose;
    }

    @Override
    protected void init() {
        if (this.shouldClose != null && Boolean.TRUE.equals(this.shouldClose.get()))
            MinecraftClient.getInstance().setScreen(previousScreen);
        // 创建提交按钮
        ButtonWidget submitButton = AbstractGuiUtils.newButton(this.width / 2 + 5, this.height / 2 + 10, 95, 20, Component.translatableClient(EI18nType.OPTION, "confirm"), button -> {
            onConfirm.run();
            MinecraftClient.getInstance().setScreen(previousScreen);
        });
        this.addDrawableChild(submitButton);
        // 创建取消按钮
        this.addDrawableChild(AbstractGuiUtils.newButton(this.width / 2 - 100, this.height / 2 + 10, 95, 20, Component.translatableClient(EI18nType.OPTION, "cancel"), button -> {
            MinecraftClient.getInstance().setScreen(previousScreen);
        }));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);
        // 绘制背景
        super.render(graphics, mouseX, mouseY, delta);
        // 绘制标题
        AbstractGuiUtils.drawString(titleText.setGraphics(graphics), this.width / 2.0f - 100, this.height / 2.0f - 33);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_4) {
            MinecraftClient.getInstance().setScreen(previousScreen);
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    /**
     * 重写键盘事件
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            MinecraftClient.getInstance().setScreen(previousScreen);
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
