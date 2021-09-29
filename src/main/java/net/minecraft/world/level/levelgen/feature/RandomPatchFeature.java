package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

public class RandomPatchFeature extends Feature<RandomPatchConfiguration> {
    public RandomPatchFeature(Codec<RandomPatchConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<RandomPatchConfiguration> param0) {
        RandomPatchConfiguration var0 = param0.config();
        Random var1 = param0.random();
        BlockPos var2 = param0.origin();
        WorldGenLevel var3 = param0.level();
        int var4 = 0;
        BlockPos.MutableBlockPos var5 = new BlockPos.MutableBlockPos();
        int var6 = var0.xzSpread() + 1;
        int var7 = var0.ySpread() + 1;

        for(int var8 = 0; var8 < var0.tries(); ++var8) {
            var5.setWithOffset(var2, var1.nextInt(var6) - var1.nextInt(var6), var1.nextInt(var7) - var1.nextInt(var7), var1.nextInt(var6) - var1.nextInt(var6));
            if (isValid(var3, var5, var0) && var0.feature().get().place(var3, param0.chunkGenerator(), var1, var5)) {
                ++var4;
            }
        }

        return var4 > 0;
    }

    public static boolean isValid(LevelAccessor param0, BlockPos param1, RandomPatchConfiguration param2) {
        BlockState var0 = param0.getBlockState(param1.below());
        return (!param2.onlyInAir() || param0.isEmptyBlock(param1))
            && (param2.allowedOn().isEmpty() || param2.allowedOn().contains(var0.getBlock()))
            && !param2.disallowedOn().contains(var0);
    }
}
