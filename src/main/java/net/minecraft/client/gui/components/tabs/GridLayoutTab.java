package net.minecraft.client.gui.components.tabs;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GridLayoutTab implements Tab {
    private final Component title;
    protected final GridLayout layout = new GridLayout();

    public GridLayoutTab(Component param0) {
        this.title = param0;
    }

    @Override
    public Component getTabTitle() {
        return this.title;
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> param0) {
        this.layout.visitWidgets(param0);
    }

    @Override
    public void doLayout(ScreenRectangle param0) {
        this.layout.arrangeElements();
        FrameLayout.alignInRectangle(this.layout, param0, 0.5F, 0.16666667F);
    }
}
