package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class KelpFeature extends Feature<NoneFeatureConfiguration> {
    public KelpFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, Function<Random, ? extends NoneFeatureConfiguration> param1) {
        super(param0, param1);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        int var0 = 0;
        int var1 = param0.getHeight(Heightmap.Types.OCEAN_FLOOR, param3.getX(), param3.getZ());
        BlockPos var2 = new BlockPos(param3.getX(), var1, param3.getZ());
        if (param0.getBlockState(var2).getBlock() == Blocks.WATER) {
            BlockState var3 = Blocks.KELP.defaultBlockState();
            BlockState var4 = Blocks.KELP_PLANT.defaultBlockState();
            int var5 = 1 + param2.nextInt(10);

            for(int var6 = 0; var6 <= var5; ++var6) {
                if (param0.getBlockState(var2).getBlock() == Blocks.WATER
                    && param0.getBlockState(var2.above()).getBlock() == Blocks.WATER
                    && var4.canSurvive(param0, var2)) {
                    if (var6 == var5) {
                        param0.setBlock(var2, var3.setValue(KelpBlock.AGE, Integer.valueOf(param2.nextInt(4) + 20)), 2);
                        ++var0;
                    } else {
                        param0.setBlock(var2, var4, 2);
                    }
                } else if (var6 > 0) {
                    BlockPos var7 = var2.below();
                    if (var3.canSurvive(param0, var7) && param0.getBlockState(var7.below()).getBlock() != Blocks.KELP) {
                        param0.setBlock(var7, var3.setValue(KelpBlock.AGE, Integer.valueOf(param2.nextInt(4) + 20)), 2);
                        ++var0;
                    }
                    break;
                }

                var2 = var2.above();
            }
        }

        return var0 > 0;
    }
}
