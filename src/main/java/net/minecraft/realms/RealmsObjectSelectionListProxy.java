package net.minecraft.realms;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsObjectSelectionListProxy<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {
    private final RealmsObjectSelectionList realmsObjectSelectionList;

    public RealmsObjectSelectionListProxy(RealmsObjectSelectionList param0, int param1, int param2, int param3, int param4, int param5) {
        super(Minecraft.getInstance(), param1, param2, param3, param4, param5);
        this.realmsObjectSelectionList = param0;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public void clear() {
        super.clearEntries();
    }

    @Override
    public boolean isFocused() {
        return this.realmsObjectSelectionList.isFocused();
    }

    protected void setSelectedItem(int param0) {
        if (param0 == -1) {
            super.setSelected((E)null);
        } else if (super.getItemCount() != 0) {
            E var0 = super.getEntry(param0);
            super.setSelected(var0);
        }

    }

    public void setSelected(@Nullable E param0) {
        super.setSelected(param0);
        this.realmsObjectSelectionList.selectItem(super.children().indexOf(param0));
    }

    @Override
    public void renderBackground() {
        this.realmsObjectSelectionList.renderBackground();
    }

    public int getWidth() {
        return this.width;
    }

    @Override
    public int getMaxPosition() {
        return this.realmsObjectSelectionList.getMaxPosition();
    }

    @Override
    public int getScrollbarPosition() {
        return this.realmsObjectSelectionList.getScrollbarPosition();
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        return this.realmsObjectSelectionList.mouseScrolled(param0, param1, param2) ? true : super.mouseScrolled(param0, param1, param2);
    }

    @Override
    public int getRowWidth() {
        return this.realmsObjectSelectionList.getRowWidth();
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        return this.realmsObjectSelectionList.mouseClicked(param0, param1, param2) ? true : access$001(this, param0, param1, param2);
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        return this.realmsObjectSelectionList.mouseReleased(param0, param1, param2);
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        return this.realmsObjectSelectionList.mouseDragged(param0, param1, param2, param3, param4)
            ? true
            : super.mouseDragged(param0, param1, param2, param3, param4);
    }

    protected final int addEntry(E param0) {
        return super.addEntry(param0);
    }

    public E remove(int param0) {
        return super.remove(param0);
    }

    public boolean removeEntry(E param0) {
        return super.removeEntry(param0);
    }

    @Override
    public void setScrollAmount(double param0) {
        super.setScrollAmount(param0);
    }

    public int y0() {
        return this.y0;
    }

    public int y1() {
        return this.y1;
    }

    public int headerHeight() {
        return this.headerHeight;
    }

    public int itemHeight() {
        return this.itemHeight;
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        return super.keyPressed(param0, param1, param2) ? true : this.realmsObjectSelectionList.keyPressed(param0, param1, param2);
    }

    @Override
    public void replaceEntries(Collection<E> param0) {
        super.replaceEntries(param0);
    }

    @Override
    public int getRowTop(int param0) {
        return super.getRowTop(param0);
    }

    @Override
    public int getRowLeft() {
        return super.getRowLeft();
    }
}
