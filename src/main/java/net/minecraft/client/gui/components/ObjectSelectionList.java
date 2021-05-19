package net.minecraft.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ObjectSelectionList<E extends ObjectSelectionList.Entry<E>> extends AbstractSelectionList<E> {
    private static final Component USAGE_NARRATION = new TranslatableComponent("narration.selection.usage");
    private boolean inFocus;

    public ObjectSelectionList(Minecraft param0, int param1, int param2, int param3, int param4, int param5) {
        super(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean changeFocus(boolean param0) {
        if (!this.inFocus && this.getItemCount() == 0) {
            return false;
        } else {
            this.inFocus = !this.inFocus;
            if (this.inFocus && this.getSelected() == null && this.getItemCount() > 0) {
                this.moveSelection(AbstractSelectionList.SelectionDirection.DOWN);
            } else if (this.inFocus && this.getSelected() != null) {
                this.refreshSelection();
            }

            return this.inFocus;
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput param0) {
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
        @Override
        public boolean changeFocus(boolean param0) {
            return false;
        }

        public abstract Component getNarration();

        @Override
        public void updateNarration(NarrationElementOutput param0) {
            param0.add(NarratedElementType.TITLE, this.getNarration());
        }
    }
}
