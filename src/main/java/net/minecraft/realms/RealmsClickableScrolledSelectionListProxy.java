package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ScrolledSelectionList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsClickableScrolledSelectionListProxy extends ScrolledSelectionList {
    private final RealmsClickableScrolledSelectionList realmsClickableScrolledSelectionList;

    public RealmsClickableScrolledSelectionListProxy(RealmsClickableScrolledSelectionList param0, int param1, int param2, int param3, int param4, int param5) {
        super(Minecraft.getInstance(), param1, param2, param3, param4, param5);
        this.realmsClickableScrolledSelectionList = param0;
    }

    @Override
    public int getItemCount() {
        return this.realmsClickableScrolledSelectionList.getItemCount();
    }

    @Override
    public boolean selectItem(int param0, int param1, double param2, double param3) {
        return this.realmsClickableScrolledSelectionList.selectItem(param0, param1, param2, param3);
    }

    @Override
    public boolean isSelectedItem(int param0) {
        return this.realmsClickableScrolledSelectionList.isSelectedItem(param0);
    }

    @Override
    public void renderBackground() {
        this.realmsClickableScrolledSelectionList.renderBackground();
    }

    @Override
    public void renderItem(int param0, int param1, int param2, int param3, int param4, int param5, float param6) {
        this.realmsClickableScrolledSelectionList.renderItem(param0, param1, param2, param3, param4, param5);
    }

    public int getWidth() {
        return this.width;
    }

    @Override
    public int getMaxPosition() {
        return this.realmsClickableScrolledSelectionList.getMaxPosition();
    }

    @Override
    public int getScrollbarPosition() {
        return this.realmsClickableScrolledSelectionList.getScrollbarPosition();
    }

    public void itemClicked(int param0, int param1, int param2, int param3, int param4) {
        this.realmsClickableScrolledSelectionList.itemClicked(param0, param1, (double)param2, (double)param3, param4);
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        return this.realmsClickableScrolledSelectionList.mouseScrolled(param0, param1, param2) ? true : super.mouseScrolled(param0, param1, param2);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        return this.realmsClickableScrolledSelectionList.mouseClicked(param0, param1, param2) ? true : super.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        return this.realmsClickableScrolledSelectionList.mouseReleased(param0, param1, param2);
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        return this.realmsClickableScrolledSelectionList.mouseDragged(param0, param1, param2, param3, param4)
            ? true
            : super.mouseDragged(param0, param1, param2, param3, param4);
    }

    public void renderSelected(int param0, int param1, int param2, Tezzelator param3) {
        this.realmsClickableScrolledSelectionList.renderSelected(param0, param1, param2, param3);
    }

    @Override
    public void renderList(int param0, int param1, int param2, int param3, float param4) {
        int var0 = this.getItemCount();

        for(int var1 = 0; var1 < var0; ++var1) {
            int var2 = param1 + var1 * this.itemHeight + this.headerHeight;
            int var3 = this.itemHeight - 4;
            if (var2 > this.y1 || var2 + var3 < this.y0) {
                this.updateItemPosition(var1, param0, var2, param4);
            }

            if (this.renderSelection && this.isSelectedItem(var1)) {
                this.renderSelected(this.width, var2, var3, Tezzelator.instance);
            }

            this.renderItem(var1, param0, var2, var3, param2, param3, param4);
        }

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

    public double yo() {
        return this.yo;
    }

    public int itemHeight() {
        return this.itemHeight;
    }
}
