package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ObjectSelectionList<E extends ObjectSelectionList.Entry<E>> extends AbstractSelectionList<E> {
    private static final Component USAGE_NARRATION = Component.translatable("narration.selection.usage");

    public ObjectSelectionList(Minecraft param0, int param1, int param2, int param3, int param4) {
        super(param0, param1, param2, param3, param4);
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent param0) {
        if (this.getItemCount() == 0) {
            return null;
        } else if (this.isFocused() && param0 instanceof FocusNavigationEvent.ArrowNavigation var0) {
            E var1 = this.nextEntry(var0.direction());
            return var1 != null ? ComponentPath.path(this, ComponentPath.leaf(var1)) : null;
        } else if (!this.isFocused()) {
            E var2 = this.getSelected();
            if (var2 == null) {
                var2 = this.nextEntry(param0.getVerticalDirectionForInitialFocus());
            }

            return var2 == null ? null : ComponentPath.path(this, ComponentPath.leaf(var2));
        } else {
            return null;
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput param0) {
        E var0 = this.getHovered();
        if (var0 != null) {
            this.narrateListElementPosition(param0.nest(), var0);
            var0.updateNarration(param0);
        } else {
            E var1 = this.getSelected();
            if (var1 != null) {
                this.narrateListElementPosition(param0.nest(), var1);
                var1.updateNarration(param0);
            }
        }

        if (this.isFocused()) {
            param0.add(NarratedElementType.USAGE, USAGE_NARRATION);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry<E extends ObjectSelectionList.Entry<E>> extends AbstractSelectionList.Entry<E> implements NarrationSupplier {
        public abstract Component getNarration();

        @Override
        public void updateNarration(NarrationElementOutput param0) {
            param0.add(NarratedElementType.TITLE, this.getNarration());
        }
    }
}
