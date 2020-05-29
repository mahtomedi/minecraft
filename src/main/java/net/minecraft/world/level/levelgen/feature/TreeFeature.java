package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class TreeFeature extends Feature<TreeConfiguration> {
    public TreeFeature(Codec<TreeConfiguration> param0) {
        super(param0);
    }

    public static boolean isFree(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(
            param1,
            param0x -> {
                Block var0x = param0x.getBlock();
                return param0x.isAir()
                    || param0x.is(BlockTags.LEAVES)
                    || isDirt(var0x)
                    || param0x.is(BlockTags.LOGS)
                    || param0x.is(BlockTags.SAPLINGS)
                    || param0x.is(Blocks.VINE)
                    || param0x.is(Blocks.WATER);
            }
        );
    }

    private static boolean isVine(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> param0x.is(Blocks.VINE));
    }

    private static boolean isBlockWater(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> param0x.is(Blocks.WATER));
    }

    public static boolean isAirOrLeaves(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> param0x.isAir() || param0x.is(BlockTags.LEAVES));
    }

    private static boolean isGrassOrDirtOrFarmland(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> {
            Block var0x = param0x.getBlock();
            return isDirt(var0x) || var0x == Blocks.FARMLAND;
        });
    }

    private static boolean isReplaceablePlant(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> {
            Material var0x = param0x.getMaterial();
            return var0x == Material.REPLACEABLE_PLANT;
        });
    }

    public static void setBlockKnownShape(LevelWriter param0, BlockPos param1, BlockState param2) {
        param0.setBlock(param1, param2, 19);
    }

    public static boolean validTreePos(LevelSimulatedRW param0, BlockPos param1) {
        return isAirOrLeaves(param0, param1) || isReplaceablePlant(param0, param1) || isBlockWater(param0, param1);
    }

    private boolean doPlace(
        LevelSimulatedRW param0, Random param1, BlockPos param2, Set<BlockPos> param3, Set<BlockPos> param4, BoundingBox param5, TreeConfiguration param6
    ) {
        int var0 = param6.trunkPlacer.getTreeHeight(param1);
        int var1 = param6.foliagePlacer.foliageHeight(param1, var0, param6);
        int var2 = var0 - var1;
        int var3 = param6.foliagePlacer.foliageRadius(param1, var2);
        BlockPos var9;
        if (!param6.fromSapling) {
            int var4 = param0.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, param2).getY();
            int var5 = param0.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, param2).getY();
            if (var5 - var4 > param6.maxWaterDepth) {
                return false;
            }

            int var6;
            if (param6.heightmap == Heightmap.Types.OCEAN_FLOOR) {
                var6 = var4;
            } else if (param6.heightmap == Heightmap.Types.WORLD_SURFACE) {
                var6 = var5;
            } else {
                var6 = param0.getHeightmapPos(param6.heightmap, param2).getY();
            }

            var9 = new BlockPos(param2.getX(), var6, param2.getZ());
        } else {
            var9 = param2;
        }

        if (var9.getY() < 1 || var9.getY() + var0 + 1 > 256) {
            return false;
        } else if (!isGrassOrDirtOrFarmland(param0, var9.below())) {
            return false;
        } else {
            BlockPos.MutableBlockPos var11 = new BlockPos.MutableBlockPos();
            OptionalInt var12 = param6.minimumSize.minClippedHeight();
            int var13 = var0;

            for(int var14 = 0; var14 <= var0 + 1; ++var14) {
                int var15 = param6.minimumSize.getSizeAtHeight(var0, var14);

                for(int var16 = -var15; var16 <= var15; ++var16) {
                    for(int var17 = -var15; var17 <= var15; ++var17) {
                        var11.setWithOffset(var9, var16, var14, var17);
                        if (!isFree(param0, var11) || !param6.ignoreVines && isVine(param0, var11)) {
                            if (!var12.isPresent() || var14 - 1 < var12.getAsInt() + 1) {
                                return false;
                            }

                            var13 = var14 - 2;
                            break;
                        }
                    }
                }
            }

            List<FoliagePlacer.FoliageAttachment> var19 = param6.trunkPlacer.placeTrunk(param0, param1, var13, var9, param3, param5, param6);
            var19.forEach(param8 -> param6.foliagePlacer.createFoliage(param0, param1, param6, var13, param8, var1, var3, param4, param5));
            return true;
        }
    }

    @Override
    protected void setBlock(LevelWriter param0, BlockPos param1, BlockState param2) {
        setBlockKnownShape(param0, param1, param2);
    }

    public final boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, TreeConfiguration param5
    ) {
        Set<BlockPos> var0 = Sets.newHashSet();
        Set<BlockPos> var1 = Sets.newHashSet();
        Set<BlockPos> var2 = Sets.newHashSet();
        BoundingBox var3 = BoundingBox.getUnknownBox();
        boolean var4 = this.doPlace(param0, param3, param4, var0, var1, var3, param5);
        if (var3.x0 <= var3.x1 && var4 && !var0.isEmpty()) {
            if (!param5.decorators.isEmpty()) {
                List<BlockPos> var5 = Lists.newArrayList(var0);
                List<BlockPos> var6 = Lists.newArrayList(var1);
                var5.sort(Comparator.comparingInt(Vec3i::getY));
                var6.sort(Comparator.comparingInt(Vec3i::getY));
                param5.decorators.forEach(param6 -> param6.place(param0, param3, var5, var6, var2, var3));
            }

            DiscreteVoxelShape var7 = this.updateLeaves(param0, var3, var0, var2);
            StructureTemplate.updateShapeAtEdge(param0, 3, var7, var3.x0, var3.y0, var3.z0);
            return true;
        } else {
            return false;
        }
    }

    private DiscreteVoxelShape updateLeaves(LevelAccessor param0, BoundingBox param1, Set<BlockPos> param2, Set<BlockPos> param3) {
        List<Set<BlockPos>> var0 = Lists.newArrayList();
        DiscreteVoxelShape var1 = new BitSetDiscreteVoxelShape(param1.getXSpan(), param1.getYSpan(), param1.getZSpan());
        int var2 = 6;

        for(int var3 = 0; var3 < 6; ++var3) {
            var0.add(Sets.newHashSet());
        }

        BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

        for(BlockPos var5 : Lists.newArrayList(param3)) {
            if (param1.isInside(var5)) {
                var1.setFull(var5.getX() - param1.x0, var5.getY() - param1.y0, var5.getZ() - param1.z0, true, true);
            }
        }

        for(BlockPos var6 : Lists.newArrayList(param2)) {
            if (param1.isInside(var6)) {
                var1.setFull(var6.getX() - param1.x0, var6.getY() - param1.y0, var6.getZ() - param1.z0, true, true);
            }

            for(Direction var7 : Direction.values()) {
                var4.setWithOffset(var6, var7);
                if (!param2.contains(var4)) {
                    BlockState var8 = param0.getBlockState(var4);
                    if (var8.hasProperty(BlockStateProperties.DISTANCE)) {
                        var0.get(0).add(var4.immutable());
                        setBlockKnownShape(param0, var4, var8.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(1)));
                        if (param1.isInside(var4)) {
                            var1.setFull(var4.getX() - param1.x0, var4.getY() - param1.y0, var4.getZ() - param1.z0, true, true);
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
                    var1.setFull(var12.getX() - param1.x0, var12.getY() - param1.y0, var12.getZ() - param1.z0, true, true);
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
                                    var1.setFull(var4.getX() - param1.x0, var4.getY() - param1.y0, var4.getZ() - param1.z0, true, true);
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
