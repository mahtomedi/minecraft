package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class TreeFeature extends Feature<TreeConfiguration> {
    private static final int BLOCK_UPDATE_FLAGS = 19;

    public TreeFeature(Codec<TreeConfiguration> param0) {
        super(param0);
    }

    private static boolean isVine(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> param0x.is(Blocks.VINE));
    }

    public static boolean isBlockWater(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> param0x.is(Blocks.WATER));
    }

    public static boolean isAirOrLeaves(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> param0x.isAir() || param0x.is(BlockTags.LEAVES));
    }

    private static boolean isReplaceablePlant(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> {
            Material var0x = param0x.getMaterial();
            return var0x == Material.REPLACEABLE_PLANT || var0x == Material.REPLACEABLE_WATER_PLANT || var0x == Material.REPLACEABLE_FIREPROOF_PLANT;
        });
    }

    private static void setBlockKnownShape(LevelWriter param0, BlockPos param1, BlockState param2) {
        param0.setBlock(param1, param2, 19);
    }

    public static boolean validTreePos(LevelSimulatedReader param0, BlockPos param1) {
        return isAirOrLeaves(param0, param1) || isReplaceablePlant(param0, param1) || isBlockWater(param0, param1);
    }

    private boolean doPlace(
        WorldGenLevel param0,
        RandomSource param1,
        BlockPos param2,
        BiConsumer<BlockPos, BlockState> param3,
        BiConsumer<BlockPos, BlockState> param4,
        BiConsumer<BlockPos, BlockState> param5,
        TreeConfiguration param6
    ) {
        int var0 = param6.trunkPlacer.getTreeHeight(param1);
        int var1 = param6.foliagePlacer.foliageHeight(param1, var0, param6);
        int var2 = var0 - var1;
        int var3 = param6.foliagePlacer.foliageRadius(param1, var2);
        BlockPos var4 = param6.rootPlacer.<BlockPos>map(param2x -> param2x.getTrunkOrigin(param2, param1)).orElse(param2);
        int var5 = Math.min(param2.getY(), var4.getY());
        int var6 = Math.max(param2.getY(), var4.getY()) + var0 + 1;
        if (var5 >= param0.getMinBuildHeight() + 1 && var6 <= param0.getMaxBuildHeight()) {
            OptionalInt var7 = param6.minimumSize.minClippedHeight();
            int var8 = this.getMaxFreeTreeHeight(param0, var0, var4, param6);
            if (var8 >= var0 || !var7.isEmpty() && var8 >= var7.getAsInt()) {
                if (param6.rootPlacer.isPresent() && !param6.rootPlacer.get().placeRoots(param0, param3, param1, param2, var4, param6)) {
                    return false;
                } else {
                    List<FoliagePlacer.FoliageAttachment> var9 = param6.trunkPlacer.placeTrunk(param0, param4, param1, var8, var4, param6);
                    var9.forEach(param7 -> param6.foliagePlacer.createFoliage(param0, param5, param1, param6, var8, param7, var1, var3));
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private int getMaxFreeTreeHeight(LevelSimulatedReader param0, int param1, BlockPos param2, TreeConfiguration param3) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(int var1 = 0; var1 <= param1 + 1; ++var1) {
            int var2 = param3.minimumSize.getSizeAtHeight(param1, var1);

            for(int var3 = -var2; var3 <= var2; ++var3) {
                for(int var4 = -var2; var4 <= var2; ++var4) {
                    var0.setWithOffset(param2, var3, var1, var4);
                    if (!param3.trunkPlacer.isFree(param0, var0) || !param3.ignoreVines && isVine(param0, var0)) {
                        return var1 - 2;
                    }
                }
            }
        }

        return param1;
    }

    @Override
    protected void setBlock(LevelWriter param0, BlockPos param1, BlockState param2) {
        setBlockKnownShape(param0, param1, param2);
    }

    @Override
    public final boolean place(FeaturePlaceContext<TreeConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        RandomSource var1 = param0.random();
        BlockPos var2 = param0.origin();
        TreeConfiguration var3 = param0.config();
        Set<BlockPos> var4 = Sets.newHashSet();
        Set<BlockPos> var5 = Sets.newHashSet();
        Set<BlockPos> var6 = Sets.newHashSet();
        Set<BlockPos> var7 = Sets.newHashSet();
        BiConsumer<BlockPos, BlockState> var8 = (param2, param3) -> {
            var4.add(param2.immutable());
            var0.setBlock(param2, param3, 19);
        };
        BiConsumer<BlockPos, BlockState> var9 = (param2, param3) -> {
            var5.add(param2.immutable());
            var0.setBlock(param2, param3, 19);
        };
        BiConsumer<BlockPos, BlockState> var10 = (param2, param3) -> {
            var6.add(param2.immutable());
            var0.setBlock(param2, param3, 19);
        };
        BiConsumer<BlockPos, BlockState> var11 = (param2, param3) -> {
            var7.add(param2.immutable());
            var0.setBlock(param2, param3, 19);
        };
        boolean var12 = this.doPlace(var0, var1, var2, var8, var9, var10, var3);
        if (var12 && (!var5.isEmpty() || !var6.isEmpty())) {
            if (!var3.decorators.isEmpty()) {
                TreeDecorator.Context var13 = new TreeDecorator.Context(var0, var11, var1, var5, var6, var4);
                var3.decorators.forEach(param1 -> param1.place(var13));
            }

            return BoundingBox.encapsulatingPositions(Iterables.concat(var4, var5, var6, var7)).map(param4 -> {
                DiscreteVoxelShape var0x = updateLeaves(var0, param4, var5, var7, var4);
                StructureTemplate.updateShapeAtEdge(var0, 3, var0x, param4.minX(), param4.minY(), param4.minZ());
                return true;
            }).orElse(false);
        } else {
            return false;
        }
    }

    private static DiscreteVoxelShape updateLeaves(LevelAccessor param0, BoundingBox param1, Set<BlockPos> param2, Set<BlockPos> param3, Set<BlockPos> param4) {
        List<Set<BlockPos>> var0 = Lists.newArrayList();
        DiscreteVoxelShape var1 = new BitSetDiscreteVoxelShape(param1.getXSpan(), param1.getYSpan(), param1.getZSpan());
        int var2 = 6;

        for(int var3 = 0; var3 < 6; ++var3) {
            var0.add(Sets.newHashSet());
        }

        BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

        for(BlockPos var5 : Lists.newArrayList(Sets.union(param3, param4))) {
            if (param1.isInside(var5)) {
                var1.fill(var5.getX() - param1.minX(), var5.getY() - param1.minY(), var5.getZ() - param1.minZ());
            }
        }

        for(BlockPos var6 : Lists.newArrayList(param2)) {
            if (param1.isInside(var6)) {
                var1.fill(var6.getX() - param1.minX(), var6.getY() - param1.minY(), var6.getZ() - param1.minZ());
            }

            for(Direction var7 : Direction.values()) {
                var4.setWithOffset(var6, var7);
                if (!param2.contains(var4)) {
                    BlockState var8 = param0.getBlockState(var4);
                    if (var8.hasProperty(BlockStateProperties.DISTANCE)) {
                        var0.get(0).add(var4.immutable());
                        setBlockKnownShape(param0, var4, var8.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(1)));
                        if (param1.isInside(var4)) {
                            var1.fill(var4.getX() - param1.minX(), var4.getY() - param1.minY(), var4.getZ() - param1.minZ());
                        }
                    }
                }
            }
        }

        for(int var9 = 1; var9 < 6; ++var9) {
            Set<BlockPos> var10 = var0.get(var9 - 1);
            Set<BlockPos> var11 = var0.get(var9);

            for(BlockPos var12 : var10) {
                if (param1.isInside(var12)) {
                    var1.fill(var12.getX() - param1.minX(), var12.getY() - param1.minY(), var12.getZ() - param1.minZ());
                }

                for(Direction var13 : Direction.values()) {
                    var4.setWithOffset(var12, var13);
                    if (!var10.contains(var4) && !var11.contains(var4)) {
                        BlockState var14 = param0.getBlockState(var4);
                        if (var14.hasProperty(BlockStateProperties.DISTANCE)) {
                            int var15 = var14.getValue(BlockStateProperties.DISTANCE);
                            if (var15 > var9 + 1) {
                                BlockState var16 = var14.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(var9 + 1));
                                setBlockKnownShape(param0, var4, var16);
                                if (param1.isInside(var4)) {
                                    var1.fill(var4.getX() - param1.minX(), var4.getY() - param1.minY(), var4.getZ() - param1.minZ());
                                }

                                var11.add(var4.immutable());
                            }
                        }
                    }
                }
            }
        }

        return var1;
    }
}
