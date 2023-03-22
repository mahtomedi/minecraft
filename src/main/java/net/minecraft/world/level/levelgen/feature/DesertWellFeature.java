package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class DesertWellFeature extends Feature<NoneFeatureConfiguration> {
    private static final BlockStatePredicate IS_SAND = BlockStatePredicate.forBlock(Blocks.SAND);
    private final BlockState sand = Blocks.SAND.defaultBlockState();
    private final BlockState sandSlab = Blocks.SANDSTONE_SLAB.defaultBlockState();
    private final BlockState sandstone = Blocks.SANDSTONE.defaultBlockState();
    private final BlockState water = Blocks.WATER.defaultBlockState();

    public DesertWellFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        var1 = var1.above();

        while(var0.isEmptyBlock(var1) && var1.getY() > var0.getMinBuildHeight() + 2) {
            var1 = var1.below();
        }

        if (!IS_SAND.test(var0.getBlockState(var1))) {
            return false;
        } else {
            for(int var2 = -2; var2 <= 2; ++var2) {
                for(int var3 = -2; var3 <= 2; ++var3) {
                    if (var0.isEmptyBlock(var1.offset(var2, -1, var3)) && var0.isEmptyBlock(var1.offset(var2, -2, var3))) {
                        return false;
                    }
                }
            }

            for(int var4 = -2; var4 <= 0; ++var4) {
                for(int var5 = -2; var5 <= 2; ++var5) {
                    for(int var6 = -2; var6 <= 2; ++var6) {
                        var0.setBlock(var1.offset(var5, var4, var6), this.sandstone, 2);
                    }
                }
            }

            var0.setBlock(var1, this.water, 2);

            for(Direction var7 : Direction.Plane.HORIZONTAL) {
                var0.setBlock(var1.relative(var7), this.water, 2);
            }

            BlockPos var8 = var1.below();
            var0.setBlock(var8, this.sand, 2);

            for(Direction var9 : Direction.Plane.HORIZONTAL) {
                var0.setBlock(var8.relative(var9), this.sand, 2);
            }

            for(int var10 = -2; var10 <= 2; ++var10) {
                for(int var11 = -2; var11 <= 2; ++var11) {
                    if (var10 == -2 || var10 == 2 || var11 == -2 || var11 == 2) {
                        var0.setBlock(var1.offset(var10, 1, var11), this.sandstone, 2);
                    }
                }
            }

            var0.setBlock(var1.offset(2, 1, 0), this.sandSlab, 2);
            var0.setBlock(var1.offset(-2, 1, 0), this.sandSlab, 2);
            var0.setBlock(var1.offset(0, 1, 2), this.sandSlab, 2);
            var0.setBlock(var1.offset(0, 1, -2), this.sandSlab, 2);

            for(int var12 = -1; var12 <= 1; ++var12) {
                for(int var13 = -1; var13 <= 1; ++var13) {
                    if (var12 == 0 && var13 == 0) {
                        var0.setBlock(var1.offset(var12, 4, var13), this.sandstone, 2);
                    } else {
                        var0.setBlock(var1.offset(var12, 4, var13), this.sandSlab, 2);
                    }
                }
            }

            for(int var14 = 1; var14 <= 3; ++var14) {
                var0.setBlock(var1.offset(-1, var14, -1), this.sandstone, 2);
                var0.setBlock(var1.offset(-1, var14, 1), this.sandstone, 2);
                var0.setBlock(var1.offset(1, var14, -1), this.sandstone, 2);
                var0.setBlock(var1.offset(1, var14, 1), this.sandstone, 2);
            }

            List<BlockPos> var16 = List.of(var1, var1.east(), var1.south(), var1.west(), var1.north());
            RandomSource var17 = param0.random();
            placeSusSand(var0, Util.getRandom(var16, var17).below(1));
            placeSusSand(var0, Util.getRandom(var16, var17).below(2));
            return true;
        }
    }

    private static void placeSusSand(WorldGenLevel param0, BlockPos param1) {
        param0.setBlock(param1, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 3);
        param0.getBlockEntity(param1, BlockEntityType.BRUSHABLE_BLOCK)
            .ifPresent(param1x -> param1x.setLootTable(BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY, param1.asLong()));
    }
}
