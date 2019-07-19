package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ScrolledSelectionList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsScrolledSelectionListProxy extends ScrolledSelectionList {
    private final RealmsScrolledSelectionList realmsScrolledSelectionList;

    public RealmsScrolledSelectionListProxy(RealmsScrolledSelectionList param0, int param1, int param2, int param3, int param4, int param5) {
        super(Minecraft.getInstance(), param1, param2, param3, param4, param5);
        this.realmsScrolledSelectionList = param0;
    }

    @Override
    public int getItemCount() {
        return this.realmsScrolledSelectionList.getItemCount();
    }

    @Override
    public boolean selectItem(int param0, int param1, double param2, double param3) {
        return this.realmsScrolledSelectionList.selectItem(param0, param1, param2, param3);
    }

    @Override
    public boolean isSelectedItem(int param0) {
        return this.realmsScrolledSelectionList.isSelectedItem(param0);
    }

    @Override
    public void renderBackground() {
        this.realmsScrolledSelectionList.renderBackground();
    }

    @Override
    public void renderItem(int param0, int param1, int param2, int param3, int param4, int param5, float param6) {
        this.realmsScrolledSelectionList.renderItem(param0, param1, param2, param3, param4, param5);
    }

    public int getWidth() {
        return this.width;
    }

    @Override
    public int getMaxPosition() {
        return this.realmsScrolledSelectionList.getMaxPosition();
    }

    @Override
    public int getScrollbarPosition() {
        return this.realmsScrolledSelectionList.getScrollbarPosition();
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        return this.realmsScrolledSelectionList.mouseScrolled(param0, param1, param2) ? true : super.mouseScrolled(param0, param1, param2);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        return this.realmsScrolledSelectionList.mouseClicked(param0, param1, param2) ? true : super.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        return this.realmsScrolledSelectionList.mouseReleased(param0, param1, param2);
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        return this.realmsScrolledSelectionList.mouseDragged(param0, param1, param2, param3, param4);
    }
}
