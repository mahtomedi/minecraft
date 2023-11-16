package net.minecraft.client.gui.screens;

import net.minecraft.client.Options;
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
}
