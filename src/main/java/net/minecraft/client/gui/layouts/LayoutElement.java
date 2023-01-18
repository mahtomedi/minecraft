package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface LayoutElement {
    void setX(int var1);

    void setY(int var1);

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    default void setPosition(int param0, int param1) {
        this.setX(param0);
        this.setY(param1);
    }

    void visitWidgets(Consumer<AbstractWidget> var1);
}
