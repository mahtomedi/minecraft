package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;

public class WaterloggedVegetationPatchFeature extends VegetationPatchFeature {
    public WaterloggedVegetationPatchFeature(Codec<VegetationPatchConfiguration> param0) {
        super(param0);
    }

    @Override
    protected Set<BlockPos> placeGroundPatch(
        WorldGenLevel param0, VegetationPatchConfiguration param1, Random param2, BlockPos param3, Predicate<BlockState> param4, int param5, int param6
    ) {
        Set<BlockPos> var0 = super.placeGroundPatch(param0, param1, param2, param3, param4, param5, param6);
        Set<BlockPos> var1 = new HashSet<>();
        BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();

        for(BlockPos var3 : var0) {
            if (!isExposed(param0, var0, var3, var2)) {
                var1.add(var3);
            }
        }

        for(BlockPos var4 : var1) {
            param0.setBlock(var4, Blocks.WATER.defaultBlockState(), 2);
        }

        return var1;
    }

    private static boolean isExposed(WorldGenLevel param0, Set<BlockPos> param1, BlockPos param2, BlockPos.MutableBlockPos param3) {
        return isExposedDirection(param0, param2, param3, Direction.NORTH)
            || isExposedDirection(param0, param2, param3, Direction.EAST)
            || isExposedDirection(param0, param2, param3, Direction.SOUTH)
            || isExposedDirection(param0, param2, param3, Direction.WEST);
    }

    private static boolean isExposedDirection(WorldGenLevel param0, BlockPos param1, BlockPos.MutableBlockPos param2, Direction param3) {
        param2.setWithOffset(param1, param3);
        return !param0.getBlockState(param2).isFaceSturdy(param0, param2, param3.getOpposite());
    }

    @Override
    protected boolean placeVegetation(WorldGenLevel param0, VegetationPatchConfiguration param1, ChunkGenerator param2, Random param3, BlockPos param4) {
        if (super.placeVegetation(param0, param1, param2, param3, param4.below())) {
            BlockState var0 = param0.getBlockState(param4);
            if (var0.hasProperty(BlockStateProperties.WATERLOGGED) && !var0.getValue(BlockStateProperties.WATERLOGGED)) {
                param0.setBlock(param4, var0.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)), 2);
            }

            return true;
        } else {
            return false;
        }
    }
}
