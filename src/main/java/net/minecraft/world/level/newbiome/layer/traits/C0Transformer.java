package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface C0Transformer extends AreaTransformer1, DimensionOffset0Transformer {
    int apply(Context var1, int var2);

    @Override
    default int applyPixel(BigContext<?> param0, Area param1, int param2, int param3) {
        return this.apply(param0, param1.get(this.getParentX(param2), this.getParentY(param3)));
    }
}
