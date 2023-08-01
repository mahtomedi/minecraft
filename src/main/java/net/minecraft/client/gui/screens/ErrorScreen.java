package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ErrorScreen extends Screen {
    private final Component message;

    public ErrorScreen(Component param0, Component param1) {
        super(param0);
        this.message = param1;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(null)).bounds(this.width / 2 - 100, 140, 200, 20).build()
        );
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 90, 16777215);
        param0.drawCenteredString(this.font, this.message, this.width / 2, 110, 16777215);
    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        param0.fillGradient(0, 0, this.width, this.height, -12574688, -11530224);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
