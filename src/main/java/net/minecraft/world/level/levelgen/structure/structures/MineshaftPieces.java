package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.slf4j.Logger;

public class MineshaftPieces {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_SHAFT_WIDTH = 3;
    private static final int DEFAULT_SHAFT_HEIGHT = 3;
    private static final int DEFAULT_SHAFT_LENGTH = 5;
    private static final int MAX_PILLAR_HEIGHT = 20;
    private static final int MAX_CHAIN_HEIGHT = 50;
    private static final int MAX_DEPTH = 8;
    public static final int MAGIC_START_Y = 50;

    private static MineshaftPieces.MineShaftPiece createRandomShaftPiece(
        StructurePieceAccessor param0,
        RandomSource param1,
        int param2,
        int param3,
        int param4,
        @Nullable Direction param5,
        int param6,
        MineshaftStructure.Type param7
    ) {
        int var0 = param1.nextInt(100);
        if (var0 >= 80) {
            BoundingBox var1 = MineshaftPieces.MineShaftCrossing.findCrossing(param0, param1, param2, param3, param4, param5);
            if (var1 != null) {
                return new MineshaftPieces.MineShaftCrossing(param6, var1, param5, param7);
            }
        } else if (var0 >= 70) {
            BoundingBox var2 = MineshaftPieces.MineShaftStairs.findStairs(param0, param1, param2, param3, param4, param5);
            if (var2 != null) {
                return new MineshaftPieces.MineShaftStairs(param6, var2, param5, param7);
            }
        } else {
            BoundingBox var3 = MineshaftPieces.MineShaftCorridor.findCorridorSize(param0, param1, param2, param3, param4, param5);
            if (var3 != null) {
                return new MineshaftPieces.MineShaftCorridor(param6, param1, var3, param5, param7);
            }
        }

        return null;
    }

    static MineshaftPieces.MineShaftPiece generateAndAddPiece(
        StructurePiece param0, StructurePieceAccessor param1, RandomSource param2, int param3, int param4, int param5, Direction param6, int param7
    ) {
        if (param7 > 8) {
            return null;
        } else if (Math.abs(param3 - param0.getBoundingBox().minX()) <= 80 && Math.abs(param5 - param0.getBoundingBox().minZ()) <= 80) {
            MineshaftStructure.Type var0 = ((MineshaftPieces.MineShaftPiece)param0).type;
            MineshaftPieces.MineShaftPiece var1 = createRandomShaftPiece(param1, param2, param3, param4, param5, param6, param7 + 1, var0);
            if (var1 != null) {
                param1.addPiece(var1);
                var1.addChildren(param0, param1, param2);
            }

            return var1;
        } else {
            return null;
        }
    }

    public static class MineShaftCorridor extends MineshaftPieces.MineShaftPiece {
        private final boolean hasRails;
        private final boolean spiderCorridor;
        private boolean hasPlacedSpider;
        private final int numSections;

        public MineShaftCorridor(CompoundTag param0) {
            super(StructurePieceType.MINE_SHAFT_CORRIDOR, param0);
            this.hasRails = param0.getBoolean("hr");
            this.spiderCorridor = param0.getBoolean("sc");
            this.hasPlacedSpider = param0.getBoolean("hps");
            this.numSections = param0.getInt("Num");
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext param0, CompoundTag param1) {
            super.addAdditionalSaveData(param0, param1);
            param1.putBoolean("hr", this.hasRails);
            param1.putBoolean("sc", this.spiderCorridor);
            param1.putBoolean("hps", this.hasPlacedSpider);
            param1.putInt("Num", this.numSections);
        }

        public MineShaftCorridor(int param0, RandomSource param1, BoundingBox param2, Direction param3, MineshaftStructure.Type param4) {
            super(StructurePieceType.MINE_SHAFT_CORRIDOR, param0, param4, param2);
            this.setOrientation(param3);
            this.hasRails = param1.nextInt(3) == 0;
            this.spiderCorridor = !this.hasRails && param1.nextInt(23) == 0;
            if (this.getOrientation().getAxis() == Direction.Axis.Z) {
                this.numSections = param2.getZSpan() / 5;
            } else {
                this.numSections = param2.getXSpan() / 5;
            }

        }

        @Nullable
        public static BoundingBox findCorridorSize(StructurePieceAccessor param0, RandomSource param1, int param2, int param3, int param4, Direction param5) {
            for(int var0 = param1.nextInt(3) + 2; var0 > 0; --var0) {
                int var1 = var0 * 5;

                BoundingBox var5 = switch(param5) {
                    default -> new BoundingBox(0, 0, -(var1 - 1), 2, 2, 0);
                    case SOUTH -> new BoundingBox(0, 0, 0, 2, 2, var1 - 1);
                    case WEST -> new BoundingBox(-(var1 - 1), 0, 0, 0, 2, 2);
                    case EAST -> new BoundingBox(0, 0, 0, var1 - 1, 2, 2);
                };
                var5.move(param2, param3, param4);
                if (param0.findCollisionPiece(var5) == null) {
                    return var5;
                }
            }

            return null;
        }

