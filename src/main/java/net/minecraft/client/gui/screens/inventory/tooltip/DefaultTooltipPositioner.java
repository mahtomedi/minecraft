package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@OnlyIn(Dist.CLIENT)
public class DefaultTooltipPositioner implements ClientTooltipPositioner {
    public static final ClientTooltipPositioner INSTANCE = new DefaultTooltipPositioner();

    private DefaultTooltipPositioner() {
    }

    @Override
    public Vector2ic positionTooltip(int param0, int param1, int param2, int param3, int param4, int param5) {
        Vector2i var0 = new Vector2i(param2, param3).add(12, -12);
        this.positionTooltip(param0, param1, var0, param4, param5);
        return var0;
    }

    private void positionTooltip(int param0, int param1, Vector2i param2, int param3, int param4) {
        if (param2.x + param3 > param0) {
            param2.x = Math.max(param2.x - 24 - param3, 4);
        }

        int var0 = param4 + 3;
        if (param2.y + var0 > param1) {
            param2.y = param1 - var0;
        }

    }
}
