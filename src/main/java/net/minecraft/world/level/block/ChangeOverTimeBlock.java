package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface ChangeOverTimeBlock<T extends Enum<T>> {
    Optional<BlockState> getNext(BlockState var1);

    float getChanceModifier();

    default void onRandomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        float var0 = 0.05688889F;
        if (param3.nextFloat() < 0.05688889F) {
            this.applyChangeOverTime(param0, param1, param2, param3);
        }

    }

    T getAge();

    default void applyChangeOverTime(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        int var0 = this.getAge().ordinal();
        int var1 = 0;
        int var2 = 0;

        for(BlockPos var3 : BlockPos.withinManhattan(param2, 4, 4, 4)) {
            int var4 = var3.distManhattan(param2);
            if (var4 > 4) {
                break;
            }

            if (!var3.equals(param2)) {
                BlockState var5 = param1.getBlockState(var3);
                Block var6 = var5.getBlock();
                if (var6 instanceof ChangeOverTimeBlock) {
                    Enum<?> var7 = ((ChangeOverTimeBlock)var6).getAge();
                    if (this.getAge().getClass() == var7.getClass()) {
                        int var8 = var7.ordinal();
                        if (var8 < var0) {
                            return;
                        }

                        if (var8 > var0) {
                            ++var2;
                        } else {
                            ++var1;
                        }
                    }
                }
            }
        }

        float var9 = (float)(var2 + 1) / (float)(var2 + var1 + 1);
        float var10 = var9 * var9 * this.getChanceModifier();
        if (param3.nextFloat() < var10) {
            this.getNext(param0).ifPresent(param2x -> param1.setBlockAndUpdate(param2, param2x));
        }

    }
}