        @Override
        public void addChildren(StructurePiece param0, StructurePieceAccessor param1, RandomSource param2) {
            int var0 = this.getGenDepth();
            int var1 = param2.nextInt(4);
            Direction var2 = this.getOrientation();
            if (var2 != null) {
                switch(var2) {
                    case NORTH:
                    default:
                        if (var1 <= 1) {
                            MineshaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.minX(),
                                this.boundingBox.minY() - 1 + param2.nextInt(3),
                                this.boundingBox.minZ() - 1,
                                var2,
                                var0
                            );
                        } else if (var1 == 2) {
                            MineshaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.minX() - 1,
                                this.boundingBox.minY() - 1 + param2.nextInt(3),
                                this.boundingBox.minZ(),
                                Direction.WEST,
                                var0
                            );
                        } else {
                            MineshaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.maxX() + 1,
                                this.boundingBox.minY() - 1 + param2.nextInt(3),
                                this.boundingBox.minZ(),
                                Direction.EAST,
                                var0
                            );
                        }
                        break;
                    case SOUTH:
                        if (var1 <= 1) {
                            MineshaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.minX(),
                                this.boundingBox.minY() - 1 + param2.nextInt(3),
                                this.boundingBox.maxZ() + 1,
                                var2,
                                var0
                            );
                        } else if (var1 == 2) {
                            MineshaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.minX() - 1,
                                this.boundingBox.minY() - 1 + param2.nextInt(3),
                                this.boundingBox.maxZ() - 3,
                                Direction.WEST,
                                var0
                            );
                        } else {
                            MineshaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.maxX() + 1,
                                this.boundingBox.minY() - 1 + param2.nextInt(3),
                                this.boundingBox.maxZ() - 3,
                                Direction.EAST,
                                var0
                            );
                        }
                        break;
                    case WEST:
                        if (var1 <= 1) {
                            MineshaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.minX() - 1,
                                this.boundingBox.minY() - 1 + param2.nextInt(3),
                                this.boundingBox.minZ(),
                                var2,
                                var0
                            );
                        } else if (var1 == 2) {
                            MineshaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.minX(),
                                this.boundingBox.minY() - 1 + param2.nextInt(3),
                                this.boundingBox.minZ() - 1,
                                Direction.NORTH,
                                var0
                            );
                        } else {
                            MineshaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.minX(),
                                this.boundingBox.minY() - 1 + param2.nextInt(3),
                                this.boundingBox.maxZ() + 1,
                                Direction.SOUTH,
                                var0
                            );
                        }
                        break;
                    case EAST:
                        if (var1 <= 1) {
                            MineshaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.maxX() + 1,
                                this.boundingBox.minY() - 1 + param2.nextInt(3),
                                this.boundingBox.minZ(),
                                var2,
                                var0
                            );
                        } else if (var1 == 2) {
                            MineshaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.maxX() - 3,
                                this.boundingBox.minY() - 1 + param2.nextInt(3),
                                this.boundingBox.minZ() - 1,
                                Direction.NORTH,
                                var0
                            );
                        } else {
                            MineshaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.maxX() - 3,
                                this.boundingBox.minY() - 1 + param2.nextInt(3),
                                this.boundingBox.maxZ() + 1,
                                Direction.SOUTH,
                                var0
                            );
                        }
                }
            }

            if (var0 < 8) {
                if (var2 != Direction.NORTH && var2 != Direction.SOUTH) {
                    for(int var5 = this.boundingBox.minX() + 3; var5 + 3 <= this.boundingBox.maxX(); var5 += 5) {
                        int var6 = param2.nextInt(5);
                        if (var6 == 0) {
                            MineshaftPieces.generateAndAddPiece(
                                param0, param1, param2, var5, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, var0 + 1
                            );
                        } else if (var6 == 1) {
                            MineshaftPieces.generateAndAddPiece(
                                param0, param1, param2, var5, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, var0 + 1
                            );
                        }
                    }
                } else {
                    for(int var3 = this.boundingBox.minZ() + 3; var3 + 3 <= this.boundingBox.maxZ(); var3 += 5) {
                        int var4 = param2.nextInt(5);
                        if (var4 == 0) {
                            MineshaftPieces.generateAndAddPiece(
                                param0, param1, param2, this.boundingBox.minX() - 1, this.boundingBox.minY(), var3, Direction.WEST, var0 + 1
                            );
                        } else if (var4 == 1) {
                            MineshaftPieces.generateAndAddPiece(
                                param0, param1, param2, this.boundingBox.maxX() + 1, this.boundingBox.minY(), var3, Direction.EAST, var0 + 1
                            );
                        }
                    }
                }
            }

        }

        @Override
        protected boolean createChest(
            WorldGenLevel param0, BoundingBox param1, RandomSource param2, int param3, int param4, int param5, ResourceLocation param6
        ) {
            BlockPos var0 = this.getWorldPos(param3, param4, param5);
            if (param1.isInside(var0) && param0.getBlockState(var0).isAir() && !param0.getBlockState(var0.below()).isAir()) {
                BlockState var1 = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, param2.nextBoolean() ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST);
                this.placeBlock(param0, var1, param3, param4, param5, param1);
                MinecartChest var2 = new MinecartChest(param0.getLevel(), (double)var0.getX() + 0.5, (double)var0.getY() + 0.5, (double)var0.getZ() + 0.5);
                var2.setLootTable(param6, param2.nextLong());
                param0.addFreshEntity(var2);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            if (!this.isInInvalidLocation(param0, param4)) {
                int var0 = 0;
                int var1 = 2;
                int var2 = 0;
                int var3 = 2;
                int var4 = this.numSections * 5 - 1;
                BlockState var5 = this.type.getPlanksState();
                this.generateBox(param0, param4, 0, 0, 0, 2, 1, var4, CAVE_AIR, CAVE_AIR, false);
                this.generateMaybeBox(param0, param4, param3, 0.8F, 0, 2, 0, 2, 2, var4, CAVE_AIR, CAVE_AIR, false, false);
                if (this.spiderCorridor) {
                    this.generateMaybeBox(param0, param4, param3, 0.6F, 0, 0, 0, 2, 1, var4, Blocks.COBWEB.defaultBlockState(), CAVE_AIR, false, true);
                }

                for(int var6 = 0; var6 < this.numSections; ++var6) {
                    int var7 = 2 + var6 * 5;
                    this.placeSupport(param0, param4, 0, 0, var7, 2, 2, param3);
                    this.maybePlaceCobWeb(param0, param4, param3, 0.1F, 0, 2, var7 - 1);
                    this.maybePlaceCobWeb(param0, param4, param3, 0.1F, 2, 2, var7 - 1);
                    this.maybePlaceCobWeb(param0, param4, param3, 0.1F, 0, 2, var7 + 1);
                    this.maybePlaceCobWeb(param0, param4, param3, 0.1F, 2, 2, var7 + 1);
                    this.maybePlaceCobWeb(param0, param4, param3, 0.05F, 0, 2, var7 - 2);
                    this.maybePlaceCobWeb(param0, param4, param3, 0.05F, 2, 2, var7 - 2);
                    this.maybePlaceCobWeb(param0, param4, param3, 0.05F, 0, 2, var7 + 2);
                    this.maybePlaceCobWeb(param0, param4, param3, 0.05F, 2, 2, var7 + 2);
                    if (param3.nextInt(100) == 0) {
                        this.createChest(param0, param4, param3, 2, 0, var7 - 1, BuiltInLootTables.ABANDONED_MINESHAFT);
                    }

                    if (param3.nextInt(100) == 0) {
                        this.createChest(param0, param4, param3, 0, 0, var7 + 1, BuiltInLootTables.ABANDONED_MINESHAFT);
                    }

                    if (this.spiderCorridor && !this.hasPlacedSpider) {
                        int var8 = 1;
                        int var9 = var7 - 1 + param3.nextInt(3);
                        BlockPos var10 = this.getWorldPos(1, 0, var9);
                        if (param4.isInside(var10) && this.isInterior(param0, 1, 0, var9, param4)) {
                            this.hasPlacedSpider = true;
                            param0.setBlock(var10, Blocks.SPAWNER.defaultBlockState(), 2);
                            BlockEntity var11 = param0.getBlockEntity(var10);
                            if (var11 instanceof SpawnerBlockEntity var12) {
                                var12.setEntityId(EntityType.CAVE_SPIDER, param3);
                            }
                        }
                    }
                }

                for(int var13 = 0; var13 <= 2; ++var13) {
                    for(int var14 = 0; var14 <= var4; ++var14) {
                        this.setPlanksBlock(param0, param4, var5, var13, -1, var14);
                    }
                }

                int var15 = 2;
                this.placeDoubleLowerOrUpperSupport(param0, param4, 0, -1, 2);
                if (this.numSections > 1) {
                    int var16 = var4 - 2;
                    this.placeDoubleLowerOrUpperSupport(param0, param4, 0, -1, var16);
                }

                if (this.hasRails) {
                    BlockState var17 = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_SOUTH);

                    for(int var18 = 0; var18 <= var4; ++var18) {
                        BlockState var19 = this.getBlock(param0, 1, -1, var18, param4);
                        if (!var19.isAir() && var19.isSolidRender(param0, this.getWorldPos(1, -1, var18))) {
                            float var20 = this.isInterior(param0, 1, 0, var18, param4) ? 0.7F : 0.9F;
                            this.maybeGenerateBlock(param0, param4, param3, var20, 1, 0, var18, var17);
                        }
                    }
                }

            }
        }

        private void placeDoubleLowerOrUpperSupport(WorldGenLevel param0, BoundingBox param1, int param2, int param3, int param4) {
            BlockState var0 = this.type.getWoodState();
            BlockState var1 = this.type.getPlanksState();
            if (this.getBlock(param0, param2, param3, param4, param1).is(var1.getBlock())) {
                this.fillPillarDownOrChainUp(param0, var0, param2, param3, param4, param1);
            }

            if (this.getBlock(param0, param2 + 2, param3, param4, param1).is(var1.getBlock())) {
                this.fillPillarDownOrChainUp(param0, var0, param2 + 2, param3, param4, param1);
            }

        }

        @Override
        protected void fillColumnDown(WorldGenLevel param0, BlockState param1, int param2, int param3, int param4, BoundingBox param5) {
            BlockPos.MutableBlockPos var0 = this.getWorldPos(param2, param3, param4);
            if (param5.isInside(var0)) {
                int var1 = var0.getY();

                while(this.isReplaceableByStructures(param0.getBlockState(var0)) && var0.getY() > param0.getMinBuildHeight() + 1) {
                    var0.move(Direction.DOWN);
                }

                if (this.canPlaceColumnOnTopOf(param0, var0, param0.getBlockState(var0))) {
                    while(var0.getY() < var1) {
                        var0.move(Direction.UP);
                        param0.setBlock(var0, param1, 2);
                    }

                }
            }
        }

        protected void fillPillarDownOrChainUp(WorldGenLevel param0, BlockState param1, int param2, int param3, int param4, BoundingBox param5) {
            BlockPos.MutableBlockPos var0 = this.getWorldPos(param2, param3, param4);
            if (param5.isInside(var0)) {
                int var1 = var0.getY();
                int var2 = 1;
                boolean var3 = true;

                for(boolean var4 = true; var3 || var4; ++var2) {
                    if (var3) {
                        var0.setY(var1 - var2);
                        BlockState var5 = param0.getBlockState(var0);
                        boolean var6 = this.isReplaceableByStructures(var5) && !var5.is(Blocks.LAVA);
                        if (!var6 && this.canPlaceColumnOnTopOf(param0, var0, var5)) {
                            fillColumnBetween(param0, param1, var0, var1 - var2 + 1, var1);
                            return;
                        }

                        var3 = var2 <= 20 && var6 && var0.getY() > param0.getMinBuildHeight() + 1;
                    }

                    if (var4) {
                        var0.setY(var1 + var2);
                        BlockState var7 = param0.getBlockState(var0);
                        boolean var8 = this.isReplaceableByStructures(var7);
                        if (!var8 && this.canHangChainBelow(param0, var0, var7)) {
                            param0.setBlock(var0.setY(var1 + 1), this.type.getFenceState(), 2);
                            fillColumnBetween(param0, Blocks.CHAIN.defaultBlockState(), var0, var1 + 2, var1 + var2);
                            return;
                        }

                        var4 = var2 <= 50 && var8 && var0.getY() < param0.getMaxBuildHeight() - 1;
                    }
                }

            }
        }

        private static void fillColumnBetween(WorldGenLevel param0, BlockState param1, BlockPos.MutableBlockPos param2, int param3, int param4) {
            for(int var0 = param3; var0 < param4; ++var0) {
                param0.setBlock(param2.setY(var0), param1, 2);
            }

        }

        private boolean canPlaceColumnOnTopOf(LevelReader param0, BlockPos param1, BlockState param2) {
            return param2.isFaceSturdy(param0, param1, Direction.UP);
        }

        private boolean canHangChainBelow(LevelReader param0, BlockPos param1, BlockState param2) {
            return Block.canSupportCenter(param0, param1, Direction.DOWN) && !(param2.getBlock() instanceof FallingBlock);
        }

        private void placeSupport(WorldGenLevel param0, BoundingBox param1, int param2, int param3, int param4, int param5, int param6, RandomSource param7) {
            if (this.isSupportingBox(param0, param1, param2, param6, param5, param4)) {
                BlockState var0 = this.type.getPlanksState();
                BlockState var1 = this.type.getFenceState();
                this.generateBox(
                    param0, param1, param2, param3, param4, param2, param5 - 1, param4, var1.setValue(FenceBlock.WEST, Boolean.valueOf(true)), CAVE_AIR, false
                );
                this.generateBox(
                    param0, param1, param6, param3, param4, param6, param5 - 1, param4, var1.setValue(FenceBlock.EAST, Boolean.valueOf(true)), CAVE_AIR, false
                );
                if (param7.nextInt(4) == 0) {
                    this.generateBox(param0, param1, param2, param5, param4, param2, param5, param4, var0, CAVE_AIR, false);
                    this.generateBox(param0, param1, param6, param5, param4, param6, param5, param4, var0, CAVE_AIR, false);
                } else {
                    this.generateBox(param0, param1, param2, param5, param4, param6, param5, param4, var0, CAVE_AIR, false);
                    this.maybeGenerateBlock(
                        param0,
                        param1,
                        param7,
                        0.05F,
                        param2 + 1,
                        param5,
                        param4 - 1,
                        Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH)
                    );
                    this.maybeGenerateBlock(
                        param0,
                        param1,
                        param7,
                        0.05F,
                        param2 + 1,
                        param5,
                        param4 + 1,
                        Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH)
                    );
                }

            }
        }

        private void maybePlaceCobWeb(WorldGenLevel param0, BoundingBox param1, RandomSource param2, float param3, int param4, int param5, int param6) {
            if (this.isInterior(param0, param4, param5, param6, param1)
                && param2.nextFloat() < param3
                && this.hasSturdyNeighbours(param0, param1, param4, param5, param6, 2)) {
                this.placeBlock(param0, Blocks.COBWEB.defaultBlockState(), param4, param5, param6, param1);
            }

        }

        private boolean hasSturdyNeighbours(WorldGenLevel param0, BoundingBox param1, int param2, int param3, int param4, int param5) {
            BlockPos.MutableBlockPos var0 = this.getWorldPos(param2, param3, param4);
            int var1 = 0;

            for(Direction var2 : Direction.values()) {
                var0.move(var2);
                if (param1.isInside(var0) && param0.getBlockState(var0).isFaceSturdy(param0, var0, var2.getOpposite())) {
                    if (++var1 >= param5) {
                        return true;
                    }
                }

                var0.move(var2.getOpposite());
            }

            return false;
        }
    }

    public static class MineShaftCrossing extends MineshaftPieces.MineShaftPiece {
        private final Direction direction;
        private final boolean isTwoFloored;

        public MineShaftCrossing(CompoundTag param0) {
            super(StructurePieceType.MINE_SHAFT_CROSSING, param0);
            this.isTwoFloored = param0.getBoolean("tf");
            this.direction = Direction.from2DDataValue(param0.getInt("D"));
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext param0, CompoundTag param1) {
            super.addAdditionalSaveData(param0, param1);
            param1.putBoolean("tf", this.isTwoFloored);
            param1.putInt("D", this.direction.get2DDataValue());
        }

        public MineShaftCrossing(int param0, BoundingBox param1, @Nullable Direction param2, MineshaftStructure.Type param3) {
            super(StructurePieceType.MINE_SHAFT_CROSSING, param0, param3, param1);
            this.direction = param2;
            this.isTwoFloored = param1.getYSpan() > 3;
        }

        @Nullable
        public static BoundingBox findCrossing(StructurePieceAccessor param0, RandomSource param1, int param2, int param3, int param4, Direction param5) {
            int var0;
            if (param1.nextInt(4) == 0) {
                var0 = 6;
            } else {
                var0 = 2;
            }

            BoundingBox var5 = switch(param5) {
                default -> new BoundingBox(-1, 0, -4, 3, var0, 0);
                case SOUTH -> new BoundingBox(-1, 0, 0, 3, var0, 4);
                case WEST -> new BoundingBox(-4, 0, -1, 0, var0, 3);
                case EAST -> new BoundingBox(0, 0, -1, 4, var0, 3);
            };
            var5.move(param2, param3, param4);
            return param0.findCollisionPiece(var5) != null ? null : var5;
        }

        @Override
        public void addChildren(StructurePiece param0, StructurePieceAccessor param1, RandomSource param2) {
            int var0 = this.getGenDepth();
            switch(this.direction) {
                case NORTH:
                default:
                    MineshaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, var0
                    );
                    MineshaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, var0
                    );
                    MineshaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, var0
                    );
                    break;
                case SOUTH:
                    MineshaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, var0
                    );
                    MineshaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, var0
                    );
                    MineshaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, var0
                    );
                    break;
                case WEST:
                    MineshaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, var0
                    );
                    MineshaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, var0
                    );
                    MineshaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, var0
                    );
                    break;
                case EAST:
                    MineshaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, var0
                    );
                    MineshaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, var0
                    );
                    MineshaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, var0
                    );
            }

            if (this.isTwoFloored) {
                if (param2.nextBoolean()) {
                    MineshaftPieces.generateAndAddPiece(
                        param0,
                        param1,
                        param2,
                        this.boundingBox.minX() + 1,
                        this.boundingBox.minY() + 3 + 1,
                        this.boundingBox.minZ() - 1,
                        Direction.NORTH,
                        var0
                    );
                }

                if (param2.nextBoolean()) {
                    MineshaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.minX() - 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() + 1, Direction.WEST, var0
                    );
                }

                if (param2.nextBoolean()) {
                    MineshaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.maxX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() + 1, Direction.EAST, var0
                    );
                }

                if (param2.nextBoolean()) {
                    MineshaftPieces.generateAndAddPiece(
                        param0,
                        param1,
                        param2,
                        this.boundingBox.minX() + 1,
                        this.boundingBox.minY() + 3 + 1,
                        this.boundingBox.maxZ() + 1,
                        Direction.SOUTH,
                        var0
                    );
                }
            }

        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            if (!this.isInInvalidLocation(param0, param4)) {
                BlockState var0 = this.type.getPlanksState();
                if (this.isTwoFloored) {
                    this.generateBox(
                        param0,
                        param4,
                        this.boundingBox.minX() + 1,
                        this.boundingBox.minY(),
                        this.boundingBox.minZ(),
                        this.boundingBox.maxX() - 1,
                        this.boundingBox.minY() + 3 - 1,
                        this.boundingBox.maxZ(),
                        CAVE_AIR,
                        CAVE_AIR,
                        false
                    );
                    this.generateBox(
                        param0,
                        param4,
                        this.boundingBox.minX(),
                        this.boundingBox.minY(),
                        this.boundingBox.minZ() + 1,
                        this.boundingBox.maxX(),
                        this.boundingBox.minY() + 3 - 1,
                        this.boundingBox.maxZ() - 1,
                        CAVE_AIR,
                        CAVE_AIR,
                        false
                    );
                    this.generateBox(
                        param0,
                        param4,
                        this.boundingBox.minX() + 1,
                        this.boundingBox.maxY() - 2,
                        this.boundingBox.minZ(),
                        this.boundingBox.maxX() - 1,
                        this.boundingBox.maxY(),
                        this.boundingBox.maxZ(),
                        CAVE_AIR,
                        CAVE_AIR,
                        false
                    );
                    this.generateBox(
                        param0,
                        param4,
                        this.boundingBox.minX(),
                        this.boundingBox.maxY() - 2,
                        this.boundingBox.minZ() + 1,
                        this.boundingBox.maxX(),
                        this.boundingBox.maxY(),
                        this.boundingBox.maxZ() - 1,
                        CAVE_AIR,
                        CAVE_AIR,
                        false
                    );
                    this.generateBox(
                        param0,
                        param4,
                        this.boundingBox.minX() + 1,
                        this.boundingBox.minY() + 3,
                        this.boundingBox.minZ() + 1,
                        this.boundingBox.maxX() - 1,
                        this.boundingBox.minY() + 3,
                        this.boundingBox.maxZ() - 1,
                        CAVE_AIR,
                        CAVE_AIR,
                        false
                    );
                } else {
                    this.generateBox(
                        param0,
                        param4,
                        this.boundingBox.minX() + 1,
                        this.boundingBox.minY(),
                        this.boundingBox.minZ(),
                        this.boundingBox.maxX() - 1,
                        this.boundingBox.maxY(),
                        this.boundingBox.maxZ(),
                        CAVE_AIR,
                        CAVE_AIR,
                        false
                    );
                    this.generateBox(
                        param0,
                        param4,
                        this.boundingBox.minX(),
                        this.boundingBox.minY(),
                        this.boundingBox.minZ() + 1,
                        this.boundingBox.maxX(),
                        this.boundingBox.maxY(),
                        this.boundingBox.maxZ() - 1,
                        CAVE_AIR,
                        CAVE_AIR,
                        false
                    );
                }

                this.placeSupportPillar(
                    param0, param4, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxY()
                );
                this.placeSupportPillar(
                    param0, param4, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() - 1, this.boundingBox.maxY()
                );
                this.placeSupportPillar(
                    param0, param4, this.boundingBox.maxX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxY()
                );
                this.placeSupportPillar(
                    param0, param4, this.boundingBox.maxX() - 1, this.boundingBox.minY(), this.boundingBox.maxZ() - 1, this.boundingBox.maxY()
                );
                int var1 = this.boundingBox.minY() - 1;

                for(int var2 = this.boundingBox.minX(); var2 <= this.boundingBox.maxX(); ++var2) {
                    for(int var3 = this.boundingBox.minZ(); var3 <= this.boundingBox.maxZ(); ++var3) {
                        this.setPlanksBlock(param0, param4, var0, var2, var1, var3);
                    }
                }

            }
        }

        private void placeSupportPillar(WorldGenLevel param0, BoundingBox param1, int param2, int param3, int param4, int param5) {
            if (!this.getBlock(param0, param2, param5 + 1, param4, param1).isAir()) {
                this.generateBox(param0, param1, param2, param3, param4, param2, param5, param4, this.type.getPlanksState(), CAVE_AIR, false);
            }

        }
    }

    abstract static class MineShaftPiece extends StructurePiece {
        protected MineshaftStructure.Type type;

        public MineShaftPiece(StructurePieceType param0, int param1, MineshaftStructure.Type param2, BoundingBox param3) {
            super(param0, param1, param3);
            this.type = param2;
        }

        public MineShaftPiece(StructurePieceType param0, CompoundTag param1) {
            super(param0, param1);
            this.type = MineshaftStructure.Type.byId(param1.getInt("MST"));
        }

        @Override
        protected boolean canBeReplaced(LevelReader param0, int param1, int param2, int param3, BoundingBox param4) {
            BlockState var0 = this.getBlock(param0, param1, param2, param3, param4);
            return !var0.is(this.type.getPlanksState().getBlock())
                && !var0.is(this.type.getWoodState().getBlock())
                && !var0.is(this.type.getFenceState().getBlock())
                && !var0.is(Blocks.CHAIN);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext param0, CompoundTag param1) {
            param1.putInt("MST", this.type.ordinal());
        }

        protected boolean isSupportingBox(BlockGetter param0, BoundingBox param1, int param2, int param3, int param4, int param5) {
            for(int var0 = param2; var0 <= param3; ++var0) {
                if (this.getBlock(param0, var0, param4 + 1, param5, param1).isAir()) {
                    return false;
                }
            }

            return true;
        }

        protected boolean isInInvalidLocation(LevelAccessor param0, BoundingBox param1) {
            int var0 = Math.max(this.boundingBox.minX() - 1, param1.minX());
            int var1 = Math.max(this.boundingBox.minY() - 1, param1.minY());
            int var2 = Math.max(this.boundingBox.minZ() - 1, param1.minZ());
            int var3 = Math.min(this.boundingBox.maxX() + 1, param1.maxX());
            int var4 = Math.min(this.boundingBox.maxY() + 1, param1.maxY());
            int var5 = Math.min(this.boundingBox.maxZ() + 1, param1.maxZ());
            BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos((var0 + var3) / 2, (var1 + var4) / 2, (var2 + var5) / 2);
            if (param0.getBiome(var6).is(BiomeTags.MINESHAFT_BLOCKING)) {
                return true;
            } else {
                for(int var7 = var0; var7 <= var3; ++var7) {
                    for(int var8 = var2; var8 <= var5; ++var8) {
                        if (param0.getBlockState(var6.set(var7, var1, var8)).liquid()) {
                            return true;
                        }

                        if (param0.getBlockState(var6.set(var7, var4, var8)).liquid()) {
                            return true;
                        }
                    }
                }

                for(int var9 = var0; var9 <= var3; ++var9) {
                    for(int var10 = var1; var10 <= var4; ++var10) {
                        if (param0.getBlockState(var6.set(var9, var10, var2)).liquid()) {
                            return true;
                        }

                        if (param0.getBlockState(var6.set(var9, var10, var5)).liquid()) {
                            return true;
                        }
                    }
                }

                for(int var11 = var2; var11 <= var5; ++var11) {
                    for(int var12 = var1; var12 <= var4; ++var12) {
                        if (param0.getBlockState(var6.set(var0, var12, var11)).liquid()) {
                            return true;
                        }

                        if (param0.getBlockState(var6.set(var3, var12, var11)).liquid()) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }

        protected void setPlanksBlock(WorldGenLevel param0, BoundingBox param1, BlockState param2, int param3, int param4, int param5) {
            if (this.isInterior(param0, param3, param4, param5, param1)) {
                BlockPos var0 = this.getWorldPos(param3, param4, param5);
                BlockState var1 = param0.getBlockState(var0);
                if (!var1.isFaceSturdy(param0, var0, Direction.UP)) {
                    param0.setBlock(var0, param2, 2);
                }

            }
        }
    }

    public static class MineShaftRoom extends MineshaftPieces.MineShaftPiece {
        private final List<BoundingBox> childEntranceBoxes = Lists.newLinkedList();

        public MineShaftRoom(int param0, RandomSource param1, int param2, int param3, MineshaftStructure.Type param4) {
            super(
                StructurePieceType.MINE_SHAFT_ROOM,
                param0,
                param4,
                new BoundingBox(param2, 50, param3, param2 + 7 + param1.nextInt(6), 54 + param1.nextInt(6), param3 + 7 + param1.nextInt(6))
            );
            this.type = param4;
        }

        public MineShaftRoom(CompoundTag param0) {
            super(StructurePieceType.MINE_SHAFT_ROOM, param0);
            BoundingBox.CODEC
                .listOf()
                .parse(NbtOps.INSTANCE, param0.getList("Entrances", 11))
                .resultOrPartial(MineshaftPieces.LOGGER::error)
                .ifPresent(this.childEntranceBoxes::addAll);
        }

        @Override
        public void addChildren(StructurePiece param0, StructurePieceAccessor param1, RandomSource param2) {
            int var0 = this.getGenDepth();
            int var1 = this.boundingBox.getYSpan() - 3 - 1;
            if (var1 <= 0) {
                var1 = 1;
            }

            int var2;
            for(var2 = 0; var2 < this.boundingBox.getXSpan(); var2 += 4) {
                var2 += param2.nextInt(this.boundingBox.getXSpan());
                if (var2 + 3 > this.boundingBox.getXSpan()) {
                    break;
                }

                MineshaftPieces.MineShaftPiece var3 = MineshaftPieces.generateAndAddPiece(
                    param0,
                    param1,
                    param2,
                    this.boundingBox.minX() + var2,
                    this.boundingBox.minY() + param2.nextInt(var1) + 1,
                    this.boundingBox.minZ() - 1,
                    Direction.NORTH,
                    var0
                );
                if (var3 != null) {
                    BoundingBox var4 = var3.getBoundingBox();
                    this.childEntranceBoxes
                        .add(new BoundingBox(var4.minX(), var4.minY(), this.boundingBox.minZ(), var4.maxX(), var4.maxY(), this.boundingBox.minZ() + 1));
                }
            }

            for(var2 = 0; var2 < this.boundingBox.getXSpan(); var2 += 4) {
                var2 += param2.nextInt(this.boundingBox.getXSpan());
                if (var2 + 3 > this.boundingBox.getXSpan()) {
                    break;
                }

                MineshaftPieces.MineShaftPiece var5 = MineshaftPieces.generateAndAddPiece(
                    param0,
                    param1,
                    param2,
                    this.boundingBox.minX() + var2,
                    this.boundingBox.minY() + param2.nextInt(var1) + 1,
                    this.boundingBox.maxZ() + 1,
                    Direction.SOUTH,
                    var0
                );
                if (var5 != null) {
                    BoundingBox var6 = var5.getBoundingBox();
                    this.childEntranceBoxes
                        .add(new BoundingBox(var6.minX(), var6.minY(), this.boundingBox.maxZ() - 1, var6.maxX(), var6.maxY(), this.boundingBox.maxZ()));
                }
            }

            for(var2 = 0; var2 < this.boundingBox.getZSpan(); var2 += 4) {
                var2 += param2.nextInt(this.boundingBox.getZSpan());
                if (var2 + 3 > this.boundingBox.getZSpan()) {
                    break;
                }

                MineshaftPieces.MineShaftPiece var7 = MineshaftPieces.generateAndAddPiece(
                    param0,
                    param1,
                    param2,
                    this.boundingBox.minX() - 1,
                    this.boundingBox.minY() + param2.nextInt(var1) + 1,
                    this.boundingBox.minZ() + var2,
                    Direction.WEST,
                    var0
                );
                if (var7 != null) {
                    BoundingBox var8 = var7.getBoundingBox();
                    this.childEntranceBoxes
                        .add(new BoundingBox(this.boundingBox.minX(), var8.minY(), var8.minZ(), this.boundingBox.minX() + 1, var8.maxY(), var8.maxZ()));
                }
            }

            for(var2 = 0; var2 < this.boundingBox.getZSpan(); var2 += 4) {
                var2 += param2.nextInt(this.boundingBox.getZSpan());
                if (var2 + 3 > this.boundingBox.getZSpan()) {
                    break;
                }

                StructurePiece var9 = MineshaftPieces.generateAndAddPiece(
                    param0,
                    param1,
                    param2,
                    this.boundingBox.maxX() + 1,
                    this.boundingBox.minY() + param2.nextInt(var1) + 1,
                    this.boundingBox.minZ() + var2,
                    Direction.EAST,
                    var0
                );
                if (var9 != null) {
                    BoundingBox var10 = var9.getBoundingBox();
                    this.childEntranceBoxes
                        .add(new BoundingBox(this.boundingBox.maxX() - 1, var10.minY(), var10.minZ(), this.boundingBox.maxX(), var10.maxY(), var10.maxZ()));
                }
            }

        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            if (!this.isInInvalidLocation(param0, param4)) {
                this.generateBox(
                    param0,
                    param4,
                    this.boundingBox.minX(),
                    this.boundingBox.minY() + 1,
                    this.boundingBox.minZ(),
                    this.boundingBox.maxX(),
                    Math.min(this.boundingBox.minY() + 3, this.boundingBox.maxY()),
                    this.boundingBox.maxZ(),
                    CAVE_AIR,
                    CAVE_AIR,
                    false
                );

                for(BoundingBox var0 : this.childEntranceBoxes) {
                    this.generateBox(
                        param0, param4, var0.minX(), var0.maxY() - 2, var0.minZ(), var0.maxX(), var0.maxY(), var0.maxZ(), CAVE_AIR, CAVE_AIR, false
                    );
                }

                this.generateUpperHalfSphere(
                    param0,
                    param4,
                    this.boundingBox.minX(),
                    this.boundingBox.minY() + 4,
                    this.boundingBox.minZ(),
                    this.boundingBox.maxX(),
                    this.boundingBox.maxY(),
                    this.boundingBox.maxZ(),
                    CAVE_AIR,
                    false
                );
            }
        }

        @Override
        public void move(int param0, int param1, int param2) {
            super.move(param0, param1, param2);

            for(BoundingBox var0 : this.childEntranceBoxes) {
                var0.move(param0, param1, param2);
            }

        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext param0, CompoundTag param1) {
            super.addAdditionalSaveData(param0, param1);
            BoundingBox.CODEC
                .listOf()
                .encodeStart(NbtOps.INSTANCE, this.childEntranceBoxes)
                .resultOrPartial(MineshaftPieces.LOGGER::error)
                .ifPresent(param1x -> param1.put("Entrances", param1x));
        }
    }

    public static class MineShaftStairs extends MineshaftPieces.MineShaftPiece {
        public MineShaftStairs(int param0, BoundingBox param1, Direction param2, MineshaftStructure.Type param3) {
            super(StructurePieceType.MINE_SHAFT_STAIRS, param0, param3, param1);
            this.setOrientation(param2);
        }

        public MineShaftStairs(CompoundTag param0) {
            super(StructurePieceType.MINE_SHAFT_STAIRS, param0);
        }

        @Nullable
        public static BoundingBox findStairs(StructurePieceAccessor param0, RandomSource param1, int param2, int param3, int param4, Direction param5) {
            BoundingBox var3 = switch(param5) {
                default -> new BoundingBox(0, -5, -8, 2, 2, 0);
                case SOUTH -> new BoundingBox(0, -5, 0, 2, 2, 8);
                case WEST -> new BoundingBox(-8, -5, 0, 0, 2, 2);
                case EAST -> new BoundingBox(0, -5, 0, 8, 2, 2);
            };
            var3.move(param2, param3, param4);
            return param0.findCollisionPiece(var3) != null ? null : var3;
        }

        @Override
        public void addChildren(StructurePiece param0, StructurePieceAccessor param1, RandomSource param2) {
            int var0 = this.getGenDepth();
            Direction var1 = this.getOrientation();
            if (var1 != null) {
                switch(var1) {
                    case NORTH:
                    default:
                        MineshaftPieces.generateAndAddPiece(
                            param0, param1, param2, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, var0
                        );
                        break;
                    case SOUTH:
                        MineshaftPieces.generateAndAddPiece(
                            param0, param1, param2, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, var0
                        );
                        break;
                    case WEST:
                        MineshaftPieces.generateAndAddPiece(
                            param0, param1, param2, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ(), Direction.WEST, var0
                        );
                        break;
                    case EAST:
                        MineshaftPieces.generateAndAddPiece(
                            param0, param1, param2, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ(), Direction.EAST, var0
                        );
                }
            }

        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            if (!this.isInInvalidLocation(param0, param4)) {
                this.generateBox(param0, param4, 0, 5, 0, 2, 7, 1, CAVE_AIR, CAVE_AIR, false);
                this.generateBox(param0, param4, 0, 0, 7, 2, 2, 8, CAVE_AIR, CAVE_AIR, false);

                for(int var0 = 0; var0 < 5; ++var0) {
                    this.generateBox(param0, param4, 0, 5 - var0 - (var0 < 4 ? 1 : 0), 2 + var0, 2, 7 - var0, 2 + var0, CAVE_AIR, CAVE_AIR, false);
                }

            }
        }
    }
}
