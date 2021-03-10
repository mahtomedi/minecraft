package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureFeatureManager;
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
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MineShaftPieces {
    private static final Logger LOGGER = LogManager.getLogger();

    private static MineShaftPieces.MineShaftPiece createRandomShaftPiece(
        List<StructurePiece> param0, Random param1, int param2, int param3, int param4, @Nullable Direction param5, int param6, MineshaftFeature.Type param7
    ) {
        int var0 = param1.nextInt(100);
        if (var0 >= 80) {
            BoundingBox var1 = MineShaftPieces.MineShaftCrossing.findCrossing(param0, param1, param2, param3, param4, param5);
            if (var1 != null) {
                return new MineShaftPieces.MineShaftCrossing(param6, var1, param5, param7);
            }
        } else if (var0 >= 70) {
            BoundingBox var2 = MineShaftPieces.MineShaftStairs.findStairs(param0, param1, param2, param3, param4, param5);
            if (var2 != null) {
                return new MineShaftPieces.MineShaftStairs(param6, var2, param5, param7);
            }
        } else {
            BoundingBox var3 = MineShaftPieces.MineShaftCorridor.findCorridorSize(param0, param1, param2, param3, param4, param5);
            if (var3 != null) {
                return new MineShaftPieces.MineShaftCorridor(param6, param1, var3, param5, param7);
            }
        }

        return null;
    }

    private static MineShaftPieces.MineShaftPiece generateAndAddPiece(
        StructurePiece param0, List<StructurePiece> param1, Random param2, int param3, int param4, int param5, Direction param6, int param7
    ) {
        if (param7 > 8) {
            return null;
        } else if (Math.abs(param3 - param0.getBoundingBox().x0) <= 80 && Math.abs(param5 - param0.getBoundingBox().z0) <= 80) {
            MineshaftFeature.Type var0 = ((MineShaftPieces.MineShaftPiece)param0).type;
            MineShaftPieces.MineShaftPiece var1 = createRandomShaftPiece(param1, param2, param3, param4, param5, param6, param7 + 1, var0);
            if (var1 != null) {
                param1.add(var1);
                var1.addChildren(param0, param1, param2);
            }

            return var1;
        } else {
            return null;
        }
    }

    public static class MineShaftCorridor extends MineShaftPieces.MineShaftPiece {
        private final boolean hasRails;
        private final boolean spiderCorridor;
        private boolean hasPlacedSpider;
        private final int numSections;

        public MineShaftCorridor(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.MINE_SHAFT_CORRIDOR, param1);
            this.hasRails = param1.getBoolean("hr");
            this.spiderCorridor = param1.getBoolean("sc");
            this.hasPlacedSpider = param1.getBoolean("hps");
            this.numSections = param1.getInt("Num");
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag param0) {
            super.addAdditionalSaveData(param0);
            param0.putBoolean("hr", this.hasRails);
            param0.putBoolean("sc", this.spiderCorridor);
            param0.putBoolean("hps", this.hasPlacedSpider);
            param0.putInt("Num", this.numSections);
        }

        public MineShaftCorridor(int param0, Random param1, BoundingBox param2, Direction param3, MineshaftFeature.Type param4) {
            super(StructurePieceType.MINE_SHAFT_CORRIDOR, param0, param4);
            this.setOrientation(param3);
            this.boundingBox = param2;
            this.hasRails = param1.nextInt(3) == 0;
            this.spiderCorridor = !this.hasRails && param1.nextInt(23) == 0;
            if (this.getOrientation().getAxis() == Direction.Axis.Z) {
                this.numSections = param2.getZSpan() / 5;
            } else {
                this.numSections = param2.getXSpan() / 5;
            }

        }

        public static BoundingBox findCorridorSize(List<StructurePiece> param0, Random param1, int param2, int param3, int param4, Direction param5) {
            BoundingBox var0 = new BoundingBox(param2, param3, param4, param2, param3 + 3 - 1, param4);

            int var1;
            for(var1 = param1.nextInt(3) + 2; var1 > 0; --var1) {
                int var2 = var1 * 5;
                switch(param5) {
                    case NORTH:
                    default:
                        var0.x1 = param2 + 3 - 1;
                        var0.z0 = param4 - (var2 - 1);
                        break;
                    case SOUTH:
                        var0.x1 = param2 + 3 - 1;
                        var0.z1 = param4 + var2 - 1;
                        break;
                    case WEST:
                        var0.x0 = param2 - (var2 - 1);
                        var0.z1 = param4 + 3 - 1;
                        break;
                    case EAST:
                        var0.x1 = param2 + var2 - 1;
                        var0.z1 = param4 + 3 - 1;
                }

                if (StructurePiece.findCollisionPiece(param0, var0) == null) {
                    break;
                }
            }

            return var1 > 0 ? var0 : null;
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
            int var0 = this.getGenDepth();
            int var1 = param2.nextInt(4);
            Direction var2 = this.getOrientation();
            if (var2 != null) {
                switch(var2) {
                    case NORTH:
                    default:
                        if (var1 <= 1) {
                            MineShaftPieces.generateAndAddPiece(
                                param0, param1, param2, this.boundingBox.x0, this.boundingBox.y0 - 1 + param2.nextInt(3), this.boundingBox.z0 - 1, var2, var0
                            );
                        } else if (var1 == 2) {
                            MineShaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.x0 - 1,
                                this.boundingBox.y0 - 1 + param2.nextInt(3),
                                this.boundingBox.z0,
                                Direction.WEST,
                                var0
                            );
                        } else {
                            MineShaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.x1 + 1,
                                this.boundingBox.y0 - 1 + param2.nextInt(3),
                                this.boundingBox.z0,
                                Direction.EAST,
                                var0
                            );
                        }
                        break;
                    case SOUTH:
                        if (var1 <= 1) {
                            MineShaftPieces.generateAndAddPiece(
                                param0, param1, param2, this.boundingBox.x0, this.boundingBox.y0 - 1 + param2.nextInt(3), this.boundingBox.z1 + 1, var2, var0
                            );
                        } else if (var1 == 2) {
                            MineShaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.x0 - 1,
                                this.boundingBox.y0 - 1 + param2.nextInt(3),
                                this.boundingBox.z1 - 3,
                                Direction.WEST,
                                var0
                            );
                        } else {
                            MineShaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.x1 + 1,
                                this.boundingBox.y0 - 1 + param2.nextInt(3),
                                this.boundingBox.z1 - 3,
                                Direction.EAST,
                                var0
                            );
                        }
                        break;
                    case WEST:
                        if (var1 <= 1) {
                            MineShaftPieces.generateAndAddPiece(
                                param0, param1, param2, this.boundingBox.x0 - 1, this.boundingBox.y0 - 1 + param2.nextInt(3), this.boundingBox.z0, var2, var0
                            );
                        } else if (var1 == 2) {
                            MineShaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.x0,
                                this.boundingBox.y0 - 1 + param2.nextInt(3),
                                this.boundingBox.z0 - 1,
                                Direction.NORTH,
                                var0
                            );
                        } else {
                            MineShaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.x0,
                                this.boundingBox.y0 - 1 + param2.nextInt(3),
                                this.boundingBox.z1 + 1,
                                Direction.SOUTH,
                                var0
                            );
                        }
                        break;
                    case EAST:
                        if (var1 <= 1) {
                            MineShaftPieces.generateAndAddPiece(
                                param0, param1, param2, this.boundingBox.x1 + 1, this.boundingBox.y0 - 1 + param2.nextInt(3), this.boundingBox.z0, var2, var0
                            );
                        } else if (var1 == 2) {
                            MineShaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.x1 - 3,
                                this.boundingBox.y0 - 1 + param2.nextInt(3),
                                this.boundingBox.z0 - 1,
                                Direction.NORTH,
                                var0
                            );
                        } else {
                            MineShaftPieces.generateAndAddPiece(
                                param0,
                                param1,
                                param2,
                                this.boundingBox.x1 - 3,
                                this.boundingBox.y0 - 1 + param2.nextInt(3),
                                this.boundingBox.z1 + 1,
                                Direction.SOUTH,
                                var0
                            );
                        }
                }
            }

            if (var0 < 8) {
                if (var2 != Direction.NORTH && var2 != Direction.SOUTH) {
                    for(int var5 = this.boundingBox.x0 + 3; var5 + 3 <= this.boundingBox.x1; var5 += 5) {
                        int var6 = param2.nextInt(5);
                        if (var6 == 0) {
                            MineShaftPieces.generateAndAddPiece(
                                param0, param1, param2, var5, this.boundingBox.y0, this.boundingBox.z0 - 1, Direction.NORTH, var0 + 1
                            );
                        } else if (var6 == 1) {
                            MineShaftPieces.generateAndAddPiece(
                                param0, param1, param2, var5, this.boundingBox.y0, this.boundingBox.z1 + 1, Direction.SOUTH, var0 + 1
                            );
                        }
                    }
                } else {
                    for(int var3 = this.boundingBox.z0 + 3; var3 + 3 <= this.boundingBox.z1; var3 += 5) {
                        int var4 = param2.nextInt(5);
                        if (var4 == 0) {
                            MineShaftPieces.generateAndAddPiece(
                                param0, param1, param2, this.boundingBox.x0 - 1, this.boundingBox.y0, var3, Direction.WEST, var0 + 1
                            );
                        } else if (var4 == 1) {
                            MineShaftPieces.generateAndAddPiece(
                                param0, param1, param2, this.boundingBox.x1 + 1, this.boundingBox.y0, var3, Direction.EAST, var0 + 1
                            );
                        }
                    }
                }
            }

        }

        @Override
        protected boolean createChest(WorldGenLevel param0, BoundingBox param1, Random param2, int param3, int param4, int param5, ResourceLocation param6) {
            BlockPos var0 = new BlockPos(this.getWorldX(param3, param5), this.getWorldY(param4), this.getWorldZ(param3, param5));
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
        public boolean postProcess(
            WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            if (this.edgesLiquid(param0, param4)) {
                return false;
            } else {
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
                    this.placeCobWeb(param0, param4, param3, 0.1F, 0, 2, var7 - 1);
                    this.placeCobWeb(param0, param4, param3, 0.1F, 2, 2, var7 - 1);
                    this.placeCobWeb(param0, param4, param3, 0.1F, 0, 2, var7 + 1);
                    this.placeCobWeb(param0, param4, param3, 0.1F, 2, 2, var7 + 1);
                    this.placeCobWeb(param0, param4, param3, 0.05F, 0, 2, var7 - 2);
                    this.placeCobWeb(param0, param4, param3, 0.05F, 2, 2, var7 - 2);
                    this.placeCobWeb(param0, param4, param3, 0.05F, 0, 2, var7 + 2);
                    this.placeCobWeb(param0, param4, param3, 0.05F, 2, 2, var7 + 2);
                    if (param3.nextInt(100) == 0) {
                        this.createChest(param0, param4, param3, 2, 0, var7 - 1, BuiltInLootTables.ABANDONED_MINESHAFT);
                    }

                    if (param3.nextInt(100) == 0) {
                        this.createChest(param0, param4, param3, 0, 0, var7 + 1, BuiltInLootTables.ABANDONED_MINESHAFT);
                    }

                    if (this.spiderCorridor && !this.hasPlacedSpider) {
                        int var8 = this.getWorldY(0);
                        int var9 = var7 - 1 + param3.nextInt(3);
                        int var10 = this.getWorldX(1, var9);
                        int var11 = this.getWorldZ(1, var9);
                        BlockPos var12 = new BlockPos(var10, var8, var11);
                        if (param4.isInside(var12) && this.isInterior(param0, 1, 0, var9, param4)) {
                            this.hasPlacedSpider = true;
                            param0.setBlock(var12, Blocks.SPAWNER.defaultBlockState(), 2);
                            BlockEntity var13 = param0.getBlockEntity(var12);
                            if (var13 instanceof SpawnerBlockEntity) {
                                ((SpawnerBlockEntity)var13).getSpawner().setEntityId(EntityType.CAVE_SPIDER);
                            }
                        }
                    }
                }

                for(int var14 = 0; var14 <= 2; ++var14) {
                    for(int var15 = 0; var15 <= var4; ++var15) {
                        this.setPlanksBlock(param0, param4, var5, var14, -1, var15);
                    }
                }

                int var16 = 2;
                this.placeDoubleLowerOrUpperSupport(param0, param4, 0, -1, 2);
                if (this.numSections > 1) {
                    int var17 = var4 - 2;
                    this.placeDoubleLowerOrUpperSupport(param0, param4, 0, -1, var17);
                }

                if (this.hasRails) {
                    BlockState var18 = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_SOUTH);

                    for(int var19 = 0; var19 <= var4; ++var19) {
                        BlockState var20 = this.getBlock(param0, 1, -1, var19, param4);
                        if (!var20.isAir() && var20.isSolidRender(param0, new BlockPos(this.getWorldX(1, var19), this.getWorldY(-1), this.getWorldZ(1, var19)))
                            )
                         {
                            float var21 = this.isInterior(param0, 1, 0, var19, param4) ? 0.7F : 0.9F;
                            this.maybeGenerateBlock(param0, param4, param3, var21, 1, 0, var19, var18, false);
                        }
                    }
                }

                return true;
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
            int var0 = this.getWorldX(param2, param4);
            int var1 = this.getWorldY(param3);
            int var2 = this.getWorldZ(param2, param4);
            BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos(var0, var1, var2);
            if (param5.isInside(var3)) {
                while(this.isReplaceableByStructures(param0.getBlockState(var3)) && var3.getY() > param0.getMinBuildHeight() + 1) {
                    var3.move(Direction.DOWN);
                }

                if (this.canPlaceColumnOnTopOf(param0.getBlockState(var3))) {
                    while(var3.getY() < var1) {
                        var3.move(Direction.UP);
                        param0.setBlock(var3, param1, 2);
                    }

                }
            }
        }

        protected void fillPillarDownOrChainUp(WorldGenLevel param0, BlockState param1, int param2, int param3, int param4, BoundingBox param5) {
            int var0 = this.getWorldX(param2, param4);
            int var1 = this.getWorldY(param3);
            int var2 = this.getWorldZ(param2, param4);
            BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos(var0, var1, var2);
            if (param5.isInside(var3)) {
                int var4 = 1;
                boolean var5 = true;

                for(boolean var6 = true; var5 || var6; ++var4) {
                    if (var5) {
                        var3.setY(var1 - var4);
                        BlockState var7 = param0.getBlockState(var3);
                        boolean var8 = this.isReplaceableByStructures(var7);
                        if (!var8 && this.canPlaceColumnOnTopOf(var7)) {
                            fillColumnBetween(param0, param1, var3, var1 - var4 + 1, var1);
                            return;
                        }

                        var5 = var4 <= 10 && var8 && var3.getY() > param0.getMinBuildHeight() + 1;
                    }

                    if (var6) {
                        var3.setY(var1 + var4);
                        BlockState var9 = param0.getBlockState(var3);
                        boolean var10 = this.isReplaceableByStructures(var9);
                        if (!var10 && this.canHangChainBelow(param0, var3, var9)) {
                            param0.setBlock(var3.setY(var1 + 1), this.type.getFenceState(), 2);
                            fillColumnBetween(param0, Blocks.CHAIN.defaultBlockState(), var3, var1 + 2, var1 + var4);
                            return;
                        }

                        var6 = var4 <= 20 && var10 && var3.getY() < param0.getMaxBuildHeight() - 1;
                    }
                }

            }
        }

        private static void fillColumnBetween(WorldGenLevel param0, BlockState param1, BlockPos.MutableBlockPos param2, int param3, int param4) {
            for(int var0 = param3; var0 < param4; ++var0) {
                param0.setBlock(param2.setY(var0), param1, 2);
            }

        }

        private boolean canPlaceColumnOnTopOf(BlockState param0) {
            return !param0.is(Blocks.RAIL) && !param0.is(Blocks.LAVA);
        }

        private boolean canHangChainBelow(LevelReader param0, BlockPos param1, BlockState param2) {
            return Block.canSupportCenter(param0, param1, Direction.DOWN) && !(param2.getBlock() instanceof FallingBlock);
        }

        private void placeSupport(WorldGenLevel param0, BoundingBox param1, int param2, int param3, int param4, int param5, int param6, Random param7) {
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
                        Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH),
                        false
                    );
                    this.maybeGenerateBlock(
                        param0,
                        param1,
                        param7,
                        0.05F,
                        param2 + 1,
                        param5,
                        param4 + 1,
                        Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH),
                        false
                    );
                }

            }
        }

        private void placeCobWeb(WorldGenLevel param0, BoundingBox param1, Random param2, float param3, int param4, int param5, int param6) {
            if (this.isInterior(param0, param4, param5, param6, param1)) {
                this.maybeGenerateBlock(param0, param1, param2, param3, param4, param5, param6, Blocks.COBWEB.defaultBlockState(), true);
            }

        }
    }

    public static class MineShaftCrossing extends MineShaftPieces.MineShaftPiece {
        private final Direction direction;
        private final boolean isTwoFloored;

        public MineShaftCrossing(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.MINE_SHAFT_CROSSING, param1);
            this.isTwoFloored = param1.getBoolean("tf");
            this.direction = Direction.from2DDataValue(param1.getInt("D"));
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag param0) {
            super.addAdditionalSaveData(param0);
            param0.putBoolean("tf", this.isTwoFloored);
            param0.putInt("D", this.direction.get2DDataValue());
        }

        public MineShaftCrossing(int param0, BoundingBox param1, @Nullable Direction param2, MineshaftFeature.Type param3) {
            super(StructurePieceType.MINE_SHAFT_CROSSING, param0, param3);
            this.direction = param2;
            this.boundingBox = param1;
            this.isTwoFloored = param1.getYSpan() > 3;
        }

        public static BoundingBox findCrossing(List<StructurePiece> param0, Random param1, int param2, int param3, int param4, Direction param5) {
            BoundingBox var0 = new BoundingBox(param2, param3, param4, param2, param3 + 3 - 1, param4);
            if (param1.nextInt(4) == 0) {
                var0.y1 += 4;
            }

            switch(param5) {
                case NORTH:
                default:
                    var0.x0 = param2 - 1;
                    var0.x1 = param2 + 3;
                    var0.z0 = param4 - 4;
                    break;
                case SOUTH:
                    var0.x0 = param2 - 1;
                    var0.x1 = param2 + 3;
                    var0.z1 = param4 + 3 + 1;
                    break;
                case WEST:
                    var0.x0 = param2 - 4;
                    var0.z0 = param4 - 1;
                    var0.z1 = param4 + 3;
                    break;
                case EAST:
                    var0.x1 = param2 + 3 + 1;
                    var0.z0 = param4 - 1;
                    var0.z1 = param4 + 3;
            }

            return StructurePiece.findCollisionPiece(param0, var0) != null ? null : var0;
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
            int var0 = this.getGenDepth();
            switch(this.direction) {
                case NORTH:
                default:
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z0 - 1, Direction.NORTH, var0
                    );
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x0 - 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.WEST, var0
                    );
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x1 + 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.EAST, var0
                    );
                    break;
                case SOUTH:
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z1 + 1, Direction.SOUTH, var0
                    );
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x0 - 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.WEST, var0
                    );
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x1 + 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.EAST, var0
                    );
                    break;
                case WEST:
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z0 - 1, Direction.NORTH, var0
                    );
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z1 + 1, Direction.SOUTH, var0
                    );
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x0 - 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.WEST, var0
                    );
                    break;
                case EAST:
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z0 - 1, Direction.NORTH, var0
                    );
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z1 + 1, Direction.SOUTH, var0
                    );
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x1 + 1, this.boundingBox.y0, this.boundingBox.z0 + 1, Direction.EAST, var0
                    );
            }

            if (this.isTwoFloored) {
                if (param2.nextBoolean()) {
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x0 + 1, this.boundingBox.y0 + 3 + 1, this.boundingBox.z0 - 1, Direction.NORTH, var0
                    );
                }

                if (param2.nextBoolean()) {
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x0 - 1, this.boundingBox.y0 + 3 + 1, this.boundingBox.z0 + 1, Direction.WEST, var0
                    );
                }

                if (param2.nextBoolean()) {
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x1 + 1, this.boundingBox.y0 + 3 + 1, this.boundingBox.z0 + 1, Direction.EAST, var0
                    );
                }

                if (param2.nextBoolean()) {
                    MineShaftPieces.generateAndAddPiece(
                        param0, param1, param2, this.boundingBox.x0 + 1, this.boundingBox.y0 + 3 + 1, this.boundingBox.z1 + 1, Direction.SOUTH, var0
                    );
                }
            }

        }

        @Override
        public boolean postProcess(
            WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            if (this.edgesLiquid(param0, param4)) {
                return false;
            } else {
                BlockState var0 = this.type.getPlanksState();
                if (this.isTwoFloored) {
                    this.generateBox(
                        param0,
                        param4,
                        this.boundingBox.x0 + 1,
                        this.boundingBox.y0,
                        this.boundingBox.z0,
                        this.boundingBox.x1 - 1,
                        this.boundingBox.y0 + 3 - 1,
                        this.boundingBox.z1,
                        CAVE_AIR,
                        CAVE_AIR,
                        false
                    );
                    this.generateBox(
                        param0,
                        param4,
                        this.boundingBox.x0,
                        this.boundingBox.y0,
                        this.boundingBox.z0 + 1,
                        this.boundingBox.x1,
                        this.boundingBox.y0 + 3 - 1,
                        this.boundingBox.z1 - 1,
                        CAVE_AIR,
                        CAVE_AIR,
                        false
                    );
                    this.generateBox(
                        param0,
                        param4,
                        this.boundingBox.x0 + 1,
                        this.boundingBox.y1 - 2,
                        this.boundingBox.z0,
                        this.boundingBox.x1 - 1,
                        this.boundingBox.y1,
                        this.boundingBox.z1,
                        CAVE_AIR,
                        CAVE_AIR,
                        false
                    );
                    this.generateBox(
                        param0,
                        param4,
                        this.boundingBox.x0,
                        this.boundingBox.y1 - 2,
                        this.boundingBox.z0 + 1,
                        this.boundingBox.x1,
                        this.boundingBox.y1,
                        this.boundingBox.z1 - 1,
                        CAVE_AIR,
                        CAVE_AIR,
                        false
                    );
                    this.generateBox(
                        param0,
                        param4,
                        this.boundingBox.x0 + 1,
                        this.boundingBox.y0 + 3,
                        this.boundingBox.z0 + 1,
                        this.boundingBox.x1 - 1,
                        this.boundingBox.y0 + 3,
                        this.boundingBox.z1 - 1,
                        CAVE_AIR,
                        CAVE_AIR,
                        false
                    );
                } else {
                    this.generateBox(
                        param0,
                        param4,
                        this.boundingBox.x0 + 1,
                        this.boundingBox.y0,
                        this.boundingBox.z0,
                        this.boundingBox.x1 - 1,
                        this.boundingBox.y1,
                        this.boundingBox.z1,
                        CAVE_AIR,
                        CAVE_AIR,
                        false
                    );
                    this.generateBox(
                        param0,
                        param4,
                        this.boundingBox.x0,
                        this.boundingBox.y0,
                        this.boundingBox.z0 + 1,
                        this.boundingBox.x1,
                        this.boundingBox.y1,
                        this.boundingBox.z1 - 1,
                        CAVE_AIR,
                        CAVE_AIR,
                        false
                    );
                }

                this.placeSupportPillar(param0, param4, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z0 + 1, this.boundingBox.y1);
                this.placeSupportPillar(param0, param4, this.boundingBox.x0 + 1, this.boundingBox.y0, this.boundingBox.z1 - 1, this.boundingBox.y1);
                this.placeSupportPillar(param0, param4, this.boundingBox.x1 - 1, this.boundingBox.y0, this.boundingBox.z0 + 1, this.boundingBox.y1);
                this.placeSupportPillar(param0, param4, this.boundingBox.x1 - 1, this.boundingBox.y0, this.boundingBox.z1 - 1, this.boundingBox.y1);
                int var1 = this.boundingBox.y0 - 1;

                for(int var2 = this.boundingBox.x0; var2 <= this.boundingBox.x1; ++var2) {
                    for(int var3 = this.boundingBox.z0; var3 <= this.boundingBox.z1; ++var3) {
                        this.setPlanksBlock(param0, param4, var0, var2, var1, var3);
                    }
                }

                return true;
            }
        }

        private void placeSupportPillar(WorldGenLevel param0, BoundingBox param1, int param2, int param3, int param4, int param5) {
            if (!this.getBlock(param0, param2, param5 + 1, param4, param1).isAir()) {
                this.generateBox(param0, param1, param2, param3, param4, param2, param5, param4, this.type.getPlanksState(), CAVE_AIR, false);
            }

        }
    }

    abstract static class MineShaftPiece extends StructurePiece {
        protected MineshaftFeature.Type type;

        public MineShaftPiece(StructurePieceType param0, int param1, MineshaftFeature.Type param2) {
            super(param0, param1);
            this.type = param2;
        }

        public MineShaftPiece(StructurePieceType param0, CompoundTag param1) {
            super(param0, param1);
            this.type = MineshaftFeature.Type.byId(param1.getInt("MST"));
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
        protected void addAdditionalSaveData(CompoundTag param0) {
            param0.putInt("MST", this.type.ordinal());
        }

        protected boolean isSupportingBox(BlockGetter param0, BoundingBox param1, int param2, int param3, int param4, int param5) {
            for(int var0 = param2; var0 <= param3; ++var0) {
                if (this.getBlock(param0, var0, param4 + 1, param5, param1).isAir()) {
                    return false;
                }
            }

            return true;
        }

        protected boolean edgesLiquid(BlockGetter param0, BoundingBox param1) {
            int var0 = Math.max(this.boundingBox.x0 - 1, param1.x0);
            int var1 = Math.max(this.boundingBox.y0 - 1, param1.y0);
            int var2 = Math.max(this.boundingBox.z0 - 1, param1.z0);
            int var3 = Math.min(this.boundingBox.x1 + 1, param1.x1);
            int var4 = Math.min(this.boundingBox.y1 + 1, param1.y1);
            int var5 = Math.min(this.boundingBox.z1 + 1, param1.z1);
            BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos();

            for(int var7 = var0; var7 <= var3; ++var7) {
                for(int var8 = var2; var8 <= var5; ++var8) {
                    if (param0.getBlockState(var6.set(var7, var1, var8)).getMaterial().isLiquid()) {
                        return true;
                    }

                    if (param0.getBlockState(var6.set(var7, var4, var8)).getMaterial().isLiquid()) {
                        return true;
                    }
                }
            }

            for(int var9 = var0; var9 <= var3; ++var9) {
                for(int var10 = var1; var10 <= var4; ++var10) {
                    if (param0.getBlockState(var6.set(var9, var10, var2)).getMaterial().isLiquid()) {
                        return true;
                    }

                    if (param0.getBlockState(var6.set(var9, var10, var5)).getMaterial().isLiquid()) {
                        return true;
                    }
                }
            }

            for(int var11 = var2; var11 <= var5; ++var11) {
                for(int var12 = var1; var12 <= var4; ++var12) {
                    if (param0.getBlockState(var6.set(var0, var12, var11)).getMaterial().isLiquid()) {
                        return true;
                    }

                    if (param0.getBlockState(var6.set(var3, var12, var11)).getMaterial().isLiquid()) {
                        return true;
                    }
                }
            }

            return false;
        }

        protected void setPlanksBlock(WorldGenLevel param0, BoundingBox param1, BlockState param2, int param3, int param4, int param5) {
            if (this.isInterior(param0, param3, param4, param5, param1)) {
                BlockPos var0 = this.getWorldPos(param3, param4, param5);
                BlockState var1 = param0.getBlockState(var0);
                if (var1.isAir() || var1.is(Blocks.CHAIN)) {
                    param0.setBlock(var0, param2, 2);
                }

            }
        }
    }

    public static class MineShaftRoom extends MineShaftPieces.MineShaftPiece {
        private final List<BoundingBox> childEntranceBoxes = Lists.newLinkedList();

        public MineShaftRoom(int param0, Random param1, int param2, int param3, MineshaftFeature.Type param4) {
            super(StructurePieceType.MINE_SHAFT_ROOM, param0, param4);
            this.type = param4;
            this.boundingBox = new BoundingBox(param2, 50, param3, param2 + 7 + param1.nextInt(6), 54 + param1.nextInt(6), param3 + 7 + param1.nextInt(6));
        }

        public MineShaftRoom(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.MINE_SHAFT_ROOM, param1);
            BoundingBox.CODEC
                .listOf()
                .parse(NbtOps.INSTANCE, param1.getList("Entrances", 11))
                .resultOrPartial(MineShaftPieces.LOGGER::error)
                .ifPresent(this.childEntranceBoxes::addAll);
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
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

                MineShaftPieces.MineShaftPiece var3 = MineShaftPieces.generateAndAddPiece(
                    param0,
                    param1,
                    param2,
                    this.boundingBox.x0 + var2,
                    this.boundingBox.y0 + param2.nextInt(var1) + 1,
                    this.boundingBox.z0 - 1,
                    Direction.NORTH,
                    var0
                );
                if (var3 != null) {
                    BoundingBox var4 = var3.getBoundingBox();
                    this.childEntranceBoxes.add(new BoundingBox(var4.x0, var4.y0, this.boundingBox.z0, var4.x1, var4.y1, this.boundingBox.z0 + 1));
                }
            }

            for(var2 = 0; var2 < this.boundingBox.getXSpan(); var2 += 4) {
                var2 += param2.nextInt(this.boundingBox.getXSpan());
                if (var2 + 3 > this.boundingBox.getXSpan()) {
                    break;
                }

                MineShaftPieces.MineShaftPiece var5 = MineShaftPieces.generateAndAddPiece(
                    param0,
                    param1,
                    param2,
                    this.boundingBox.x0 + var2,
                    this.boundingBox.y0 + param2.nextInt(var1) + 1,
                    this.boundingBox.z1 + 1,
                    Direction.SOUTH,
                    var0
                );
                if (var5 != null) {
                    BoundingBox var6 = var5.getBoundingBox();
                    this.childEntranceBoxes.add(new BoundingBox(var6.x0, var6.y0, this.boundingBox.z1 - 1, var6.x1, var6.y1, this.boundingBox.z1));
                }
            }

            for(var2 = 0; var2 < this.boundingBox.getZSpan(); var2 += 4) {
                var2 += param2.nextInt(this.boundingBox.getZSpan());
                if (var2 + 3 > this.boundingBox.getZSpan()) {
                    break;
                }

                MineShaftPieces.MineShaftPiece var7 = MineShaftPieces.generateAndAddPiece(
                    param0,
                    param1,
                    param2,
                    this.boundingBox.x0 - 1,
                    this.boundingBox.y0 + param2.nextInt(var1) + 1,
                    this.boundingBox.z0 + var2,
                    Direction.WEST,
                    var0
                );
                if (var7 != null) {
                    BoundingBox var8 = var7.getBoundingBox();
                    this.childEntranceBoxes.add(new BoundingBox(this.boundingBox.x0, var8.y0, var8.z0, this.boundingBox.x0 + 1, var8.y1, var8.z1));
                }
            }

            for(var2 = 0; var2 < this.boundingBox.getZSpan(); var2 += 4) {
                var2 += param2.nextInt(this.boundingBox.getZSpan());
                if (var2 + 3 > this.boundingBox.getZSpan()) {
                    break;
                }

                StructurePiece var9 = MineShaftPieces.generateAndAddPiece(
                    param0,
                    param1,
                    param2,
                    this.boundingBox.x1 + 1,
                    this.boundingBox.y0 + param2.nextInt(var1) + 1,
                    this.boundingBox.z0 + var2,
                    Direction.EAST,
                    var0
                );
                if (var9 != null) {
                    BoundingBox var10 = var9.getBoundingBox();
                    this.childEntranceBoxes.add(new BoundingBox(this.boundingBox.x1 - 1, var10.y0, var10.z0, this.boundingBox.x1, var10.y1, var10.z1));
                }
            }

        }

        @Override
        public boolean postProcess(
            WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            if (this.edgesLiquid(param0, param4)) {
                return false;
            } else {
                this.generateBox(
                    param0,
                    param4,
                    this.boundingBox.x0,
                    this.boundingBox.y0,
                    this.boundingBox.z0,
                    this.boundingBox.x1,
                    this.boundingBox.y0,
                    this.boundingBox.z1,
                    Blocks.DIRT.defaultBlockState(),
                    CAVE_AIR,
                    true
                );
                this.generateBox(
                    param0,
                    param4,
                    this.boundingBox.x0,
                    this.boundingBox.y0 + 1,
                    this.boundingBox.z0,
                    this.boundingBox.x1,
                    Math.min(this.boundingBox.y0 + 3, this.boundingBox.y1),
                    this.boundingBox.z1,
                    CAVE_AIR,
                    CAVE_AIR,
                    false
                );

                for(BoundingBox var0 : this.childEntranceBoxes) {
                    this.generateBox(param0, param4, var0.x0, var0.y1 - 2, var0.z0, var0.x1, var0.y1, var0.z1, CAVE_AIR, CAVE_AIR, false);
                }

                this.generateUpperHalfSphere(
                    param0,
                    param4,
                    this.boundingBox.x0,
                    this.boundingBox.y0 + 4,
                    this.boundingBox.z0,
                    this.boundingBox.x1,
                    this.boundingBox.y1,
                    this.boundingBox.z1,
                    CAVE_AIR,
                    false
                );
                return true;
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
        protected void addAdditionalSaveData(CompoundTag param0) {
            super.addAdditionalSaveData(param0);
            BoundingBox.CODEC
                .listOf()
                .encodeStart(NbtOps.INSTANCE, this.childEntranceBoxes)
                .resultOrPartial(MineShaftPieces.LOGGER::error)
                .ifPresent(param1 -> param0.put("Entrances", param1));
        }
    }

    public static class MineShaftStairs extends MineShaftPieces.MineShaftPiece {
        public MineShaftStairs(int param0, BoundingBox param1, Direction param2, MineshaftFeature.Type param3) {
            super(StructurePieceType.MINE_SHAFT_STAIRS, param0, param3);
            this.setOrientation(param2);
            this.boundingBox = param1;
        }

        public MineShaftStairs(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.MINE_SHAFT_STAIRS, param1);
        }

        public static BoundingBox findStairs(List<StructurePiece> param0, Random param1, int param2, int param3, int param4, Direction param5) {
            BoundingBox var0 = new BoundingBox(param2, param3 - 5, param4, param2, param3 + 3 - 1, param4);
            switch(param5) {
                case NORTH:
                default:
                    var0.x1 = param2 + 3 - 1;
                    var0.z0 = param4 - 8;
                    break;
                case SOUTH:
                    var0.x1 = param2 + 3 - 1;
                    var0.z1 = param4 + 8;
                    break;
                case WEST:
                    var0.x0 = param2 - 8;
                    var0.z1 = param4 + 3 - 1;
                    break;
                case EAST:
                    var0.x1 = param2 + 8;
                    var0.z1 = param4 + 3 - 1;
            }

            return StructurePiece.findCollisionPiece(param0, var0) != null ? null : var0;
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
            int var0 = this.getGenDepth();
            Direction var1 = this.getOrientation();
            if (var1 != null) {
                switch(var1) {
                    case NORTH:
                    default:
                        MineShaftPieces.generateAndAddPiece(
                            param0, param1, param2, this.boundingBox.x0, this.boundingBox.y0, this.boundingBox.z0 - 1, Direction.NORTH, var0
                        );
                        break;
                    case SOUTH:
                        MineShaftPieces.generateAndAddPiece(
                            param0, param1, param2, this.boundingBox.x0, this.boundingBox.y0, this.boundingBox.z1 + 1, Direction.SOUTH, var0
                        );
                        break;
                    case WEST:
                        MineShaftPieces.generateAndAddPiece(
                            param0, param1, param2, this.boundingBox.x0 - 1, this.boundingBox.y0, this.boundingBox.z0, Direction.WEST, var0
                        );
                        break;
                    case EAST:
                        MineShaftPieces.generateAndAddPiece(
                            param0, param1, param2, this.boundingBox.x1 + 1, this.boundingBox.y0, this.boundingBox.z0, Direction.EAST, var0
                        );
                }
            }

        }

        @Override
        public boolean postProcess(
            WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            if (this.edgesLiquid(param0, param4)) {
                return false;
            } else {
                this.generateBox(param0, param4, 0, 5, 0, 2, 7, 1, CAVE_AIR, CAVE_AIR, false);
                this.generateBox(param0, param4, 0, 0, 7, 2, 2, 8, CAVE_AIR, CAVE_AIR, false);

                for(int var0 = 0; var0 < 5; ++var0) {
                    this.generateBox(param0, param4, 0, 5 - var0 - (var0 < 4 ? 1 : 0), 2 + var0, 2, 7 - var0, 2 + var0, CAVE_AIR, CAVE_AIR, false);
                }

                return true;
            }
        }
    }
}
