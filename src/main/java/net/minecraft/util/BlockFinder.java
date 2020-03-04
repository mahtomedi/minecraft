package net.minecraft.util;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;

public class BlockFinder {
    public static Optional<BlockPos> findClosestMatchingBlockPos(BlockPos param0, int param1, int param2, Predicate<BlockPos> param3) {
        if (param3.test(param0)) {
            return Optional.of(param0);
        } else {
            int var0 = Math.max(param1, param2);
            BlockPos.MutableBlockPos var1 = param0.mutable();

            for(int var2 = 1; var2 <= var0; ++var2) {
                for(int var3 = -var2; var3 <= var2; ++var3) {
                    if (var3 <= param1 && var3 >= -param1) {
                        boolean var4 = var3 == -var2 || var3 == var2;

                        for(int var5 = -var2; var5 <= var2; ++var5) {
                            if (var5 <= param2 && var5 >= -param2) {
                                boolean var6 = var5 == -var2 || var5 == var2;

                                for(int var7 = -var2; var7 <= var2; ++var7) {
                                    if (var7 <= param1 && var7 >= -param1) {
                                        boolean var8 = var7 == -var2 || var7 == var2;
                                        if ((var4 || var6 || var8) && param3.test(var1.setWithOffset(param0, var3, var5, var7))) {
                                            return Optional.of(param0.offset(var3, var5, var7));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return Optional.empty();
        }
    }
}
