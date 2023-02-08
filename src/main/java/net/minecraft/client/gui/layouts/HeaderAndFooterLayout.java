package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HeaderAndFooterLayout implements Layout {
    private static final int DEFAULT_HEADER_AND_FOOTER_HEIGHT = 36;
    private final FrameLayout headerFrame = new FrameLayout();
    private final FrameLayout footerFrame = new FrameLayout();
    private final FrameLayout contentsFrame = new FrameLayout();
    private final Screen screen;
    private int headerHeight;
    private int footerHeight;

    public HeaderAndFooterLayout(Screen param0) {
        this(param0, 36);
    }

    public HeaderAndFooterLayout(Screen param0, int param1) {
        this(param0, param1, param1);
    }

    public HeaderAndFooterLayout(Screen param0, int param1, int param2) {
        this.screen = param0;
        this.headerHeight = param1;
        this.footerHeight = param2;
        this.headerFrame.defaultChildLayoutSetting().align(0.5F, 0.5F);
        this.footerFrame.defaultChildLayoutSetting().align(0.5F, 0.5F);
        this.contentsFrame.defaultChildLayoutSetting().align(0.5F, 0.5F);
    }

    @Override
    public void setX(int param0) {
    }

    @Override
    public void setY(int param0) {
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public int getWidth() {
        return this.screen.width;
    }

    @Override
    public int getHeight() {
        return this.screen.height;
    }

    public int getFooterHeight() {
        return this.footerHeight;
    }

    public void setFooterHeight(int param0) {
        this.footerHeight = param0;
    }

    public void setHeaderHeight(int param0) {
        this.headerHeight = param0;
    }

    public int getHeaderHeight() {
        return this.headerHeight;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> param0) {
        this.headerFrame.visitChildren(param0);
        this.contentsFrame.visitChildren(param0);
        this.footerFrame.visitChildren(param0);
    }

    @Override
    public void arrangeElements() {
        int var0 = this.getHeaderHeight();
        int var1 = this.getFooterHeight();
        this.headerFrame.setMinWidth(this.screen.width);
        this.headerFrame.setMinHeight(var0);
        this.headerFrame.setPosition(0, 0);
        this.headerFrame.arrangeElements();
        this.footerFrame.setMinWidth(this.screen.width);
        this.footerFrame.setMinHeight(var1);
        this.footerFrame.arrangeElements();
        this.footerFrame.setY(this.screen.height - var1);
        this.contentsFrame.setMinWidth(this.screen.width);
        this.contentsFrame.setMinHeight(this.screen.height - var0 - var1);
        this.contentsFrame.setPosition(0, var0);
        this.contentsFrame.arrangeElements();
    }

    public <T extends LayoutElement> T addToHeader(T param0) {
        return this.headerFrame.addChild(param0);
    }

    public <T extends LayoutElement> T addToHeader(T param0, LayoutSettings param1) {
        return this.headerFrame.addChild(param0, param1);
    }

    public <T extends LayoutElement> T addToFooter(T param0) {
        return this.footerFrame.addChild(param0);
    }

    public <T extends LayoutElement> T addToFooter(T param0, LayoutSettings param1) {
        return this.footerFrame.addChild(param0, param1);
    }

    public <T extends LayoutElement> T addToContents(T param0) {
        return this.contentsFrame.addChild(param0);
    }

    public <T extends LayoutElement> T addToContents(T param0, LayoutSettings param1) {
        return this.contentsFrame.addChild(param0, param1);
    }

    public LayoutSettings newHeaderLayoutSettings() {
        return this.headerFrame.newChildLayoutSettings();
    }

    public LayoutSettings newContentLayoutSettings() {
        return this.contentsFrame.newChildLayoutSettings();
    }

    public LayoutSettings newFooterLayoutSettings() {
        return this.footerFrame.newChildLayoutSettings();
    }
}
