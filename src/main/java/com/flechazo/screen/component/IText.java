package com.flechazo.screen.component;

import com.flechazo.util.I18nUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

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
     * 文本颜色
     */
    private int color = 0xFFFFFFFF;
    /**
     * 文本背景色
     */
    private int bgColor = 0x00000000;
    /**
     * 文本
     */
    private String text;
    /**
     * 是否有阴影
     */
    private boolean shadow;
    /**
     * 是否粗体
     */
    private boolean bold;
    /**
     * 是否斜体
     */
    private boolean italic;
    /**
     * 是否删除线
     */
    private boolean underlined;
    /**
     * 是否中划线
     */
    private boolean strikethrough;
    /**
     * 是否混淆
     */
    private boolean obfuscated;
    /**
     * 文本对齐方式(仅多行绘制时)
     */
    private Align align = Align.LEFT;
    /**
     * 鼠标悬浮时文本颜色
     */
    private int hoverColor = 0xFFFFFFFF;
    /**
     * 鼠标悬浮时文本背景色
     */
    private int hoverBgColor = 0x00000000;
    /**
     * 鼠标悬浮时文本
     */
    private String hoverText;
    /**
     * 鼠标悬浮时是否有阴影
     */
    private boolean hoverShadow;
    /**
     * 鼠标悬浮时是否粗体
     */
    private boolean hoverBold;
    /**
     * 鼠标悬浮时是否斜体
     */
    private boolean hoverItalic;
    /**
     * 鼠标悬浮时是否删除线
     */
    private boolean hoverUnderlined;
    /**
     * 鼠标悬浮时是否中划线
     */
    private boolean hoverStrikethrough;
    /**
     * 鼠标悬浮时是否混淆
     */
    private boolean hoverObfuscated;
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
        this.text = text;
        this.hoverText = text;
    }

    public static IText literal(String text) {
        return new IText (text);
    }

    public static IText translatable(String key) {
        return new IText (I18nUtils.get(key));
    }

    public static IText i18n(String key, Object... args) {
        return new IText (I18nUtils.getByZh(key, args));
    }

    public IText copy() {
        return new IText ()
                .setHovered(this.hovered)
                .setColor(this.color)
                .setBgColor(this.bgColor)
                .setText(this.text)
                .setShadow(this.shadow)
                .setBold(this.bold)
                .setItalic(this.italic)
                .setUnderlined(this.underlined)
                .setStrikethrough(this.strikethrough)
                .setObfuscated(this.obfuscated)
                .setAlign(this.align)
                .setHoverColor(this.hoverColor)
                .setHoverBgColor(this.hoverBgColor)
                .setHoverText(this.hoverText)
                .setHoverShadow(this.hoverShadow)
                .setHoverBold(this.hoverBold)
                .setHoverItalic(this.hoverItalic)
                .setHoverUnderlined(this.hoverUnderlined)
                .setHoverStrikethrough(this.hoverStrikethrough)
                .setHoverObfuscated(this.hoverObfuscated)
                .setHoverAlign(this.hoverAlign)
                .setGraphics(this.graphics)
                .setFont(this.font);
    }

    public TextRenderer getFont() {
        return font == null ? MinecraftClient.getInstance().inGameHud.getTextRenderer() : this.font;
    }

    public int getColor() {
        return this.hovered ? this.hoverColor : this.color;
    }

    public int getBgColor() {
        return this.hovered ? this.hoverBgColor : this.bgColor;
    }

    public String getContent() {
        return this.hovered ? this.hoverText : this.text;
    }

    public boolean isShadow() {
        return this.hovered ? this.hoverShadow : this.shadow;
    }

    public boolean isBold() {
        return this.hovered ? this.hoverBold : this.bold;
    }

    public boolean isItalic() {
        return this.hovered ? this.hoverItalic : this.italic;
    }

    public boolean isUnderlined() {
        return this.hovered ? this.hoverUnderlined : this.underlined;
    }

    public boolean isStrikethrough() {
        return this.hovered ? this.hoverStrikethrough : this.strikethrough;
    }

    public boolean isObfuscated() {
        return this.hovered ? this.hoverObfuscated : this.obfuscated;
    }

    public Align getAlign() {
        return this.hovered ? this.hoverAlign : this.align;
    }

    public IText setColor(int color) {
        this.color = color;
        this.hoverColor = color;
        return this;
    }

    public IText setBgColor(int bgColor) {
        this.bgColor = bgColor;
        this.hoverBgColor = bgColor;
        return this;
    }

    public IText setText(String text) {
        this.text = text;
        this.hoverText = text;
        return this;
    }

    public IText setShadow(boolean shadow) {
        this.shadow = shadow;
        this.hoverShadow = shadow;
        return this;
    }

    public IText setBold(boolean bold) {
        this.bold = bold;
        this.hoverBold = bold;
        return this;
    }

    public IText setItalic(boolean italic) {
        this.italic = italic;
        this.hoverItalic = italic;
        return this;
    }

    public IText setUnderlined(boolean underlined) {
        this.underlined = underlined;
        this.hoverUnderlined = underlined;
        return this;
    }

    public IText setStrikethrough(boolean strikethrough) {
        this.strikethrough = strikethrough;
        this.hoverStrikethrough = strikethrough;
        return this;
    }

    public IText setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
        this.hoverObfuscated = obfuscated;
        return this;
    }

    public IText setAlign(Align align) {
        this.align = align;
        this.hoverAlign = align;
        return this;
    }
}
