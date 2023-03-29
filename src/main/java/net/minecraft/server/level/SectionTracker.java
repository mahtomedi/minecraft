package net.minecraft.server.level;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;

public abstract class SectionTracker extends DynamicGraphMinFixedPoint {
    protected SectionTracker(int param0, int param1, int param2) {
        super(param0, param1, param2);
    }

    @Override
    protected void checkNeighborsAfterUpdate(long param0, int param1, boolean param2) {
        if (!param2 || param1 < this.levelCount - 2) {
            for(int var0 = -1; var0 <= 1; ++var0) {
                for(int var1 = -1; var1 <= 1; ++var1) {
                    for(int var2 = -1; var2 <= 1; ++var2) {
                        long var3 = SectionPos.offset(param0, var0, var1, var2);
                        if (var3 != param0) {
                            this.checkNeighbor(param0, var3, param1, param2);
                        }
                    }
                }
            }

        }
    }

    @Override
    protected int getComputedLevel(long param0, long param1, int param2) {
        int var0 = param2;

        for(int var1 = -1; var1 <= 1; ++var1) {
            for(int var2 = -1; var2 <= 1; ++var2) {
                for(int var3 = -1; var3 <= 1; ++var3) {
                    long var4 = SectionPos.offset(param0, var1, var2, var3);
                    if (var4 == param0) {
                        var4 = Long.MAX_VALUE;
                    }

                    if (var4 != param1) {
                        int var5 = this.computeLevelFromNeighbor(var4, param0, this.getLevel(var4));
                        if (var0 > var5) {
                            var0 = var5;
                        }

                        if (var0 == 0) {
                            return var0;
                        }
                    }
                }
            }
        }

        return var0;
    }

    @Override
    protected int computeLevelFromNeighbor(long param0, long param1, int param2) {
        return this.isSource(param0) ? this.getLevelFromSource(param1) : param2 + 1;
    }

    protected abstract int getLevelFromSource(long var1);

    public void update(long param0, int param1, boolean param2) {
        this.checkEdge(Long.MAX_VALUE, param0, param1, param2);
    }
}
