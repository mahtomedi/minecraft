package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallSeagrass;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.SeagrassFeatureConfiguration;

public class SeagrassFeature extends Feature<SeagrassFeatureConfiguration> {
    public SeagrassFeature(Function<Dynamic<?>, ? extends SeagrassFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, SeagrassFeatureConfiguration param4
    ) {
        int var0 = 0;

        for(int var1 = 0; var1 < param4.count; ++var1) {
            int var2 = param2.nextInt(8) - param2.nextInt(8);
            int var3 = param2.nextInt(8) - param2.nextInt(8);
            int var4 = param0.getHeight(Heightmap.Types.OCEAN_FLOOR, param3.getX() + var2, param3.getZ() + var3);
            BlockPos var5 = new BlockPos(param3.getX() + var2, var4, param3.getZ() + var3);
            if (param0.getBlockState(var5).getBlock() == Blocks.WATER) {
                boolean var6 = param2.nextDouble() < param4.tallSeagrassProbability;
                BlockState var7 = var6 ? Blocks.TALL_SEAGRASS.defaultBlockState() : Blocks.SEAGRASS.defaultBlockState();
                if (var7.canSurvive(param0, var5)) {
                    if (var6) {
                        BlockState var8 = var7.setValue(TallSeagrass.HALF, DoubleBlockHalf.UPPER);
                        BlockPos var9 = var5.above();
                        if (param0.getBlockState(var9).getBlock() == Blocks.WATER) {
                            param0.setBlock(var5, var7, 2);
                            param0.setBlock(var9, var8, 2);
                        }
                    } else {
                        param0.setBlock(var5, var7, 2);
                    }

                    ++var0;
                }
            }
        }

        return var0 > 0;
    }
}
