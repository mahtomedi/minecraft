package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.NetherForestVegetationFeature;
import net.minecraft.world.level.levelgen.feature.TwistingVinesFeature;
import net.minecraft.world.level.lighting.LayerLightEngine;

public class NyliumBlock extends Block implements BonemealableBlock {
    protected NyliumBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    private static boolean canBeNylium(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.above();
        BlockState var1 = param1.getBlockState(var0);
        int var2 = LayerLightEngine.getLightBlockInto(param1, param0, param2, var1, var0, Direction.UP, var1.getLightBlock(param1, var0));
        return var2 < param1.getMaxLightLevel();
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (!canBeNylium(param0, param1, param2)) {
            param1.setBlockAndUpdate(param2, Blocks.NETHERRACK.defaultBlockState());
        }

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
        Block var0 = param0.getBlockState(param2).getBlock();
        BlockPos var1 = param2.above();
        if (var0 == Blocks.CRIMSON_NYLIUM) {
            NetherForestVegetationFeature.place(param0, param1, var1, BiomeDefaultFeatures.CRIMSON_FOREST_CONFIG, 3, 1);
        } else if (var0 == Blocks.WARPED_NYLIUM) {
            NetherForestVegetationFeature.place(param0, param1, var1, BiomeDefaultFeatures.WARPED_FOREST_CONFIG, 3, 1);
            NetherForestVegetationFeature.place(param0, param1, var1, BiomeDefaultFeatures.NETHER_SPROUTS_CONFIG, 3, 1);
            if (param1.nextInt(8) == 0) {
                TwistingVinesFeature.place(param0, param1, var1, 3, 1, 2);
            }
        }

    }
}
