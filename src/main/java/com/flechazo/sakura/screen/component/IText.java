package com.flechazo.sakura.screen.component;

import com.flechazo.sakura.enums.EI18nType;
import com.flechazo.sakura.util.Component;
import com.flechazo.sakura.util.SakuraUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

@Setter
@Accessors (chain = true)
@Environment (EnvType.CLIENT)
public class IText {
    /**
     * 矩阵栈
     */
    @Getter
    private DrawContext graphics;
    /**
     * 字体渲染器
     */
    private TextRenderer font;
    /**
     * 是否悬浮(需手动设置状态)
     */
    private boolean hovered;
    /**
     * 文本
     */
    private Component text = Component.empty().clone();
    /**
     * 文本对齐方式(仅多行绘制时)
     */
    private Align align = Align.LEFT;
    /**
     * 鼠标悬浮时文本
     */
    private Component hoverText = Component.empty().clone();
    /**
     * 鼠标悬浮时对齐方式(仅多行绘制时)
     */
    private Align hoverAlign = Align.LEFT;

    /**
     * 文字对齐方向(仅多行绘制时)
     */
    public enum Align {
        LEFT, CENTER, RIGHT
    }

    private IText () {
    }

    private IText (String text) {
        this.text = Component.literal(text);
        this.hoverText = Component.literal(text);
    }

    public IText(Component text) {
        this.text = text;
        this.hoverText = text;
    }


    public static IText literal(String text) {
        return new IText (text);
    }

    public static IText translatable(EI18nType type, String key, Object... args) {
        return new IText(Component.translatableClient(type, key, args));
    }

    public IText copy() {
        return new IText ()
                .setText(this.text.clone())
                .setHoverText(this.hoverText.clone())
                .setHovered(this.hovered)
                .setAlign(this.align)
                .setHoverAlign(this.hoverAlign)
                .setGraphics(this.graphics)
                .setFont(this.font);
    }

    public TextRenderer getFont() {
        return font == null ? MinecraftClient.getInstance().inGameHud.getTextRenderer() : this.font;
    }

    public int getColor() {
        return this.hovered ? this.hoverText.getColor() : this.text.getColor();
    }

    public int getBgColor() {
        return this.hovered ? this.hoverText.getBgColor() : this.text.getBgColor();
    }

    public String getContent() {
        return getContent(true);
    }

    /**
     * 获取文本内容, 忽略样式
     *
     * @param ignoreStyle 是否忽略样式
     */
    public String getContent(boolean ignoreStyle) {
        return this.hovered ? this.hoverText.getString(SakuraUtils.getClientLanguage(), ignoreStyle, true) : this.text.getString(SakuraUtils.getClientLanguage(), ignoreStyle, true);
    }

    public boolean isShadow() {
        return this.hovered ? this.hoverText.isShadow() : this.text.isShadow();
    }

    public boolean isBold() {
        return this.hovered ? this.hoverText.isBold() : this.text.isBold();
    }

    public boolean isItalic() {
        return this.hovered ? this.hoverText.isItalic() : this.text.isItalic();
    }

    public boolean isUnderlined() {
        return this.hovered ? this.hoverText.isUnderlined() : this.text.isUnderlined();
    }

    public boolean isStrikethrough() {
        return this.hovered ? this.hoverText.isStrikethrough() : this.text.isStrikethrough();
    }

    public boolean isObfuscated() {
        return this.hovered ? this.hoverText.isObfuscated() : this.text.isObfuscated();
    }
    public Align getAlign() {
        return this.hovered ? this.hoverAlign : this.align;
    }

    public IText setColor(int color) {
        this.text.setColor(color);
        this.hoverText.setColor(color);
        return this;
    }

    public IText setBgColor(int bgColor) {
        this.text.setBgColor(bgColor);
        this.hoverText.setBgColor(bgColor);
        return this;
    }

    public IText setText(String text) {
        this.text.setI18nType(EI18nType.PLAIN).setText(text);
        this.hoverText.setI18nType(EI18nType.PLAIN).setText(text);
        return this;
    }

    public IText setText(Component text) {
        this.text = text;
        this.hoverText = text;
        return this;
    }

    public IText setHoverText(String text) {
        this.hoverText.setI18nType(EI18nType.PLAIN).setText(text);
        return this;
    }

    public IText setHoverText(Component text) {
        this.hoverText = text;
        return this;
    }


    public IText setShadow(boolean shadow) {
        this.text.setShadow(shadow);
        this.hoverText.setShadow(shadow);
        return this;
    }

    public IText setBold(boolean bold) {
        this.text.setBold(bold);
        this.hoverText.setBold(bold);
        return this;
    }

    public IText setItalic(boolean italic) {
        this.text.setItalic(italic);
        this.hoverText.setItalic(italic);
        return this;
    }

    public IText setUnderlined(boolean underlined) {
        this.text.setUnderlined(underlined);
        this.hoverText.setUnderlined(underlined);
        return this;
    }

    public IText setStrikethrough(boolean strikethrough) {
        this.text.setStrikethrough(strikethrough);
        this.hoverText.setStrikethrough(strikethrough);
        return this;
    }

    public IText setObfuscated(boolean obfuscated) {
        this.text.setObfuscated(obfuscated);
        this.hoverText.setObfuscated(obfuscated);
        return this;
    }

    public IText setAlign(Align align) {
        this.align = align;
        this.hoverAlign = align;
        return this;
    }

    public IText withStyle(IText text) {
        this.text.withStyle(text.text);
        this.hoverText.withStyle(text.hoverText);
        return this;
    }

    public Component toComponent() {
        return this.hovered ? this.hoverText : this.text;
    }

    public static int getTextComponentColor(Text textComponent) {
        return getTextComponentColor(textComponent, 0xFFFFFFFF);
    }

    public static int getTextComponentColor(Text textComponent, int defaultColor) {
        return textComponent.getStyle().getColor() == null ? defaultColor : textComponent.getStyle().getColor().getRgb();
    }

    public static IText fromTextComponent(Text component) {
        return IText.literal(component.getString())
                .setColor(getTextComponentColor(component))
                .setBold(component.getStyle().isBold())
                .setItalic(component.getStyle().isItalic())
                .setUnderlined(component.getStyle().isUnderlined())
                .setStrikethrough(component.getStyle().isStrikethrough())
                .setObfuscated(component.getStyle().isObfuscated());
    }
}
