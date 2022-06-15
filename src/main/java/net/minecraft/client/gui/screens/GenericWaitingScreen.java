package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GenericWaitingScreen extends Screen {
    private static final int TITLE_Y = 80;
    private static final int MESSAGE_Y = 120;
    private static final int MESSAGE_MAX_WIDTH = 360;
    private final Component initialButtonLabel;
    private Runnable buttonCallback;
    @Nullable
    private MultiLineLabel message;
    private Button button;
    private long disableButtonUntil;

    public GenericWaitingScreen(Component param0, Component param1, Runnable param2) {
        super(param0);
        this.initialButtonLabel = param1;
        this.buttonCallback = param2;
    }

    @Override
    protected void init() {
        super.init();
        this.initButton(this.initialButtonLabel);
    }

    @Override
    public void tick() {
        this.button.active = Util.getMillis() > this.disableButtonUntil;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 80, 16777215);
        if (this.message == null) {
            String var0 = LoadingDotsText.get(Util.getMillis());
            drawCenteredString(param0, this.font, var0, this.width / 2, 120, 10526880);
        } else {
            this.message.renderCentered(param0, this.width / 2, 120);
        }

        super.render(param0, param1, param2, param3);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    public void update(Component param0, Runnable param1) {
        this.update(null, param0, param1);
    }

    public void update(@Nullable Component param0, Component param1, Runnable param2) {
        this.buttonCallback = param2;
        if (param0 != null) {
            this.message = MultiLineLabel.create(this.font, param0, 360);
            NarratorChatListener.INSTANCE.sayNow(param0);
        } else {
            this.message = null;
        }

        this.initButton(param1);
        this.disableButtonUntil = Util.getMillis() + TimeUnit.SECONDS.toMillis(1L);
    }

    private void initButton(Component param0) {
        this.removeWidget(this.button);
        int var0 = 150;
        int var1 = 20;
        int var2 = this.message != null ? this.message.getLineCount() : 1;
        int var3 = Math.min(120 + (var2 + 4) * 9, this.height - 40);
        this.button = this.addRenderableWidget(new Button((this.width - 150) / 2, var3, 150, 20, param0, param0x -> this.buttonCallback.run()));
    }
}
