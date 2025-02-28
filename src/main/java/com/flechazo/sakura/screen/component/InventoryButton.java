package com.flechazo.sakura.screen.component;

import com.flechazo.sakura.SakuraSignInFabric;
import com.flechazo.sakura.enums.EI18nType;
import com.flechazo.sakura.screen.coordinate.Coordinate;
import com.flechazo.sakura.util.AbstractGuiUtils;
import com.flechazo.sakura.util.Component;
import com.flechazo.sakura.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

/**
 * 库存按钮组件,用于在游戏界面中显示可交互的按钮。
 * 支持拖拽功能和自定义纹理。
 *
 * @author Flechazo
 */
@Getter
@Setter
@Accessors(chain = true)
@Environment(EnvType.CLIENT)
public class InventoryButton extends ClickableWidget {

    /**
     * 按钮是否被按下
     */
    private boolean pressed;
    /**
     * 当前按下的按键
     */
    private int keyCode = -1, modifiers = -1;
    /**
     * 鼠标按下时坐标
     */
    private int mouseButton = -1, mouseClickX = -1, mouseClickY = -1;
    /**
     * 鼠标是否拖动
     */
    private boolean mouseDrag = false;
    /**
     * 按钮坐标
     */
    private int x_, y_;
    /**
     * 屏幕宽高
     */
    private int screenWidth = 427, screenHeight = 240;
    /**
     * 按钮的UV坐标
     */
    private double u0, v0, uWidth, vHeight, totalWidth, totalHeight;
    /**
     * 按钮点击事件
     */
    private Consumer<InventoryButton> onClick;
    /**
     * 当鼠标拖动结束
     */
    private Consumer<Coordinate> onDragEnd;

    public InventoryButton(int x, int y, int width, int height, String title) {
        super(x, y, width, height, Component.literal(title).toTextComponent());
        this.x_ = x;
        this.y_ = y;
    }

    /**
     * 设置按钮的UV坐标和纹理尺寸
     *
     * @param coordinate 包含UV坐标的对象
     * @param totalWidth 纹理总宽度
     * @param totalHeight 纹理总高度
     * @return 当前按钮实例,支持链式调用
     */
    public InventoryButton setUV(Coordinate coordinate, int totalWidth, int totalHeight) {
        return setUV(coordinate.getU0(), coordinate.getV0(), coordinate.getUWidth(), coordinate.getVHeight(), totalWidth, totalHeight);
    }

    /**
     * 设置按钮的UV坐标和纹理尺寸
     *
     * @param u0 U坐标起点
     * @param v0 V坐标起点
     * @param uWidth U方向宽度
     * @param vHeight V方向高度
     * @param totalWidth 纹理总宽度
     * @param totalHeight 纹理总高度
     * @return 当前按钮实例,支持链式调用
     */
    public InventoryButton setUV(double u0, double v0, double uWidth, double vHeight, int totalWidth, int totalHeight) {
        this.u0 = u0;
        this.v0 = v0;
        this.uWidth = uWidth;
        this.vHeight = vHeight;
        this.totalWidth = totalWidth;
        this.totalHeight = totalHeight;
        return this;
    }

