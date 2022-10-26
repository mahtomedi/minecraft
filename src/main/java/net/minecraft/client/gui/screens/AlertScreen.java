package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AlertScreen extends Screen {
    private static final int LABEL_Y = 90;
    private final Component messageText;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private final Runnable callback;
    private final Component okButton;
    private final boolean shouldCloseOnEsc;

    public AlertScreen(Runnable param0, Component param1, Component param2) {
        this(param0, param1, param2, CommonComponents.GUI_BACK, true);
    }

    public AlertScreen(Runnable param0, Component param1, Component param2, Component param3, boolean param4) {
        super(param1);
        this.callback = param0;
        this.messageText = param2;
        this.okButton = param3;
        this.shouldCloseOnEsc = param4;
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), this.messageText);
    }

    @Override
    protected void init() {
        super.init();
        this.message = MultiLineLabel.create(this.font, this.messageText, this.width - 50);
        int var0 = this.message.getLineCount() * 9;
        int var1 = Mth.clamp(90 + var0 + 12, this.height / 6 + 96, this.height - 24);
        int var2 = 150;
        this.addRenderableWidget(Button.builder(this.okButton, param0 -> this.callback.run()).bounds((this.width - 150) / 2, var1, 150, 20).build());
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 70, 16777215);
        this.message.renderCentered(param0, this.width / 2, 90);
        super.render(param0, param1, param2, param3);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.shouldCloseOnEsc;
    }
}
