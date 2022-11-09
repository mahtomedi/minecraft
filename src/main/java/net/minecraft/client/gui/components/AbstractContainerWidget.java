package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractContainerWidget extends AbstractWidget implements ContainerEventHandler {
    @Nullable
    private GuiEventListener focused;
    private boolean dragging;

    public AbstractContainerWidget(int param0, int param1, int param2, int param3, Component param4) {
        super(param0, param1, param2, param3, param4);
    }

    @Override
    public void renderButton(PoseStack param0, int param1, int param2, float param3) {
        for(AbstractWidget var0 : this.getContainedChildren()) {
            var0.render(param0, param1, param2, param3);
        }

    }

    @Override
    public boolean isMouseOver(double param0, double param1) {
        for(AbstractWidget var0 : this.getContainedChildren()) {
            if (var0.isMouseOver(param0, param1)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void mouseMoved(double param0, double param1) {
        this.getContainedChildren().forEach(param2 -> param2.mouseMoved(param0, param1));
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.getContainedChildren();
    }

    protected abstract List<? extends AbstractWidget> getContainedChildren();

    @Override
    public boolean isDragging() {
        return this.dragging;
    }

    @Override
    public void setDragging(boolean param0) {
        this.dragging = param0;
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        boolean var0 = false;

        for(AbstractWidget var1 : this.getContainedChildren()) {
            if (var1.isMouseOver(param0, param1) && var1.mouseScrolled(param0, param1, param2)) {
                var0 = true;
            }
        }

        return var0 || super.mouseScrolled(param0, param1, param2);
    }

    @Override
    public boolean changeFocus(boolean param0) {
        return ContainerEventHandler.super.changeFocus(param0);
    }

    @Nullable
    protected GuiEventListener getHovered() {
        for(AbstractWidget var0 : this.getContainedChildren()) {
            if (var0.isHovered) {
                return var0;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener param0) {
        this.focused = param0;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput param0) {
        GuiEventListener var0 = this.getHovered();
        if (var0 != null) {
            if (var0 instanceof NarrationSupplier var1) {
                var1.updateNarration(param0.nest());
            }
        } else {
            GuiEventListener var2 = this.getFocused();
            if (var2 != null && var2 instanceof NarrationSupplier var3) {
                var3.updateNarration(param0.nest());
            }
        }

    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.isHovered || this.getHovered() != null) {
            return NarratableEntry.NarrationPriority.HOVERED;
        } else {
            return this.focused != null ? NarratableEntry.NarrationPriority.FOCUSED : super.narrationPriority();
        }
    }

    @Override
    public void setX(int param0) {
        for(AbstractWidget var0 : this.getContainedChildren()) {
            int var1 = var0.getX() + (param0 - this.getX());
            var0.setX(var1);
        }

        super.setX(param0);
    }

    @Override
    public void setY(int param0) {
        for(AbstractWidget var0 : this.getContainedChildren()) {
            int var1 = var0.getY() + (param0 - this.getY());
            var0.setY(var1);
        }

        super.setY(param0);
    }

    @Override
    public Optional<GuiEventListener> getChildAt(double param0, double param1) {
        return ContainerEventHandler.super.getChildAt(param0, param1);
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

    @OnlyIn(Dist.CLIENT)
    protected abstract static class AbstractChildWrapper {
        public final AbstractWidget child;
        public final LayoutSettings.LayoutSettingsImpl layoutSettings;

        protected AbstractChildWrapper(AbstractWidget param0, LayoutSettings param1) {
            this.child = param0;
            this.layoutSettings = param1.getExposed();
        }

        public int getHeight() {
            return this.child.getHeight() + this.layoutSettings.paddingTop + this.layoutSettings.paddingBottom;
        }

        public int getWidth() {
            return this.child.getWidth() + this.layoutSettings.paddingLeft + this.layoutSettings.paddingRight;
        }

        public void setX(int param0, int param1) {
            float var0 = (float)this.layoutSettings.paddingLeft;
            float var1 = (float)(param1 - this.child.getWidth() - this.layoutSettings.paddingRight);
            int var2 = (int)Mth.lerp(this.layoutSettings.xAlignment, var0, var1);
            this.child.setX(var2 + param0);
        }

        public void setY(int param0, int param1) {
            float var0 = (float)this.layoutSettings.paddingTop;
            float var1 = (float)(param1 - this.child.getHeight() - this.layoutSettings.paddingBottom);
            int var2 = (int)Mth.lerp(this.layoutSettings.yAlignment, var0, var1);
            this.child.setY(var2 + param0);
        }
    }
}
