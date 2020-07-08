package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;

public class DeltaFeature extends Feature<DeltaFeatureConfiguration> {
    private static final ImmutableList<Block> CANNOT_REPLACE = ImmutableList.of(
        Blocks.BEDROCK, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER
    );
    private static final Direction[] DIRECTIONS = Direction.values();

    public DeltaFeature(Codec<DeltaFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, DeltaFeatureConfiguration param4) {
        boolean var0 = false;
        boolean var1 = param2.nextDouble() < 0.9;
        int var2 = var1 ? param4.rimSize().sample(param2) : 0;
        int var3 = var1 ? param4.rimSize().sample(param2) : 0;
        boolean var4 = var1 && var2 != 0 && var3 != 0;
        int var5 = param4.size().sample(param2);
        int var6 = param4.size().sample(param2);
        int var7 = Math.max(var5, var6);

        for(BlockPos var8 : BlockPos.withinManhattan(param3, var5, 0, var6)) {
            if (var8.distManhattan(param3) > var7) {
                break;
            }

            if (isClear(param0, var8, param4)) {
                if (var4) {
                    var0 = true;
                    this.setBlock(param0, var8, param4.rim());
                }

                BlockPos var9 = var8.offset(var2, 0, var3);
                if (isClear(param0, var9, param4)) {
                    var0 = true;
                    this.setBlock(param0, var9, param4.contents());
                }
            }
        }

        return var0;
    }

    private static boolean isClear(LevelAccessor param0, BlockPos param1, DeltaFeatureConfiguration param2) {
        BlockState var0 = param0.getBlockState(param1);
        if (var0.is(param2.contents().getBlock())) {
            return false;
        } else if (CANNOT_REPLACE.contains(var0.getBlock())) {
            return false;
        } else {
            for(Direction var1 : DIRECTIONS) {
                boolean var2 = param0.getBlockState(param1.relative(var1)).isAir();
                if (var2 && var1 != Direction.UP || !var2 && var1 == Direction.UP) {
                    return false;
                }
            }

            return true;
        }
    }
}
