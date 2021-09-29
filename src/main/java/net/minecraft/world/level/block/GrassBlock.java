package net.minecraft.world.level.block;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.Features;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

public class GrassBlock extends SpreadingSnowyDirtBlock implements BonemealableBlock {
    public GrassBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter param0, BlockPos param1, BlockState param2, boolean param3) {
        return param0.getBlockState(param1.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level param0, Random param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, Random param1, BlockPos param2, BlockState param3) {
        BlockPos var0 = param2.above();
        BlockState var1 = Blocks.GRASS.defaultBlockState();

        label46:
        for(int var2 = 0; var2 < 128; ++var2) {
            BlockPos var3 = var0;

            for(int var4 = 0; var4 < var2 / 16; ++var4) {
                var3 = var3.offset(param1.nextInt(3) - 1, (param1.nextInt(3) - 1) * param1.nextInt(3) / 2, param1.nextInt(3) - 1);
                if (!param0.getBlockState(var3.below()).is(this) || param0.getBlockState(var3).isCollisionShapeFullBlock(param0, var3)) {
                    continue label46;
                }
            }

            BlockState var5 = param0.getBlockState(var3);
            if (var5.is(var1.getBlock()) && param1.nextInt(10) == 0) {
                ((BonemealableBlock)var1.getBlock()).performBonemeal(param0, param1, var3, var5);
            }

            if (var5.isAir()) {
                ConfiguredFeature<?, ?> var7;
                if (param1.nextInt(8) == 0) {
                    List<ConfiguredFeature<?, ?>> var6 = param0.getBiome(var3).getGenerationSettings().getFlowerFeatures();
                    if (var6.isEmpty()) {
                        continue;
                    }

                    var7 = ((RandomPatchConfiguration)var6.get(0).config()).feature().get();
                } else {
                    var7 = Features.GRASS_BONEMEAL;
                }

                var7.place(param0, param0.getChunkSource().getGenerator(), param1, var3);
            }
        }

    }
}
