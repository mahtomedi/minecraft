package net.minecraft.world.level.border;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum BorderStatus {
    GROWING(4259712),
    SHRINKING(16724016),
    STATIONARY(2138367);

    private final int color;

    private BorderStatus(int param0) {
        this.color = param0;
    }

    public int getColor() {
        return this.color;
    }
}
