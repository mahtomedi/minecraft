package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.navigation.ScreenRectangle;
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
    private final ScreenRectangle screenRectangle;

    public MenuTooltipPositioner(ScreenRectangle param0) {
        this.screenRectangle = param0;
    }

    @Override
    public Vector2ic positionTooltip(int param0, int param1, int param2, int param3, int param4, int param5) {
        Vector2i var0 = new Vector2i(param2 + 12, param3);
        if (var0.x + param4 > param0 - 5) {
            var0.x = Math.max(param2 - 12 - param4, 9);
        }

        var0.y += 3;
        int var1 = param5 + 3 + 3;
        int var2 = this.screenRectangle.bottom() + 3 + getOffset(0, 0, this.screenRectangle.height());
        int var3 = param1 - 5;
        if (var2 + var1 <= var3) {
            var0.y += getOffset(var0.y, this.screenRectangle.top(), this.screenRectangle.height());
        } else {
            var0.y -= var1 + getOffset(var0.y, this.screenRectangle.bottom(), this.screenRectangle.height());
        }

        return var0;
    }

    private static int getOffset(int param0, int param1, int param2) {
        int var0 = Math.min(Math.abs(param0 - param1), param2);
        return Math.round(Mth.lerp((float)var0 / (float)param2, (float)(param2 - 3), 5.0F));
    }
}
