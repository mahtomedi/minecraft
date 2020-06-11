package net.minecraft.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ObjectSelectionList<E extends AbstractSelectionList.Entry<E>> extends AbstractSelectionList<E> {
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

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry<E extends ObjectSelectionList.Entry<E>> extends AbstractSelectionList.Entry<E> {
        @Override
        public boolean changeFocus(boolean param0) {
            return false;
        }
    }
}
