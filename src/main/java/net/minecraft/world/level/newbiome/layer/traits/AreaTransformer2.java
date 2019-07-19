package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface AreaTransformer2 extends DimensionTransformer {
    default <R extends Area> AreaFactory<R> run(BigContext<R> param0, AreaFactory<R> param1, AreaFactory<R> param2) {
        return () -> {
            R var0 = param1.make();
            R var1x = param2.make();
            return param0.createResult((param3, param4) -> {
                param0.initRandom((long)param3, (long)param4);
                return this.applyPixel(param0, var0, var1x, param3, param4);
            }, var0, (R)var1x);
        };
    }

    int applyPixel(Context var1, Area var2, Area var3, int var4, int var5);
}
