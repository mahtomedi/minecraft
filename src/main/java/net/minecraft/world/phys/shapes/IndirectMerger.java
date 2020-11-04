package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;

public class IndirectMerger implements IndexMerger {
    private static final DoubleList EMPTY = DoubleLists.unmodifiable(DoubleArrayList.wrap(new double[]{0.0}));
    private final double[] result;
    private final int[] firstIndices;
    private final int[] secondIndices;
    private final int resultLength;

    public IndirectMerger(DoubleList param0, DoubleList param1, boolean param2, boolean param3) {
        double var0 = Double.NaN;
        int var1 = param0.size();
        int var2 = param1.size();
        int var3 = var1 + var2;
        this.result = new double[var3];
        this.firstIndices = new int[var3];
        this.secondIndices = new int[var3];
        boolean var4 = !param2;
        boolean var5 = !param3;
        int var6 = 0;
        int var7 = 0;
        int var8 = 0;

        while(true) {
            boolean var11;
            while(true) {
                boolean var9 = var7 >= var1;
                boolean var10 = var8 >= var2;
                if (var9 && var10) {
                    this.resultLength = Math.max(1, var6);
                    return;
                }

                var11 = !var9 && (var10 || param0.getDouble(var7) < param1.getDouble(var8) + 1.0E-7);
                if (var11) {
                    ++var7;
                    if (!var4 || var8 != 0 && !var10) {
                        break;
                    }
                } else {
                    ++var8;
                    if (!var5 || var7 != 0 && !var9) {
                        break;
                    }
                }
            }

            int var12 = var7 - 1;
            int var13 = var8 - 1;
            double var14 = var11 ? param0.getDouble(var12) : param1.getDouble(var13);
            if (!(var0 >= var14 - 1.0E-7)) {
                this.firstIndices[var6] = var12;
                this.secondIndices[var6] = var13;
                this.result[var6] = var14;
                ++var6;
                var0 = var14;
            } else {
                this.firstIndices[var6 - 1] = var12;
                this.secondIndices[var6 - 1] = var13;
            }
        }
    }

    @Override
    public boolean forMergedIndexes(IndexMerger.IndexConsumer param0) {
        int var0 = this.resultLength - 1;

        for(int var1 = 0; var1 < var0; ++var1) {
            if (!param0.merge(this.firstIndices[var1], this.secondIndices[var1], var1)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int size() {
        return this.resultLength;
    }

    @Override
    public DoubleList getList() {
        return (DoubleList)(this.resultLength <= 1 ? EMPTY : DoubleArrayList.wrap(this.result, this.resultLength));
    }
}
