package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DatapackLoadFailureScreen extends Screen {
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private final Runnable cancelCallback;
    private final Runnable safeModeCallback;

    public DatapackLoadFailureScreen(Runnable param0, Runnable param1) {
        super(Component.translatable("datapackFailure.title"));
        this.cancelCallback = param0;
        this.safeModeCallback = param1;
    }

    @Override
    protected void init() {
        super.init();
        this.message = MultiLineLabel.create(this.font, this.getTitle(), this.width - 50);
        this.addRenderableWidget(
            Button.builder(Component.translatable("datapackFailure.safeMode"), param0 -> this.safeModeCallback.run())
                .bounds(this.width / 2 - 155, this.height / 6 + 96, 150, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_BACK, param0 -> this.cancelCallback.run())
                .bounds(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20)
                .build()
        );
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.message.renderCentered(param0, this.width / 2, 70);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
