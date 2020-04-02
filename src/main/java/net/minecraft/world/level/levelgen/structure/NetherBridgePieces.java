package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class NetherBridgePieces {
    private static final NetherBridgePieces.PieceWeight[] BRIDGE_PIECE_WEIGHTS = new NetherBridgePieces.PieceWeight[]{
        new NetherBridgePieces.PieceWeight(NetherBridgePieces.BridgeStraight.class, 30, 0, true),
        new NetherBridgePieces.PieceWeight(NetherBridgePieces.BridgeCrossing.class, 10, 4),
        new NetherBridgePieces.PieceWeight(NetherBridgePieces.RoomCrossing.class, 10, 4),
        new NetherBridgePieces.PieceWeight(NetherBridgePieces.StairsRoom.class, 10, 3),
        new NetherBridgePieces.PieceWeight(NetherBridgePieces.MonsterThrone.class, 5, 2),
        new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleEntrance.class, 5, 1)
    };
    private static final NetherBridgePieces.PieceWeight[] CASTLE_PIECE_WEIGHTS = new NetherBridgePieces.PieceWeight[]{
        new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleSmallCorridorPiece.class, 25, 0, true),
        new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleSmallCorridorCrossingPiece.class, 15, 5),
        new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleSmallCorridorRightTurnPiece.class, 5, 10),
        new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleSmallCorridorLeftTurnPiece.class, 5, 10),
        new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleCorridorStairsPiece.class, 10, 3, true),
        new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleCorridorTBalconyPiece.class, 7, 2),
        new NetherBridgePieces.PieceWeight(NetherBridgePieces.CastleStalkRoom.class, 5, 2)
    };

    private static NetherBridgePieces.NetherBridgePiece findAndCreateBridgePieceFactory(
        NetherBridgePieces.PieceWeight param0, List<StructurePiece> param1, Random param2, int param3, int param4, int param5, Direction param6, int param7
    ) {
        Class<? extends NetherBridgePieces.NetherBridgePiece> var0 = param0.pieceClass;
        NetherBridgePieces.NetherBridgePiece var1 = null;
        if (var0 == NetherBridgePieces.BridgeStraight.class) {
            var1 = NetherBridgePieces.BridgeStraight.createPiece(param1, param2, param3, param4, param5, param6, param7);
        } else if (var0 == NetherBridgePieces.BridgeCrossing.class) {
            var1 = NetherBridgePieces.BridgeCrossing.createPiece(param1, param3, param4, param5, param6, param7);
        } else if (var0 == NetherBridgePieces.RoomCrossing.class) {
            var1 = NetherBridgePieces.RoomCrossing.createPiece(param1, param3, param4, param5, param6, param7);
        } else if (var0 == NetherBridgePieces.StairsRoom.class) {
            var1 = NetherBridgePieces.StairsRoom.createPiece(param1, param3, param4, param5, param7, param6);
        } else if (var0 == NetherBridgePieces.MonsterThrone.class) {
            var1 = NetherBridgePieces.MonsterThrone.createPiece(param1, param3, param4, param5, param7, param6);
        } else if (var0 == NetherBridgePieces.CastleEntrance.class) {
            var1 = NetherBridgePieces.CastleEntrance.createPiece(param1, param2, param3, param4, param5, param6, param7);
        } else if (var0 == NetherBridgePieces.CastleSmallCorridorPiece.class) {
            var1 = NetherBridgePieces.CastleSmallCorridorPiece.createPiece(param1, param3, param4, param5, param6, param7);
        } else if (var0 == NetherBridgePieces.CastleSmallCorridorRightTurnPiece.class) {
            var1 = NetherBridgePieces.CastleSmallCorridorRightTurnPiece.createPiece(param1, param2, param3, param4, param5, param6, param7);
        } else if (var0 == NetherBridgePieces.CastleSmallCorridorLeftTurnPiece.class) {
            var1 = NetherBridgePieces.CastleSmallCorridorLeftTurnPiece.createPiece(param1, param2, param3, param4, param5, param6, param7);
        } else if (var0 == NetherBridgePieces.CastleCorridorStairsPiece.class) {
            var1 = NetherBridgePieces.CastleCorridorStairsPiece.createPiece(param1, param3, param4, param5, param6, param7);
        } else if (var0 == NetherBridgePieces.CastleCorridorTBalconyPiece.class) {
            var1 = NetherBridgePieces.CastleCorridorTBalconyPiece.createPiece(param1, param3, param4, param5, param6, param7);
        } else if (var0 == NetherBridgePieces.CastleSmallCorridorCrossingPiece.class) {
            var1 = NetherBridgePieces.CastleSmallCorridorCrossingPiece.createPiece(param1, param3, param4, param5, param6, param7);
        } else if (var0 == NetherBridgePieces.CastleStalkRoom.class) {
            var1 = NetherBridgePieces.CastleStalkRoom.createPiece(param1, param3, param4, param5, param6, param7);
        }

        return var1;
    }

    public static class BridgeCrossing extends NetherBridgePieces.NetherBridgePiece {
        public BridgeCrossing(int param0, BoundingBox param1, Direction param2) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, param0);
            this.setOrientation(param2);
            this.boundingBox = param1;
        }

        protected BridgeCrossing(Random param0, int param1, int param2) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, 0);
            this.setOrientation(Direction.Plane.HORIZONTAL.getRandomDirection(param0));
            if (this.getOrientation().getAxis() == Direction.Axis.Z) {
                this.boundingBox = new BoundingBox(param1, 64, param2, param1 + 19 - 1, 73, param2 + 19 - 1);
            } else {
                this.boundingBox = new BoundingBox(param1, 64, param2, param1 + 19 - 1, 73, param2 + 19 - 1);
            }

        }

        protected BridgeCrossing(StructurePieceType param0, CompoundTag param1) {
            super(param0, param1);
        }

        public BridgeCrossing(StructureManager param0, CompoundTag param1) {
            this(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, param1);
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
            this.generateChildForward((NetherBridgePieces.StartPiece)param0, param1, param2, 8, 3, false);
            this.generateChildLeft((NetherBridgePieces.StartPiece)param0, param1, param2, 3, 8, false);
            this.generateChildRight((NetherBridgePieces.StartPiece)param0, param1, param2, 3, 8, false);
        }

        public static NetherBridgePieces.BridgeCrossing createPiece(
            List<StructurePiece> param0, int param1, int param2, int param3, Direction param4, int param5
        ) {
            BoundingBox var0 = BoundingBox.orientBox(param1, param2, param3, -8, -3, 0, 19, 10, 19, param4);
            return isOkBox(var0) && StructurePiece.findCollisionPiece(param0, var0) == null
                ? new NetherBridgePieces.BridgeCrossing(param5, var0, param4)
                : null;
        }

        @Override
        public boolean postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            this.generateBox(param0, param4, 7, 3, 0, 11, 4, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 3, 7, 18, 4, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 8, 5, 0, 10, 7, 18, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 5, 8, 18, 7, 10, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 7, 5, 0, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 7, 5, 11, 7, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 11, 5, 0, 11, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 11, 5, 11, 11, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 5, 7, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 11, 5, 7, 18, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 5, 11, 7, 5, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 11, 5, 11, 18, 5, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 7, 2, 0, 11, 2, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 7, 2, 13, 11, 2, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 7, 0, 0, 11, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 7, 0, 15, 11, 1, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for(int var0 = 7; var0 <= 11; ++var0) {
                for(int var1 = 0; var1 <= 2; ++var1) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var0, -1, var1, param4);
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var0, -1, 18 - var1, param4);
                }
            }

            this.generateBox(param0, param4, 0, 2, 7, 5, 2, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 13, 2, 7, 18, 2, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 0, 7, 3, 1, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 15, 0, 7, 18, 1, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for(int var2 = 0; var2 <= 2; ++var2) {
                for(int var3 = 7; var3 <= 11; ++var3) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var2, -1, var3, param4);
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), 18 - var2, -1, var3, param4);
                }
            }

            return true;
        }
    }

    public static class BridgeEndFiller extends NetherBridgePieces.NetherBridgePiece {
        private final int selfSeed;

        public BridgeEndFiller(int param0, Random param1, BoundingBox param2, Direction param3) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END_FILLER, param0);
            this.setOrientation(param3);
            this.boundingBox = param2;
            this.selfSeed = param1.nextInt();
        }

        public BridgeEndFiller(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END_FILLER, param1);
            this.selfSeed = param1.getInt("Seed");
        }

        public static NetherBridgePieces.BridgeEndFiller createPiece(
            List<StructurePiece> param0, Random param1, int param2, int param3, int param4, Direction param5, int param6
        ) {
            BoundingBox var0 = BoundingBox.orientBox(param2, param3, param4, -1, -3, 0, 5, 10, 8, param5);
            return isOkBox(var0) && StructurePiece.findCollisionPiece(param0, var0) == null
                ? new NetherBridgePieces.BridgeEndFiller(param6, param1, var0, param5)
                : null;
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag param0) {
            super.addAdditionalSaveData(param0);
            param0.putInt("Seed", this.selfSeed);
        }

        @Override
        public boolean postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            Random var0 = new Random((long)this.selfSeed);

            for(int var1 = 0; var1 <= 4; ++var1) {
                for(int var2 = 3; var2 <= 4; ++var2) {
                    int var3 = var0.nextInt(8);
                    this.generateBox(
                        param0,
                        param4,
                        var1,
                        var2,
                        0,
                        var1,
                        var2,
                        var3,
                        Blocks.NETHER_BRICKS.defaultBlockState(),
                        Blocks.NETHER_BRICKS.defaultBlockState(),
                        false
                    );
                }
            }

            int var4 = var0.nextInt(8);
            this.generateBox(param0, param4, 0, 5, 0, 0, 5, var4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            var4 = var0.nextInt(8);
            this.generateBox(param0, param4, 4, 5, 0, 4, 5, var4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for(int var6 = 0; var6 <= 4; ++var6) {
                int var7 = var0.nextInt(5);
                this.generateBox(
                    param0, param4, var6, 2, 0, var6, 2, var7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false
                );
            }

            for(int var8 = 0; var8 <= 4; ++var8) {
                for(int var9 = 0; var9 <= 1; ++var9) {
                    int var10 = var0.nextInt(3);
                    this.generateBox(
                        param0,
                        param4,
                        var8,
                        var9,
                        0,
                        var8,
                        var9,
                        var10,
                        Blocks.NETHER_BRICKS.defaultBlockState(),
                        Blocks.NETHER_BRICKS.defaultBlockState(),
                        false
                    );
                }
            }

            return true;
        }
    }

    public static class BridgeStraight extends NetherBridgePieces.NetherBridgePiece {
        public BridgeStraight(int param0, Random param1, BoundingBox param2, Direction param3) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT, param0);
            this.setOrientation(param3);
            this.boundingBox = param2;
        }

        public BridgeStraight(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT, param1);
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
            this.generateChildForward((NetherBridgePieces.StartPiece)param0, param1, param2, 1, 3, false);
        }

        public static NetherBridgePieces.BridgeStraight createPiece(
            List<StructurePiece> param0, Random param1, int param2, int param3, int param4, Direction param5, int param6
        ) {
            BoundingBox var0 = BoundingBox.orientBox(param2, param3, param4, -1, -3, 0, 5, 10, 19, param5);
            return isOkBox(var0) && StructurePiece.findCollisionPiece(param0, var0) == null
                ? new NetherBridgePieces.BridgeStraight(param6, param1, var0, param5)
                : null;
        }

        @Override
        public boolean postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            this.generateBox(param0, param4, 0, 3, 0, 4, 4, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 5, 0, 3, 7, 18, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 5, 0, 0, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 4, 5, 0, 4, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 0, 4, 2, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 13, 4, 2, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 0, 0, 4, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 0, 15, 4, 1, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for(int var0 = 0; var0 <= 4; ++var0) {
                for(int var1 = 0; var1 <= 2; ++var1) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var0, -1, var1, param4);
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var0, -1, 18 - var1, param4);
                }
            }

            BlockState var2 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.NORTH, Boolean.valueOf(true))
                .setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            BlockState var3 = var2.setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState var4 = var2.setValue(FenceBlock.WEST, Boolean.valueOf(true));
            this.generateBox(param0, param4, 0, 1, 1, 0, 4, 1, var3, var3, false);
            this.generateBox(param0, param4, 0, 3, 4, 0, 4, 4, var3, var3, false);
            this.generateBox(param0, param4, 0, 3, 14, 0, 4, 14, var3, var3, false);
            this.generateBox(param0, param4, 0, 1, 17, 0, 4, 17, var3, var3, false);
            this.generateBox(param0, param4, 4, 1, 1, 4, 4, 1, var4, var4, false);
            this.generateBox(param0, param4, 4, 3, 4, 4, 4, 4, var4, var4, false);
            this.generateBox(param0, param4, 4, 3, 14, 4, 4, 14, var4, var4, false);
            this.generateBox(param0, param4, 4, 1, 17, 4, 4, 17, var4, var4, false);
            return true;
        }
    }

    public static class CastleCorridorStairsPiece extends NetherBridgePieces.NetherBridgePiece {
        public CastleCorridorStairsPiece(int param0, BoundingBox param1, Direction param2) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS, param0);
            this.setOrientation(param2);
            this.boundingBox = param1;
        }

        public CastleCorridorStairsPiece(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS, param1);
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
            this.generateChildForward((NetherBridgePieces.StartPiece)param0, param1, param2, 1, 0, true);
        }

        public static NetherBridgePieces.CastleCorridorStairsPiece createPiece(
            List<StructurePiece> param0, int param1, int param2, int param3, Direction param4, int param5
        ) {
            BoundingBox var0 = BoundingBox.orientBox(param1, param2, param3, -1, -7, 0, 5, 14, 10, param4);
            return isOkBox(var0) && StructurePiece.findCollisionPiece(param0, var0) == null
                ? new NetherBridgePieces.CastleCorridorStairsPiece(param5, var0, param4)
                : null;
        }

        @Override
        public boolean postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            BlockState var0 = Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
            BlockState var1 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.NORTH, Boolean.valueOf(true))
                .setValue(FenceBlock.SOUTH, Boolean.valueOf(true));

            for(int var2 = 0; var2 <= 9; ++var2) {
                int var3 = Math.max(1, 7 - var2);
                int var4 = Math.min(Math.max(var3 + 5, 14 - var2), 13);
                int var5 = var2;
                this.generateBox(
                    param0, param4, 0, 0, var2, 4, var3, var2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false
                );
                this.generateBox(param0, param4, 1, var3 + 1, var2, 3, var4 - 1, var2, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
                if (var2 <= 6) {
                    this.placeBlock(param0, var0, 1, var3 + 1, var2, param4);
                    this.placeBlock(param0, var0, 2, var3 + 1, var2, param4);
                    this.placeBlock(param0, var0, 3, var3 + 1, var2, param4);
                }

                this.generateBox(
                    param0, param4, 0, var4, var2, 4, var4, var2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false
                );
                this.generateBox(
                    param0,
                    param4,
                    0,
                    var3 + 1,
                    var2,
                    0,
                    var4 - 1,
                    var2,
                    Blocks.NETHER_BRICKS.defaultBlockState(),
                    Blocks.NETHER_BRICKS.defaultBlockState(),
                    false
                );
                this.generateBox(
                    param0,
                    param4,
                    4,
                    var3 + 1,
                    var2,
                    4,
                    var4 - 1,
                    var2,
                    Blocks.NETHER_BRICKS.defaultBlockState(),
                    Blocks.NETHER_BRICKS.defaultBlockState(),
                    false
                );
                if ((var2 & 1) == 0) {
                    this.generateBox(param0, param4, 0, var3 + 2, var2, 0, var3 + 3, var2, var1, var1, false);
                    this.generateBox(param0, param4, 4, var3 + 2, var2, 4, var3 + 3, var2, var1, var1, false);
                }

                for(int var6 = 0; var6 <= 4; ++var6) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var6, -1, var5, param4);
                }
            }

            return true;
        }
    }

    public static class CastleCorridorTBalconyPiece extends NetherBridgePieces.NetherBridgePiece {
        public CastleCorridorTBalconyPiece(int param0, BoundingBox param1, Direction param2) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY, param0);
            this.setOrientation(param2);
            this.boundingBox = param1;
        }

        public CastleCorridorTBalconyPiece(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY, param1);
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
            int var0 = 1;
            Direction var1 = this.getOrientation();
            if (var1 == Direction.WEST || var1 == Direction.NORTH) {
                var0 = 5;
            }

            this.generateChildLeft((NetherBridgePieces.StartPiece)param0, param1, param2, 0, var0, param2.nextInt(8) > 0);
            this.generateChildRight((NetherBridgePieces.StartPiece)param0, param1, param2, 0, var0, param2.nextInt(8) > 0);
        }

        public static NetherBridgePieces.CastleCorridorTBalconyPiece createPiece(
            List<StructurePiece> param0, int param1, int param2, int param3, Direction param4, int param5
        ) {
            BoundingBox var0 = BoundingBox.orientBox(param1, param2, param3, -3, 0, 0, 9, 7, 9, param4);
            return isOkBox(var0) && StructurePiece.findCollisionPiece(param0, var0) == null
                ? new NetherBridgePieces.CastleCorridorTBalconyPiece(param5, var0, param4)
                : null;
        }

        @Override
        public boolean postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            BlockState var0 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.NORTH, Boolean.valueOf(true))
                .setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            BlockState var1 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.WEST, Boolean.valueOf(true))
                .setValue(FenceBlock.EAST, Boolean.valueOf(true));
            this.generateBox(param0, param4, 0, 0, 0, 8, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 0, 8, 5, 8, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 6, 0, 8, 6, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 0, 2, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 6, 2, 0, 8, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 3, 0, 1, 4, 0, var1, var1, false);
            this.generateBox(param0, param4, 7, 3, 0, 7, 4, 0, var1, var1, false);
            this.generateBox(param0, param4, 0, 2, 4, 8, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 1, 4, 2, 2, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 6, 1, 4, 7, 2, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 3, 8, 7, 3, 8, var1, var1, false);
            this.placeBlock(
                param0,
                Blocks.NETHER_BRICK_FENCE
                    .defaultBlockState()
                    .setValue(FenceBlock.EAST, Boolean.valueOf(true))
                    .setValue(FenceBlock.SOUTH, Boolean.valueOf(true)),
                0,
                3,
                8,
                param4
            );
            this.placeBlock(
                param0,
                Blocks.NETHER_BRICK_FENCE
                    .defaultBlockState()
                    .setValue(FenceBlock.WEST, Boolean.valueOf(true))
                    .setValue(FenceBlock.SOUTH, Boolean.valueOf(true)),
                8,
                3,
                8,
                param4
            );
            this.generateBox(param0, param4, 0, 3, 6, 0, 3, 7, var0, var0, false);
            this.generateBox(param0, param4, 8, 3, 6, 8, 3, 7, var0, var0, false);
            this.generateBox(param0, param4, 0, 3, 4, 0, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 8, 3, 4, 8, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 3, 5, 2, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 6, 3, 5, 7, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 4, 5, 1, 5, 5, var1, var1, false);
            this.generateBox(param0, param4, 7, 4, 5, 7, 5, 5, var1, var1, false);

            for(int var2 = 0; var2 <= 5; ++var2) {
                for(int var3 = 0; var3 <= 8; ++var3) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var3, -1, var2, param4);
                }
            }

            return true;
        }
    }

    public static class CastleEntrance extends NetherBridgePieces.NetherBridgePiece {
        public CastleEntrance(int param0, Random param1, BoundingBox param2, Direction param3) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_ENTRANCE, param0);
            this.setOrientation(param3);
            this.boundingBox = param2;
        }

        public CastleEntrance(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_ENTRANCE, param1);
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
            this.generateChildForward((NetherBridgePieces.StartPiece)param0, param1, param2, 5, 3, true);
        }

        public static NetherBridgePieces.CastleEntrance createPiece(
            List<StructurePiece> param0, Random param1, int param2, int param3, int param4, Direction param5, int param6
        ) {
            BoundingBox var0 = BoundingBox.orientBox(param2, param3, param4, -5, -3, 0, 13, 14, 13, param5);
            return isOkBox(var0) && StructurePiece.findCollisionPiece(param0, var0) == null
                ? new NetherBridgePieces.CastleEntrance(param6, param1, var0, param5)
                : null;
        }

        @Override
        public boolean postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            this.generateBox(param0, param4, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 5, 0, 12, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(
                param0, param4, 5, 8, 0, 7, 8, 0, Blocks.NETHER_BRICK_FENCE.defaultBlockState(), Blocks.NETHER_BRICK_FENCE.defaultBlockState(), false
            );
            BlockState var0 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.WEST, Boolean.valueOf(true))
                .setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState var1 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.NORTH, Boolean.valueOf(true))
                .setValue(FenceBlock.SOUTH, Boolean.valueOf(true));

            for(int var2 = 1; var2 <= 11; var2 += 2) {
                this.generateBox(param0, param4, var2, 10, 0, var2, 11, 0, var0, var0, false);
                this.generateBox(param0, param4, var2, 10, 12, var2, 11, 12, var0, var0, false);
                this.generateBox(param0, param4, 0, 10, var2, 0, 11, var2, var1, var1, false);
                this.generateBox(param0, param4, 12, 10, var2, 12, 11, var2, var1, var1, false);
                this.placeBlock(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var2, 13, 0, param4);
                this.placeBlock(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var2, 13, 12, param4);
                this.placeBlock(param0, Blocks.NETHER_BRICKS.defaultBlockState(), 0, 13, var2, param4);
                this.placeBlock(param0, Blocks.NETHER_BRICKS.defaultBlockState(), 12, 13, var2, param4);
                if (var2 != 11) {
                    this.placeBlock(param0, var0, var2 + 1, 13, 0, param4);
                    this.placeBlock(param0, var0, var2 + 1, 13, 12, param4);
                    this.placeBlock(param0, var1, 0, 13, var2 + 1, param4);
                    this.placeBlock(param0, var1, 12, 13, var2 + 1, param4);
                }
            }

            this.placeBlock(
                param0,
                Blocks.NETHER_BRICK_FENCE
                    .defaultBlockState()
                    .setValue(FenceBlock.NORTH, Boolean.valueOf(true))
                    .setValue(FenceBlock.EAST, Boolean.valueOf(true)),
                0,
                13,
                0,
                param4
            );
            this.placeBlock(
                param0,
                Blocks.NETHER_BRICK_FENCE
                    .defaultBlockState()
                    .setValue(FenceBlock.SOUTH, Boolean.valueOf(true))
                    .setValue(FenceBlock.EAST, Boolean.valueOf(true)),
                0,
                13,
                12,
                param4
            );
            this.placeBlock(
                param0,
                Blocks.NETHER_BRICK_FENCE
                    .defaultBlockState()
                    .setValue(FenceBlock.SOUTH, Boolean.valueOf(true))
                    .setValue(FenceBlock.WEST, Boolean.valueOf(true)),
                12,
                13,
                12,
                param4
            );
            this.placeBlock(
                param0,
                Blocks.NETHER_BRICK_FENCE
                    .defaultBlockState()
                    .setValue(FenceBlock.NORTH, Boolean.valueOf(true))
                    .setValue(FenceBlock.WEST, Boolean.valueOf(true)),
                12,
                13,
                0,
                param4
            );

            for(int var3 = 3; var3 <= 9; var3 += 2) {
                this.generateBox(
                    param0,
                    param4,
                    1,
                    7,
                    var3,
                    1,
                    8,
                    var3,
                    var1.setValue(FenceBlock.WEST, Boolean.valueOf(true)),
                    var1.setValue(FenceBlock.WEST, Boolean.valueOf(true)),
                    false
                );
                this.generateBox(
                    param0,
                    param4,
                    11,
                    7,
                    var3,
                    11,
                    8,
                    var3,
                    var1.setValue(FenceBlock.EAST, Boolean.valueOf(true)),
                    var1.setValue(FenceBlock.EAST, Boolean.valueOf(true)),
                    false
                );
            }

            this.generateBox(param0, param4, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for(int var4 = 4; var4 <= 8; ++var4) {
                for(int var5 = 0; var5 <= 2; ++var5) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var4, -1, var5, param4);
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var4, -1, 12 - var5, param4);
                }
            }

            for(int var6 = 0; var6 <= 2; ++var6) {
                for(int var7 = 4; var7 <= 8; ++var7) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var6, -1, var7, param4);
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), 12 - var6, -1, var7, param4);
                }
            }

            this.generateBox(param0, param4, 5, 5, 5, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 6, 1, 6, 6, 4, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.placeBlock(param0, Blocks.NETHER_BRICKS.defaultBlockState(), 6, 0, 6, param4);
            this.placeBlock(param0, Blocks.LAVA.defaultBlockState(), 6, 5, 6, param4);
            BlockPos var8 = new BlockPos(this.getWorldX(6, 6), this.getWorldY(5), this.getWorldZ(6, 6));
            if (param4.isInside(var8)) {
                param0.getLiquidTicks().scheduleTick(var8, Fluids.LAVA, 0);
            }

            return true;
        }
    }

    public static class CastleSmallCorridorCrossingPiece extends NetherBridgePieces.NetherBridgePiece {
        public CastleSmallCorridorCrossingPiece(int param0, BoundingBox param1, Direction param2) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING, param0);
            this.setOrientation(param2);
            this.boundingBox = param1;
        }

        public CastleSmallCorridorCrossingPiece(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING, param1);
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
            this.generateChildForward((NetherBridgePieces.StartPiece)param0, param1, param2, 1, 0, true);
            this.generateChildLeft((NetherBridgePieces.StartPiece)param0, param1, param2, 0, 1, true);
            this.generateChildRight((NetherBridgePieces.StartPiece)param0, param1, param2, 0, 1, true);
        }

        public static NetherBridgePieces.CastleSmallCorridorCrossingPiece createPiece(
            List<StructurePiece> param0, int param1, int param2, int param3, Direction param4, int param5
        ) {
            BoundingBox var0 = BoundingBox.orientBox(param1, param2, param3, -1, 0, 0, 5, 7, 5, param4);
            return isOkBox(var0) && StructurePiece.findCollisionPiece(param0, var0) == null
                ? new NetherBridgePieces.CastleSmallCorridorCrossingPiece(param5, var0, param4)
                : null;
        }

        @Override
        public boolean postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            this.generateBox(param0, param4, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 4, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 4, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for(int var0 = 0; var0 <= 4; ++var0) {
                for(int var1 = 0; var1 <= 4; ++var1) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var0, -1, var1, param4);
                }
            }

            return true;
        }
    }

    public static class CastleSmallCorridorLeftTurnPiece extends NetherBridgePieces.NetherBridgePiece {
        private boolean isNeedingChest;

        public CastleSmallCorridorLeftTurnPiece(int param0, Random param1, BoundingBox param2, Direction param3) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN, param0);
            this.setOrientation(param3);
            this.boundingBox = param2;
            this.isNeedingChest = param1.nextInt(3) == 0;
        }

        public CastleSmallCorridorLeftTurnPiece(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN, param1);
            this.isNeedingChest = param1.getBoolean("Chest");
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag param0) {
            super.addAdditionalSaveData(param0);
            param0.putBoolean("Chest", this.isNeedingChest);
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
            this.generateChildLeft((NetherBridgePieces.StartPiece)param0, param1, param2, 0, 1, true);
        }

        public static NetherBridgePieces.CastleSmallCorridorLeftTurnPiece createPiece(
            List<StructurePiece> param0, Random param1, int param2, int param3, int param4, Direction param5, int param6
        ) {
            BoundingBox var0 = BoundingBox.orientBox(param2, param3, param4, -1, 0, 0, 5, 7, 5, param5);
            return isOkBox(var0) && StructurePiece.findCollisionPiece(param0, var0) == null
                ? new NetherBridgePieces.CastleSmallCorridorLeftTurnPiece(param6, param1, var0, param5)
                : null;
        }

        @Override
        public boolean postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            this.generateBox(param0, param4, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            BlockState var0 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.WEST, Boolean.valueOf(true))
                .setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState var1 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.NORTH, Boolean.valueOf(true))
                .setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            this.generateBox(param0, param4, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 4, 3, 1, 4, 4, 1, var1, var1, false);
            this.generateBox(param0, param4, 4, 3, 3, 4, 4, 3, var1, var1, false);
            this.generateBox(param0, param4, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 4, 3, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 3, 4, 1, 4, 4, var0, var0, false);
            this.generateBox(param0, param4, 3, 3, 4, 3, 4, 4, var0, var0, false);
            if (this.isNeedingChest && param4.isInside(new BlockPos(this.getWorldX(3, 3), this.getWorldY(2), this.getWorldZ(3, 3)))) {
                this.isNeedingChest = false;
                this.createChest(param0, param4, param3, 3, 2, 3, BuiltInLootTables.NETHER_BRIDGE);
            }

            this.generateBox(param0, param4, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for(int var2 = 0; var2 <= 4; ++var2) {
                for(int var3 = 0; var3 <= 4; ++var3) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var2, -1, var3, param4);
                }
            }

            return true;
        }
    }

    public static class CastleSmallCorridorPiece extends NetherBridgePieces.NetherBridgePiece {
        public CastleSmallCorridorPiece(int param0, BoundingBox param1, Direction param2) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR, param0);
            this.setOrientation(param2);
            this.boundingBox = param1;
        }

        public CastleSmallCorridorPiece(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR, param1);
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
            this.generateChildForward((NetherBridgePieces.StartPiece)param0, param1, param2, 1, 0, true);
        }

        public static NetherBridgePieces.CastleSmallCorridorPiece createPiece(
            List<StructurePiece> param0, int param1, int param2, int param3, Direction param4, int param5
        ) {
            BoundingBox var0 = BoundingBox.orientBox(param1, param2, param3, -1, 0, 0, 5, 7, 5, param4);
            return isOkBox(var0) && StructurePiece.findCollisionPiece(param0, var0) == null
                ? new NetherBridgePieces.CastleSmallCorridorPiece(param5, var0, param4)
                : null;
        }

        @Override
        public boolean postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            this.generateBox(param0, param4, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            BlockState var0 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.NORTH, Boolean.valueOf(true))
                .setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            this.generateBox(param0, param4, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 3, 1, 0, 4, 1, var0, var0, false);
            this.generateBox(param0, param4, 0, 3, 3, 0, 4, 3, var0, var0, false);
            this.generateBox(param0, param4, 4, 3, 1, 4, 4, 1, var0, var0, false);
            this.generateBox(param0, param4, 4, 3, 3, 4, 4, 3, var0, var0, false);
            this.generateBox(param0, param4, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for(int var1 = 0; var1 <= 4; ++var1) {
                for(int var2 = 0; var2 <= 4; ++var2) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var1, -1, var2, param4);
                }
            }

            return true;
        }
    }

    public static class CastleSmallCorridorRightTurnPiece extends NetherBridgePieces.NetherBridgePiece {
        private boolean isNeedingChest;

        public CastleSmallCorridorRightTurnPiece(int param0, Random param1, BoundingBox param2, Direction param3) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN, param0);
            this.setOrientation(param3);
            this.boundingBox = param2;
            this.isNeedingChest = param1.nextInt(3) == 0;
        }

        public CastleSmallCorridorRightTurnPiece(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN, param1);
            this.isNeedingChest = param1.getBoolean("Chest");
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag param0) {
            super.addAdditionalSaveData(param0);
            param0.putBoolean("Chest", this.isNeedingChest);
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
            this.generateChildRight((NetherBridgePieces.StartPiece)param0, param1, param2, 0, 1, true);
        }

        public static NetherBridgePieces.CastleSmallCorridorRightTurnPiece createPiece(
            List<StructurePiece> param0, Random param1, int param2, int param3, int param4, Direction param5, int param6
        ) {
            BoundingBox var0 = BoundingBox.orientBox(param2, param3, param4, -1, 0, 0, 5, 7, 5, param5);
            return isOkBox(var0) && StructurePiece.findCollisionPiece(param0, var0) == null
                ? new NetherBridgePieces.CastleSmallCorridorRightTurnPiece(param6, param1, var0, param5)
                : null;
        }

        @Override
        public boolean postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            this.generateBox(param0, param4, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            BlockState var0 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.WEST, Boolean.valueOf(true))
                .setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState var1 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.NORTH, Boolean.valueOf(true))
                .setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            this.generateBox(param0, param4, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 3, 1, 0, 4, 1, var1, var1, false);
            this.generateBox(param0, param4, 0, 3, 3, 0, 4, 3, var1, var1, false);
            this.generateBox(param0, param4, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 3, 4, 1, 4, 4, var0, var0, false);
            this.generateBox(param0, param4, 3, 3, 4, 3, 4, 4, var0, var0, false);
            if (this.isNeedingChest && param4.isInside(new BlockPos(this.getWorldX(1, 3), this.getWorldY(2), this.getWorldZ(1, 3)))) {
                this.isNeedingChest = false;
                this.createChest(param0, param4, param3, 1, 2, 3, BuiltInLootTables.NETHER_BRIDGE);
            }

            this.generateBox(param0, param4, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for(int var2 = 0; var2 <= 4; ++var2) {
                for(int var3 = 0; var3 <= 4; ++var3) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var2, -1, var3, param4);
                }
            }

            return true;
        }
    }

    public static class CastleStalkRoom extends NetherBridgePieces.NetherBridgePiece {
        public CastleStalkRoom(int param0, BoundingBox param1, Direction param2) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM, param0);
            this.setOrientation(param2);
            this.boundingBox = param1;
        }

        public CastleStalkRoom(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM, param1);
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
            this.generateChildForward((NetherBridgePieces.StartPiece)param0, param1, param2, 5, 3, true);
            this.generateChildForward((NetherBridgePieces.StartPiece)param0, param1, param2, 5, 11, true);
        }

        public static NetherBridgePieces.CastleStalkRoom createPiece(
            List<StructurePiece> param0, int param1, int param2, int param3, Direction param4, int param5
        ) {
            BoundingBox var0 = BoundingBox.orientBox(param1, param2, param3, -5, -3, 0, 13, 14, 13, param4);
            return isOkBox(var0) && StructurePiece.findCollisionPiece(param0, var0) == null
                ? new NetherBridgePieces.CastleStalkRoom(param5, var0, param4)
                : null;
        }

        @Override
        public boolean postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            this.generateBox(param0, param4, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 5, 0, 12, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState var0 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.WEST, Boolean.valueOf(true))
                .setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState var1 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.NORTH, Boolean.valueOf(true))
                .setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            BlockState var2 = var1.setValue(FenceBlock.WEST, Boolean.valueOf(true));
            BlockState var3 = var1.setValue(FenceBlock.EAST, Boolean.valueOf(true));

            for(int var4 = 1; var4 <= 11; var4 += 2) {
                this.generateBox(param0, param4, var4, 10, 0, var4, 11, 0, var0, var0, false);
                this.generateBox(param0, param4, var4, 10, 12, var4, 11, 12, var0, var0, false);
                this.generateBox(param0, param4, 0, 10, var4, 0, 11, var4, var1, var1, false);
                this.generateBox(param0, param4, 12, 10, var4, 12, 11, var4, var1, var1, false);
                this.placeBlock(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var4, 13, 0, param4);
                this.placeBlock(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var4, 13, 12, param4);
                this.placeBlock(param0, Blocks.NETHER_BRICKS.defaultBlockState(), 0, 13, var4, param4);
                this.placeBlock(param0, Blocks.NETHER_BRICKS.defaultBlockState(), 12, 13, var4, param4);
                if (var4 != 11) {
                    this.placeBlock(param0, var0, var4 + 1, 13, 0, param4);
                    this.placeBlock(param0, var0, var4 + 1, 13, 12, param4);
                    this.placeBlock(param0, var1, 0, 13, var4 + 1, param4);
                    this.placeBlock(param0, var1, 12, 13, var4 + 1, param4);
                }
            }

            this.placeBlock(
                param0,
                Blocks.NETHER_BRICK_FENCE
                    .defaultBlockState()
                    .setValue(FenceBlock.NORTH, Boolean.valueOf(true))
                    .setValue(FenceBlock.EAST, Boolean.valueOf(true)),
                0,
                13,
                0,
                param4
            );
            this.placeBlock(
                param0,
                Blocks.NETHER_BRICK_FENCE
                    .defaultBlockState()
                    .setValue(FenceBlock.SOUTH, Boolean.valueOf(true))
                    .setValue(FenceBlock.EAST, Boolean.valueOf(true)),
                0,
                13,
                12,
                param4
            );
            this.placeBlock(
                param0,
                Blocks.NETHER_BRICK_FENCE
                    .defaultBlockState()
                    .setValue(FenceBlock.SOUTH, Boolean.valueOf(true))
                    .setValue(FenceBlock.WEST, Boolean.valueOf(true)),
                12,
                13,
                12,
                param4
            );
            this.placeBlock(
                param0,
                Blocks.NETHER_BRICK_FENCE
                    .defaultBlockState()
                    .setValue(FenceBlock.NORTH, Boolean.valueOf(true))
                    .setValue(FenceBlock.WEST, Boolean.valueOf(true)),
                12,
                13,
                0,
                param4
            );

            for(int var5 = 3; var5 <= 9; var5 += 2) {
                this.generateBox(param0, param4, 1, 7, var5, 1, 8, var5, var2, var2, false);
                this.generateBox(param0, param4, 11, 7, var5, 11, 8, var5, var3, var3, false);
            }

            BlockState var6 = Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);

            for(int var7 = 0; var7 <= 6; ++var7) {
                int var8 = var7 + 4;

                for(int var9 = 5; var9 <= 7; ++var9) {
                    this.placeBlock(param0, var6, var9, 5 + var7, var8, param4);
                }

                if (var8 >= 5 && var8 <= 8) {
                    this.generateBox(
                        param0,
                        param4,
                        5,
                        5,
                        var8,
                        7,
                        var7 + 4,
                        var8,
                        Blocks.NETHER_BRICKS.defaultBlockState(),
                        Blocks.NETHER_BRICKS.defaultBlockState(),
                        false
                    );
                } else if (var8 >= 9 && var8 <= 10) {
                    this.generateBox(
                        param0,
                        param4,
                        5,
                        8,
                        var8,
                        7,
                        var7 + 4,
                        var8,
                        Blocks.NETHER_BRICKS.defaultBlockState(),
                        Blocks.NETHER_BRICKS.defaultBlockState(),
                        false
                    );
                }

                if (var7 >= 1) {
                    this.generateBox(
                        param0, param4, 5, 6 + var7, var8, 7, 9 + var7, var8, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false
                    );
                }
            }

            for(int var10 = 5; var10 <= 7; ++var10) {
                this.placeBlock(param0, var6, var10, 12, 11, param4);
            }

            this.generateBox(param0, param4, 5, 6, 7, 5, 7, 7, var3, var3, false);
            this.generateBox(param0, param4, 7, 6, 7, 7, 7, 7, var2, var2, false);
            this.generateBox(param0, param4, 5, 13, 12, 7, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 5, 2, 3, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 5, 9, 3, 5, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 5, 4, 2, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 9, 5, 2, 10, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 9, 5, 9, 10, 5, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 10, 5, 4, 10, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState var11 = var6.setValue(StairBlock.FACING, Direction.EAST);
            BlockState var12 = var6.setValue(StairBlock.FACING, Direction.WEST);
            this.placeBlock(param0, var12, 4, 5, 2, param4);
            this.placeBlock(param0, var12, 4, 5, 3, param4);
            this.placeBlock(param0, var12, 4, 5, 9, param4);
            this.placeBlock(param0, var12, 4, 5, 10, param4);
            this.placeBlock(param0, var11, 8, 5, 2, param4);
            this.placeBlock(param0, var11, 8, 5, 3, param4);
            this.placeBlock(param0, var11, 8, 5, 9, param4);
            this.placeBlock(param0, var11, 8, 5, 10, param4);
            this.generateBox(param0, param4, 3, 4, 4, 4, 4, 8, Blocks.SOUL_SAND.defaultBlockState(), Blocks.SOUL_SAND.defaultBlockState(), false);
            this.generateBox(param0, param4, 8, 4, 4, 9, 4, 8, Blocks.SOUL_SAND.defaultBlockState(), Blocks.SOUL_SAND.defaultBlockState(), false);
            this.generateBox(param0, param4, 3, 5, 4, 4, 5, 8, Blocks.NETHER_WART.defaultBlockState(), Blocks.NETHER_WART.defaultBlockState(), false);
            this.generateBox(param0, param4, 8, 5, 4, 9, 5, 8, Blocks.NETHER_WART.defaultBlockState(), Blocks.NETHER_WART.defaultBlockState(), false);
            this.generateBox(param0, param4, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

            for(int var13 = 4; var13 <= 8; ++var13) {
                for(int var14 = 0; var14 <= 2; ++var14) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var13, -1, var14, param4);
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var13, -1, 12 - var14, param4);
                }
            }

            for(int var15 = 0; var15 <= 2; ++var15) {
                for(int var16 = 4; var16 <= 8; ++var16) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var15, -1, var16, param4);
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), 12 - var15, -1, var16, param4);
                }
            }

            return true;
        }
    }

    public static class MonsterThrone extends NetherBridgePieces.NetherBridgePiece {
        private boolean hasPlacedSpawner;

        public MonsterThrone(int param0, BoundingBox param1, Direction param2) {
            super(StructurePieceType.NETHER_FORTRESS_MONSTER_THRONE, param0);
            this.setOrientation(param2);
            this.boundingBox = param1;
        }

        public MonsterThrone(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FORTRESS_MONSTER_THRONE, param1);
            this.hasPlacedSpawner = param1.getBoolean("Mob");
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag param0) {
            super.addAdditionalSaveData(param0);
            param0.putBoolean("Mob", this.hasPlacedSpawner);
        }

        public static NetherBridgePieces.MonsterThrone createPiece(
            List<StructurePiece> param0, int param1, int param2, int param3, int param4, Direction param5
        ) {
            BoundingBox var0 = BoundingBox.orientBox(param1, param2, param3, -2, 0, 0, 7, 8, 9, param5);
            return isOkBox(var0) && StructurePiece.findCollisionPiece(param0, var0) == null ? new NetherBridgePieces.MonsterThrone(param4, var0, param5) : null;
        }

        @Override
        public boolean postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            this.generateBox(param0, param4, 0, 2, 0, 6, 7, 7, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 0, 0, 5, 1, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 2, 1, 5, 2, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 3, 2, 5, 3, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 4, 3, 5, 4, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 2, 0, 1, 4, 2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 5, 2, 0, 5, 4, 2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 5, 2, 1, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 5, 5, 2, 5, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 5, 3, 0, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 6, 5, 3, 6, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 5, 8, 5, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState var0 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.WEST, Boolean.valueOf(true))
                .setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState var1 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.NORTH, Boolean.valueOf(true))
                .setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            this.placeBlock(param0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)), 1, 6, 3, param4);
            this.placeBlock(param0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)), 5, 6, 3, param4);
            this.placeBlock(
                param0,
                Blocks.NETHER_BRICK_FENCE
                    .defaultBlockState()
                    .setValue(FenceBlock.EAST, Boolean.valueOf(true))
                    .setValue(FenceBlock.NORTH, Boolean.valueOf(true)),
                0,
                6,
                3,
                param4
            );
            this.placeBlock(
                param0,
                Blocks.NETHER_BRICK_FENCE
                    .defaultBlockState()
                    .setValue(FenceBlock.WEST, Boolean.valueOf(true))
                    .setValue(FenceBlock.NORTH, Boolean.valueOf(true)),
                6,
                6,
                3,
                param4
            );
            this.generateBox(param0, param4, 0, 6, 4, 0, 6, 7, var1, var1, false);
            this.generateBox(param0, param4, 6, 6, 4, 6, 6, 7, var1, var1, false);
            this.placeBlock(
                param0,
                Blocks.NETHER_BRICK_FENCE
                    .defaultBlockState()
                    .setValue(FenceBlock.EAST, Boolean.valueOf(true))
                    .setValue(FenceBlock.SOUTH, Boolean.valueOf(true)),
                0,
                6,
                8,
                param4
            );
            this.placeBlock(
                param0,
                Blocks.NETHER_BRICK_FENCE
                    .defaultBlockState()
                    .setValue(FenceBlock.WEST, Boolean.valueOf(true))
                    .setValue(FenceBlock.SOUTH, Boolean.valueOf(true)),
                6,
                6,
                8,
                param4
            );
            this.generateBox(param0, param4, 1, 6, 8, 5, 6, 8, var0, var0, false);
            this.placeBlock(param0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)), 1, 7, 8, param4);
            this.generateBox(param0, param4, 2, 7, 8, 4, 7, 8, var0, var0, false);
            this.placeBlock(param0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)), 5, 7, 8, param4);
            this.placeBlock(param0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)), 2, 8, 8, param4);
            this.placeBlock(param0, var0, 3, 8, 8, param4);
            this.placeBlock(param0, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)), 4, 8, 8, param4);
            if (!this.hasPlacedSpawner) {
                BlockPos var2 = new BlockPos(this.getWorldX(3, 5), this.getWorldY(5), this.getWorldZ(3, 5));
                if (param4.isInside(var2)) {
                    this.hasPlacedSpawner = true;
                    param0.setBlock(var2, Blocks.SPAWNER.defaultBlockState(), 2);
                    BlockEntity var3 = param0.getBlockEntity(var2);
                    if (var3 instanceof SpawnerBlockEntity) {
                        ((SpawnerBlockEntity)var3).getSpawner().setEntityId(EntityType.BLAZE);
                    }
                }
            }

            for(int var4 = 0; var4 <= 6; ++var4) {
                for(int var5 = 0; var5 <= 6; ++var5) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var4, -1, var5, param4);
                }
            }

            return true;
        }
    }

    abstract static class NetherBridgePiece extends StructurePiece {
        protected NetherBridgePiece(StructurePieceType param0, int param1) {
            super(param0, param1);
        }

        public NetherBridgePiece(StructurePieceType param0, CompoundTag param1) {
            super(param0, param1);
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag param0) {
        }

        private int updatePieceWeight(List<NetherBridgePieces.PieceWeight> param0) {
            boolean var0 = false;
            int var1 = 0;

            for(NetherBridgePieces.PieceWeight var2 : param0) {
                if (var2.maxPlaceCount > 0 && var2.placeCount < var2.maxPlaceCount) {
                    var0 = true;
                }

                var1 += var2.weight;
            }

            return var0 ? var1 : -1;
        }

        private NetherBridgePieces.NetherBridgePiece generatePiece(
            NetherBridgePieces.StartPiece param0,
            List<NetherBridgePieces.PieceWeight> param1,
            List<StructurePiece> param2,
            Random param3,
            int param4,
            int param5,
            int param6,
            Direction param7,
            int param8
        ) {
            int var0 = this.updatePieceWeight(param1);
            boolean var1 = var0 > 0 && param8 <= 30;
            int var2 = 0;

            while(var2 < 5 && var1) {
                ++var2;
                int var3 = param3.nextInt(var0);

                for(NetherBridgePieces.PieceWeight var4 : param1) {
                    var3 -= var4.weight;
                    if (var3 < 0) {
                        if (!var4.doPlace(param8) || var4 == param0.previousPiece && !var4.allowInRow) {
                            break;
                        }

                        NetherBridgePieces.NetherBridgePiece var5 = NetherBridgePieces.findAndCreateBridgePieceFactory(
                            var4, param2, param3, param4, param5, param6, param7, param8
                        );
                        if (var5 != null) {
                            ++var4.placeCount;
                            param0.previousPiece = var4;
                            if (!var4.isValid()) {
                                param1.remove(var4);
                            }

                            return var5;
                        }
                    }
                }
            }

            return NetherBridgePieces.BridgeEndFiller.createPiece(param2, param3, param4, param5, param6, param7, param8);
        }

        private StructurePiece generateAndAddPiece(
            NetherBridgePieces.StartPiece param0,
            List<StructurePiece> param1,
            Random param2,
            int param3,
            int param4,
            int param5,
            @Nullable Direction param6,
            int param7,
            boolean param8
        ) {
            if (Math.abs(param3 - param0.getBoundingBox().x0) <= 112 && Math.abs(param5 - param0.getBoundingBox().z0) <= 112) {
                List<NetherBridgePieces.PieceWeight> var0 = param0.availableBridgePieces;
                if (param8) {
                    var0 = param0.availableCastlePieces;
                }

                StructurePiece var1 = this.generatePiece(param0, var0, param1, param2, param3, param4, param5, param6, param7 + 1);
                if (var1 != null) {
                    param1.add(var1);
                    param0.pendingChildren.add(var1);
                }

                return var1;
            } else {
                return NetherBridgePieces.BridgeEndFiller.createPiece(param1, param2, param3, param4, param5, param6, param7);
            }
        }

        @Nullable
        protected StructurePiece generateChildForward(
            NetherBridgePieces.StartPiece param0, List<StructurePiece> param1, Random param2, int param3, int param4, boolean param5
        ) {
            Direction var0 = this.getOrientation();
            if (var0 != null) {
                switch(var0) {
                    case NORTH:
                        return this.generateAndAddPiece(
                            param0,
                            param1,
                            param2,
                            this.boundingBox.x0 + param3,
                            this.boundingBox.y0 + param4,
                            this.boundingBox.z0 - 1,
                            var0,
                            this.getGenDepth(),
                            param5
                        );
                    case SOUTH:
                        return this.generateAndAddPiece(
                            param0,
                            param1,
                            param2,
                            this.boundingBox.x0 + param3,
                            this.boundingBox.y0 + param4,
                            this.boundingBox.z1 + 1,
                            var0,
                            this.getGenDepth(),
                            param5
                        );
                    case WEST:
                        return this.generateAndAddPiece(
                            param0,
                            param1,
                            param2,
                            this.boundingBox.x0 - 1,
                            this.boundingBox.y0 + param4,
                            this.boundingBox.z0 + param3,
                            var0,
                            this.getGenDepth(),
                            param5
                        );
                    case EAST:
                        return this.generateAndAddPiece(
                            param0,
                            param1,
                            param2,
                            this.boundingBox.x1 + 1,
                            this.boundingBox.y0 + param4,
                            this.boundingBox.z0 + param3,
                            var0,
                            this.getGenDepth(),
                            param5
                        );
                }
            }

            return null;
        }

        @Nullable
        protected StructurePiece generateChildLeft(
            NetherBridgePieces.StartPiece param0, List<StructurePiece> param1, Random param2, int param3, int param4, boolean param5
        ) {
            Direction var0 = this.getOrientation();
            if (var0 != null) {
                switch(var0) {
                    case NORTH:
                        return this.generateAndAddPiece(
                            param0,
                            param1,
                            param2,
                            this.boundingBox.x0 - 1,
                            this.boundingBox.y0 + param3,
                            this.boundingBox.z0 + param4,
                            Direction.WEST,
                            this.getGenDepth(),
                            param5
                        );
                    case SOUTH:
                        return this.generateAndAddPiece(
                            param0,
                            param1,
                            param2,
                            this.boundingBox.x0 - 1,
                            this.boundingBox.y0 + param3,
                            this.boundingBox.z0 + param4,
                            Direction.WEST,
                            this.getGenDepth(),
                            param5
                        );
                    case WEST:
                        return this.generateAndAddPiece(
                            param0,
                            param1,
                            param2,
                            this.boundingBox.x0 + param4,
                            this.boundingBox.y0 + param3,
                            this.boundingBox.z0 - 1,
                            Direction.NORTH,
                            this.getGenDepth(),
                            param5
                        );
                    case EAST:
                        return this.generateAndAddPiece(
                            param0,
                            param1,
                            param2,
                            this.boundingBox.x0 + param4,
                            this.boundingBox.y0 + param3,
                            this.boundingBox.z0 - 1,
                            Direction.NORTH,
                            this.getGenDepth(),
                            param5
                        );
                }
            }

            return null;
        }

        @Nullable
        protected StructurePiece generateChildRight(
            NetherBridgePieces.StartPiece param0, List<StructurePiece> param1, Random param2, int param3, int param4, boolean param5
        ) {
            Direction var0 = this.getOrientation();
            if (var0 != null) {
                switch(var0) {
                    case NORTH:
                        return this.generateAndAddPiece(
                            param0,
                            param1,
                            param2,
                            this.boundingBox.x1 + 1,
                            this.boundingBox.y0 + param3,
                            this.boundingBox.z0 + param4,
                            Direction.EAST,
                            this.getGenDepth(),
                            param5
                        );
                    case SOUTH:
                        return this.generateAndAddPiece(
                            param0,
                            param1,
                            param2,
                            this.boundingBox.x1 + 1,
                            this.boundingBox.y0 + param3,
                            this.boundingBox.z0 + param4,
                            Direction.EAST,
                            this.getGenDepth(),
                            param5
                        );
                    case WEST:
                        return this.generateAndAddPiece(
                            param0,
                            param1,
                            param2,
                            this.boundingBox.x0 + param4,
                            this.boundingBox.y0 + param3,
                            this.boundingBox.z1 + 1,
                            Direction.SOUTH,
                            this.getGenDepth(),
                            param5
                        );
                    case EAST:
                        return this.generateAndAddPiece(
                            param0,
                            param1,
                            param2,
                            this.boundingBox.x0 + param4,
                            this.boundingBox.y0 + param3,
                            this.boundingBox.z1 + 1,
                            Direction.SOUTH,
                            this.getGenDepth(),
                            param5
                        );
                }
            }

            return null;
        }

        protected static boolean isOkBox(BoundingBox param0) {
            return param0 != null && param0.y0 > 10;
        }
    }

    static class PieceWeight {
        public final Class<? extends NetherBridgePieces.NetherBridgePiece> pieceClass;
        public final int weight;
        public int placeCount;
        public final int maxPlaceCount;
        public final boolean allowInRow;

        public PieceWeight(Class<? extends NetherBridgePieces.NetherBridgePiece> param0, int param1, int param2, boolean param3) {
            this.pieceClass = param0;
            this.weight = param1;
            this.maxPlaceCount = param2;
            this.allowInRow = param3;
        }

        public PieceWeight(Class<? extends NetherBridgePieces.NetherBridgePiece> param0, int param1, int param2) {
            this(param0, param1, param2, false);
        }

        public boolean doPlace(int param0) {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }

        public boolean isValid() {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }
    }

    public static class RoomCrossing extends NetherBridgePieces.NetherBridgePiece {
        public RoomCrossing(int param0, BoundingBox param1, Direction param2) {
            super(StructurePieceType.NETHER_FORTRESS_ROOM_CROSSING, param0);
            this.setOrientation(param2);
            this.boundingBox = param1;
        }

        public RoomCrossing(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FORTRESS_ROOM_CROSSING, param1);
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
            this.generateChildForward((NetherBridgePieces.StartPiece)param0, param1, param2, 2, 0, false);
            this.generateChildLeft((NetherBridgePieces.StartPiece)param0, param1, param2, 0, 2, false);
            this.generateChildRight((NetherBridgePieces.StartPiece)param0, param1, param2, 0, 2, false);
        }

        public static NetherBridgePieces.RoomCrossing createPiece(List<StructurePiece> param0, int param1, int param2, int param3, Direction param4, int param5) {
            BoundingBox var0 = BoundingBox.orientBox(param1, param2, param3, -2, 0, 0, 7, 9, 7, param4);
            return isOkBox(var0) && StructurePiece.findCollisionPiece(param0, var0) == null ? new NetherBridgePieces.RoomCrossing(param5, var0, param4) : null;
        }

        @Override
        public boolean postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            this.generateBox(param0, param4, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 0, 6, 7, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 0, 1, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 6, 1, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 5, 2, 0, 6, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 5, 2, 6, 6, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 0, 0, 6, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 5, 0, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 6, 2, 0, 6, 6, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 6, 2, 5, 6, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState var0 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.WEST, Boolean.valueOf(true))
                .setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState var1 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.NORTH, Boolean.valueOf(true))
                .setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            this.generateBox(param0, param4, 2, 6, 0, 4, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 5, 0, 4, 5, 0, var0, var0, false);
            this.generateBox(param0, param4, 2, 6, 6, 4, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 5, 6, 4, 5, 6, var0, var0, false);
            this.generateBox(param0, param4, 0, 6, 2, 0, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 5, 2, 0, 5, 4, var1, var1, false);
            this.generateBox(param0, param4, 6, 6, 2, 6, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 6, 5, 2, 6, 5, 4, var1, var1, false);

            for(int var2 = 0; var2 <= 6; ++var2) {
                for(int var3 = 0; var3 <= 6; ++var3) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var2, -1, var3, param4);
                }
            }

            return true;
        }
    }

    public static class StairsRoom extends NetherBridgePieces.NetherBridgePiece {
        public StairsRoom(int param0, BoundingBox param1, Direction param2) {
            super(StructurePieceType.NETHER_FORTRESS_STAIRS_ROOM, param0);
            this.setOrientation(param2);
            this.boundingBox = param1;
        }

        public StairsRoom(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FORTRESS_STAIRS_ROOM, param1);
        }

        @Override
        public void addChildren(StructurePiece param0, List<StructurePiece> param1, Random param2) {
            this.generateChildRight((NetherBridgePieces.StartPiece)param0, param1, param2, 6, 2, false);
        }

        public static NetherBridgePieces.StairsRoom createPiece(List<StructurePiece> param0, int param1, int param2, int param3, int param4, Direction param5) {
            BoundingBox var0 = BoundingBox.orientBox(param1, param2, param3, -2, 0, 0, 7, 11, 7, param5);
            return isOkBox(var0) && StructurePiece.findCollisionPiece(param0, var0) == null ? new NetherBridgePieces.StairsRoom(param4, var0, param5) : null;
        }

        @Override
        public boolean postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            this.generateBox(param0, param4, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 0, 6, 10, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 0, 1, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 5, 2, 0, 6, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 0, 2, 1, 0, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 6, 2, 1, 6, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 2, 6, 5, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState var0 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.WEST, Boolean.valueOf(true))
                .setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState var1 = Blocks.NETHER_BRICK_FENCE
                .defaultBlockState()
                .setValue(FenceBlock.NORTH, Boolean.valueOf(true))
                .setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            this.generateBox(param0, param4, 0, 3, 2, 0, 5, 4, var1, var1, false);
            this.generateBox(param0, param4, 6, 3, 2, 6, 5, 2, var1, var1, false);
            this.generateBox(param0, param4, 6, 3, 4, 6, 5, 4, var1, var1, false);
            this.placeBlock(param0, Blocks.NETHER_BRICKS.defaultBlockState(), 5, 2, 5, param4);
            this.generateBox(param0, param4, 4, 2, 5, 4, 3, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 3, 2, 5, 3, 4, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 2, 5, 2, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 2, 5, 1, 6, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 7, 1, 5, 7, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 6, 8, 2, 6, 8, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 6, 0, 4, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 5, 0, 4, 5, 0, var0, var0, false);

            for(int var2 = 0; var2 <= 6; ++var2) {
                for(int var3 = 0; var3 <= 6; ++var3) {
                    this.fillColumnDown(param0, Blocks.NETHER_BRICKS.defaultBlockState(), var2, -1, var3, param4);
                }
            }

            return true;
        }
    }

    public static class StartPiece extends NetherBridgePieces.BridgeCrossing {
        public NetherBridgePieces.PieceWeight previousPiece;
        public List<NetherBridgePieces.PieceWeight> availableBridgePieces;
        public List<NetherBridgePieces.PieceWeight> availableCastlePieces;
        public final List<StructurePiece> pendingChildren = Lists.newArrayList();

        public StartPiece(Random param0, int param1, int param2) {
            super(param0, param1, param2);
            this.availableBridgePieces = Lists.newArrayList();

            for(NetherBridgePieces.PieceWeight var0 : NetherBridgePieces.BRIDGE_PIECE_WEIGHTS) {
                var0.placeCount = 0;
                this.availableBridgePieces.add(var0);
            }

            this.availableCastlePieces = Lists.newArrayList();

            for(NetherBridgePieces.PieceWeight var1 : NetherBridgePieces.CASTLE_PIECE_WEIGHTS) {
                var1.placeCount = 0;
                this.availableCastlePieces.add(var1);
            }

        }

        public StartPiece(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FORTRESS_START, param1);
        }
    }
}
