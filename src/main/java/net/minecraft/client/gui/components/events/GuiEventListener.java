package net.minecraft.client.gui.components.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface GuiEventListener {
    default void mouseMoved(double param0, double param1) {
    }

    default boolean mouseClicked(double param0, double param1, int param2) {
        return false;
    }

    default boolean mouseReleased(double param0, double param1, int param2) {
        return false;
    }

    default boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        return false;
    }

    default boolean mouseScrolled(double param0, double param1, double param2) {
        return false;
    }

    default boolean keyPressed(int param0, int param1, int param2) {
        return false;
    }

    default boolean keyReleased(int param0, int param1, int param2) {
        return false;
    }

    default boolean charTyped(char param0, int param1) {
        return false;
    }

    default boolean changeFocus(boolean param0) {
        return false;
    }

    default boolean isMouseOver(double param0, double param1) {
        return false;
    }
}
