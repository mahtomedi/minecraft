package net.minecraft.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractButton extends AbstractWidget {
    public AbstractButton(int param0, int param1, int param2, int param3, Component param4) {
        super(param0, param1, param2, param3, param4);
    }

    public abstract void onPress();

    @Override
    public void onClick(double param0, double param1) {
        this.onPress();
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (!this.active || !this.visible) {
            return false;
        } else if (param0 != 257 && param0 != 32 && param0 != 335) {
            return false;
        } else {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
            return true;
        }
    }
}
