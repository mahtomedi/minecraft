package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfirmScreen extends Screen {
    private final Component title2;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    protected Component yesButton;
    protected Component noButton;
    private int delayTicker;
    protected final BooleanConsumer callback;

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
    public String getNarrationMessage() {
        return super.getNarrationMessage() + ". " + this.title2.getString();
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new Button(this.width / 2 - 155, this.height / 6 + 96, 150, 20, this.yesButton, param0 -> this.callback.accept(true)));
        this.addButton(new Button(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20, this.noButton, param0 -> this.callback.accept(false)));
        this.message = MultiLineLabel.create(this.font, this.title2, this.width - 50);
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

        for(AbstractWidget var0 : this.buttons) {
            var0.active = false;
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (--this.delayTicker == 0) {
            for(AbstractWidget var0 : this.buttons) {
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
