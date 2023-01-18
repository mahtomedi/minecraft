package net.minecraft.client.gui.components.events;

import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface GuiEventListener {
    long DOUBLE_CLICK_THRESHOLD_MS = 250L;

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

    @Nullable
    default ComponentPath nextFocusPath(FocusNavigationEvent param0) {
        return null;
    }

    default boolean isMouseOver(double param0, double param1) {
        return false;
    }

    void setFocused(boolean var1);

    boolean isFocused();

    @Nullable
    default ComponentPath getCurrentFocusPath() {
        return this.isFocused() ? ComponentPath.leaf(this) : null;
    }

    default ScreenRectangle getRectangle() {
        return ScreenRectangle.empty();
    }
}
