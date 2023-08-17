package net.minecraft.realms;

import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RealmsObjectSelectionList<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {
    protected RealmsObjectSelectionList(int param0, int param1, int param2, int param3, int param4) {
        super(Minecraft.getInstance(), param0, param1, param2, param3, param4);
    }

    public void setSelectedItem(int param0) {
        if (param0 == -1) {
            this.setSelected((E)null);
        } else if (super.getItemCount() != 0) {
            this.setSelected(this.getEntry(param0));
        }

    }

    public void selectItem(int param0) {
        this.setSelectedItem(param0);
    }

    @Override
    public int getMaxPosition() {
        return 0;
    }

    @Override
    public int getScrollbarPosition() {
        return this.getRowLeft() + this.getRowWidth();
    }

    @Override
    public int getRowWidth() {
        return (int)((double)this.width * 0.6);
    }

    @Override
    public void replaceEntries(Collection<E> param0) {
        super.replaceEntries(param0);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public int getRowTop(int param0) {
        return super.getRowTop(param0);
    }

    @Override
    public int getRowLeft() {
        return super.getRowLeft();
    }

    public int addEntry(E param0) {
        return super.addEntry(param0);
    }

    public void clear() {
        this.clearEntries();
    }
}
