package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;

public class BasaltColumnsFeature extends Feature<ColumnFeatureConfiguration> {
    private static final ImmutableList<Block> CANNOT_PLACE_ON = ImmutableList.of(
        Blocks.LAVA,
        Blocks.BEDROCK,
        Blocks.MAGMA_BLOCK,
        Blocks.SOUL_SAND,
        Blocks.NETHER_BRICKS,
        Blocks.NETHER_BRICK_FENCE,
        Blocks.NETHER_BRICK_STAIRS,
        Blocks.NETHER_WART,
        Blocks.CHEST,
        Blocks.SPAWNER
    );

    public BasaltColumnsFeature(Codec<ColumnFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, ColumnFeatureConfiguration param4) {
        int var0 = param1.getSeaLevel();
        if (!canPlaceAt(param0, var0, param3.mutable())) {
            return false;
        } else {
            int var1 = param4.height().sample(param2);
            boolean var2 = param2.nextFloat() < 0.9F;
            int var3 = Math.min(var1, var2 ? 5 : 8);
            int var4 = var2 ? 50 : 15;
            boolean var5 = false;

            for(BlockPos var6 : BlockPos.randomBetweenClosed(
                param2, var4, param3.getX() - var3, param3.getY(), param3.getZ() - var3, param3.getX() + var3, param3.getY(), param3.getZ() + var3
            )) {
                int var7 = var1 - var6.distManhattan(param3);
                if (var7 >= 0) {
                    var5 |= this.placeColumn(param0, var0, var6, var7, param4.reach().sample(param2));
                }
            }

            return var5;
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
                        if (!param0.getBlockState(var5).is(Blocks.BASALT)) {
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
        while(param2.getY() > param0.getMinBuildHeight() + 1 && param3 > 0) {
            --param3;
            if (canPlaceAt(param0, param1, param2)) {
                return param2;
            }

            param2.move(Direction.DOWN);
        }

        return null;
    }

    private static boolean canPlaceAt(LevelAccessor param0, int param1, BlockPos.MutableBlockPos param2) {
        if (!isAirOrLavaOcean(param0, param1, param2)) {
            return false;
        } else {
            BlockState var0 = param0.getBlockState(param2.move(Direction.DOWN));
            param2.move(Direction.UP);
            return !var0.isAir() && !CANNOT_PLACE_ON.contains(var0.getBlock());
        }
    }

    @Nullable
    private static BlockPos findAir(LevelAccessor param0, BlockPos.MutableBlockPos param1, int param2) {
        while(param1.getY() < param0.getMaxBuildHeight() && param2 > 0) {
            --param2;
            BlockState var0 = param0.getBlockState(param1);
            if (CANNOT_PLACE_ON.contains(var0.getBlock())) {
                return null;
            }

            if (var0.isAir()) {
                return param1;
            }

            param1.move(Direction.UP);
        }

        return null;
    }

    private static boolean isAirOrLavaOcean(LevelAccessor param0, int param1, BlockPos param2) {
        BlockState var0 = param0.getBlockState(param2);
        return var0.isAir() || var0.is(Blocks.LAVA) && param2.getY() <= param1;
    }
}
