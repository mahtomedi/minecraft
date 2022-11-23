package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@OnlyIn(Dist.CLIENT)
public class BelowOrAboveWidgetTooltipPositioner implements ClientTooltipPositioner {
    private final AbstractWidget widget;

    public BelowOrAboveWidgetTooltipPositioner(AbstractWidget param0) {
        this.widget = param0;
    }

    @Override
    public Vector2ic positionTooltip(Screen param0, int param1, int param2, int param3, int param4) {
        Vector2i var0 = new Vector2i();
        var0.x = this.widget.getX() + 3;
        var0.y = this.widget.getY() + this.widget.getHeight() + 3 + 1;
        if (var0.y + param4 + 3 > param0.height) {
            var0.y = this.widget.getY() - param4 - 3 - 1;
        }

        if (var0.x + param3 > param0.width) {
            var0.x = Math.max(this.widget.getX() + this.widget.getWidth() - param3 - 3, 4);
        }

        return var0;
    }
}
