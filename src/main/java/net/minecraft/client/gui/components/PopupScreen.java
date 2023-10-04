package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PopupScreen extends Screen {
    private static final ResourceLocation BACKGROUND_SPRITE = new ResourceLocation("popup/background");
    private static final int SPACING = 12;
    private static final int BG_BORDER_WITH_SPACING = 18;
    private static final int BUTTON_SPACING = 6;
    private static final int IMAGE_SIZE_X = 130;
    private static final int IMAGE_SIZE_Y = 64;
    private static final int POPUP_DEFAULT_WIDTH = 250;
    private final Screen backgroundScreen;
    @Nullable
    private final ResourceLocation image;
    private final Component message;
    private final List<PopupScreen.ButtonOption> buttons;
    @Nullable
    private final Runnable onClose;
    private final int contentWidth;
    private final LinearLayout layout = LinearLayout.vertical();

    PopupScreen(
        Screen param0,
        int param1,
        @Nullable ResourceLocation param2,
        Component param3,
        Component param4,
        List<PopupScreen.ButtonOption> param5,
        @Nullable Runnable param6
    ) {
        super(param3);
        this.backgroundScreen = param0;
        this.image = param2;
        this.message = param4;
        this.buttons = param5;
        this.onClose = param6;
        this.contentWidth = param1 - 36;
    }

    @Override
    protected void init() {
        this.layout.spacing(12).defaultCellSetting().alignHorizontallyCenter();
        this.layout
            .addChild(new MultiLineTextWidget(this.title.copy().withStyle(ChatFormatting.BOLD), this.font).setMaxWidth(this.contentWidth).setCentered(true));
        if (this.image != null) {
            this.layout.addChild(ImageWidget.texture(130, 64, this.image, 130, 64));
        }

        this.layout.addChild(new MultiLineTextWidget(this.message, this.font).setMaxWidth(this.contentWidth).setCentered(true));
        this.layout.addChild(this.buildButtonRow());
        this.layout.visitWidgets(param1 -> {
        });
        this.repositionElements();
    }

    private LinearLayout buildButtonRow() {
        int var0 = 6 * (this.buttons.size() - 1);
        int var1 = Math.min((this.contentWidth - var0) / this.buttons.size(), 150);
        LinearLayout var2 = LinearLayout.horizontal();
        var2.spacing(6);

        for(PopupScreen.ButtonOption var3 : this.buttons) {
            var2.addChild(Button.builder(var3.message(), param1 -> var3.action().accept(this)).width(var1).build());
        }

        return var2;
    }

    @Override
    protected void repositionElements() {
        this.backgroundScreen.resize(this.minecraft, this.width, this.height);
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        this.backgroundScreen.render(param0, -1, -1, param3);
        param0.flush();
        RenderSystem.clear(256, Minecraft.ON_OSX);
        this.renderTransparentBackground(param0);
        param0.blitSprite(BACKGROUND_SPRITE, this.layout.getX() - 18, this.layout.getY() - 18, this.layout.getWidth() + 36, this.layout.getHeight() + 36);
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(this.title, this.message);
    }

    @Override
    public void onClose() {
        if (this.onClose != null) {
            this.onClose.run();
        }

        this.minecraft.setScreen(this.backgroundScreen);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Screen backgroundScreen;
        private final Component title;
        private Component message = CommonComponents.EMPTY;
        private int width = 250;
        @Nullable
        private ResourceLocation image;
        private final List<PopupScreen.ButtonOption> buttons = new ArrayList<>();
        @Nullable
        private Runnable onClose = null;

        public Builder(Screen param0, Component param1) {
            this.backgroundScreen = param0;
            this.title = param1;
        }

        public PopupScreen.Builder setWidth(int param0) {
            this.width = param0;
            return this;
        }

        public PopupScreen.Builder setImage(ResourceLocation param0) {
            this.image = param0;
            return this;
        }

        public PopupScreen.Builder setMessage(Component param0) {
            this.message = param0;
            return this;
        }

        public PopupScreen.Builder addButton(Component param0, Consumer<PopupScreen> param1) {
            this.buttons.add(new PopupScreen.ButtonOption(param0, param1));
            return this;
        }

        public PopupScreen.Builder onClose(Runnable param0) {
            this.onClose = param0;
            return this;
        }

        public PopupScreen build() {
            if (this.buttons.isEmpty()) {
                throw new IllegalStateException("Popup must have at least one button");
            } else {
                return new PopupScreen(this.backgroundScreen, this.width, this.image, this.title, this.message, List.copyOf(this.buttons), this.onClose);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record ButtonOption(Component message, Consumer<PopupScreen> action) {
    }
}
