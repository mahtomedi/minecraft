package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer2;
import net.minecraft.world.level.newbiome.layer.traits.DimensionOffset0Transformer;

public enum OceanMixerLayer implements AreaTransformer2, DimensionOffset0Transformer {
    INSTANCE;

    @Override
    public int applyPixel(Context param0, Area param1, Area param2, int param3, int param4) {
        int var0 = param1.get(this.getParentX(param3), this.getParentY(param4));
        int var1 = param2.get(this.getParentX(param3), this.getParentY(param4));
        if (!Layers.isOcean(var0)) {
            return var0;
        } else {
            int var2 = 8;
            int var3 = 4;

            for(int var4 = -8; var4 <= 8; var4 += 4) {
                for(int var5 = -8; var5 <= 8; var5 += 4) {
                    int var6 = param1.get(this.getParentX(param3 + var4), this.getParentY(param4 + var5));
                    if (!Layers.isOcean(var6)) {
                        if (var1 == 44) {
                            return 45;
                        }

                        if (var1 == 10) {
                            return 46;
                        }
                    }
                }
            }

            if (var0 == 24) {
                if (var1 == 45) {
                    return 48;
                }

                if (var1 == 0) {
                    return 24;
                }

                if (var1 == 46) {
                    return 49;
                }

                if (var1 == 10) {
                    return 50;
                }
            }

            return var1;
        }
    }
}
