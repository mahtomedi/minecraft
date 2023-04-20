package net.minecraft.client.gui.screens;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptionsSubScreen extends Screen {
    protected final Screen lastScreen;
    protected final Options options;

    public OptionsSubScreen(Screen param0, Options param1, Component param2) {
        super(param2);
        this.lastScreen = param0;
        this.options = param1;
    }

    @Override
    public void removed() {
        this.minecraft.options.save();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    protected void basicListRender(GuiGraphics param0, OptionsList param1, int param2, int param3, float param4) {
        this.renderBackground(param0);
        param1.render(param0, param2, param3, param4);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
        super.render(param0, param2, param3, param4);
    }
}