    /**
     * 渲染按钮
     * 包括按钮纹理、悬停效果和拖拽提示
     *
     * @param graphics 绘图上下文
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param partialTicks 部分刻
     */
    @Override
    @ParametersAreNonnullByDefault
    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        // 无法直接监听鼠标移动事件, 直接在绘制时调用
        this.mouseMoved(mouseX, mouseY);
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen != null) {
            this.screenWidth = screen.width;
            this.screenHeight = screen.height;
        }
        // 绘制自定义纹理
        int offset = this.hovered && !this.mouseDrag ? 1 : 0;
        AbstractGuiUtils.setDepth(graphics, AbstractGuiUtils.EDepth.TOOLTIP);
        AbstractGuiUtils.blit(graphics, SakuraSignInFabric.getThemeTexture(), super.getX() - offset, super.getY() - offset, this.width + offset * 2, this.height + offset * 2, (int) this.u0, (int) this.v0, (int) this.uWidth, (int) this.vHeight, (int) totalWidth, (int) totalHeight);
        AbstractGuiUtils.resetDepth(graphics);
        if (this.mouseDrag) {
            IText text;
            if (this.modifiers == GLFW.GLFW_MOD_ALT) {
                text = IText.literal(String.format("X: %s\nY: %s"
                        , StringUtils.toPercent((super.getX() - 2.0d) / (screenWidth - this.width - 2.0d * 2))
                        , StringUtils.toPercent((super.getY() - 2.0d) / (screenHeight - this.height - 2.0d * 2))));
            } else {
                text = IText.literal(String.format("X: %d\nY: %d", super.getX(), super.getY()));
            }
            text.setGraphics(graphics);
            AbstractGuiUtils.drawPopupMessage(text, super.getX() + (AbstractGuiUtils.multilineTextWidth(text) - this.width) / 2, super.getY() + this.height / 2, screenWidth, screenHeight);
        } else if (this.hovered) {
            if (this.modifiers == GLFW.GLFW_MOD_SHIFT) {
                AbstractGuiUtils.drawPopupMessage(IText.translatable(EI18nType.TIPS, "drag_inventory_button").setGraphics(graphics), mouseX, mouseY, screenWidth, screenHeight);
            } else {
                AbstractGuiUtils.drawPopupMessage(IText.fromTextComponent(this.getMessage().copy()).setGraphics(graphics), mouseX, mouseY, screenWidth, screenHeight);
            }
        }
    }

    /**
     * 处理鼠标点击事件
     *
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param button 按下的鼠标按键
     * @return 是否处理了此事件
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.pressed = this.isMouseOver(mouseX, mouseY);
        this.mouseButton = button;
        this.mouseClickX = (int) mouseX;
        this.mouseClickY = (int) mouseY;
        return this.pressed;
    }

    /**
     * 处理鼠标释放事件
     * 包括拖拽结束和点击事件的处理
     *
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param button 释放的鼠标按键
     * @return 是否处理了此事件
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean flag = false;
        this.hovered = this.isMouseOver(mouseX, mouseY);
        if (this.pressed && this.mouseDrag) {
            if (this.modifiers == GLFW.GLFW_MOD_ALT) {
                Screen screen = MinecraftClient.getInstance().currentScreen;
                if (screen != null) {
                    this.onDragEnd.accept(new Coordinate().setX((super.getX() - 2.0d) / (screen.width - this.width - 2.0d * 2)).setY((super.getY() - 2.0d) / (screen.height - this.height - 2.0d * 2)));
                    flag = true;
                }
            } else {
                this.onDragEnd.accept(new Coordinate().setX(super.getX()).setY(super.getY()));
                flag = true;
            }
            this.x_ = super.getX();
            this.y_ = super.getY();
        } else if (this.pressed && this.hovered && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            onClick.accept(this);
            flag = true;
        }
        this.pressed = false;
        this.mouseDrag = false;
        this.mouseButton = -1;
        this.mouseClickX = -1;
        this.mouseClickY = -1;
        this.keyCode = -1;
        this.modifiers = -1;
        return flag;
    }

    /**
     * 处理鼠标移动事件
     * 更新按钮状态和位置
     *
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     */
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.hovered = this.isMouseOver(mouseX, mouseY);
        super.setFocused(true);
        if (this.pressed) {
            if (((this.keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || this.keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) && this.modifiers == GLFW.GLFW_MOD_CONTROL)
                    || ((this.keyCode == GLFW.GLFW_KEY_LEFT_ALT || this.keyCode == GLFW.GLFW_KEY_RIGHT_ALT) && this.modifiers == GLFW.GLFW_MOD_ALT)
                    || this.mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                this.mouseDrag = true;
                super.setX((int) getValidX(this.x_ + (mouseX - this.mouseClickX), this.width));
                super.setY((int) getValidY(this.y_ + (mouseY - this.mouseClickY), this.height));
            }
        }
        super.mouseMoved(mouseX, mouseY);
    }

    /**
     * 处理按键按下事件
     *
     * @param keyCode 按键代码
     * @param scanCode 扫描码
     * @param modifiers 修饰键
     * @return 是否处理了此事件
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.keyCode = keyCode;
        this.modifiers = modifiers;
        return false;
    }

    /**
     * 处理按键释放事件
     *
     * @param keyCode 按键代码
     * @param scanCode 扫描码
     * @param modifiers 修饰键
     * @return 是否处理了此事件
     */
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.keyCode = -1;
        this.modifiers = -1;
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void appendClickableNarrations(NarrationMessageBuilder narration) {
    }

    @Override
    @ParametersAreNonnullByDefault
    public void renderButton(DrawContext graphics, int mouseX, int mouseY, float delta) {
        this.render(graphics, mouseX, mouseY, delta);
    }

    public void tick() {
        // 用于更新按钮状态
    }

    /**
     * 获取有效的X坐标
     * 确保按钮不会超出屏幕边界
     *
     * @param x 原始X坐标
     * @param width 按钮宽度
     * @return 有效的X坐标
     */
    public static double getValidX(double x, int width) {
        int screenWidth = 427;
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen != null) {
            screenWidth = screen.width;
        }
        return Math.min(screenWidth - 2 - width, Math.max(2, x));
    }

    /**
     * 获取有效的Y坐标
     * 确保按钮不会超出屏幕边界
     *
     * @param y 原始Y坐标
     * @param height 按钮高度
     * @return 有效的Y坐标
     */
    public static double getValidY(double y, int height) {
        int screenHeight = 240;
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen != null) {
            screenHeight = screen.height;
        }
        return Math.min(screenHeight - 2 - height, Math.max(2, y));
    }

    /**
     * 处理鼠标拖动事件
     * 更新按钮位置
     *
     * @param mouseX 当前鼠标X坐标
     * @param mouseY 当前鼠标Y坐标
     * @param button 按下的鼠标按键
     * @param deltaX X方向移动距离
     * @param deltaY Y方向移动距离
     * @return 是否处理了此事件
     */
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.pressed) {
            if (((this.keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || this.keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) && this.modifiers == GLFW.GLFW_MOD_CONTROL)
                    || ((this.keyCode == GLFW.GLFW_KEY_LEFT_ALT || this.keyCode == GLFW.GLFW_KEY_RIGHT_ALT) && this.modifiers == GLFW.GLFW_MOD_ALT)
                    || this.mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                this.mouseDrag = true;
                super.setX((int) getValidX(this.x_ + deltaX, this.width));
                super.setY((int) getValidY(this.y_ + deltaY, this.height));
                return true;
            }
        }
        return false;
    }
}