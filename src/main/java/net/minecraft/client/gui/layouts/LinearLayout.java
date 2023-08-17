package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LinearLayout implements Layout {
    private final GridLayout wrapped;
    private final LinearLayout.Orientation orientation;
    private int nextChildIndex = 0;

    private LinearLayout(LinearLayout.Orientation param0) {
        this(0, 0, param0);
    }

    public LinearLayout(int param0, int param1, LinearLayout.Orientation param2) {
        this.wrapped = new GridLayout(param0, param1);
        this.orientation = param2;
    }

    public LinearLayout spacing(int param0) {
        this.orientation.setSpacing(this.wrapped, param0);
        return this;
    }

    public LayoutSettings newCellSettings() {
        return this.wrapped.newCellSettings();
    }

    public LayoutSettings defaultCellSetting() {
        return this.wrapped.defaultCellSetting();
    }

    public <T extends LayoutElement> T addChild(T param0, LayoutSettings param1) {
        return this.orientation.addChild(this.wrapped, param0, this.nextChildIndex++, param1);
    }

    public <T extends LayoutElement> T addChild(T param0) {
        return this.addChild(param0, this.newCellSettings());
    }

    public <T extends LayoutElement> T addChild(T param0, Consumer<LayoutSettings> param1) {
        return this.orientation.addChild(this.wrapped, param0, this.nextChildIndex++, Util.make(this.newCellSettings(), param1));
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> param0) {
        this.wrapped.visitChildren(param0);
    }

    @Override
    public void arrangeElements() {
        this.wrapped.arrangeElements();
    }

    @Override
    public int getWidth() {
        return this.wrapped.getWidth();
    }

    @Override
    public int getHeight() {
        return this.wrapped.getHeight();
    }

    @Override
    public void setX(int param0) {
        this.wrapped.setX(param0);
    }

    @Override
    public void setY(int param0) {
        this.wrapped.setY(param0);
    }

    @Override
    public int getX() {
        return this.wrapped.getX();
    }

    @Override
    public int getY() {
        return this.wrapped.getY();
    }

    public static LinearLayout vertical() {
        return new LinearLayout(LinearLayout.Orientation.VERTICAL);
    }

    public static LinearLayout horizontal() {
        return new LinearLayout(LinearLayout.Orientation.HORIZONTAL);
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Orientation {
        HORIZONTAL,
        VERTICAL;

        void setSpacing(GridLayout param0, int param1) {
            switch(this) {
                case HORIZONTAL:
                    param0.columnSpacing(param1);
                    break;
                case VERTICAL:
                    param0.rowSpacing(param1);
            }

        }

        public <T extends LayoutElement> T addChild(GridLayout param0, T param1, int param2, LayoutSettings param3) {
            return (T)(switch(this) {
                case HORIZONTAL -> param0.addChild(param1, 0, param2, param3);
                case VERTICAL -> param0.addChild(param1, param2, 0, param3);
            });
        }
    }
}
