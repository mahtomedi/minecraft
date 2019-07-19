package net.minecraft.world.level.newbiome.context;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.layer.traits.PixelTransformer;

public interface BigContext<R extends Area> extends Context {
    void initRandom(long var1, long var3);

    R createResult(PixelTransformer var1);

    default R createResult(PixelTransformer param0, R param1) {
        return this.createResult(param0);
    }

    default R createResult(PixelTransformer param0, R param1, R param2) {
        return this.createResult(param0);
    }

    default int random(int param0, int param1) {
        return this.nextRandom(2) == 0 ? param0 : param1;
    }

    default int random(int param0, int param1, int param2, int param3) {
        int var0 = this.nextRandom(4);
        if (var0 == 0) {
            return param0;
        } else if (var0 == 1) {
            return param1;
        } else {
            return var0 == 2 ? param2 : param3;
        }
    }
}
