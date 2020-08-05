package net.minecraft.world.level.newbiome.layer;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum ShoreLayer implements CastleTransformer {
    INSTANCE;

    private static final IntSet SNOWY = new IntOpenHashSet(new int[]{26, 11, 12, 13, 140, 30, 31, 158, 10});
    private static final IntSet JUNGLES = new IntOpenHashSet(new int[]{168, 169, 21, 22, 23, 149, 151});

    @Override
    public int apply(Context param0, int param1, int param2, int param3, int param4, int param5) {
        if (param5 == 14) {
            if (Layers.isShallowOcean(param1) || Layers.isShallowOcean(param2) || Layers.isShallowOcean(param3) || Layers.isShallowOcean(param4)) {
                return 15;
            }
        } else if (JUNGLES.contains(param5)) {
            if (!isJungleCompatible(param1) || !isJungleCompatible(param2) || !isJungleCompatible(param3) || !isJungleCompatible(param4)) {
                return 23;
            }

            if (Layers.isOcean(param1) || Layers.isOcean(param2) || Layers.isOcean(param3) || Layers.isOcean(param4)) {
                return 16;
            }
        } else if (param5 != 3 && param5 != 34 && param5 != 20) {
            if (SNOWY.contains(param5)) {
                if (!Layers.isOcean(param5) && (Layers.isOcean(param1) || Layers.isOcean(param2) || Layers.isOcean(param3) || Layers.isOcean(param4))) {
                    return 26;
                }
            } else if (param5 != 37 && param5 != 38) {
                if (!Layers.isOcean(param5)
                    && param5 != 7
                    && param5 != 6
                    && (Layers.isOcean(param1) || Layers.isOcean(param2) || Layers.isOcean(param3) || Layers.isOcean(param4))) {
                    return 16;
                }
            } else if (!Layers.isOcean(param1)
                && !Layers.isOcean(param2)
                && !Layers.isOcean(param3)
                && !Layers.isOcean(param4)
                && (!this.isMesa(param1) || !this.isMesa(param2) || !this.isMesa(param3) || !this.isMesa(param4))) {
                return 2;
            }
        } else if (!Layers.isOcean(param5) && (Layers.isOcean(param1) || Layers.isOcean(param2) || Layers.isOcean(param3) || Layers.isOcean(param4))) {
            return 25;
        }

        return param5;
    }

    private static boolean isJungleCompatible(int param0) {
        return JUNGLES.contains(param0) || param0 == 4 || param0 == 5 || Layers.isOcean(param0);
    }

    private boolean isMesa(int param0) {
        return param0 == 37 || param0 == 38 || param0 == 39 || param0 == 165 || param0 == 166 || param0 == 167;
    }
}
