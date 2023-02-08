package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@OnlyIn(Dist.CLIENT)
public class MenuTooltipPositioner implements ClientTooltipPositioner {
    private static final int MARGIN = 5;
    private static final int MOUSE_OFFSET_X = 12;
    public static final int MAX_OVERLAP_WITH_WIDGET = 3;
    public static final int MAX_DISTANCE_TO_WIDGET = 5;
    private final AbstractWidget widget;

    public MenuTooltipPositioner(AbstractWidget param0) {
        this.widget = param0;
    }

    @Override
    public Vector2ic positionTooltip(Screen param0, int param1, int param2, int param3, int param4) {
        Vector2i var0 = new Vector2i(param1 + 12, param2);
        if (var0.x + param3 > param0.width - 5) {
            var0.x = Math.max(param1 - 12 - param3, 9);
        }

        var0.y += 3;
        int var1 = param4 + 3 + 3;
        int var2 = this.widget.getY() + this.widget.getHeight() + 3 + getOffset(0, 0, this.widget.getHeight());
        int var3 = param0.height - 5;
        if (var2 + var1 <= var3) {
            var0.y += getOffset(var0.y, this.widget.getY(), this.widget.getHeight());
        } else {
            var0.y -= var1 + getOffset(var0.y, this.widget.getY() + this.widget.getHeight(), this.widget.getHeight());
        }

        return var0;
    }

    private static int getOffset(int param0, int param1, int param2) {
        int var0 = Math.min(Math.abs(param0 - param1), param2);
        return Math.round((float)Mth.lerp((float)var0 / (float)param2, param2 - 3, 5));
    }
}
