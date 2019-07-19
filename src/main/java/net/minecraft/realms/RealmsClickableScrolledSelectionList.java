package net.minecraft.realms;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RealmsClickableScrolledSelectionList extends RealmsGuiEventListener {
    private final RealmsClickableScrolledSelectionListProxy proxy;

    public RealmsClickableScrolledSelectionList(int param0, int param1, int param2, int param3, int param4) {
        this.proxy = new RealmsClickableScrolledSelectionListProxy(this, param0, param1, param2, param3, param4);
    }

    public void render(int param0, int param1, float param2) {
        this.proxy.render(param0, param1, param2);
    }

    public int width() {
        return this.proxy.getWidth();
    }

    protected void renderItem(int param0, int param1, int param2, int param3, Tezzelator param4, int param5, int param6) {
    }

    public void renderItem(int param0, int param1, int param2, int param3, int param4, int param5) {
        this.renderItem(param0, param1, param2, param3, Tezzelator.instance, param4, param5);
    }

    public int getItemCount() {
        return 0;
    }

    public boolean selectItem(int param0, int param1, double param2, double param3) {
        return true;
    }

    public boolean isSelectedItem(int param0) {
        return false;
    }

    public void renderBackground() {
    }

    public int getMaxPosition() {
        return 0;
    }

    public int getScrollbarPosition() {
        return this.proxy.getWidth() / 2 + 124;
    }

    @Override
    public GuiEventListener getProxy() {
        return this.proxy;
    }

    public void scroll(int param0) {
        this.proxy.scroll(param0);
    }

    public int getScroll() {
        return this.proxy.getScroll();
    }

    protected void renderList(int param0, int param1, int param2, int param3) {
    }

    public void itemClicked(int param0, int param1, double param2, double param3, int param4) {
    }

    public void renderSelected(int param0, int param1, int param2, Tezzelator param3) {
    }

    public void setLeftPos(int param0) {
        this.proxy.setLeftPos(param0);
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

    public double yo() {
        return this.proxy.yo();
    }

    public int itemHeight() {
        return this.proxy.itemHeight();
    }

    public boolean isVisible() {
        return this.proxy.isVisible();
    }
}
