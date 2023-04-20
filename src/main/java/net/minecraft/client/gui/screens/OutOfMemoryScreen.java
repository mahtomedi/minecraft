package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OutOfMemoryScreen extends Screen {
    private MultiLineLabel message = MultiLineLabel.EMPTY;

    public OutOfMemoryScreen() {
        super(Component.translatable("outOfMemory.title"));
    }

    @Override
    protected void init() {
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_TO_TITLE, param0 -> this.minecraft.setScreen(new TitleScreen()))
                .bounds(this.width / 2 - 155, this.height / 4 + 120 + 12, 150, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("menu.quit"), param0 -> this.minecraft.stop())
                .bounds(this.width / 2 - 155 + 160, this.height / 4 + 120 + 12, 150, 20)
                .build()
        );
        this.message = MultiLineLabel.create(this.font, Component.translatable("outOfMemory.message"), 295);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        param0.drawCenteredString(this.font, this.title, this.width / 2, this.height / 4 - 60 + 20, 16777215);
        this.message.renderLeftAligned(param0, this.width / 2 - 145, this.height / 4, 9, 10526880);
        super.render(param0, param1, param2, param3);
    }
}
