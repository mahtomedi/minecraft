package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer1;

public enum VoronoiZoom implements AreaTransformer1 {
    INSTANCE;

    @Override
    public int applyPixel(BigContext<?> param0, Area param1, int param2, int param3) {
        int var0 = param2 - 2;
        int var1 = param3 - 2;
        int var2 = var0 >> 2;
        int var3 = var1 >> 2;
        int var4 = var2 << 2;
        int var5 = var3 << 2;
        param0.initRandom((long)var4, (long)var5);
        double var6 = ((double)param0.nextRandom(1024) / 1024.0 - 0.5) * 3.6;
        double var7 = ((double)param0.nextRandom(1024) / 1024.0 - 0.5) * 3.6;
        param0.initRandom((long)(var4 + 4), (long)var5);
        double var8 = ((double)param0.nextRandom(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
        double var9 = ((double)param0.nextRandom(1024) / 1024.0 - 0.5) * 3.6;
        param0.initRandom((long)var4, (long)(var5 + 4));
        double var10 = ((double)param0.nextRandom(1024) / 1024.0 - 0.5) * 3.6;
        double var11 = ((double)param0.nextRandom(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
        param0.initRandom((long)(var4 + 4), (long)(var5 + 4));
        double var12 = ((double)param0.nextRandom(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
        double var13 = ((double)param0.nextRandom(1024) / 1024.0 - 0.5) * 3.6 + 4.0;
        int var14 = var0 & 3;
        int var15 = var1 & 3;
        double var16 = ((double)var15 - var7) * ((double)var15 - var7) + ((double)var14 - var6) * ((double)var14 - var6);
        double var17 = ((double)var15 - var9) * ((double)var15 - var9) + ((double)var14 - var8) * ((double)var14 - var8);
        double var18 = ((double)var15 - var11) * ((double)var15 - var11) + ((double)var14 - var10) * ((double)var14 - var10);
        double var19 = ((double)var15 - var13) * ((double)var15 - var13) + ((double)var14 - var12) * ((double)var14 - var12);
        if (var16 < var17 && var16 < var18 && var16 < var19) {
            return param1.get(this.getParentX(var4), this.getParentY(var5));
        } else if (var17 < var16 && var17 < var18 && var17 < var19) {
            return param1.get(this.getParentX(var4 + 4), this.getParentY(var5)) & 0xFF;
        } else {
            return var18 < var16 && var18 < var17 && var18 < var19
                ? param1.get(this.getParentX(var4), this.getParentY(var5 + 4))
                : param1.get(this.getParentX(var4 + 4), this.getParentY(var5 + 4)) & 0xFF;
        }
    }

    @Override
    public int getParentX(int param0) {
        return param0 >> 2;
    }

    @Override
    public int getParentY(int param0) {
        return param0 >> 2;
    }
}
