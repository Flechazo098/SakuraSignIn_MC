package com.flechazo.screen;

import com.flechazo.config.StringList;
import com.flechazo.screen.component.IText;
import com.flechazo.screen.component.TextList;
import com.flechazo.util.AbstractGuiUtils;
import com.flechazo.util.CollectionUtils;
import com.flechazo.util.I18nUtils;
import com.flechazo.util.StringUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 字符串输入 Screen
 */
public class StringInputScreen extends Screen {

    /**
     * 父级 Screen
     */
    private final Screen previousScreen;
    /**
     * 标题
     */
    private final TextList titleText;
    /**
     * 提示
     */
    private final TextList messageText;
    /**
     * 输入数据校验
     */
    private final StringList validator;
    /**
     * 输入数据回调1
     */
    private final Consumer < StringList > onDataReceived1;
    /**
     * 输入数据回调2
     */
    private final Function <StringList, StringList> onDataReceived2;
    /**
     * 是否要显示该界面, 若为false则直接关闭当前界面并返回到调用者的 Screen
     */
    private final Supplier <Boolean> shouldClose;
    /**
     * 输入框
     */
    private final List <TextFieldWidget> inputField = new ArrayList <> ();
    /**
     * 已输入内容
     */
    private final List<String> inputValue = new ArrayList<>();
    /**
     * 确认按钮
     */
    private ButtonWidget submitButton;
    /**
     * 输入框默认值
     */
    private final StringList defaultValue;
    /**
     * 输入错误提示
     */
    private final TextList errorText = new TextList();

    int layoutHeight = 75, yStart = 0;


    public StringInputScreen(Screen callbackScreen, IText titleText, IText messageText, String validator, Consumer<StringList> onDataReceived) {
        this(callbackScreen, new TextList(titleText), new TextList(messageText), new StringList(validator), onDataReceived);
    }


