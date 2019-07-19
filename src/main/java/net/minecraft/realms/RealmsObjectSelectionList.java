package net.minecraft.realms;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RealmsObjectSelectionList<E extends RealmListEntry> extends RealmsGuiEventListener {
    private final RealmsObjectSelectionListProxy proxy;

    public RealmsObjectSelectionList(int param0, int param1, int param2, int param3, int param4) {
        this.proxy = new RealmsObjectSelectionListProxy(this, param0, param1, param2, param3, param4);
    }

    public void render(int param0, int param1, float param2) {
        this.proxy.render(param0, param1, param2);
    }

    public void addEntry(E param0) {
        this.proxy.addEntry(param0);
    }

    public void remove(int param0) {
        this.proxy.remove(param0);
    }

    public void clear() {
        this.proxy.clear();
    }

    public boolean removeEntry(E param0) {
        return this.proxy.removeEntry(param0);
    }

    public int width() {
        return this.proxy.getWidth();
    }

    protected void renderItem(int param0, int param1, int param2, int param3, Tezzelator param4, int param5, int param6) {
    }

    public void setLeftPos(int param0) {
        this.proxy.setLeftPos(param0);
    }

    public void renderItem(int param0, int param1, int param2, int param3, int param4, int param5) {
        this.renderItem(param0, param1, param2, param3, Tezzelator.instance, param4, param5);
    }

    public void setSelected(int param0) {
        this.proxy.setSelectedItem(param0);
    }

    public void itemClicked(int param0, int param1, double param2, double param3, int param4) {
    }

    public int getItemCount() {
        return this.proxy.getItemCount();
    }

    public void renderBackground() {
    }

    public int getMaxPosition() {
        return 0;
    }

    public int getScrollbarPosition() {
        return this.proxy.getRowLeft() + this.proxy.getRowWidth();
    }

    public int y0() {
        return this.proxy.y0();
    }

    public int y1() {
        return this.proxy.y1();
    }

    public int headerHeight() {
        return this.proxy.headerHeight();
    }

    public int itemHeight() {
        return this.proxy.itemHeight();
    }

    public void scroll(int param0) {
        this.proxy.setScrollAmount((double)param0);
    }

    public int getScroll() {
        return (int)this.proxy.getScrollAmount();
    }

    @Override
    public GuiEventListener getProxy() {
        return this.proxy;
    }

    public int getRowWidth() {
        return (int)((double)this.width() * 0.6);
    }

    public abstract boolean isFocused();

    public void selectItem(int param0) {
        this.setSelected(param0);
    }

    @Nullable
    public E getSelected() {
        return (E)this.proxy.getSelected();
    }

    public List<E> children() {
        return this.proxy.children();
    }

    public void replaceEntries(Collection<E> param0) {
        this.proxy.replaceEntries(param0);
    }

    public int getRowTop(int param0) {
        return this.proxy.getRowTop(param0);
    }

    public int getRowLeft() {
        return this.proxy.getRowLeft();
    }
}
