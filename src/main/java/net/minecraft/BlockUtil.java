package net.minecraft;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BlockUtil {
    public static BlockUtil.FoundRectangle getLargestRectangleAround(
        BlockPos param0, Direction.Axis param1, int param2, Direction.Axis param3, int param4, Predicate<BlockPos> param5
    ) {
        BlockPos.MutableBlockPos var0 = param0.mutable();
        Direction var1 = Direction.get(Direction.AxisDirection.NEGATIVE, param1);
        Direction var2 = var1.getOpposite();
        Direction var3 = Direction.get(Direction.AxisDirection.NEGATIVE, param3);
        Direction var4 = var3.getOpposite();
        int var5 = getLimit(param5, var0.set(param0), var1, param2);
        int var6 = getLimit(param5, var0.set(param0), var2, param2);
        int var7 = var5;
        BlockUtil.IntBounds[] var8 = new BlockUtil.IntBounds[var5 + 1 + var6];
        var8[var5] = new BlockUtil.IntBounds(getLimit(param5, var0.set(param0), var3, param4), getLimit(param5, var0.set(param0), var4, param4));
        int var9 = var8[var5].min;

        for(int var10 = 1; var10 <= var5; ++var10) {
            BlockUtil.IntBounds var11 = var8[var7 - (var10 - 1)];
            var8[var7 - var10] = new BlockUtil.IntBounds(
                getLimit(param5, var0.set(param0).move(var1, var10), var3, var11.min), getLimit(param5, var0.set(param0).move(var1, var10), var4, var11.max)
            );
        }

        for(int var12 = 1; var12 <= var6; ++var12) {
            BlockUtil.IntBounds var13 = var8[var7 + var12 - 1];
            var8[var7 + var12] = new BlockUtil.IntBounds(
                getLimit(param5, var0.set(param0).move(var2, var12), var3, var13.min), getLimit(param5, var0.set(param0).move(var2, var12), var4, var13.max)
            );
        }

        int var14 = 0;
        int var15 = 0;
        int var16 = 0;
        int var17 = 0;
        int[] var18 = new int[var8.length];

        for(int var19 = var9; var19 >= 0; --var19) {
            for(int var20 = 0; var20 < var8.length; ++var20) {
                BlockUtil.IntBounds var21 = var8[var20];
                int var22 = var9 - var21.min;
                int var23 = var9 + var21.max;
                var18[var20] = var19 >= var22 && var19 <= var23 ? var23 + 1 - var19 : 0;
            }

            Pair<BlockUtil.IntBounds, Integer> var24 = getMaxRectangleLocation(var18);
            BlockUtil.IntBounds var25 = var24.getFirst();
            int var26 = 1 + var25.max - var25.min;
            int var27 = var24.getSecond();
            if (var26 * var27 > var16 * var17) {
                var14 = var25.min;
                var15 = var19;
                var16 = var26;
                var17 = var27;
            }
        }

        return new BlockUtil.FoundRectangle(param0.relative(param1, var14 - var7).relative(param3, var15 - var9), var16, var17);
    }

    private static int getLimit(Predicate<BlockPos> param0, BlockPos.MutableBlockPos param1, Direction param2, int param3) {
        int var0 = 0;

        while(var0 < param3 && param0.test(param1.move(param2))) {
            ++var0;
        }

        return var0;
    }

    @VisibleForTesting
    static Pair<BlockUtil.IntBounds, Integer> getMaxRectangleLocation(int[] param0) {
        int var0 = 0;
        int var1 = 0;
        int var2 = 0;
        IntStack var3 = new IntArrayList();
        var3.push(0);

        for(int var4 = 1; var4 <= param0.length; ++var4) {
            int var5 = var4 == param0.length ? 0 : param0[var4];

            while(!var3.isEmpty()) {
                int var6 = param0[var3.topInt()];
                if (var5 >= var6) {
                    var3.push(var4);
                    break;
                }

                var3.popInt();
                int var7 = var3.isEmpty() ? 0 : var3.topInt() + 1;
                if (var6 * (var4 - var7) > var2 * (var1 - var0)) {
                    var1 = var4;
                    var0 = var7;
                    var2 = var6;
                }
            }

            if (var3.isEmpty()) {
                var3.push(var4);
            }
        }

        return new Pair<>(new BlockUtil.IntBounds(var0, var1 - 1), var2);
    }

    public static class FoundRectangle {
        public final BlockPos minCorner;
        public final int axis1Size;
        public final int axis2Size;

        public FoundRectangle(BlockPos param0, int param1, int param2) {
            this.minCorner = param0;
            this.axis1Size = param1;
            this.axis2Size = param2;
        }
    }

    public static class IntBounds {
        public final int min;
        public final int max;

        public IntBounds(int param0, int param1) {
            this.min = param0;
            this.max = param1;
        }

        @Override
        public String toString() {
            return "IntBounds{min=" + this.min + ", max=" + this.max + '}';
        }
    }
}
