package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class WeepingVinesFeature extends Feature<NoneFeatureConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public WeepingVinesFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        RandomSource var2 = param0.random();
        if (!var0.isEmptyBlock(var1)) {
            return false;
        } else {
            BlockState var3 = var0.getBlockState(var1.above());
            if (!var3.is(Blocks.NETHERRACK) && !var3.is(Blocks.NETHER_WART_BLOCK)) {
                return false;
            } else {
                this.placeRoofNetherWart(var0, var2, var1);
                this.placeRoofWeepingVines(var0, var2, var1);
                return true;
            }
        }
    }

    private void placeRoofNetherWart(LevelAccessor param0, RandomSource param1, BlockPos param2) {
        param0.setBlock(param2, Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

        for(int var2 = 0; var2 < 200; ++var2) {
            var0.setWithOffset(param2, param1.nextInt(6) - param1.nextInt(6), param1.nextInt(2) - param1.nextInt(5), param1.nextInt(6) - param1.nextInt(6));
            if (param0.isEmptyBlock(var0)) {
                int var3 = 0;

                for(Direction var4 : DIRECTIONS) {
                    BlockState var5 = param0.getBlockState(var1.setWithOffset(var0, var4));
                    if (var5.is(Blocks.NETHERRACK) || var5.is(Blocks.NETHER_WART_BLOCK)) {
                        ++var3;
                    }

                    if (var3 > 1) {
                        break;
                    }
                }

                if (var3 == 1) {
                    param0.setBlock(var0, Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
                }
            }
        }

    }

    private void placeRoofWeepingVines(LevelAccessor param0, RandomSource param1, BlockPos param2) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(int var1 = 0; var1 < 100; ++var1) {
            var0.setWithOffset(param2, param1.nextInt(8) - param1.nextInt(8), param1.nextInt(2) - param1.nextInt(7), param1.nextInt(8) - param1.nextInt(8));
            if (param0.isEmptyBlock(var0)) {
                BlockState var2 = param0.getBlockState(var0.above());
                if (var2.is(Blocks.NETHERRACK) || var2.is(Blocks.NETHER_WART_BLOCK)) {
                    int var3 = Mth.nextInt(param1, 1, 8);
                    if (param1.nextInt(6) == 0) {
                        var3 *= 2;
                    }

                    if (param1.nextInt(5) == 0) {
                        var3 = 1;
                    }

                    int var4 = 17;
                    int var5 = 25;
                    placeWeepingVinesColumn(param0, param1, var0, var3, 17, 25);
                }
            }
        }

    }

    public static void placeWeepingVinesColumn(LevelAccessor param0, RandomSource param1, BlockPos.MutableBlockPos param2, int param3, int param4, int param5) {
        for(int var0 = 0; var0 <= param3; ++var0) {
            if (param0.isEmptyBlock(param2)) {
                if (var0 == param3 || !param0.isEmptyBlock(param2.below())) {
                    param0.setBlock(
                        param2,
                        Blocks.WEEPING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, Integer.valueOf(Mth.nextInt(param1, param4, param5))),
                        2
                    );
                    break;
                }

                param0.setBlock(param2, Blocks.WEEPING_VINES_PLANT.defaultBlockState(), 2);
            }

            param2.move(Direction.DOWN);
        }

    }
}
