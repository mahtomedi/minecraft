package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public abstract class AbstractTreeFeature<T extends FeatureConfiguration> extends Feature<T> {
    public AbstractTreeFeature(Function<Dynamic<?>, ? extends T> param0, boolean param1) {
        super(param0, param1);
    }

    protected static boolean isFree(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(
            param1,
            param0x -> {
                Block var0x = param0x.getBlock();
                return param0x.isAir()
                    || param0x.is(BlockTags.LEAVES)
                    || var0x == Blocks.GRASS_BLOCK
                    || Block.equalsDirt(var0x)
                    || var0x.is(BlockTags.LOGS)
                    || var0x.is(BlockTags.SAPLINGS)
                    || var0x == Blocks.VINE;
            }
        );
    }

    protected static boolean isAir(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, BlockState::isAir);
    }

    protected static boolean isDirt(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> Block.equalsDirt(param0x.getBlock()));
    }

    protected static boolean isBlockWater(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> param0x.getBlock() == Blocks.WATER);
    }

    protected static boolean isLeaves(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> param0x.is(BlockTags.LEAVES));
    }

    protected static boolean isAirOrLeaves(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> param0x.isAir() || param0x.is(BlockTags.LEAVES));
    }

    protected static boolean isGrassOrDirt(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> {
            Block var0x = param0x.getBlock();
            return Block.equalsDirt(var0x) || var0x == Blocks.GRASS_BLOCK;
        });
    }

    protected static boolean isGrassOrDirtOrFarmland(LevelSimulatedReader param0, BlockPos param1) {
        return param0.isStateAtPosition(param1, param0x -> {
            Block var0x = param0x.getBlock();
            return Block.equalsDirt(var0x) || var0x == Blocks.GRASS_BLOCK || var0x == Blocks.FARMLAND;
        });
    }

    protected static boolean isReplaceablePlant(LevelSimulatedReader param0, BlockPos param1) {
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

    @Override
    protected void setBlock(LevelWriter param0, BlockPos param1, BlockState param2) {
        this.setBlockKnownShape(param0, param1, param2);
    }

    protected final void setBlock(Set<BlockPos> param0, LevelWriter param1, BlockPos param2, BlockState param3, BoundingBox param4) {
        this.setBlockKnownShape(param1, param2, param3);
        param4.expand(new BoundingBox(param2, param2));
        if (BlockTags.LOGS.contains(param3.getBlock())) {
            param0.add(param2.immutable());
        }

    }

    private void setBlockKnownShape(LevelWriter param0, BlockPos param1, BlockState param2) {
        if (this.doUpdate) {
            param0.setBlock(param1, param2, 19);
        } else {
            param0.setBlock(param1, param2, 18);
        }

    }

    @Override
    public final boolean place(LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, T param4) {
        Set<BlockPos> var0 = Sets.newHashSet();
        BoundingBox var1 = BoundingBox.getUnknownBox();
        boolean var2 = this.doPlace(var0, param0, param2, param3, var1);
        if (var1.x0 > var1.x1) {
            return false;
        } else {
            List<Set<BlockPos>> var3 = Lists.newArrayList();
            int var4 = 6;

            for(int var5 = 0; var5 < 6; ++var5) {
                var3.add(Sets.newHashSet());
            }

            DiscreteVoxelShape var6 = new BitSetDiscreteVoxelShape(var1.getXSpan(), var1.getYSpan(), var1.getZSpan());

            try (BlockPos.PooledMutableBlockPos var7 = BlockPos.PooledMutableBlockPos.acquire()) {
                if (var2 && !var0.isEmpty()) {
                    for(BlockPos var8 : Lists.newArrayList(var0)) {
                        if (var1.isInside(var8)) {
                            var6.setFull(var8.getX() - var1.x0, var8.getY() - var1.y0, var8.getZ() - var1.z0, true, true);
                        }

                        for(Direction var9 : Direction.values()) {
                            var7.set(var8).move(var9);
                            if (!var0.contains(var7)) {
                                BlockState var10 = param0.getBlockState(var7);
                                if (var10.hasProperty(BlockStateProperties.DISTANCE)) {
                                    var3.get(0).add(var7.immutable());
                                    this.setBlockKnownShape(param0, var7, var10.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(1)));
                                    if (var1.isInside(var7)) {
                                        var6.setFull(var7.getX() - var1.x0, var7.getY() - var1.y0, var7.getZ() - var1.z0, true, true);
                                    }
                                }
                            }
                        }
                    }
                }

                for(int var11 = 1; var11 < 6; ++var11) {
                    Set<BlockPos> var12 = var3.get(var11 - 1);
                    Set<BlockPos> var13 = var3.get(var11);

                    for(BlockPos var14 : var12) {
                        if (var1.isInside(var14)) {
                            var6.setFull(var14.getX() - var1.x0, var14.getY() - var1.y0, var14.getZ() - var1.z0, true, true);
                        }

                        for(Direction var15 : Direction.values()) {
                            var7.set(var14).move(var15);
                            if (!var12.contains(var7) && !var13.contains(var7)) {
                                BlockState var16 = param0.getBlockState(var7);
                                if (var16.hasProperty(BlockStateProperties.DISTANCE)) {
                                    int var17 = var16.getValue(BlockStateProperties.DISTANCE);
                                    if (var17 > var11 + 1) {
                                        BlockState var18 = var16.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(var11 + 1));
                                        this.setBlockKnownShape(param0, var7, var18);
                                        if (var1.isInside(var7)) {
                                            var6.setFull(var7.getX() - var1.x0, var7.getY() - var1.y0, var7.getZ() - var1.z0, true, true);
                                        }

                                        var13.add(var7.immutable());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            StructureTemplate.updateShapeAtEdge(param0, 3, var6, var1.x0, var1.y0, var1.z0);
            return var2;
        }
    }

    protected abstract boolean doPlace(Set<BlockPos> var1, LevelSimulatedRW var2, Random var3, BlockPos var4, BoundingBox var5);
}
