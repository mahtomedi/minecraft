package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ContainerObjectSelectionList<E extends ContainerObjectSelectionList.Entry<E>> extends AbstractSelectionList<E> {
    public ContainerObjectSelectionList(Minecraft param0, int param1, int param2, int param3, int param4, int param5) {
        super(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean changeFocus(boolean param0) {
        boolean var0 = super.changeFocus(param0);
        if (var0) {
            this.ensureVisible(this.getFocused());
        }

        return var0;
    }

    @Override
    protected boolean isSelectedItem(int param0) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry<E extends ContainerObjectSelectionList.Entry<E>> extends AbstractSelectionList.Entry<E> implements ContainerEventHandler {
        @Nullable
        private GuiEventListener focused;
        private boolean dragging;

        @Override
        public boolean isDragging() {
            return this.dragging;
        }

        @Override
        public void setDragging(boolean param0) {
            this.dragging = param0;
        }

        @Override
        public void setFocused(@Nullable GuiEventListener param0) {
            this.focused = param0;
        }

        @Nullable
        @Override
        public GuiEventListener getFocused() {
            return this.focused;
        }
    }
}
