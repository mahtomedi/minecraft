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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
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

    public final boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, T param4, boolean param5
    ) {
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

            if (param5) {
                Biome var19 = param0.getBiome(param3);
                if (var19 == Biomes.FLOWER_FOREST || var19 == Biomes.SUNFLOWER_PLAINS || var19 == Biomes.PLAINS) {
                    this.spawnBeehive(param0, param2, param3, var1, var3, var19);
                }
            }

            StructureTemplate.updateShapeAtEdge(param0, 3, var6, var1.x0, var1.y0, var1.z0);
            return var2;
        }
    }

    @Override
    public final boolean place(LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, T param4) {
        return this.place(param0, param1, param2, param3, param4, true);
    }

    private void spawnBeehive(LevelAccessor param0, Random param1, BlockPos param2, BoundingBox param3, List<Set<BlockPos>> param4, Biome param5) {
        float var0 = param5 == Biomes.FLOWER_FOREST ? 0.01F : 0.05F;
        if (param1.nextFloat() < var0) {
            Direction var1 = BeehiveBlock.SPAWN_DIRECTIONS[param1.nextInt(BeehiveBlock.SPAWN_DIRECTIONS.length)];
            int var2 = param3.y1;
            if (!param4.isEmpty()) {
                for(BlockPos var3 : param4.get(0)) {
                    if (var3.getY() < var2) {
                        var2 = var3.getY();
                    }
                }
            }

            BlockState var4 = Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, Direction.SOUTH);
            BlockPos var5 = param2.offset(var1.getStepX(), var2 - 1 - param2.getY(), var1.getStepZ());
            if (param0.isEmptyBlock(var5) && param0.isEmptyBlock(var5.relative(Direction.SOUTH))) {
                this.setBlock(param0, var5, var4);
                BlockEntity var6 = param0.getBlockEntity(var5);
                if (var6 instanceof BeehiveBlockEntity) {
                    BeehiveBlockEntity var7 = (BeehiveBlockEntity)var6;
                    int var8 = 2 + param1.nextInt(2);

                    for(int var9 = 0; var9 < var8; ++var9) {
                        Bee var10 = new Bee(EntityType.BEE, param0.getLevel());
                        var7.addOccupantWithPresetTicks(var10, false, param1.nextInt(599));
                    }
                }
            }
        }

    }

    protected abstract boolean doPlace(Set<BlockPos> var1, LevelSimulatedRW var2, Random var3, BlockPos var4, BoundingBox var5);
}
