package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1;

public enum ZoomLayer implements AreaTransformer1 {
    NORMAL,
    FUZZY {
        @Override
        protected int modeOrRandom(BigContext<?> param0, int param1, int param2, int param3, int param4) {
            return param0.random(param1, param2, param3, param4);
        }
    };

    private ZoomLayer() {
    }

    @Override
    public int getParentX(int param0) {
        return param0 >> 1;
    }

    @Override
    public int getParentY(int param0) {
        return param0 >> 1;
    }

    @Override
    public int applyPixel(BigContext<?> param0, Area param1, int param2, int param3) {
        int var0 = param1.get(this.getParentX(param2), this.getParentY(param3));
        param0.initRandom((long)(param2 >> 1 << 1), (long)(param3 >> 1 << 1));
        int var1 = param2 & 1;
        int var2 = param3 & 1;
        if (var1 == 0 && var2 == 0) {
            return var0;
        } else {
            int var3 = param1.get(this.getParentX(param2), this.getParentY(param3 + 1));
            int var4 = param0.random(var0, var3);
            if (var1 == 0 && var2 == 1) {
                return var4;
            } else {
                int var5 = param1.get(this.getParentX(param2 + 1), this.getParentY(param3));
                int var6 = param0.random(var0, var5);
                if (var1 == 1 && var2 == 0) {
                    return var6;
                } else {
                    int var7 = param1.get(this.getParentX(param2 + 1), this.getParentY(param3 + 1));
                    return this.modeOrRandom(param0, var0, var5, var3, var7);
                }
            }
        }
    }

    protected int modeOrRandom(BigContext<?> param0, int param1, int param2, int param3, int param4) {
        if (param2 == param3 && param3 == param4) {
            return param2;
        } else if (param1 == param2 && param1 == param3) {
            return param1;
        } else if (param1 == param2 && param1 == param4) {
            return param1;
        } else if (param1 == param3 && param1 == param4) {
            return param1;
        } else if (param1 == param2 && param3 != param4) {
            return param1;
        } else if (param1 == param3 && param2 != param4) {
            return param1;
        } else if (param1 == param4 && param2 != param3) {
            return param1;
        } else if (param2 == param3 && param1 != param4) {
            return param2;
        } else if (param2 == param4 && param1 != param3) {
            return param2;
        } else {
            return param3 == param4 && param1 != param2 ? param3 : param0.random(param1, param2, param3, param4);
        }
    }
}
