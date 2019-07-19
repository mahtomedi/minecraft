package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface AreaTransformer0 {
    default <R extends Area> AreaFactory<R> run(BigContext<R> param0) {
        return () -> param0.createResult((param1, param2) -> {
                param0.initRandom((long)param1, (long)param2);
                return this.applyPixel(param0, param1, param2);
            });
    }

    int applyPixel(Context var1, int var2, int var3);
}
