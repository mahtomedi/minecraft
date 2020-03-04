package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.Dynamic;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public abstract class AbstractTreeFeature<T extends TreeConfiguration> extends Feature<T> {
    public AbstractTreeFeature(Function<Dynamic<?>, ? extends T> param0) {
        super(param0);
    }

    protected static boolean isFree(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(
            param1,
            param0x -> {
                Block var0x = param0x.getBlock();
                return param0x.isAir()
                    || param0x.is(BlockTags.LEAVES)
                    || isDirt(var0x)
                    || var0x.is(BlockTags.LOGS)
                    || var0x.is(BlockTags.SAPLINGS)
                    || var0x == Blocks.VINE;
            }
        );
    }

    public static boolean isAir(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, BlockState::isAir);
    }

    protected static boolean isDirt(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> {
            Block var0x = param0x.getBlock();
            return isDirt(var0x) && var0x != Blocks.GRASS_BLOCK && var0x != Blocks.MYCELIUM;
        });
    }

    protected static boolean isVine(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> param0x.getBlock() == Blocks.VINE);
    }

    public static boolean isBlockWater(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> param0x.getBlock() == Blocks.WATER);
    }

    public static boolean isAirOrLeaves(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> param0x.isAir() || param0x.is(BlockTags.LEAVES));
    }

    public static boolean isGrassOrDirt(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> isDirt(param0x.getBlock()));
    }

    protected static boolean isGrassOrDirtOrFarmland(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> {
            Block var0x = param0x.getBlock();
            return isDirt(var0x) || var0x == Blocks.FARMLAND;
        });
    }

    public static boolean isReplaceablePlant(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> {
            Material var0x = param0x.getMaterial();
            return var0x == Material.REPLACEABLE_PLANT;
        });
    }

    protected void setDirtAt(LevelSimulatedRW param0, BlockPos param1) {
        if (!isDirt(param0, param1)) {
            this.setBlock(param0, param1, Blocks.DIRT.defaultBlockState());
        }

    }

    protected boolean placeLog(LevelSimulatedRW param0, Random param1, BlockPos param2, Set<BlockPos> param3, BoundingBox param4, TreeConfiguration param5) {
        if (!isAirOrLeaves(param0, param2) && !isReplaceablePlant(param0, param2) && !isBlockWater(param0, param2)) {
            return false;
        } else {
            this.setBlock(param0, param2, param5.trunkProvider.getState(param1, param2), param4);
            param3.add(param2.immutable());
            return true;
        }
    }

    protected boolean placeLeaf(LevelSimulatedRW param0, Random param1, BlockPos param2, Set<BlockPos> param3, BoundingBox param4, TreeConfiguration param5) {
        if (!isAirOrLeaves(param0, param2) && !isReplaceablePlant(param0, param2) && !isBlockWater(param0, param2)) {
            return false;
        } else {
            this.setBlock(param0, param2, param5.leavesProvider.getState(param1, param2), param4);
            param3.add(param2.immutable());
            return true;
        }
    }

    @Override
    protected void setBlock(LevelWriter param0, BlockPos param1, BlockState param2) {
        this.setBlockKnownShape(param0, param1, param2);
    }

    protected final void setBlock(LevelWriter param0, BlockPos param1, BlockState param2, BoundingBox param3) {
        this.setBlockKnownShape(param0, param1, param2);
        param3.expand(new BoundingBox(param1, param1));
    }

    private void setBlockKnownShape(LevelWriter param0, BlockPos param1, BlockState param2) {
        param0.setBlock(param1, param2, 19);
    }

    public final boolean place(LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, T param4) {
        Set<BlockPos> var0 = Sets.newHashSet();
        Set<BlockPos> var1 = Sets.newHashSet();
        Set<BlockPos> var2 = Sets.newHashSet();
        BoundingBox var3 = BoundingBox.getUnknownBox();
        boolean var4 = this.doPlace(param0, param2, param3, var0, var1, var3, param4);
        if (var3.x0 <= var3.x1 && var4 && !var0.isEmpty()) {
            if (!param4.decorators.isEmpty()) {
                List<BlockPos> var5 = Lists.newArrayList(var0);
                List<BlockPos> var6 = Lists.newArrayList(var1);
                var5.sort(Comparator.comparingInt(Vec3i::getY));
                var6.sort(Comparator.comparingInt(Vec3i::getY));
                param4.decorators.forEach(param6 -> param6.place(param0, param2, var5, var6, var2, var3));
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
                        this.setBlockKnownShape(param0, var4, var8.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(1)));
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
                                this.setBlockKnownShape(param0, var4, var16);
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

    protected abstract boolean doPlace(LevelSimulatedRW var1, Random var2, BlockPos var3, Set<BlockPos> var4, Set<BlockPos> var5, BoundingBox var6, T var7);
}
