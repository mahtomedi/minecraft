package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset0Transformer;

public enum RiverMixerLayer implements AreaTransformer2, DimensionOffset0Transformer {
    INSTANCE;

    @Override
    public int applyPixel(Context param0, Area param1, Area param2, int param3, int param4) {
        int var0 = param1.get(this.getParentX(param3), this.getParentY(param4));
        int var1 = param2.get(this.getParentX(param3), this.getParentY(param4));
        if (Layers.isOcean(var0)) {
            return var0;
        } else if (var1 == 7) {
            if (var0 == 12) {
                return 11;
            } else {
                return var0 != 14 && var0 != 15 ? var1 & 0xFF : 15;
            }
        } else {
            return var0;
        }
    }
}
