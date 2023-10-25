package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public interface ChangeOverTimeBlock<T extends Enum<T>> {
    int SCAN_DISTANCE = 4;

    Optional<BlockState> getNext(BlockState var1);

    float getChanceModifier();

    default void changeOverTime(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        float var0 = 0.05688889F;
        if (param3.nextFloat() < 0.05688889F) {
            this.getNextState(param0, param1, param2, param3).ifPresent(param2x -> param1.setBlockAndUpdate(param2, param2x));
        }

    }

    T getAge();

    default Optional<BlockState> getNextState(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        int var0 = this.getAge().ordinal();
        int var1 = 0;
        int var2 = 0;

        for(BlockPos var3 : BlockPos.withinManhattan(param2, 4, 4, 4)) {
            int var4 = var3.distManhattan(param2);
            if (var4 > 4) {
                break;
            }

            if (!var3.equals(param2)) {
                Block var6 = param1.getBlockState(var3).getBlock();
                if (var6 instanceof ChangeOverTimeBlock var5) {
                    Enum<?> var6x = var5.getAge();
                    if (this.getAge().getClass() == var6x.getClass()) {
                        int var7 = var6x.ordinal();
                        if (var7 < var0) {
                            return Optional.empty();
                        }

                        if (var7 > var0) {
                            ++var2;
                        } else {
                            ++var1;
                        }
                    }
                }
            }
        }

        float var8 = (float)(var2 + 1) / (float)(var2 + var1 + 1);
        float var9 = var8 * var8 * this.getChanceModifier();
        return param3.nextFloat() < var9 ? this.getNext(param0) : Optional.empty();
    }
}
