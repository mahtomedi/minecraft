package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public abstract class AbstractHugeMushroomFeature extends Feature<HugeMushroomFeatureConfiguration> {
    public AbstractHugeMushroomFeature(Codec<HugeMushroomFeatureConfiguration> param0) {
        super(param0);
    }

    protected void placeTrunk(
        LevelAccessor param0, RandomSource param1, BlockPos param2, HugeMushroomFeatureConfiguration param3, int param4, BlockPos.MutableBlockPos param5
    ) {
        for(int var0 = 0; var0 < param4; ++var0) {
            param5.set(param2).move(Direction.UP, var0);
            if (!param0.getBlockState(param5).isSolidRender(param0, param5)) {
                this.setBlock(param0, param5, param3.stemProvider.getState(param1, param2));
            }
        }

    }

    protected int getTreeHeight(RandomSource param0) {
        int var0 = param0.nextInt(3) + 4;
        if (param0.nextInt(12) == 0) {
            var0 *= 2;
        }

        return var0;
    }

    protected boolean isValidPosition(
        LevelAccessor param0, BlockPos param1, int param2, BlockPos.MutableBlockPos param3, HugeMushroomFeatureConfiguration param4
    ) {
        int var0 = param1.getY();
        if (var0 >= param0.getMinBuildHeight() + 1 && var0 + param2 + 1 < param0.getMaxBuildHeight()) {
            BlockState var1 = param0.getBlockState(param1.below());
            if (!isDirt(var1) && !var1.is(BlockTags.MUSHROOM_GROW_BLOCK)) {
                return false;
            } else {
                for(int var2 = 0; var2 <= param2; ++var2) {
                    int var3 = this.getTreeRadiusForHeight(-1, -1, param4.foliageRadius, var2);

                    for(int var4 = -var3; var4 <= var3; ++var4) {
                        for(int var5 = -var3; var5 <= var3; ++var5) {
                            BlockState var6 = param0.getBlockState(param3.setWithOffset(param1, var4, var2, var5));
                            if (!var6.isAir() && !var6.is(BlockTags.LEAVES)) {
                                return false;
                            }
                        }
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean place(FeaturePlaceContext<HugeMushroomFeatureConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        RandomSource var2 = param0.random();
        HugeMushroomFeatureConfiguration var3 = param0.config();
        int var4 = this.getTreeHeight(var2);
        BlockPos.MutableBlockPos var5 = new BlockPos.MutableBlockPos();
        if (!this.isValidPosition(var0, var1, var4, var5, var3)) {
            return false;
        } else {
            this.makeCap(var0, var2, var1, var4, var5, var3);
            this.placeTrunk(var0, var2, var1, var3, var4, var5);
            return true;
        }
    }

    protected abstract int getTreeRadiusForHeight(int var1, int var2, int var3, int var4);

    protected abstract void makeCap(
        LevelAccessor var1, RandomSource var2, BlockPos var3, int var4, BlockPos.MutableBlockPos var5, HugeMushroomFeatureConfiguration var6
    );
}
