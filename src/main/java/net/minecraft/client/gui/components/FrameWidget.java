package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FrameWidget extends AbstractContainerWidget {
    private final List<FrameWidget.ChildContainer> children = new ArrayList<>();
    private final List<AbstractWidget> containedChildrenView = Collections.unmodifiableList(Lists.transform(this.children, param0x -> param0x.child));
    private int minWidth;
    private int minHeight;
    private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults().align(0.5F, 0.5F);

    public static FrameWidget withMinDimensions(int param0, int param1) {
        return new FrameWidget(0, 0, 0, 0).setMinDimensions(param0, param1);
    }

    public FrameWidget() {
        this(0, 0, 0, 0);
    }

    public FrameWidget(int param0, int param1, int param2, int param3) {
        super(param0, param1, param2, param3, Component.empty());
    }

    public FrameWidget setMinDimensions(int param0, int param1) {
        return this.setMinWidth(param0).setMinHeight(param1);
    }

    public FrameWidget setMinHeight(int param0) {
        this.minHeight = param0;
        return this;
    }

    public FrameWidget setMinWidth(int param0) {
        this.minWidth = param0;
        return this;
    }

    public LayoutSettings newChildLayoutSettings() {
        return this.defaultChildLayoutSettings.copy();
    }

    public LayoutSettings defaultChildLayoutSetting() {
        return this.defaultChildLayoutSettings;
    }

    public void pack() {
        int var0 = this.minWidth;
        int var1 = this.minHeight;

        for(FrameWidget.ChildContainer var2 : this.children) {
            var0 = Math.max(var0, var2.getWidth());
            var1 = Math.max(var1, var2.getHeight());
        }

        for(FrameWidget.ChildContainer var3 : this.children) {
            var3.setX(this.getX(), var0);
            var3.setY(this.getY(), var1);
        }

        this.width = var0;
        this.height = var1;
    }

    public <T extends AbstractWidget> T addChild(T param0) {
        return this.addChild(param0, this.newChildLayoutSettings());
    }

    public <T extends AbstractWidget> T addChild(T param0, LayoutSettings param1) {
        this.children.add(new FrameWidget.ChildContainer(param0, param1));
        return param0;
    }

    @Override
    protected List<AbstractWidget> getContainedChildren() {
        return this.containedChildrenView;
    }

    public static void centerInRectangle(AbstractWidget param0, int param1, int param2, int param3, int param4) {
        alignInRectangle(param0, param1, param2, param3, param4, 0.5F, 0.5F);
    }

    public static void alignInRectangle(AbstractWidget param0, int param1, int param2, int param3, int param4, float param5, float param6) {
        alignInDimension(param1, param3, param0.getWidth(), param0::setX, param5);
        alignInDimension(param2, param4, param0.getHeight(), param0::setY, param6);
    }

    public static void alignInDimension(int param0, int param1, int param2, Consumer<Integer> param3, float param4) {
        int var0 = (int)Mth.lerp(param4, 0.0F, (float)(param1 - param2));
        param3.accept(param0 + var0);
    }

    @OnlyIn(Dist.CLIENT)
    static class ChildContainer extends AbstractContainerWidget.AbstractChildWrapper {
        protected ChildContainer(AbstractWidget param0, LayoutSettings param1) {
            super(param0, param1);
        }
    }
}
