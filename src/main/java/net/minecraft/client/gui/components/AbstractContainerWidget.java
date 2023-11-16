package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractContainerWidget extends AbstractWidget implements ContainerEventHandler {
    @Nullable
    private GuiEventListener focused;
    private boolean isDragging;

    public AbstractContainerWidget(int param0, int param1, int param2, int param3, Component param4) {
        super(param0, param1, param2, param3, param4);
    }

    @Override
    public final boolean isDragging() {
        return this.isDragging;
    }

    @Override
    public final void setDragging(boolean param0) {
        this.isDragging = param0;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener param0) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }

        if (param0 != null) {
            param0.setFocused(true);
        }

        this.focused = param0;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent param0) {
        return ContainerEventHandler.super.nextFocusPath(param0);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        return ContainerEventHandler.super.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        return ContainerEventHandler.super.mouseReleased(param0, param1, param2);
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        return ContainerEventHandler.super.mouseDragged(param0, param1, param2, param3, param4);
    }

    @Override
    public boolean isFocused() {
        return ContainerEventHandler.super.isFocused();
    }

    @Override
    public void setFocused(boolean param0) {
        ContainerEventHandler.super.setFocused(param0);
    }
}
