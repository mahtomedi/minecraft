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
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;

public class DeltaFeature extends Feature<DeltaFeatureConfiguration> {
    private static final ImmutableList<Block> CANNOT_REPLACE = ImmutableList.of(
        Blocks.BEDROCK, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER
    );
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final double RIM_SPAWN_CHANCE = 0.9;

    public DeltaFeature(Codec<DeltaFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<DeltaFeatureConfiguration> param0) {
        boolean var0 = false;
        Random var1 = param0.random();
        WorldGenLevel var2 = param0.level();
        DeltaFeatureConfiguration var3 = param0.config();
        BlockPos var4 = param0.origin();
        boolean var5 = var1.nextDouble() < 0.9;
        int var6 = var5 ? var3.rimSize().sample(var1) : 0;
        int var7 = var5 ? var3.rimSize().sample(var1) : 0;
        boolean var8 = var5 && var6 != 0 && var7 != 0;
        int var9 = var3.size().sample(var1);
        int var10 = var3.size().sample(var1);
        int var11 = Math.max(var9, var10);

        for(BlockPos var12 : BlockPos.withinManhattan(var4, var9, 0, var10)) {
            if (var12.distManhattan(var4) > var11) {
                break;
            }

            if (isClear(var2, var12, var3)) {
                if (var8) {
                    var0 = true;
                    this.setBlock(var2, var12, var3.rim());
                }

                BlockPos var13 = var12.offset(var6, 0, var7);
                if (isClear(var2, var13, var3)) {
                    var0 = true;
                    this.setBlock(var2, var13, var3.contents());
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
