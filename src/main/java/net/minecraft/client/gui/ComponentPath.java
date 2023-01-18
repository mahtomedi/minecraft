package net.minecraft.client.gui;

import javax.annotation.Nullable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ComponentPath {
    static ComponentPath leaf(GuiEventListener param0) {
        return new ComponentPath.Leaf(param0);
    }

    @Nullable
    static ComponentPath path(ContainerEventHandler param0, @Nullable ComponentPath param1) {
        return param1 == null ? null : new ComponentPath.Path(param0, param1);
    }

    static ComponentPath path(GuiEventListener param0, ContainerEventHandler... param1) {
        ComponentPath var0 = leaf(param0);

        for(ContainerEventHandler var1 : param1) {
            var0 = path(var1, var0);
        }

        return var0;
    }

    GuiEventListener component();

    void applyFocus(boolean var1);

    @OnlyIn(Dist.CLIENT)
    public static record Leaf(GuiEventListener component) implements ComponentPath {
        @Override
        public void applyFocus(boolean param0) {
            this.component.setFocused(param0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Path(ContainerEventHandler component, ComponentPath childPath) implements ComponentPath {
        @Override
        public void applyFocus(boolean param0) {
            if (!param0) {
                this.component.setFocused(null);
            } else {
                this.component.setFocused(this.childPath.component());
            }

            this.childPath.applyFocus(param0);
        }
    }
}
