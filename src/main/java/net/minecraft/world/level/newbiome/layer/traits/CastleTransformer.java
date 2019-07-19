package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface CastleTransformer extends AreaTransformer1, DimensionOffset1Transformer {
    int apply(Context var1, int var2, int var3, int var4, int var5, int var6);

    @Override
    default int applyPixel(BigContext<?> param0, Area param1, int param2, int param3) {
        return this.apply(
            param0,
            param1.get(this.getParentX(param2 + 1), this.getParentY(param3 + 0)),
            param1.get(this.getParentX(param2 + 2), this.getParentY(param3 + 1)),
            param1.get(this.getParentX(param2 + 1), this.getParentY(param3 + 2)),
            param1.get(this.getParentX(param2 + 0), this.getParentY(param3 + 1)),
            param1.get(this.getParentX(param2 + 1), this.getParentY(param3 + 1))
        );
    }
}
