package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public abstract class AbstractHugeMushroomFeature extends Feature<HugeMushroomFeatureConfiguration> {
    public AbstractHugeMushroomFeature(Function<Dynamic<?>, ? extends HugeMushroomFeatureConfiguration> param0) {
        super(param0);
    }

    protected void placeTrunk(
        LevelAccessor param0, Random param1, BlockPos param2, HugeMushroomFeatureConfiguration param3, int param4, BlockPos.MutableBlockPos param5
    ) {
        for(int var0 = 0; var0 < param4; ++var0) {
            param5.set(param2).move(Direction.UP, var0);
            if (!param0.getBlockState(param5).isSolidRender(param0, param5)) {
                this.setBlock(param0, param5, param3.stemProvider.getState(param1, param2));
            }
        }

    }

    protected int getTreeHeight(Random param0) {
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
        if (var0 >= 1 && var0 + param2 + 1 < 256) {
            Block var1 = param0.getBlockState(param1.below()).getBlock();
            if (!isDirt(var1)) {
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

    public boolean place(
        LevelAccessor param0,
        StructureFeatureManager param1,
        ChunkGenerator<? extends ChunkGeneratorSettings> param2,
        Random param3,
        BlockPos param4,
        HugeMushroomFeatureConfiguration param5
    ) {
        int var0 = this.getTreeHeight(param3);
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
        if (!this.isValidPosition(param0, param4, var0, var1, param5)) {
            return false;
        } else {
            this.makeCap(param0, param3, param4, var0, var1, param5);
            this.placeTrunk(param0, param3, param4, param5, var0, var1);
            return true;
        }
    }

    protected abstract int getTreeRadiusForHeight(int var1, int var2, int var3, int var4);

    protected abstract void makeCap(
        LevelAccessor var1, Random var2, BlockPos var3, int var4, BlockPos.MutableBlockPos var5, HugeMushroomFeatureConfiguration var6
    );
}