    public StringInputScreen(Screen callbackScreen, TextList titleText, TextList messageText, StringList validator, Consumer<StringList> onDataReceived) {
        super(Text.literal("StringInputScreen"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = onDataReceived;
        this.onDataReceived2 = null;
        this.titleText = titleText;
        this.messageText = messageText;
        this.validator = validator;
        this.defaultValue = new StringList("");
        this.shouldClose = null;
    }

    public StringInputScreen(Screen callbackScreen, IText titleText, IText messageText, String validator, String defaultValue, Consumer<StringList> onDataReceived) {
        this(callbackScreen, new TextList(titleText), new TextList(messageText), new StringList(validator), new StringList(defaultValue), onDataReceived);
    }

    public StringInputScreen(Screen callbackScreen, TextList titleText, TextList messageText, StringList validator, StringList defaultValue, Consumer<StringList> onDataReceived) {
        super(Text.literal("StringInputScreen"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = onDataReceived;
        this.onDataReceived2 = null;
        this.titleText = titleText;
        this.messageText = messageText;
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.shouldClose = null;
    }

    public StringInputScreen(Screen callbackScreen, IText titleText, IText messageText, String validator, String defaultValue, Consumer<StringList> onDataReceived, Supplier<Boolean> shouldClose) {
        this(callbackScreen, new TextList(titleText), new TextList(messageText), new StringList(validator), new StringList(defaultValue), onDataReceived, shouldClose);
    }

    public StringInputScreen(Screen callbackScreen, TextList titleText, TextList messageText, StringList validator, StringList defaultValue, Consumer<StringList> onDataReceived, Supplier<Boolean> shouldClose) {
        super(Text.literal("StringInputScreen"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = onDataReceived;
        this.onDataReceived2 = null;
        this.titleText = titleText;
        this.messageText = messageText;
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.shouldClose = shouldClose;
    }

    public StringInputScreen(Screen callbackScreen, IText titleText, IText messageText, String validator, Function<StringList, StringList> onDataReceived) {
        this(callbackScreen, new TextList(titleText), new TextList(messageText), new StringList(validator), onDataReceived);
    }

    public StringInputScreen(Screen callbackScreen, TextList titleText, TextList messageText, StringList validator, Function<StringList, StringList> onDataReceived) {
        super(Text.literal("StringInputScreen"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = null;
        this.onDataReceived2 = onDataReceived;
        this.titleText = titleText;
        this.messageText = messageText;
        this.validator = validator;
        this.defaultValue = new StringList("");
        this.shouldClose = null;
    }

    public StringInputScreen(Screen callbackScreen, IText titleText, IText messageText, String validator, String defaultValue, Function<StringList, StringList> onDataReceived) {
        this(callbackScreen, new TextList(titleText), new TextList(messageText), new StringList(validator), new StringList(defaultValue), onDataReceived);
    }

    public StringInputScreen(Screen callbackScreen, TextList titleText, TextList messageText, StringList validator, StringList defaultValue, Function<StringList, StringList> onDataReceived) {
        super(Text.literal("StringInputScreen"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = null;
        this.onDataReceived2 = onDataReceived;
        this.titleText = titleText;
        this.messageText = messageText;
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.shouldClose = null;
    }

    public StringInputScreen(Screen callbackScreen, IText titleText, IText messageText, String validator, String defaultValue, Function<StringList, StringList> onDataReceived, Supplier<Boolean> shouldClose) {
        this(callbackScreen, new TextList(titleText), new TextList(messageText), new StringList(validator), new StringList(defaultValue), onDataReceived, shouldClose);
    }

    public StringInputScreen(Screen callbackScreen, TextList titleText, TextList messageText, StringList validator, StringList defaultValue, Function<StringList, StringList> onDataReceived, Supplier<Boolean> shouldClose) {
        super(Text.literal("StringInputScreen"));
        this.previousScreen = callbackScreen;
        this.onDataReceived1 = null;
        this.onDataReceived2 = onDataReceived;
        this.titleText = titleText;
        this.messageText = messageText;
        this.validator = validator;
        this.defaultValue = defaultValue;
        this.shouldClose = shouldClose;
    }

    @Override
    protected void init() {
        if (this.shouldClose != null && Boolean.TRUE.equals(this.shouldClose.get()))
            MinecraftClient.getInstance().setScreen(previousScreen);

        this.layoutHeight = this.titleText.size() * 45 + 10 + 20;
        this.yStart = (this.height - this.layoutHeight) / 2;

        // 创建文本输入框
        this.inputField.clear();
        for (int i = 0; i < this.titleText.size(); i++) {
            IText text = this.messageText.get(i);
            TextFieldWidget input = AbstractGuiUtils.newTextFieldWidget(this.textRenderer, this.width / 2 - 100, this.yStart + 15 + 45 * i, 200, 20
                    , AbstractGuiUtils.textToComponent(text));
            input.setMaxLength(Integer.MAX_VALUE);
            if (CollectionUtils.isNotNullOrEmpty(this.validator)) {
                String regex = this.validator.get(i);
                if (StringUtils.isNotNullOrEmpty(regex)) {
                    input.setTextPredicate(s -> s.matches(regex));
                }
            }
            if (CollectionUtils.isNotNullOrEmpty(this.inputValue)) {
                input.setText(this.inputValue.get(i));
            } else {
                input.setText(this.defaultValue.get(i));
            }
            this.inputField.add(input);
            this.addDrawableChild(input);
        }

        // 创建提交按钮
        this.submitButton = AbstractGuiUtils.newButton(this.width / 2 + 5, this.yStart + this.layoutHeight - 28, 95, 20, Text.literal(I18nUtils.getByZh("取消")), button -> {
            StringList value = new StringList();
            this.inputField.stream().map(TextFieldWidget::getText).forEach(value::add);
            if (CollectionUtils.isNullOrEmpty(value) || button.getMessage().getString().equals(I18nUtils.getByZh("取消"))) {
                // 关闭当前屏幕并返回到调用者的 Screen
                MinecraftClient.getInstance().setScreen(previousScreen);
            } else {
                // 获取输入的数据，并执行回调
                if (onDataReceived1 != null) {
                    onDataReceived1.accept(value);
                    // 关闭当前屏幕并返回到调用者的 Screen
                    MinecraftClient.getInstance().setScreen(previousScreen);
                } else if (onDataReceived2 != null) {
                    StringList result = onDataReceived2.apply(value);
                    if (CollectionUtils.isNotNullOrEmpty(result) && result.stream().anyMatch(StringUtils::isNotNullOrEmpty)) {
                        this.errorText.clear();
                        for (String s : result) {
                            this.errorText.add(IText.literal(s).setColor(0xFFFF0000));
                        }
                    } else {
                        // 关闭当前屏幕并返回到调用者的 Screen
                        MinecraftClient.getInstance().setScreen(previousScreen);
                    }
                }
            }
        });
        this.addDrawableChild(this.submitButton);
        // 创建取消按钮
        this.addDrawableChild(AbstractGuiUtils.newButton(this.width / 2 - 100, this.yStart + this.layoutHeight - 28, 95, 20, Text.literal(I18nUtils.getByZh("取消")), button -> {
            // 关闭当前屏幕并返回到调用者的 Screen
            MinecraftClient.getInstance().setScreen(previousScreen);
        }));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
        this.inputValue.clear();
        this.inputField.forEach(in -> this.inputValue.add(in.getText()));
        this.renderBackground(graphics);
        // 绘制背景
        super.render(graphics, mouseX, mouseY, delta);
        // 绘制标题
        for (int i = 0; i < titleText.size(); i++) {
            IText text = titleText.get(i);
            AbstractGuiUtils.drawString(text.setGraphics(graphics), this.width / 2.0f - 100, this.yStart + 4 + 45 * i);
        }
        // 绘制错误提示
        if (CollectionUtils.isNotNullOrEmpty(this.errorText)) {
            for (int i = 0; i < this.errorText.size(); i++) {
                IText text = this.errorText.get(i);
                AbstractGuiUtils.drawLimitedText(text.setGraphics(graphics), this.width / 2.0f - 100, this.yStart - 7 + 45 * (i + 1), 200, AbstractGuiUtils.EllipsisPosition.MIDDLE);
            }
        }
        if (this.inputField.stream().allMatch(in -> StringUtils.isNotNullOrEmpty(in.getText()))) {
            this.submitButton.setMessage(Text.literal(I18nUtils.getByZh("提交")));
        } else {
            this.submitButton.setMessage(Text.literal(I18nUtils.getByZh("取消")));
        }
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
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (keyCode == GLFW.GLFW_KEY_BACKSPACE && this.inputField.stream().noneMatch(TextFieldWidget::isFocused))) {
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