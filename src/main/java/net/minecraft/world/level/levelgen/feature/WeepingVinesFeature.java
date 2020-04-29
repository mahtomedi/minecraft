package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class WeepingVinesFeature extends Feature<NoneFeatureConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public WeepingVinesFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0,
        StructureFeatureManager param1,
        ChunkGenerator<? extends ChunkGeneratorSettings> param2,
        Random param3,
        BlockPos param4,
        NoneFeatureConfiguration param5
    ) {
        if (!param0.isEmptyBlock(param4)) {
            return false;
        } else {
            BlockState var0 = param0.getBlockState(param4.above());
            if (!var0.is(Blocks.NETHERRACK) && !var0.is(Blocks.NETHER_WART_BLOCK)) {
                return false;
            } else {
                this.placeRoofNetherWart(param0, param3, param4);
                this.placeRoofWeepingVines(param0, param3, param4);
                return true;
            }
        }
    }

    private void placeRoofNetherWart(LevelAccessor param0, Random param1, BlockPos param2) {
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

    private void placeRoofWeepingVines(LevelAccessor param0, Random param1, BlockPos param2) {
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

    public static void placeWeepingVinesColumn(LevelAccessor param0, Random param1, BlockPos.MutableBlockPos param2, int param3, int param4, int param5) {
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
