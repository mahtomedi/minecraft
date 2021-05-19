package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfirmScreen extends Screen {
    private static final int LABEL_Y = 90;
    private final Component title2;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
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
        this.title2 = param2;
        this.yesButton = param3;
        this.noButton = param4;
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), this.title2);
    }

    @Override
    protected void init() {
        super.init();
        this.message = MultiLineLabel.create(this.font, this.title2, this.width - 50);
        int var0 = this.message.getLineCount() * 9;
        int var1 = Mth.clamp(90 + var0 + 12, this.height / 6 + 96, this.height - 24);
        this.exitButtons.clear();
        this.addButtons(var1);
    }

    protected void addButtons(int param0) {
        this.addExitButton(new Button(this.width / 2 - 155, param0, 150, 20, this.yesButton, param0x -> this.callback.accept(true)));
        this.addExitButton(new Button(this.width / 2 - 155 + 160, param0, 150, 20, this.noButton, param0x -> this.callback.accept(false)));
    }

    protected void addExitButton(Button param0) {
        this.exitButtons.add(this.addRenderableWidget(param0));
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 70, 16777215);
        this.message.renderCentered(param0, this.width / 2, 90);
        super.render(param0, param1, param2, param3);
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
