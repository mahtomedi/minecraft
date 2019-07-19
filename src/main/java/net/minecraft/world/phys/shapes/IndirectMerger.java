package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class IndirectMerger implements IndexMerger {
    private final DoubleArrayList result;
    private final IntArrayList firstIndices;
    private final IntArrayList secondIndices;

    protected IndirectMerger(DoubleList param0, DoubleList param1, boolean param2, boolean param3) {
        int var0 = 0;
        int var1 = 0;
        double var2 = Double.NaN;
        int var3 = param0.size();
        int var4 = param1.size();
        int var5 = var3 + var4;
        this.result = new DoubleArrayList(var5);
        this.firstIndices = new IntArrayList(var5);
        this.secondIndices = new IntArrayList(var5);

        while(true) {
            boolean var6 = var0 < var3;
            boolean var7 = var1 < var4;
            if (!var6 && !var7) {
                if (this.result.isEmpty()) {
                    this.result.add(Math.min(param0.getDouble(var3 - 1), param1.getDouble(var4 - 1)));
                }

                return;
            }

            boolean var8 = var6 && (!var7 || param0.getDouble(var0) < param1.getDouble(var1) + 1.0E-7);
            double var9 = var8 ? param0.getDouble(var0++) : param1.getDouble(var1++);
            if ((var0 != 0 && var6 || var8 || param3) && (var1 != 0 && var7 || !var8 || param2)) {
                if (!(var2 >= var9 - 1.0E-7)) {
                    this.firstIndices.add(var0 - 1);
                    this.secondIndices.add(var1 - 1);
                    this.result.add(var9);
                    var2 = var9;
                } else if (!this.result.isEmpty()) {
                    this.firstIndices.set(this.firstIndices.size() - 1, var0 - 1);
                    this.secondIndices.set(this.secondIndices.size() - 1, var1 - 1);
                }
            }
        }
    }

    @Override
    public boolean forMergedIndexes(IndexMerger.IndexConsumer param0) {
        for(int var0 = 0; var0 < this.result.size() - 1; ++var0) {
            if (!param0.merge(this.firstIndices.getInt(var0), this.secondIndices.getInt(var0), var0)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public DoubleList getList() {
        return this.result;
    }
}
