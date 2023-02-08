package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface Layout extends LayoutElement {
    void visitChildren(Consumer<LayoutElement> var1);

    @Override
    default void visitWidgets(Consumer<AbstractWidget> param0) {
        this.visitChildren(param1 -> param1.visitWidgets(param0));
    }

    default void arrangeElements() {
        this.visitChildren(param0 -> {
            if (param0 instanceof Layout var0) {
                var0.arrangeElements();
            }

        });
    }
}
