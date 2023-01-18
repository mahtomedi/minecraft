package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpacerElement implements LayoutElement {
    private int x;
    private int y;
    private final int width;
    private final int height;

    public SpacerElement(int param0, int param1) {
        this(0, 0, param0, param1);
    }

    public SpacerElement(int param0, int param1, int param2, int param3) {
        this.x = param0;
        this.y = param1;
        this.width = param2;
        this.height = param3;
    }

    public static SpacerElement width(int param0) {
        return new SpacerElement(param0, 0);
    }

    public static SpacerElement height(int param0) {
        return new SpacerElement(0, param0);
    }

    @Override
    public void setX(int param0) {
        this.x = param0;
    }

    @Override
    public void setY(int param0) {
        this.y = param0;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> param0) {
    }
}
