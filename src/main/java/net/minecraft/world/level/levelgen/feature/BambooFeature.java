package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class BambooFeature extends Feature<ProbabilityFeatureConfiguration> {
    private static final BlockState BAMBOO_TRUNK = Blocks.BAMBOO
        .defaultBlockState()
        .setValue(BambooBlock.AGE, Integer.valueOf(1))
        .setValue(BambooBlock.LEAVES, BambooLeaves.NONE)
        .setValue(BambooBlock.STAGE, Integer.valueOf(0));
    private static final BlockState BAMBOO_FINAL_LARGE = BAMBOO_TRUNK.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE)
        .setValue(BambooBlock.STAGE, Integer.valueOf(1));
    private static final BlockState BAMBOO_TOP_LARGE = BAMBOO_TRUNK.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE);
    private static final BlockState BAMBOO_TOP_SMALL = BAMBOO_TRUNK.setValue(BambooBlock.LEAVES, BambooLeaves.SMALL);

    public BambooFeature(Codec<ProbabilityFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> param0) {
        int var0 = 0;
        BlockPos var1 = param0.origin();
        WorldGenLevel var2 = param0.level();
        RandomSource var3 = param0.random();
        ProbabilityFeatureConfiguration var4 = param0.config();
        BlockPos.MutableBlockPos var5 = var1.mutable();
        BlockPos.MutableBlockPos var6 = var1.mutable();
        if (var2.isEmptyBlock(var5)) {
            if (Blocks.BAMBOO.defaultBlockState().canSurvive(var2, var5)) {
                int var7 = var3.nextInt(12) + 5;
                if (var3.nextFloat() < var4.probability) {
                    int var8 = var3.nextInt(4) + 1;

                    for(int var9 = var1.getX() - var8; var9 <= var1.getX() + var8; ++var9) {
                        for(int var10 = var1.getZ() - var8; var10 <= var1.getZ() + var8; ++var10) {
                            int var11 = var9 - var1.getX();
                            int var12 = var10 - var1.getZ();
                            if (var11 * var11 + var12 * var12 <= var8 * var8) {
                                var6.set(var9, var2.getHeight(Heightmap.Types.WORLD_SURFACE, var9, var10) - 1, var10);
                                if (isDirt(var2.getBlockState(var6))) {
                                    var2.setBlock(var6, Blocks.PODZOL.defaultBlockState(), 2);
                                }
                            }
                        }
                    }
                }

                for(int var13 = 0; var13 < var7 && var2.isEmptyBlock(var5); ++var13) {
                    var2.setBlock(var5, BAMBOO_TRUNK, 2);
                    var5.move(Direction.UP, 1);
                }

                if (var5.getY() - var1.getY() >= 3) {
                    var2.setBlock(var5, BAMBOO_FINAL_LARGE, 2);
                    var2.setBlock(var5.move(Direction.DOWN, 1), BAMBOO_TOP_LARGE, 2);
                    var2.setBlock(var5.move(Direction.DOWN, 1), BAMBOO_TOP_SMALL, 2);
                }
            }

            ++var0;
        }

        return var0 > 0;
    }
}
