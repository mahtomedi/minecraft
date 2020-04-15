package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;

public class BasaltColumnsFeature extends Feature<ColumnFeatureConfiguration> {
    public BasaltColumnsFeature(Function<Dynamic<?>, ? extends ColumnFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0,
        StructureFeatureManager param1,
        ChunkGenerator<? extends ChunkGeneratorSettings> param2,
        Random param3,
        BlockPos param4,
        ColumnFeatureConfiguration param5
    ) {
        int var0 = param2.getSeaLevel();
        BlockPos var1 = findSurface(param0, var0, param4.mutable().clamp(Direction.Axis.Y, 1, param0.getMaxBuildHeight() - 1), Integer.MAX_VALUE);
        if (var1 == null) {
            return false;
        } else {
            int var2 = calculateHeight(param3, param5);
            boolean var3 = param3.nextFloat() < 0.9F;
            int var4 = Math.min(var2, var3 ? 5 : 8);
            int var5 = var3 ? 50 : 15;
            boolean var6 = false;

            for(BlockPos var7 : BlockPos.randomBetweenClosed(
                param3, var5, var1.getX() - var4, var1.getY(), var1.getZ() - var4, var1.getX() + var4, var1.getY(), var1.getZ() + var4
            )) {
                int var8 = var2 - var7.distManhattan(var1);
                if (var8 >= 0) {
                    var6 |= this.placeColumn(param0, var0, var7, var8, calculateReach(param3, param5));
                }
            }

            return var6;
        }
    }

    private boolean placeColumn(LevelAccessor param0, int param1, BlockPos param2, int param3, int param4) {
        boolean var0 = false;

        for(BlockPos var1 : BlockPos.betweenClosed(
            param2.getX() - param4, param2.getY(), param2.getZ() - param4, param2.getX() + param4, param2.getY(), param2.getZ() + param4
        )) {
            int var2 = var1.distManhattan(param2);
            BlockPos var3 = isAirOrLavaOcean(param0, param1, var1) ? findSurface(param0, param1, var1.mutable(), var2) : findAir(param0, var1.mutable(), var2);
            if (var3 != null) {
                int var4 = param3 - var2 / 2;

                for(BlockPos.MutableBlockPos var5 = var3.mutable(); var4 >= 0; --var4) {
                    if (isAirOrLavaOcean(param0, param1, var5)) {
                        this.setBlock(param0, var5, Blocks.BASALT.defaultBlockState());
                        var5.move(Direction.UP);
                        var0 = true;
                    } else {
                        if (param0.getBlockState(var5).getBlock() != Blocks.BASALT) {
                            break;
                        }

                        var5.move(Direction.UP);
                    }
                }
            }
        }

        return var0;
    }

    @Nullable
    private static BlockPos findSurface(LevelAccessor param0, int param1, BlockPos.MutableBlockPos param2, int param3) {
        for(; param2.getY() > 1 && param3 > 0; param2.move(Direction.DOWN)) {
            --param3;
            if (isAirOrLavaOcean(param0, param1, param2)) {
                BlockState var0 = param0.getBlockState(param2.move(Direction.DOWN));
                param2.move(Direction.UP);
                Block var1 = var0.getBlock();
                if (var1 != Blocks.LAVA && var1 != Blocks.BEDROCK && var1 != Blocks.MAGMA_BLOCK && !var0.isAir()) {
                    return param2;
                }
            }
        }

        return null;
    }

    @Nullable
    private static BlockPos findAir(LevelAccessor param0, BlockPos.MutableBlockPos param1, int param2) {
        while(param1.getY() < param0.getMaxBuildHeight() && param2 > 0) {
            --param2;
            if (param0.getBlockState(param1).isAir()) {
                return param1;
            }

            param1.move(Direction.UP);
        }

        return null;
    }

    private static int calculateHeight(Random param0, ColumnFeatureConfiguration param1) {
        return param1.minimumHeight + param0.nextInt(param1.maximumHeight - param1.minimumHeight + 1);
    }

    private static int calculateReach(Random param0, ColumnFeatureConfiguration param1) {
        return param1.minimumReach + param0.nextInt(param1.maximumReach - param1.minimumReach + 1);
    }

    private static boolean isAirOrLavaOcean(LevelAccessor param0, int param1, BlockPos param2) {
        BlockState var0 = param0.getBlockState(param2);
        return var0.isAir() || var0.getBlock() == Blocks.LAVA && param2.getY() <= param1;
    }
}
