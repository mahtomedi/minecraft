package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
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

    public BambooFeature(
        Function<Dynamic<?>, ? extends ProbabilityFeatureConfiguration> param0, Function<Random, ? extends ProbabilityFeatureConfiguration> param1
    ) {
        super(param0, param1);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, ProbabilityFeatureConfiguration param4
    ) {
        int var0 = 0;
        BlockPos.MutableBlockPos var1 = param3.mutable();
        BlockPos.MutableBlockPos var2 = param3.mutable();
        if (param0.isEmptyBlock(var1)) {
            if (Blocks.BAMBOO.defaultBlockState().canSurvive(param0, var1)) {
                int var3 = param2.nextInt(12) + 5;
                if (param2.nextFloat() < param4.probability) {
                    int var4 = param2.nextInt(4) + 1;

                    for(int var5 = param3.getX() - var4; var5 <= param3.getX() + var4; ++var5) {
                        for(int var6 = param3.getZ() - var4; var6 <= param3.getZ() + var4; ++var6) {
                            int var7 = var5 - param3.getX();
                            int var8 = var6 - param3.getZ();
                            if (var7 * var7 + var8 * var8 <= var4 * var4) {
                                var2.set(var5, param0.getHeight(Heightmap.Types.WORLD_SURFACE, var5, var6) - 1, var6);
                                if (isDirt(param0.getBlockState(var2).getBlock())) {
                                    param0.setBlock(var2, Blocks.PODZOL.defaultBlockState(), 2);
                                }
                            }
                        }
                    }
                }

                for(int var9 = 0; var9 < var3 && param0.isEmptyBlock(var1); ++var9) {
                    param0.setBlock(var1, BAMBOO_TRUNK, 2);
                    var1.move(Direction.UP, 1);
                }

                if (var1.getY() - param3.getY() >= 3) {
                    param0.setBlock(var1, BAMBOO_FINAL_LARGE, 2);
                    param0.setBlock(var1.move(Direction.DOWN, 1), BAMBOO_TOP_LARGE, 2);
                    param0.setBlock(var1.move(Direction.DOWN, 1), BAMBOO_TOP_SMALL, 2);
                }
            }

            ++var0;
        }

        return var0 > 0;
    }
}
