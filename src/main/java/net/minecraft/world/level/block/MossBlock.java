package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MossBlock extends Block implements BonemealableBlock {
    public MossBlock(BlockBehaviour.Properties param0) {
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
        place(param0, param1, param2.above());
    }

    public static boolean place(WorldGenLevel param0, Random param1, BlockPos param2) {
        if (!param0.getBlockState(param2).isAir()) {
            return false;
        } else {
            int var0 = 0;
            int var1 = Mth.randomBetweenInclusive(param1, 1, 3);
            int var2 = Mth.randomBetweenInclusive(param1, 1, 3);

            for(int var3 = -var1; var3 <= var1; ++var3) {
                for(int var4 = -var2; var4 <= var2; ++var4) {
                    BlockPos var5 = param2.offset(var3, 0, var4);
                    var0 += placeFeature(param0, param1, var5);
                }
            }

            return var0 > 0;
        }
    }

    private static int placeFeature(WorldGenLevel param0, Random param1, BlockPos param2) {
        int var0 = 0;
        BlockPos var1 = param2.below();
        BlockState var2 = param0.getBlockState(var1);
        if (param0.isEmptyBlock(param2) && var2.isFaceSturdy(param0, var1, Direction.UP)) {
            createMossPatch(param0, param1, param2.below());
            if (param1.nextFloat() < 0.8F) {
                BlockState var3 = getVegetationBlockState(param1);
                if (var3.canSurvive(param0, param2)) {
                    if (var3.getBlock() instanceof DoublePlantBlock && param0.isEmptyBlock(param2.above())) {
                        DoublePlantBlock var4 = (DoublePlantBlock)var3.getBlock();
                        var4.placeAt(param0, param2, 2);
                        ++var0;
                    } else {
                        param0.setBlock(param2, var3, 2);
                        ++var0;
                    }
                }
            }
        }

        return var0;
    }

    private static void createMossPatch(WorldGenLevel param0, Random param1, BlockPos param2) {
        if (param0.getBlockState(param2).is(BlockTags.LUSH_PLANTS_REPLACEABLE)) {
            param0.setBlock(param2, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
        }

    }

    private static BlockState getVegetationBlockState(Random param0) {
        int var0 = param0.nextInt(100) + 1;
        if (var0 < 5) {
            return Blocks.FLOWERING_AZALEA.defaultBlockState();
        } else if (var0 < 15) {
            return Blocks.AZALEA.defaultBlockState();
        } else if (var0 < 40) {
            return Blocks.MOSS_CARPET.defaultBlockState();
        } else {
            return var0 < 90 ? Blocks.GRASS.defaultBlockState() : Blocks.TALL_GRASS.defaultBlockState();
        }
    }
}
