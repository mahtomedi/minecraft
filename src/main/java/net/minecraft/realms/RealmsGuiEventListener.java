package net.minecraft.realms;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RealmsGuiEventListener {
    public boolean mouseClicked(double param0, double param1, int param2) {
        return false;
    }

    public boolean mouseReleased(double param0, double param1, int param2) {
        return false;
    }

    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        return false;
    }

    public boolean mouseScrolled(double param0, double param1, double param2) {
        return false;
    }

    public boolean keyPressed(int param0, int param1, int param2) {
        return false;
    }

    public boolean keyReleased(int param0, int param1, int param2) {
        return false;
    }

    public boolean charTyped(char param0, int param1) {
        return false;
    }

    public abstract GuiEventListener getProxy();
}
