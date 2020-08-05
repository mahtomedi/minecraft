package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum BiomeEdgeLayer implements CastleTransformer {
    INSTANCE;

    @Override
    public int apply(Context param0, int param1, int param2, int param3, int param4, int param5) {
        int[] var0 = new int[1];
        if (!this.checkEdge(var0, param5)
            && !this.checkEdgeStrict(var0, param1, param2, param3, param4, param5, 38, 37)
            && !this.checkEdgeStrict(var0, param1, param2, param3, param4, param5, 39, 37)
            && !this.checkEdgeStrict(var0, param1, param2, param3, param4, param5, 32, 5)) {
            if (param5 != 2 || param1 != 12 && param2 != 12 && param4 != 12 && param3 != 12) {
                if (param5 == 6) {
                    if (param1 == 2
                        || param2 == 2
                        || param4 == 2
                        || param3 == 2
                        || param1 == 30
                        || param2 == 30
                        || param4 == 30
                        || param3 == 30
                        || param1 == 12
                        || param2 == 12
                        || param4 == 12
                        || param3 == 12) {
                        return 1;
                    }

                    if (param1 == 21 || param3 == 21 || param2 == 21 || param4 == 21 || param1 == 168 || param3 == 168 || param2 == 168 || param4 == 168) {
                        return 23;
                    }
                }

                return param5;
            } else {
                return 34;
            }
        } else {
            return var0[0];
        }
    }

    private boolean checkEdge(int[] param0, int param1) {
        if (!Layers.isSame(param1, 3)) {
            return false;
        } else {
            param0[0] = param1;
            return true;
        }
    }

    private boolean checkEdgeStrict(int[] param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        if (param5 != param6) {
            return false;
        } else {
            if (Layers.isSame(param1, param6) && Layers.isSame(param2, param6) && Layers.isSame(param4, param6) && Layers.isSame(param3, param6)) {
                param0[0] = param5;
            } else {
                param0[0] = param7;
            }

            return true;
        }
    }
}
