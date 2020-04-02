package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class TwistingVinesFeature extends Feature<NoneFeatureConfiguration> {
    public TwistingVinesFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
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
        return place(param0, param3, param4, 8, 4, 8);
    }

    public static boolean place(LevelAccessor param0, Random param1, BlockPos param2, int param3, int param4, int param5) {
        if (isInvalidPlacementLocation(param0, param2)) {
            return false;
        } else {
            placeTwistingVines(param0, param1, param2, param3, param4, param5);
            return true;
        }
    }

    private static void placeTwistingVines(LevelAccessor param0, Random param1, BlockPos param2, int param3, int param4, int param5) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(int var1 = 0; var1 < param3 * param3; ++var1) {
            var0.set(param2).move(Mth.nextInt(param1, -param3, param3), Mth.nextInt(param1, -param4, param4), Mth.nextInt(param1, -param3, param3));

            while(param0.getBlockState(var0.below()).isAir()) {
                var0.move(0, -1, 0);
            }

            if (!isInvalidPlacementLocation(param0, var0)) {
                int var2 = Mth.nextInt(param1, 1, param5);
                if (param1.nextInt(6) == 0) {
                    var2 *= 2;
                }

                if (param1.nextInt(5) == 0) {
                    var2 = 1;
                }

                int var3 = 17;
                int var4 = 25;
                placeWeepingVinesColumn(param0, param1, var0, var2, 17, 25);
            }
        }

    }

    public static void placeWeepingVinesColumn(LevelAccessor param0, Random param1, BlockPos.MutableBlockPos param2, int param3, int param4, int param5) {
        for(int var0 = 1; var0 <= param3; ++var0) {
            if (param0.isEmptyBlock(param2)) {
                if (var0 == param3 || !param0.isEmptyBlock(param2.above())) {
                    param0.setBlock(
                        param2,
                        Blocks.TWISTING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, Integer.valueOf(Mth.nextInt(param1, param4, param5))),
                        2
                    );
                    break;
                }

                param0.setBlock(param2, Blocks.TWISTING_VINES_PLANT.defaultBlockState(), 2);
            }

            param2.move(Direction.UP);
        }

    }

    private static boolean isInvalidPlacementLocation(LevelAccessor param0, BlockPos param1) {
        if (!param0.isEmptyBlock(param1)) {
            return true;
        } else {
            Block var0 = param0.getBlockState(param1.below()).getBlock();
            return var0 != Blocks.NETHERRACK && var0 != Blocks.WARPED_NYLIUM && var0 != Blocks.WARPED_WART_BLOCK;
        }
    }
}
