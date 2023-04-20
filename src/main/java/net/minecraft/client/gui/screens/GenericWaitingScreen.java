package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GenericWaitingScreen extends Screen {
    private static final int TITLE_Y = 80;
    private static final int MESSAGE_Y = 120;
    private static final int MESSAGE_MAX_WIDTH = 360;
    @Nullable
    private final Component messageText;
    private final Component buttonLabel;
    private final Runnable buttonCallback;
    @Nullable
    private MultiLineLabel message;
    private Button button;
    private int disableButtonTicks;

    public static GenericWaitingScreen createWaiting(Component param0, Component param1, Runnable param2) {
        return new GenericWaitingScreen(param0, null, param1, param2, 0);
    }

    public static GenericWaitingScreen createCompleted(Component param0, Component param1, Component param2, Runnable param3) {
        return new GenericWaitingScreen(param0, param1, param2, param3, 20);
    }

    protected GenericWaitingScreen(Component param0, @Nullable Component param1, Component param2, Runnable param3, int param4) {
        super(param0);
        this.messageText = param1;
        this.buttonLabel = param2;
        this.buttonCallback = param3;
        this.disableButtonTicks = param4;
    }

    @Override
    protected void init() {
        super.init();
        if (this.messageText != null) {
            this.message = MultiLineLabel.create(this.font, this.messageText, 360);
        }

        int var0 = 150;
        int var1 = 20;
        int var2 = this.message != null ? this.message.getLineCount() : 1;
        int var3 = Math.max(var2, 5) * 9;
        int var4 = Math.min(120 + var3, this.height - 40);
        this.button = this.addRenderableWidget(Button.builder(this.buttonLabel, param0 -> this.onClose()).bounds((this.width - 150) / 2, var4, 150, 20).build());
    }

    @Override
    public void tick() {
        if (this.disableButtonTicks > 0) {
            --this.disableButtonTicks;
        }

        this.button.active = this.disableButtonTicks == 0;
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 80, 16777215);
        if (this.message == null) {
            String var0 = LoadingDotsText.get(Util.getMillis());
            param0.drawCenteredString(this.font, var0, this.width / 2, 120, 10526880);
        } else {
            this.message.renderCentered(param0, this.width / 2, 120);
        }

        super.render(param0, param1, param2, param3);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.message != null && this.button.active;
    }

    @Override
    public void onClose() {
        this.buttonCallback.run();
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(this.title, this.messageText != null ? this.messageText : CommonComponents.EMPTY);
    }
}
