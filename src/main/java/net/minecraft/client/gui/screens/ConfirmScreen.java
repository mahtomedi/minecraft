package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfirmScreen extends Screen {
    private static final int MARGIN = 20;
    private final Component message;
    private MultiLineLabel multilineMessage = MultiLineLabel.EMPTY;
    protected Component yesButton;
    protected Component noButton;
    private int delayTicker;
    protected final BooleanConsumer callback;
    private final List<Button> exitButtons = Lists.newArrayList();

    public ConfirmScreen(BooleanConsumer param0, Component param1, Component param2) {
        this(param0, param1, param2, CommonComponents.GUI_YES, CommonComponents.GUI_NO);
    }

    public ConfirmScreen(BooleanConsumer param0, Component param1, Component param2, Component param3, Component param4) {
        super(param1);
        this.callback = param0;
        this.message = param2;
        this.yesButton = param3;
        this.noButton = param4;
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), this.message);
    }

    @Override
    protected void init() {
        super.init();
        this.multilineMessage = MultiLineLabel.create(this.font, this.message, this.width - 50);
        int var0 = Mth.clamp(this.messageTop() + this.messageHeight() + 20, this.height / 6 + 96, this.height - 24);
        this.exitButtons.clear();
        this.addButtons(var0);
    }

    protected void addButtons(int param0) {
        this.addExitButton(Button.builder(this.yesButton, param0x -> this.callback.accept(true)).bounds(this.width / 2 - 155, param0, 150, 20).build());
        this.addExitButton(Button.builder(this.noButton, param0x -> this.callback.accept(false)).bounds(this.width / 2 - 155 + 160, param0, 150, 20).build());
    }

    protected void addExitButton(Button param0) {
        this.exitButtons.add(this.addRenderableWidget(param0));
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, this.titleTop(), 16777215);
        this.multilineMessage.renderCentered(param0, this.width / 2, this.messageTop());
    }

    private int titleTop() {
        int var0 = (this.height - this.messageHeight()) / 2;
        return Mth.clamp(var0 - 20 - 9, 10, 80);
    }

    private int messageTop() {
        return this.titleTop() + 20;
    }

    private int messageHeight() {
        return this.multilineMessage.getLineCount() * 9;
    }

    public void setDelay(int param0) {
        this.delayTicker = param0;

        for(Button var0 : this.exitButtons) {
            var0.active = false;
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (--this.delayTicker == 0) {
            for(Button var0 : this.exitButtons) {
                var0.active = true;
            }
        }

    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.callback.accept(false);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }
}
