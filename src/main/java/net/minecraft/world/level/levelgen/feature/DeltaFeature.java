package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;

public class DeltaFeature extends Feature<DeltaFeatureConfiguration> {
    private static final ImmutableList<Block> CANNOT_REPLACE = ImmutableList.of(
        Blocks.BEDROCK, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER
    );
    private static final Direction[] DIRECTIONS = Direction.values();

    private static int calculateRadius(Random param0, DeltaFeatureConfiguration param1) {
        return param1.minimumRadius + param0.nextInt(param1.maximumRadius - param1.minimumRadius + 1);
    }

    private static int calculateRimSize(Random param0, DeltaFeatureConfiguration param1) {
        return param0.nextInt(param1.maximumRimSize + 1);
    }

    public DeltaFeature(Function<Dynamic<?>, ? extends DeltaFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0,
        StructureFeatureManager param1,
        ChunkGenerator<? extends ChunkGeneratorSettings> param2,
        Random param3,
        BlockPos param4,
        DeltaFeatureConfiguration param5
    ) {
        BlockPos var0 = findDeltaLevel(param0, param4.mutable().clamp(Direction.Axis.Y, 1, param0.getMaxBuildHeight() - 1));
        if (var0 == null) {
            return false;
        } else {
            boolean var1 = false;
            boolean var2 = param3.nextDouble() < 0.9;
            int var3 = var2 ? calculateRimSize(param3, param5) : 0;
            int var4 = var2 ? calculateRimSize(param3, param5) : 0;
            boolean var5 = var2 && var3 != 0 && var4 != 0;
            int var6 = calculateRadius(param3, param5);
            int var7 = calculateRadius(param3, param5);
            int var8 = Math.max(var6, var7);

            for(BlockPos var9 : BlockPos.withinManhattan(var0, var6, 0, var7)) {
                if (var9.distManhattan(var0) > var8) {
                    break;
                }

                if (isClear(param0, var9, param5)) {
                    if (var5) {
                        var1 = true;
                        this.setBlock(param0, var9, param5.rim);
                    }

                    BlockPos var10 = var9.offset(var3, 0, var4);
                    if (isClear(param0, var10, param5)) {
                        var1 = true;
                        this.setBlock(param0, var10, param5.contents);
                    }
                }
            }

            return var1;
        }
    }

    private static boolean isClear(LevelAccessor param0, BlockPos param1, DeltaFeatureConfiguration param2) {
        Block var0 = param0.getBlockState(param1).getBlock();
        if (var0 == param2.contents.getBlock()) {
            return false;
        } else if (CANNOT_REPLACE.contains(var0)) {
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

    @Nullable
    private static BlockPos findDeltaLevel(LevelAccessor param0, BlockPos.MutableBlockPos param1) {
        for(; param1.getY() > 1; param1.move(Direction.DOWN)) {
            if (param0.getBlockState(param1).isAir()) {
                BlockState var0 = param0.getBlockState(param1.move(Direction.DOWN));
                param1.move(Direction.UP);
                Block var1 = var0.getBlock();
                if (var1 != Blocks.LAVA && var1 != Blocks.BEDROCK && !var0.isAir()) {
                    return param1;
                }
            }
        }

        return null;
    }
}
