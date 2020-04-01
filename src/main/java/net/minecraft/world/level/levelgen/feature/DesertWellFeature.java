package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class DesertWellFeature extends Feature<NoneFeatureConfiguration> {
    private static final BlockStatePredicate IS_SAND = BlockStatePredicate.forBlock(Blocks.SAND);
    private final BlockState sandSlab = Blocks.SANDSTONE_SLAB.defaultBlockState();
    private final BlockState sandstone = Blocks.SANDSTONE.defaultBlockState();
    private final BlockState water = Blocks.WATER.defaultBlockState();

    public DesertWellFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, Function<Random, ? extends NoneFeatureConfiguration> param1) {
        super(param0, param1);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        param3 = param3.above();

        while(param0.isEmptyBlock(param3) && param3.getY() > 2) {
            param3 = param3.below();
        }

        if (!IS_SAND.test(param0.getBlockState(param3))) {
            return false;
        } else {
            for(int var0 = -2; var0 <= 2; ++var0) {
                for(int var1 = -2; var1 <= 2; ++var1) {
                    if (param0.isEmptyBlock(param3.offset(var0, -1, var1)) && param0.isEmptyBlock(param3.offset(var0, -2, var1))) {
                        return false;
                    }
                }
            }

            for(int var2 = -1; var2 <= 0; ++var2) {
                for(int var3 = -2; var3 <= 2; ++var3) {
                    for(int var4 = -2; var4 <= 2; ++var4) {
                        param0.setBlock(param3.offset(var3, var2, var4), this.sandstone, 2);
                    }
                }
            }

            param0.setBlock(param3, this.water, 2);

            for(Direction var5 : Direction.Plane.HORIZONTAL) {
                param0.setBlock(param3.relative(var5), this.water, 2);
            }

            for(int var6 = -2; var6 <= 2; ++var6) {
                for(int var7 = -2; var7 <= 2; ++var7) {
                    if (var6 == -2 || var6 == 2 || var7 == -2 || var7 == 2) {
                        param0.setBlock(param3.offset(var6, 1, var7), this.sandstone, 2);
                    }
                }
            }

            param0.setBlock(param3.offset(2, 1, 0), this.sandSlab, 2);
            param0.setBlock(param3.offset(-2, 1, 0), this.sandSlab, 2);
            param0.setBlock(param3.offset(0, 1, 2), this.sandSlab, 2);
            param0.setBlock(param3.offset(0, 1, -2), this.sandSlab, 2);

            for(int var8 = -1; var8 <= 1; ++var8) {
                for(int var9 = -1; var9 <= 1; ++var9) {
                    if (var8 == 0 && var9 == 0) {
                        param0.setBlock(param3.offset(var8, 4, var9), this.sandstone, 2);
                    } else {
                        param0.setBlock(param3.offset(var8, 4, var9), this.sandSlab, 2);
                    }
                }
            }

            for(int var10 = 1; var10 <= 3; ++var10) {
                param0.setBlock(param3.offset(-1, var10, -1), this.sandstone, 2);
                param0.setBlock(param3.offset(-1, var10, 1), this.sandstone, 2);
                param0.setBlock(param3.offset(1, var10, -1), this.sandstone, 2);
                param0.setBlock(param3.offset(1, var10, 1), this.sandstone, 2);
            }

            return true;
        }
    }
}
