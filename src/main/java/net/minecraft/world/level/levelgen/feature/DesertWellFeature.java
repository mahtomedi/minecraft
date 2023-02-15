package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.loot.packs.UpdateOneTwentyBuiltInLootTables;
import net.minecraft.util.RandomSource;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.apache.commons.lang3.mutable.MutableInt;

public class DesertWellFeature extends Feature<NoneFeatureConfiguration> {
    private static final BlockStatePredicate IS_SAND = BlockStatePredicate.forBlock(Blocks.SAND);
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

            for(int var4 = -1; var4 <= 0; ++var4) {
                for(int var5 = -2; var5 <= 2; ++var5) {
                    for(int var6 = -2; var6 <= 2; ++var6) {
                        var0.setBlock(var1.offset(var5, var4, var6), this.sandstone, 2);
                    }
                }
            }

            if (var0.enabledFeatures().contains(FeatureFlags.UPDATE_1_20)) {
                placeSandFloor(var0, var1, param0.random());
            }

            var0.setBlock(var1, this.water, 2);

            for(Direction var7 : Direction.Plane.HORIZONTAL) {
                var0.setBlock(var1.relative(var7), this.water, 2);
            }

            for(int var8 = -2; var8 <= 2; ++var8) {
                for(int var9 = -2; var9 <= 2; ++var9) {
                    if (var8 == -2 || var8 == 2 || var9 == -2 || var9 == 2) {
                        var0.setBlock(var1.offset(var8, 1, var9), this.sandstone, 2);
                    }
                }
            }

            var0.setBlock(var1.offset(2, 1, 0), this.sandSlab, 2);
            var0.setBlock(var1.offset(-2, 1, 0), this.sandSlab, 2);
            var0.setBlock(var1.offset(0, 1, 2), this.sandSlab, 2);
            var0.setBlock(var1.offset(0, 1, -2), this.sandSlab, 2);

            for(int var10 = -1; var10 <= 1; ++var10) {
                for(int var11 = -1; var11 <= 1; ++var11) {
                    if (var10 == 0 && var11 == 0) {
                        var0.setBlock(var1.offset(var10, 4, var11), this.sandstone, 2);
                    } else {
                        var0.setBlock(var1.offset(var10, 4, var11), this.sandSlab, 2);
                    }
                }
            }

            for(int var12 = 1; var12 <= 3; ++var12) {
                var0.setBlock(var1.offset(-1, var12, -1), this.sandstone, 2);
                var0.setBlock(var1.offset(-1, var12, 1), this.sandstone, 2);
                var0.setBlock(var1.offset(1, var12, -1), this.sandstone, 2);
                var0.setBlock(var1.offset(1, var12, 1), this.sandstone, 2);
            }

            return true;
        }
    }

    private static void placeSandFloor(WorldGenLevel param0, BlockPos param1, RandomSource param2) {
        BlockPos var0 = param1.offset(0, -1, 0);
        ObjectArrayList<BlockPos> var1 = Util.make(new ObjectArrayList<>(), param1x -> {
            param1x.add(var0.east());
            param1x.add(var0.south());
            param1x.add(var0.west());
            param1x.add(var0.north());
        });
        Util.shuffle(var1, param2);
        MutableInt var2 = new MutableInt(param2.nextInt(2, 4));
        Stream.concat(Stream.of(var0), var1.stream())
            .forEach(
                param2x -> {
                    if (var2.getAndDecrement() > 0) {
                        param0.setBlock(param2x, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 3);
                        param0.getBlockEntity(param2x, BlockEntityType.SUSPICIOUS_SAND)
                            .ifPresent(param1x -> param1x.setLootTable(UpdateOneTwentyBuiltInLootTables.DESERT_WELL_ARCHAEOLOGY, param2x.asLong()));
                    } else {
                        param0.setBlock(param2x, Blocks.SAND.defaultBlockState(), 3);
                    }
        
                }
            );
    }
}
