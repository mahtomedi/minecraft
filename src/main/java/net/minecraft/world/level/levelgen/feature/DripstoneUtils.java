package net.minecraft.world.level.levelgen.feature;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;

public class DripstoneUtils {
    protected static double getDripstoneHeight(double param0, double param1, double param2, double param3) {
        if (param0 < param3) {
            param0 = param3;
        }

        double var0 = 0.384;
        double var1 = param0 / param1 * 0.384;
        double var2 = 0.75 * Math.pow(var1, 1.3333333333333333);
        double var3 = Math.pow(var1, 0.6666666666666666);
        double var4 = 0.3333333333333333 * Math.log(var1);
        double var5 = param2 * (var2 - var3 - var4);
        var5 = Math.max(var5, 0.0);
        return var5 / 0.384 * param1;
    }

    protected static boolean isCircleMostlyEmbeddedInStone(WorldGenLevel param0, BlockPos param1, int param2) {
        if (isEmptyOrWater(param0, param1)) {
            return false;
        } else {
            float var0 = 6.0F;
            float var1 = 6.0F / (float)param2;

            for(float var2 = 0.0F; var2 < (float) (Math.PI * 2); var2 += var1) {
                int var3 = (int)(Mth.cos(var2) * (float)param2);
                int var4 = (int)(Mth.sin(var2) * (float)param2);
                if (isEmptyOrWater(param0, param1.offset(var3, 0, var4))) {
                    return false;
                }
            }

            return true;
        }
    }

    protected static boolean isEmptyOrWater(LevelAccessor param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, DripstoneUtils::isEmptyOrWater);
    }

    protected static void buildBaseToTipColumn(Direction param0, int param1, boolean param2, Consumer<BlockState> param3) {
        if (param1 >= 3) {
            param3.accept(createPointedDripstone(param0, DripstoneThickness.BASE));

            for(int var0 = 0; var0 < param1 - 3; ++var0) {
                param3.accept(createPointedDripstone(param0, DripstoneThickness.MIDDLE));
            }
        }

        if (param1 >= 2) {
            param3.accept(createPointedDripstone(param0, DripstoneThickness.FRUSTUM));
        }

        if (param1 >= 1) {
            param3.accept(createPointedDripstone(param0, param2 ? DripstoneThickness.TIP_MERGE : DripstoneThickness.TIP));
        }

    }

    protected static void growPointedDripstone(WorldGenLevel param0, BlockPos param1, Direction param2, int param3, boolean param4) {
        BlockPos.MutableBlockPos var0 = param1.mutable();
        buildBaseToTipColumn(param2, param3, param4, param3x -> {
            if (param3x.is(Blocks.POINTED_DRIPSTONE)) {
                param3x = param3x.setValue(PointedDripstoneBlock.WATERLOGGED, Boolean.valueOf(param0.isWaterAt(var0)));
            }

            param0.setBlock(var0, param3x, 2);
            var0.move(param2);
        });
    }

    protected static boolean placeDripstoneBlockIfPossible(WorldGenLevel param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        if (var0.is(BlockTags.DRIPSTONE_REPLACEABLE)) {
            param0.setBlock(param1, Blocks.DRIPSTONE_BLOCK.defaultBlockState(), 2);
            return true;
        } else {
            return false;
        }
    }

    private static BlockState createPointedDripstone(Direction param0, DripstoneThickness param1) {
        return Blocks.POINTED_DRIPSTONE
            .defaultBlockState()
            .setValue(PointedDripstoneBlock.TIP_DIRECTION, param0)
            .setValue(PointedDripstoneBlock.THICKNESS, param1);
    }

    public static boolean isDripstoneBase(BlockState param0) {
        return param0.is(Blocks.DRIPSTONE_BLOCK) || param0.is(BlockTags.DRIPSTONE_REPLACEABLE);
    }

    public static boolean isEmptyOrWater(BlockState param0x) {
        return param0x.isAir() || param0x.is(Blocks.WATER);
    }
}
