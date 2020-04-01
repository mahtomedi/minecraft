package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class BasaltPillarFeature extends Feature<NoneFeatureConfiguration> {
    public BasaltPillarFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, Function<Random, ? extends NoneFeatureConfiguration> param1) {
        super(param0, param1);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        if (param0.isEmptyBlock(param3) && !param0.isEmptyBlock(param3.above())) {
            BlockPos.MutableBlockPos var0 = param3.mutable();
            BlockPos.MutableBlockPos var1 = param3.mutable();
            boolean var2 = true;
            boolean var3 = true;
            boolean var4 = true;
            boolean var5 = true;

            while(param0.isEmptyBlock(var0)) {
                if (Level.isOutsideBuildHeight(var0)) {
                    return true;
                }

                param0.setBlock(var0, Blocks.BASALT.defaultBlockState(), 2);
                var2 = var2 && this.placeHangOff(param0, param2, var1.setWithOffset(var0, Direction.NORTH));
                var3 = var3 && this.placeHangOff(param0, param2, var1.setWithOffset(var0, Direction.SOUTH));
                var4 = var4 && this.placeHangOff(param0, param2, var1.setWithOffset(var0, Direction.WEST));
                var5 = var5 && this.placeHangOff(param0, param2, var1.setWithOffset(var0, Direction.EAST));
                var0.move(Direction.DOWN);
            }

            var0.move(Direction.UP);
            this.placeBaseHangOff(param0, param2, var1.setWithOffset(var0, Direction.NORTH));
            this.placeBaseHangOff(param0, param2, var1.setWithOffset(var0, Direction.SOUTH));
            this.placeBaseHangOff(param0, param2, var1.setWithOffset(var0, Direction.WEST));
            this.placeBaseHangOff(param0, param2, var1.setWithOffset(var0, Direction.EAST));
            BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos();

            for(int var7 = -3; var7 < 4; ++var7) {
                for(int var8 = -3; var8 < 4; ++var8) {
                    int var9 = Mth.abs(var7) * Mth.abs(var8);
                    if (param2.nextInt(10) < 10 - var9) {
                        var6.set(var0.offset(var7, 0, var8));
                        int var10 = 3;

                        while(param0.isEmptyBlock(var1.setWithOffset(var6, Direction.DOWN))) {
                            var6.move(Direction.DOWN);
                            if (--var10 <= 0) {
                                break;
                            }
                        }

                        if (!param0.isEmptyBlock(var1.setWithOffset(var6, Direction.DOWN))) {
                            param0.setBlock(var6, Blocks.BASALT.defaultBlockState(), 2);
                        }
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private void placeBaseHangOff(LevelAccessor param0, Random param1, BlockPos param2) {
        if (param1.nextBoolean()) {
            param0.setBlock(param2, Blocks.BASALT.defaultBlockState(), 2);
        }

    }

    private boolean placeHangOff(LevelAccessor param0, Random param1, BlockPos param2) {
        if (param1.nextInt(10) != 0) {
            param0.setBlock(param2, Blocks.BASALT.defaultBlockState(), 2);
            return true;
        } else {
            return false;
        }
    }
}
