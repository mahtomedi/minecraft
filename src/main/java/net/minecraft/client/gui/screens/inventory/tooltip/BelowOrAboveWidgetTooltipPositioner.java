package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@OnlyIn(Dist.CLIENT)
public class BelowOrAboveWidgetTooltipPositioner implements ClientTooltipPositioner {
    private final ScreenRectangle screenRectangle;

    public BelowOrAboveWidgetTooltipPositioner(ScreenRectangle param0) {
        this.screenRectangle = param0;
    }

    @Override
    public Vector2ic positionTooltip(int param0, int param1, int param2, int param3, int param4, int param5) {
        Vector2i var0 = new Vector2i();
        var0.x = this.screenRectangle.left() + 3;
        var0.y = this.screenRectangle.bottom() + 3 + 1;
        if (var0.y + param5 + 3 > param1) {
            var0.y = this.screenRectangle.top() - param5 - 3 - 1;
        }

        if (var0.x + param4 > param0) {
            var0.x = Math.max(this.screenRectangle.right() - param4 - 3, 4);
        }

        return var0;
    }
}
