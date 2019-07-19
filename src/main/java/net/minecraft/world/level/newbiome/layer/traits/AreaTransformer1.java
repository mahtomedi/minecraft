package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;

public interface AreaTransformer1 extends DimensionTransformer {
    default <R extends Area> AreaFactory<R> run(BigContext<R> param0, AreaFactory<R> param1) {
        return () -> {
            R var0 = param1.make();
            return param0.createResult((param2, param3) -> {
                param0.initRandom((long)param2, (long)param3);
                return this.applyPixel(param0, var0, param2, param3);
            }, var0);
        };
    }

    int applyPixel(BigContext<?> var1, Area var2, int var3, int var4);
}
