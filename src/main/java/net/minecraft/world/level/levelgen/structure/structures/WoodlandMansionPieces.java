package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class WoodlandMansionPieces {
    public static void generateMansion(
        StructureTemplateManager param0, BlockPos param1, Rotation param2, List<WoodlandMansionPieces.WoodlandMansionPiece> param3, RandomSource param4
    ) {
        WoodlandMansionPieces.MansionGrid var0 = new WoodlandMansionPieces.MansionGrid(param4);
        WoodlandMansionPieces.MansionPiecePlacer var1 = new WoodlandMansionPieces.MansionPiecePlacer(param0, param4);
        var1.createMansion(param1, param2, param3, var0);
    }

    static class FirstFloorRoomCollection extends WoodlandMansionPieces.FloorRoomCollection {
        @Override
        public String get1x1(RandomSource param0) {
            return "1x1_a" + (param0.nextInt(5) + 1);
        }

        @Override
        public String get1x1Secret(RandomSource param0) {
            return "1x1_as" + (param0.nextInt(4) + 1);
        }

        @Override
        public String get1x2SideEntrance(RandomSource param0, boolean param1) {
            return "1x2_a" + (param0.nextInt(9) + 1);
        }

        @Override
        public String get1x2FrontEntrance(RandomSource param0, boolean param1) {
            return "1x2_b" + (param0.nextInt(5) + 1);
        }

        @Override
        public String get1x2Secret(RandomSource param0) {
            return "1x2_s" + (param0.nextInt(2) + 1);
        }

        @Override
        public String get2x2(RandomSource param0) {
            return "2x2_a" + (param0.nextInt(4) + 1);
        }

        @Override
        public String get2x2Secret(RandomSource param0) {
            return "2x2_s1";
        }
    }

    abstract static class FloorRoomCollection {
        public abstract String get1x1(RandomSource var1);

        public abstract String get1x1Secret(RandomSource var1);

        public abstract String get1x2SideEntrance(RandomSource var1, boolean var2);

        public abstract String get1x2FrontEntrance(RandomSource var1, boolean var2);

        public abstract String get1x2Secret(RandomSource var1);

        public abstract String get2x2(RandomSource var1);

        public abstract String get2x2Secret(RandomSource var1);
    }

    static class MansionGrid {
        private static final int DEFAULT_SIZE = 11;
        private static final int CLEAR = 0;
        private static final int CORRIDOR = 1;
        private static final int ROOM = 2;
        private static final int START_ROOM = 3;
        private static final int TEST_ROOM = 4;
        private static final int BLOCKED = 5;
        private static final int ROOM_1x1 = 65536;
        private static final int ROOM_1x2 = 131072;
        private static final int ROOM_2x2 = 262144;
        private static final int ROOM_ORIGIN_FLAG = 1048576;
        private static final int ROOM_DOOR_FLAG = 2097152;
        private static final int ROOM_STAIRS_FLAG = 4194304;
        private static final int ROOM_CORRIDOR_FLAG = 8388608;
        private static final int ROOM_TYPE_MASK = 983040;
        private static final int ROOM_ID_MASK = 65535;
        private final RandomSource random;
        final WoodlandMansionPieces.SimpleGrid baseGrid;
        final WoodlandMansionPieces.SimpleGrid thirdFloorGrid;
        final WoodlandMansionPieces.SimpleGrid[] floorRooms;
        final int entranceX;
        final int entranceY;

        public MansionGrid(RandomSource param0) {
            this.random = param0;
            int var0 = 11;
            this.entranceX = 7;
            this.entranceY = 4;
            this.baseGrid = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
            this.baseGrid.set(this.entranceX, this.entranceY, this.entranceX + 1, this.entranceY + 1, 3);
            this.baseGrid.set(this.entranceX - 1, this.entranceY, this.entranceX - 1, this.entranceY + 1, 2);
            this.baseGrid.set(this.entranceX + 2, this.entranceY - 2, this.entranceX + 3, this.entranceY + 3, 5);
            this.baseGrid.set(this.entranceX + 1, this.entranceY - 2, this.entranceX + 1, this.entranceY - 1, 1);
            this.baseGrid.set(this.entranceX + 1, this.entranceY + 2, this.entranceX + 1, this.entranceY + 3, 1);
            this.baseGrid.set(this.entranceX - 1, this.entranceY - 1, 1);
            this.baseGrid.set(this.entranceX - 1, this.entranceY + 2, 1);
            this.baseGrid.set(0, 0, 11, 1, 5);
            this.baseGrid.set(0, 9, 11, 11, 5);
            this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY - 2, Direction.WEST, 6);
            this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY + 3, Direction.WEST, 6);
            this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY - 1, Direction.WEST, 3);
            this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY + 2, Direction.WEST, 3);

            while(this.cleanEdges(this.baseGrid)) {
            }

            this.floorRooms = new WoodlandMansionPieces.SimpleGrid[3];
            this.floorRooms[0] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
            this.floorRooms[1] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
            this.floorRooms[2] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
            this.identifyRooms(this.baseGrid, this.floorRooms[0]);
            this.identifyRooms(this.baseGrid, this.floorRooms[1]);
            this.floorRooms[0].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
            this.floorRooms[1].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
            this.thirdFloorGrid = new WoodlandMansionPieces.SimpleGrid(this.baseGrid.width, this.baseGrid.height, 5);
            this.setupThirdFloor();
            this.identifyRooms(this.thirdFloorGrid, this.floorRooms[2]);
        }

        public static boolean isHouse(WoodlandMansionPieces.SimpleGrid param0, int param1, int param2) {
            int var0 = param0.get(param1, param2);
            return var0 == 1 || var0 == 2 || var0 == 3 || var0 == 4;
        }

        public boolean isRoomId(WoodlandMansionPieces.SimpleGrid param0, int param1, int param2, int param3, int param4) {
            return (this.floorRooms[param3].get(param1, param2) & 65535) == param4;
        }

        @Nullable
        public Direction get1x2RoomDirection(WoodlandMansionPieces.SimpleGrid param0, int param1, int param2, int param3, int param4) {
            for(Direction var0 : Direction.Plane.HORIZONTAL) {
                if (this.isRoomId(param0, param1 + var0.getStepX(), param2 + var0.getStepZ(), param3, param4)) {
                    return var0;
                }
            }

            return null;
        }

        private void recursiveCorridor(WoodlandMansionPieces.SimpleGrid param0, int param1, int param2, Direction param3, int param4) {
            if (param4 > 0) {
                param0.set(param1, param2, 1);
                param0.setif(param1 + param3.getStepX(), param2 + param3.getStepZ(), 0, 1);

                for(int var0 = 0; var0 < 8; ++var0) {
                    Direction var1 = Direction.from2DDataValue(this.random.nextInt(4));
                    if (var1 != param3.getOpposite() && (var1 != Direction.EAST || !this.random.nextBoolean())) {
                        int var2 = param1 + param3.getStepX();
                        int var3 = param2 + param3.getStepZ();
                        if (param0.get(var2 + var1.getStepX(), var3 + var1.getStepZ()) == 0
                            && param0.get(var2 + var1.getStepX() * 2, var3 + var1.getStepZ() * 2) == 0) {
                            this.recursiveCorridor(
                                param0, param1 + param3.getStepX() + var1.getStepX(), param2 + param3.getStepZ() + var1.getStepZ(), var1, param4 - 1
                            );
                            break;
                        }
                    }
                }

                Direction var4 = param3.getClockWise();
                Direction var5 = param3.getCounterClockWise();
                param0.setif(param1 + var4.getStepX(), param2 + var4.getStepZ(), 0, 2);
                param0.setif(param1 + var5.getStepX(), param2 + var5.getStepZ(), 0, 2);
                param0.setif(param1 + param3.getStepX() + var4.getStepX(), param2 + param3.getStepZ() + var4.getStepZ(), 0, 2);
                param0.setif(param1 + param3.getStepX() + var5.getStepX(), param2 + param3.getStepZ() + var5.getStepZ(), 0, 2);
                param0.setif(param1 + param3.getStepX() * 2, param2 + param3.getStepZ() * 2, 0, 2);
                param0.setif(param1 + var4.getStepX() * 2, param2 + var4.getStepZ() * 2, 0, 2);
                param0.setif(param1 + var5.getStepX() * 2, param2 + var5.getStepZ() * 2, 0, 2);
            }
        }

        private boolean cleanEdges(WoodlandMansionPieces.SimpleGrid param0) {
            boolean var0 = false;

            for(int var1 = 0; var1 < param0.height; ++var1) {
                for(int var2 = 0; var2 < param0.width; ++var2) {
                    if (param0.get(var2, var1) == 0) {
                        int var3 = 0;
                        var3 += isHouse(param0, var2 + 1, var1) ? 1 : 0;
                        var3 += isHouse(param0, var2 - 1, var1) ? 1 : 0;
                        var3 += isHouse(param0, var2, var1 + 1) ? 1 : 0;
                        var3 += isHouse(param0, var2, var1 - 1) ? 1 : 0;
                        if (var3 >= 3) {
                            param0.set(var2, var1, 2);
                            var0 = true;
                        } else if (var3 == 2) {
                            int var4 = 0;
                            var4 += isHouse(param0, var2 + 1, var1 + 1) ? 1 : 0;
                            var4 += isHouse(param0, var2 - 1, var1 + 1) ? 1 : 0;
                            var4 += isHouse(param0, var2 + 1, var1 - 1) ? 1 : 0;
                            var4 += isHouse(param0, var2 - 1, var1 - 1) ? 1 : 0;
                            if (var4 <= 1) {
                                param0.set(var2, var1, 2);
                                var0 = true;
                            }
                        }
                    }
                }
            }

            return var0;
        }

        private void setupThirdFloor() {
            List<Tuple<Integer, Integer>> var0 = Lists.newArrayList();
            WoodlandMansionPieces.SimpleGrid var1 = this.floorRooms[1];

            for(int var2 = 0; var2 < this.thirdFloorGrid.height; ++var2) {
                for(int var3 = 0; var3 < this.thirdFloorGrid.width; ++var3) {
                    int var4 = var1.get(var3, var2);
                    int var5 = var4 & 983040;
                    if (var5 == 131072 && (var4 & 2097152) == 2097152) {
                        var0.add(new Tuple<>(var3, var2));
                    }
                }
            }

            if (var0.isEmpty()) {
                this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
            } else {
                Tuple<Integer, Integer> var6 = var0.get(this.random.nextInt(var0.size()));
                int var7 = var1.get(var6.getA(), var6.getB());
                var1.set(var6.getA(), var6.getB(), var7 | 4194304);
                Direction var8 = this.get1x2RoomDirection(this.baseGrid, var6.getA(), var6.getB(), 1, var7 & 65535);
                int var9 = var6.getA() + var8.getStepX();
                int var10 = var6.getB() + var8.getStepZ();

                for(int var11 = 0; var11 < this.thirdFloorGrid.height; ++var11) {
                    for(int var12 = 0; var12 < this.thirdFloorGrid.width; ++var12) {
                        if (!isHouse(this.baseGrid, var12, var11)) {
                            this.thirdFloorGrid.set(var12, var11, 5);
                        } else if (var12 == var6.getA() && var11 == var6.getB()) {
                            this.thirdFloorGrid.set(var12, var11, 3);
                        } else if (var12 == var9 && var11 == var10) {
                            this.thirdFloorGrid.set(var12, var11, 3);
                            this.floorRooms[2].set(var12, var11, 8388608);
                        }
                    }
                }

                List<Direction> var13 = Lists.newArrayList();

                for(Direction var14 : Direction.Plane.HORIZONTAL) {
                    if (this.thirdFloorGrid.get(var9 + var14.getStepX(), var10 + var14.getStepZ()) == 0) {
                        var13.add(var14);
                    }
                }

                if (var13.isEmpty()) {
                    this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
                    var1.set(var6.getA(), var6.getB(), var7);
                } else {
                    Direction var15 = var13.get(this.random.nextInt(var13.size()));
                    this.recursiveCorridor(this.thirdFloorGrid, var9 + var15.getStepX(), var10 + var15.getStepZ(), var15, 4);

                    while(this.cleanEdges(this.thirdFloorGrid)) {
                    }

                }
            }
        }

        private void identifyRooms(WoodlandMansionPieces.SimpleGrid param0, WoodlandMansionPieces.SimpleGrid param1) {
            ObjectArrayList<Tuple<Integer, Integer>> var0 = new ObjectArrayList<>();

            for(int var1 = 0; var1 < param0.height; ++var1) {
                for(int var2 = 0; var2 < param0.width; ++var2) {
                    if (param0.get(var2, var1) == 2) {
                        var0.add(new Tuple<>(var2, var1));
                    }
                }
            }

            Util.shuffle(var0, this.random);
            int var3 = 10;

            for(Tuple<Integer, Integer> var4 : var0) {
                int var5 = var4.getA();
                int var6 = var4.getB();
                if (param1.get(var5, var6) == 0) {
                    int var7 = var5;
                    int var8 = var5;
                    int var9 = var6;
                    int var10 = var6;
                    int var11 = 65536;
                    if (param1.get(var5 + 1, var6) == 0
                        && param1.get(var5, var6 + 1) == 0
                        && param1.get(var5 + 1, var6 + 1) == 0
                        && param0.get(var5 + 1, var6) == 2
                        && param0.get(var5, var6 + 1) == 2
                        && param0.get(var5 + 1, var6 + 1) == 2) {
                        var8 = var5 + 1;
                        var10 = var6 + 1;
                        var11 = 262144;
                    } else if (param1.get(var5 - 1, var6) == 0
                        && param1.get(var5, var6 + 1) == 0
                        && param1.get(var5 - 1, var6 + 1) == 0
                        && param0.get(var5 - 1, var6) == 2
                        && param0.get(var5, var6 + 1) == 2
                        && param0.get(var5 - 1, var6 + 1) == 2) {
                        var7 = var5 - 1;
                        var10 = var6 + 1;
                        var11 = 262144;
                    } else if (param1.get(var5 - 1, var6) == 0
                        && param1.get(var5, var6 - 1) == 0
                        && param1.get(var5 - 1, var6 - 1) == 0
                        && param0.get(var5 - 1, var6) == 2
                        && param0.get(var5, var6 - 1) == 2
                        && param0.get(var5 - 1, var6 - 1) == 2) {
                        var7 = var5 - 1;
                        var9 = var6 - 1;
                        var11 = 262144;
                    } else if (param1.get(var5 + 1, var6) == 0 && param0.get(var5 + 1, var6) == 2) {
                        var8 = var5 + 1;
                        var11 = 131072;
                    } else if (param1.get(var5, var6 + 1) == 0 && param0.get(var5, var6 + 1) == 2) {
                        var10 = var6 + 1;
                        var11 = 131072;
                    } else if (param1.get(var5 - 1, var6) == 0 && param0.get(var5 - 1, var6) == 2) {
                        var7 = var5 - 1;
                        var11 = 131072;
                    } else if (param1.get(var5, var6 - 1) == 0 && param0.get(var5, var6 - 1) == 2) {
                        var9 = var6 - 1;
                        var11 = 131072;
                    }

                    int var12 = this.random.nextBoolean() ? var7 : var8;
                    int var13 = this.random.nextBoolean() ? var9 : var10;
                    int var14 = 2097152;
                    if (!param0.edgesTo(var12, var13, 1)) {
                        var12 = var12 == var7 ? var8 : var7;
                        var13 = var13 == var9 ? var10 : var9;
                        if (!param0.edgesTo(var12, var13, 1)) {
                            var13 = var13 == var9 ? var10 : var9;
                            if (!param0.edgesTo(var12, var13, 1)) {
                                var12 = var12 == var7 ? var8 : var7;
                                var13 = var13 == var9 ? var10 : var9;
                                if (!param0.edgesTo(var12, var13, 1)) {
                                    var14 = 0;
                                    var12 = var7;
                                    var13 = var9;
                                }
                            }
                        }
                    }

                    for(int var15 = var9; var15 <= var10; ++var15) {
                        for(int var16 = var7; var16 <= var8; ++var16) {
                            if (var16 == var12 && var15 == var13) {
                                param1.set(var16, var15, 1048576 | var14 | var11 | var3);
                            } else {
                                param1.set(var16, var15, var11 | var3);
                            }
                        }
                    }

                    ++var3;
                }
            }

        }
    }

    static class MansionPiecePlacer {
        private final StructureTemplateManager structureTemplateManager;
        private final RandomSource random;
        private int startX;
        private int startY;

        public MansionPiecePlacer(StructureTemplateManager param0, RandomSource param1) {
            this.structureTemplateManager = param0;
            this.random = param1;
        }

        public void createMansion(
            BlockPos param0, Rotation param1, List<WoodlandMansionPieces.WoodlandMansionPiece> param2, WoodlandMansionPieces.MansionGrid param3
        ) {
            WoodlandMansionPieces.PlacementData var0 = new WoodlandMansionPieces.PlacementData();
            var0.position = param0;
            var0.rotation = param1;
            var0.wallType = "wall_flat";
            WoodlandMansionPieces.PlacementData var1 = new WoodlandMansionPieces.PlacementData();
            this.entrance(param2, var0);
            var1.position = var0.position.above(8);
            var1.rotation = var0.rotation;
            var1.wallType = "wall_window";
            if (!param2.isEmpty()) {
            }

            WoodlandMansionPieces.SimpleGrid var2 = param3.baseGrid;
            WoodlandMansionPieces.SimpleGrid var3 = param3.thirdFloorGrid;
            this.startX = param3.entranceX + 1;
            this.startY = param3.entranceY + 1;
            int var4 = param3.entranceX + 1;
            int var5 = param3.entranceY;
            this.traverseOuterWalls(param2, var0, var2, Direction.SOUTH, this.startX, this.startY, var4, var5);
            this.traverseOuterWalls(param2, var1, var2, Direction.SOUTH, this.startX, this.startY, var4, var5);
            WoodlandMansionPieces.PlacementData var6 = new WoodlandMansionPieces.PlacementData();
            var6.position = var0.position.above(19);
            var6.rotation = var0.rotation;
            var6.wallType = "wall_window";
            boolean var7 = false;

            for(int var8 = 0; var8 < var3.height && !var7; ++var8) {
                for(int var9 = var3.width - 1; var9 >= 0 && !var7; --var9) {
                    if (WoodlandMansionPieces.MansionGrid.isHouse(var3, var9, var8)) {
                        var6.position = var6.position.relative(param1.rotate(Direction.SOUTH), 8 + (var8 - this.startY) * 8);
                        var6.position = var6.position.relative(param1.rotate(Direction.EAST), (var9 - this.startX) * 8);
                        this.traverseWallPiece(param2, var6);
                        this.traverseOuterWalls(param2, var6, var3, Direction.SOUTH, var9, var8, var9, var8);
                        var7 = true;
                    }
                }
            }

            this.createRoof(param2, param0.above(16), param1, var2, var3);
            this.createRoof(param2, param0.above(27), param1, var3, null);
            if (!param2.isEmpty()) {
            }

            WoodlandMansionPieces.FloorRoomCollection[] var10 = new WoodlandMansionPieces.FloorRoomCollection[]{
                new WoodlandMansionPieces.FirstFloorRoomCollection(),
                new WoodlandMansionPieces.SecondFloorRoomCollection(),
                new WoodlandMansionPieces.ThirdFloorRoomCollection()
            };

            for(int var11 = 0; var11 < 3; ++var11) {
                BlockPos var12 = param0.above(8 * var11 + (var11 == 2 ? 3 : 0));
                WoodlandMansionPieces.SimpleGrid var13 = param3.floorRooms[var11];
                WoodlandMansionPieces.SimpleGrid var14 = var11 == 2 ? var3 : var2;
                String var15 = var11 == 0 ? "carpet_south_1" : "carpet_south_2";
                String var16 = var11 == 0 ? "carpet_west_1" : "carpet_west_2";

                for(int var17 = 0; var17 < var14.height; ++var17) {
                    for(int var18 = 0; var18 < var14.width; ++var18) {
                        if (var14.get(var18, var17) == 1) {
                            BlockPos var19 = var12.relative(param1.rotate(Direction.SOUTH), 8 + (var17 - this.startY) * 8);
                            var19 = var19.relative(param1.rotate(Direction.EAST), (var18 - this.startX) * 8);
                            param2.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "corridor_floor", var19, param1));
                            if (var14.get(var18, var17 - 1) == 1 || (var13.get(var18, var17 - 1) & 8388608) == 8388608) {
                                param2.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager, "carpet_north", var19.relative(param1.rotate(Direction.EAST), 1).above(), param1
                                    )
                                );
                            }

                            if (var14.get(var18 + 1, var17) == 1 || (var13.get(var18 + 1, var17) & 8388608) == 8388608) {
                                param2.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager,
                                        "carpet_east",
                                        var19.relative(param1.rotate(Direction.SOUTH), 1).relative(param1.rotate(Direction.EAST), 5).above(),
                                        param1
                                    )
                                );
                            }

                            if (var14.get(var18, var17 + 1) == 1 || (var13.get(var18, var17 + 1) & 8388608) == 8388608) {
                                param2.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager,
                                        var15,
                                        var19.relative(param1.rotate(Direction.SOUTH), 5).relative(param1.rotate(Direction.WEST), 1),
                                        param1
                                    )
                                );
                            }

                            if (var14.get(var18 - 1, var17) == 1 || (var13.get(var18 - 1, var17) & 8388608) == 8388608) {
                                param2.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager,
                                        var16,
                                        var19.relative(param1.rotate(Direction.WEST), 1).relative(param1.rotate(Direction.NORTH), 1),
                                        param1
                                    )
                                );
                            }
                        }
                    }
                }

                String var20 = var11 == 0 ? "indoors_wall_1" : "indoors_wall_2";
                String var21 = var11 == 0 ? "indoors_door_1" : "indoors_door_2";
                List<Direction> var22 = Lists.newArrayList();

                for(int var23 = 0; var23 < var14.height; ++var23) {
                    for(int var24 = 0; var24 < var14.width; ++var24) {
                        boolean var25 = var11 == 2 && var14.get(var24, var23) == 3;
                        if (var14.get(var24, var23) == 2 || var25) {
                            int var26 = var13.get(var24, var23);
                            int var27 = var26 & 983040;
                            int var28 = var26 & 65535;
                            var25 = var25 && (var26 & 8388608) == 8388608;
                            var22.clear();
                            if ((var26 & 2097152) == 2097152) {
                                for(Direction var29 : Direction.Plane.HORIZONTAL) {
                                    if (var14.get(var24 + var29.getStepX(), var23 + var29.getStepZ()) == 1) {
                                        var22.add(var29);
                                    }
                                }
                            }

                            Direction var30 = null;
                            if (!var22.isEmpty()) {
                                var30 = var22.get(this.random.nextInt(var22.size()));
                            } else if ((var26 & 1048576) == 1048576) {
                                var30 = Direction.UP;
                            }

                            BlockPos var31 = var12.relative(param1.rotate(Direction.SOUTH), 8 + (var23 - this.startY) * 8);
                            var31 = var31.relative(param1.rotate(Direction.EAST), -1 + (var24 - this.startX) * 8);
                            if (WoodlandMansionPieces.MansionGrid.isHouse(var14, var24 - 1, var23) && !param3.isRoomId(var14, var24 - 1, var23, var11, var28)) {
                                param2.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager, var30 == Direction.WEST ? var21 : var20, var31, param1
                                    )
                                );
                            }

                            if (var14.get(var24 + 1, var23) == 1 && !var25) {
                                BlockPos var32 = var31.relative(param1.rotate(Direction.EAST), 8);
                                param2.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager, var30 == Direction.EAST ? var21 : var20, var32, param1
                                    )
                                );
                            }

                            if (WoodlandMansionPieces.MansionGrid.isHouse(var14, var24, var23 + 1) && !param3.isRoomId(var14, var24, var23 + 1, var11, var28)) {
                                BlockPos var33 = var31.relative(param1.rotate(Direction.SOUTH), 7);
                                var33 = var33.relative(param1.rotate(Direction.EAST), 7);
                                param2.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager,
                                        var30 == Direction.SOUTH ? var21 : var20,
                                        var33,
                                        param1.getRotated(Rotation.CLOCKWISE_90)
                                    )
                                );
                            }

                            if (var14.get(var24, var23 - 1) == 1 && !var25) {
                                BlockPos var34 = var31.relative(param1.rotate(Direction.NORTH), 1);
                                var34 = var34.relative(param1.rotate(Direction.EAST), 7);
                                param2.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager,
                                        var30 == Direction.NORTH ? var21 : var20,
                                        var34,
                                        param1.getRotated(Rotation.CLOCKWISE_90)
                                    )
                                );
                            }

                            if (var27 == 65536) {
                                this.addRoom1x1(param2, var31, param1, var30, var10[var11]);
                            } else if (var27 == 131072 && var30 != null) {
                                Direction var35 = param3.get1x2RoomDirection(var14, var24, var23, var11, var28);
                                boolean var36 = (var26 & 4194304) == 4194304;
                                this.addRoom1x2(param2, var31, param1, var35, var30, var10[var11], var36);
                            } else if (var27 == 262144 && var30 != null && var30 != Direction.UP) {
                                Direction var37 = var30.getClockWise();
                                if (!param3.isRoomId(var14, var24 + var37.getStepX(), var23 + var37.getStepZ(), var11, var28)) {
                                    var37 = var37.getOpposite();
                                }

                                this.addRoom2x2(param2, var31, param1, var37, var30, var10[var11]);
                            } else if (var27 == 262144 && var30 == Direction.UP) {
                                this.addRoom2x2Secret(param2, var31, param1, var10[var11]);
                            }
                        }
                    }
                }
            }

        }

        private void traverseOuterWalls(
            List<WoodlandMansionPieces.WoodlandMansionPiece> param0,
            WoodlandMansionPieces.PlacementData param1,
            WoodlandMansionPieces.SimpleGrid param2,
            Direction param3,
            int param4,
            int param5,
            int param6,
            int param7
        ) {
            int var0 = param4;
            int var1 = param5;
            Direction var2 = param3;

            do {
                if (!WoodlandMansionPieces.MansionGrid.isHouse(param2, var0 + param3.getStepX(), var1 + param3.getStepZ())) {
                    this.traverseTurn(param0, param1);
                    param3 = param3.getClockWise();
                    if (var0 != param6 || var1 != param7 || var2 != param3) {
                        this.traverseWallPiece(param0, param1);
                    }
                } else if (WoodlandMansionPieces.MansionGrid.isHouse(param2, var0 + param3.getStepX(), var1 + param3.getStepZ())
                    && WoodlandMansionPieces.MansionGrid.isHouse(
                        param2,
                        var0 + param3.getStepX() + param3.getCounterClockWise().getStepX(),
                        var1 + param3.getStepZ() + param3.getCounterClockWise().getStepZ()
                    )) {
                    this.traverseInnerTurn(param0, param1);
                    var0 += param3.getStepX();
                    var1 += param3.getStepZ();
                    param3 = param3.getCounterClockWise();
                } else {
                    var0 += param3.getStepX();
                    var1 += param3.getStepZ();
                    if (var0 != param6 || var1 != param7 || var2 != param3) {
                        this.traverseWallPiece(param0, param1);
                    }
                }
            } while(var0 != param6 || var1 != param7 || var2 != param3);

        }

        private void createRoof(
            List<WoodlandMansionPieces.WoodlandMansionPiece> param0,
            BlockPos param1,
            Rotation param2,
            WoodlandMansionPieces.SimpleGrid param3,
            @Nullable WoodlandMansionPieces.SimpleGrid param4
        ) {
            for(int var0 = 0; var0 < param3.height; ++var0) {
                for(int var1 = 0; var1 < param3.width; ++var1) {
                    BlockPos var22 = param1.relative(param2.rotate(Direction.SOUTH), 8 + (var0 - this.startY) * 8);
                    var22 = var22.relative(param2.rotate(Direction.EAST), (var1 - this.startX) * 8);
                    boolean var3 = param4 != null && WoodlandMansionPieces.MansionGrid.isHouse(param4, var1, var0);
                    if (WoodlandMansionPieces.MansionGrid.isHouse(param3, var1, var0) && !var3) {
                        param0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof", var22.above(3), param2));
                        if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var1 + 1, var0)) {
                            BlockPos var4 = var22.relative(param2.rotate(Direction.EAST), 6);
                            param0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_front", var4, param2));
                        }

                        if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var1 - 1, var0)) {
                            BlockPos var5 = var22.relative(param2.rotate(Direction.EAST), 0);
                            var5 = var5.relative(param2.rotate(Direction.SOUTH), 7);
                            param0.add(
                                new WoodlandMansionPieces.WoodlandMansionPiece(
                                    this.structureTemplateManager, "roof_front", var5, param2.getRotated(Rotation.CLOCKWISE_180)
                                )
                            );
                        }

                        if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var1, var0 - 1)) {
                            BlockPos var6 = var22.relative(param2.rotate(Direction.WEST), 1);
                            param0.add(
                                new WoodlandMansionPieces.WoodlandMansionPiece(
                                    this.structureTemplateManager, "roof_front", var6, param2.getRotated(Rotation.COUNTERCLOCKWISE_90)
                                )
                            );
                        }

                        if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var1, var0 + 1)) {
                            BlockPos var7 = var22.relative(param2.rotate(Direction.EAST), 6);
                            var7 = var7.relative(param2.rotate(Direction.SOUTH), 6);
                            param0.add(
                                new WoodlandMansionPieces.WoodlandMansionPiece(
                                    this.structureTemplateManager, "roof_front", var7, param2.getRotated(Rotation.CLOCKWISE_90)
                                )
                            );
                        }
                    }
                }
            }

            if (param4 != null) {
                for(int var8 = 0; var8 < param3.height; ++var8) {
                    for(int var9 = 0; var9 < param3.width; ++var9) {
                        BlockPos var17 = param1.relative(param2.rotate(Direction.SOUTH), 8 + (var8 - this.startY) * 8);
                        var17 = var17.relative(param2.rotate(Direction.EAST), (var9 - this.startX) * 8);
                        boolean var11 = WoodlandMansionPieces.MansionGrid.isHouse(param4, var9, var8);
                        if (WoodlandMansionPieces.MansionGrid.isHouse(param3, var9, var8) && var11) {
                            if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var9 + 1, var8)) {
                                BlockPos var12 = var17.relative(param2.rotate(Direction.EAST), 7);
                                param0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall", var12, param2));
                            }

                            if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var9 - 1, var8)) {
                                BlockPos var13 = var17.relative(param2.rotate(Direction.WEST), 1);
                                var13 = var13.relative(param2.rotate(Direction.SOUTH), 6);
                                param0.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager, "small_wall", var13, param2.getRotated(Rotation.CLOCKWISE_180)
                                    )
                                );
                            }

                            if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var9, var8 - 1)) {
                                BlockPos var14 = var17.relative(param2.rotate(Direction.WEST), 0);
                                var14 = var14.relative(param2.rotate(Direction.NORTH), 1);
                                param0.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager, "small_wall", var14, param2.getRotated(Rotation.COUNTERCLOCKWISE_90)
                                    )
                                );
                            }

                            if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var9, var8 + 1)) {
                                BlockPos var15 = var17.relative(param2.rotate(Direction.EAST), 6);
                                var15 = var15.relative(param2.rotate(Direction.SOUTH), 7);
                                param0.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager, "small_wall", var15, param2.getRotated(Rotation.CLOCKWISE_90)
                                    )
                                );
                            }

                            if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var9 + 1, var8)) {
                                if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var9, var8 - 1)) {
                                    BlockPos var16 = var17.relative(param2.rotate(Direction.EAST), 7);
                                    var16 = var16.relative(param2.rotate(Direction.NORTH), 2);
                                    param0.add(
                                        new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", var16, param2)
                                    );
                                }

                                if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var9, var8 + 1)) {
                                    BlockPos var17x = var17.relative(param2.rotate(Direction.EAST), 8);
                                    var17x = var17x.relative(param2.rotate(Direction.SOUTH), 7);
                                    param0.add(
                                        new WoodlandMansionPieces.WoodlandMansionPiece(
                                            this.structureTemplateManager, "small_wall_corner", var17x, param2.getRotated(Rotation.CLOCKWISE_90)
                                        )
                                    );
                                }
                            }

                            if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var9 - 1, var8)) {
                                if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var9, var8 - 1)) {
                                    BlockPos var18 = var17.relative(param2.rotate(Direction.WEST), 2);
                                    var18 = var18.relative(param2.rotate(Direction.NORTH), 1);
                                    param0.add(
                                        new WoodlandMansionPieces.WoodlandMansionPiece(
                                            this.structureTemplateManager, "small_wall_corner", var18, param2.getRotated(Rotation.COUNTERCLOCKWISE_90)
                                        )
                                    );
                                }

                                if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var9, var8 + 1)) {
                                    BlockPos var19 = var17.relative(param2.rotate(Direction.WEST), 1);
                                    var19 = var19.relative(param2.rotate(Direction.SOUTH), 8);
                                    param0.add(
                                        new WoodlandMansionPieces.WoodlandMansionPiece(
                                            this.structureTemplateManager, "small_wall_corner", var19, param2.getRotated(Rotation.CLOCKWISE_180)
                                        )
                                    );
                                }
                            }
                        }
                    }
                }
            }

            for(int var20 = 0; var20 < param3.height; ++var20) {
                for(int var21 = 0; var21 < param3.width; ++var21) {
                    BlockPos var19 = param1.relative(param2.rotate(Direction.SOUTH), 8 + (var20 - this.startY) * 8);
                    var19 = var19.relative(param2.rotate(Direction.EAST), (var21 - this.startX) * 8);
                    boolean var23 = param4 != null && WoodlandMansionPieces.MansionGrid.isHouse(param4, var21, var20);
                    if (WoodlandMansionPieces.MansionGrid.isHouse(param3, var21, var20) && !var23) {
                        if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var21 + 1, var20)) {
                            BlockPos var24 = var19.relative(param2.rotate(Direction.EAST), 6);
                            if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var21, var20 + 1)) {
                                BlockPos var25 = var24.relative(param2.rotate(Direction.SOUTH), 6);
                                param0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", var25, param2));
                            } else if (WoodlandMansionPieces.MansionGrid.isHouse(param3, var21 + 1, var20 + 1)) {
                                BlockPos var26 = var24.relative(param2.rotate(Direction.SOUTH), 5);
                                param0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", var26, param2));
                            }

                            if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var21, var20 - 1)) {
                                param0.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager, "roof_corner", var24, param2.getRotated(Rotation.COUNTERCLOCKWISE_90)
                                    )
                                );
                            } else if (WoodlandMansionPieces.MansionGrid.isHouse(param3, var21 + 1, var20 - 1)) {
                                BlockPos var27 = var19.relative(param2.rotate(Direction.EAST), 9);
                                var27 = var27.relative(param2.rotate(Direction.NORTH), 2);
                                param0.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager, "roof_inner_corner", var27, param2.getRotated(Rotation.CLOCKWISE_90)
                                    )
                                );
                            }
                        }

                        if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var21 - 1, var20)) {
                            BlockPos var28 = var19.relative(param2.rotate(Direction.EAST), 0);
                            var28 = var28.relative(param2.rotate(Direction.SOUTH), 0);
                            if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var21, var20 + 1)) {
                                BlockPos var29 = var28.relative(param2.rotate(Direction.SOUTH), 6);
                                param0.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager, "roof_corner", var29, param2.getRotated(Rotation.CLOCKWISE_90)
                                    )
                                );
                            } else if (WoodlandMansionPieces.MansionGrid.isHouse(param3, var21 - 1, var20 + 1)) {
                                BlockPos var30 = var28.relative(param2.rotate(Direction.SOUTH), 8);
                                var30 = var30.relative(param2.rotate(Direction.WEST), 3);
                                param0.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager, "roof_inner_corner", var30, param2.getRotated(Rotation.COUNTERCLOCKWISE_90)
                                    )
                                );
                            }

                            if (!WoodlandMansionPieces.MansionGrid.isHouse(param3, var21, var20 - 1)) {
                                param0.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager, "roof_corner", var28, param2.getRotated(Rotation.CLOCKWISE_180)
                                    )
                                );
                            } else if (WoodlandMansionPieces.MansionGrid.isHouse(param3, var21 - 1, var20 - 1)) {
                                BlockPos var31 = var28.relative(param2.rotate(Direction.SOUTH), 1);
                                param0.add(
                                    new WoodlandMansionPieces.WoodlandMansionPiece(
                                        this.structureTemplateManager, "roof_inner_corner", var31, param2.getRotated(Rotation.CLOCKWISE_180)
                                    )
                                );
                            }
                        }
                    }
                }
            }

        }

        private void entrance(List<WoodlandMansionPieces.WoodlandMansionPiece> param0, WoodlandMansionPieces.PlacementData param1) {
            Direction var0 = param1.rotation.rotate(Direction.WEST);
            param0.add(
                new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "entrance", param1.position.relative(var0, 9), param1.rotation)
            );
            param1.position = param1.position.relative(param1.rotation.rotate(Direction.SOUTH), 16);
        }

        private void traverseWallPiece(List<WoodlandMansionPieces.WoodlandMansionPiece> param0, WoodlandMansionPieces.PlacementData param1) {
            param0.add(
                new WoodlandMansionPieces.WoodlandMansionPiece(
                    this.structureTemplateManager, param1.wallType, param1.position.relative(param1.rotation.rotate(Direction.EAST), 7), param1.rotation
                )
            );
            param1.position = param1.position.relative(param1.rotation.rotate(Direction.SOUTH), 8);
        }

        private void traverseTurn(List<WoodlandMansionPieces.WoodlandMansionPiece> param0, WoodlandMansionPieces.PlacementData param1) {
            param1.position = param1.position.relative(param1.rotation.rotate(Direction.SOUTH), -1);
            param0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "wall_corner", param1.position, param1.rotation));
            param1.position = param1.position.relative(param1.rotation.rotate(Direction.SOUTH), -7);
            param1.position = param1.position.relative(param1.rotation.rotate(Direction.WEST), -6);
            param1.rotation = param1.rotation.getRotated(Rotation.CLOCKWISE_90);
        }

        private void traverseInnerTurn(List<WoodlandMansionPieces.WoodlandMansionPiece> param0, WoodlandMansionPieces.PlacementData param1) {
            param1.position = param1.position.relative(param1.rotation.rotate(Direction.SOUTH), 6);
            param1.position = param1.position.relative(param1.rotation.rotate(Direction.EAST), 8);
            param1.rotation = param1.rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
        }

        private void addRoom1x1(
            List<WoodlandMansionPieces.WoodlandMansionPiece> param0,
            BlockPos param1,
            Rotation param2,
            Direction param3,
            WoodlandMansionPieces.FloorRoomCollection param4
        ) {
            Rotation var0 = Rotation.NONE;
            String var1 = param4.get1x1(this.random);
            if (param3 != Direction.EAST) {
                if (param3 == Direction.NORTH) {
                    var0 = var0.getRotated(Rotation.COUNTERCLOCKWISE_90);
                } else if (param3 == Direction.WEST) {
                    var0 = var0.getRotated(Rotation.CLOCKWISE_180);
                } else if (param3 == Direction.SOUTH) {
                    var0 = var0.getRotated(Rotation.CLOCKWISE_90);
                } else {
                    var1 = param4.get1x1Secret(this.random);
                }
            }

            BlockPos var2 = StructureTemplate.getZeroPositionWithTransform(new BlockPos(1, 0, 0), Mirror.NONE, var0, 7, 7);
            var0 = var0.getRotated(param2);
            var2 = var2.rotate(param2);
            BlockPos var3 = param1.offset(var2.getX(), 0, var2.getZ());
            param0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, var1, var3, var0));
        }

        private void addRoom1x2(
            List<WoodlandMansionPieces.WoodlandMansionPiece> param0,
            BlockPos param1,
            Rotation param2,
            Direction param3,
            Direction param4,
            WoodlandMansionPieces.FloorRoomCollection param5,
            boolean param6
        ) {
            if (param4 == Direction.EAST && param3 == Direction.SOUTH) {
                BlockPos var0 = param1.relative(param2.rotate(Direction.EAST), 1);
                param0.add(
                    new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, param5.get1x2SideEntrance(this.random, param6), var0, param2)
                );
            } else if (param4 == Direction.EAST && param3 == Direction.NORTH) {
                BlockPos var1 = param1.relative(param2.rotate(Direction.EAST), 1);
                var1 = var1.relative(param2.rotate(Direction.SOUTH), 6);
                param0.add(
                    new WoodlandMansionPieces.WoodlandMansionPiece(
                        this.structureTemplateManager, param5.get1x2SideEntrance(this.random, param6), var1, param2, Mirror.LEFT_RIGHT
                    )
                );
            } else if (param4 == Direction.WEST && param3 == Direction.NORTH) {
                BlockPos var2 = param1.relative(param2.rotate(Direction.EAST), 7);
                var2 = var2.relative(param2.rotate(Direction.SOUTH), 6);
                param0.add(
                    new WoodlandMansionPieces.WoodlandMansionPiece(
                        this.structureTemplateManager, param5.get1x2SideEntrance(this.random, param6), var2, param2.getRotated(Rotation.CLOCKWISE_180)
                    )
                );
            } else if (param4 == Direction.WEST && param3 == Direction.SOUTH) {
                BlockPos var3 = param1.relative(param2.rotate(Direction.EAST), 7);
                param0.add(
                    new WoodlandMansionPieces.WoodlandMansionPiece(
                        this.structureTemplateManager, param5.get1x2SideEntrance(this.random, param6), var3, param2, Mirror.FRONT_BACK
                    )
                );
            } else if (param4 == Direction.SOUTH && param3 == Direction.EAST) {
                BlockPos var4 = param1.relative(param2.rotate(Direction.EAST), 1);
                param0.add(
                    new WoodlandMansionPieces.WoodlandMansionPiece(
                        this.structureTemplateManager,
                        param5.get1x2SideEntrance(this.random, param6),
                        var4,
                        param2.getRotated(Rotation.CLOCKWISE_90),
                        Mirror.LEFT_RIGHT
                    )
                );
            } else if (param4 == Direction.SOUTH && param3 == Direction.WEST) {
                BlockPos var5 = param1.relative(param2.rotate(Direction.EAST), 7);
                param0.add(
                    new WoodlandMansionPieces.WoodlandMansionPiece(
                        this.structureTemplateManager, param5.get1x2SideEntrance(this.random, param6), var5, param2.getRotated(Rotation.CLOCKWISE_90)
                    )
                );
            } else if (param4 == Direction.NORTH && param3 == Direction.WEST) {
                BlockPos var6 = param1.relative(param2.rotate(Direction.EAST), 7);
                var6 = var6.relative(param2.rotate(Direction.SOUTH), 6);
                param0.add(
                    new WoodlandMansionPieces.WoodlandMansionPiece(
                        this.structureTemplateManager,
                        param5.get1x2SideEntrance(this.random, param6),
                        var6,
                        param2.getRotated(Rotation.CLOCKWISE_90),
                        Mirror.FRONT_BACK
                    )
                );
            } else if (param4 == Direction.NORTH && param3 == Direction.EAST) {
                BlockPos var7 = param1.relative(param2.rotate(Direction.EAST), 1);
                var7 = var7.relative(param2.rotate(Direction.SOUTH), 6);
                param0.add(
                    new WoodlandMansionPieces.WoodlandMansionPiece(
                        this.structureTemplateManager, param5.get1x2SideEntrance(this.random, param6), var7, param2.getRotated(Rotation.COUNTERCLOCKWISE_90)
                    )
                );
            } else if (param4 == Direction.SOUTH && param3 == Direction.NORTH) {
                BlockPos var8 = param1.relative(param2.rotate(Direction.EAST), 1);
                var8 = var8.relative(param2.rotate(Direction.NORTH), 8);
                param0.add(
                    new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, param5.get1x2FrontEntrance(this.random, param6), var8, param2)
                );
            } else if (param4 == Direction.NORTH && param3 == Direction.SOUTH) {
                BlockPos var9 = param1.relative(param2.rotate(Direction.EAST), 7);
                var9 = var9.relative(param2.rotate(Direction.SOUTH), 14);
                param0.add(
                    new WoodlandMansionPieces.WoodlandMansionPiece(
                        this.structureTemplateManager, param5.get1x2FrontEntrance(this.random, param6), var9, param2.getRotated(Rotation.CLOCKWISE_180)
                    )
                );
            } else if (param4 == Direction.WEST && param3 == Direction.EAST) {
                BlockPos var10 = param1.relative(param2.rotate(Direction.EAST), 15);
                param0.add(
                    new WoodlandMansionPieces.WoodlandMansionPiece(
                        this.structureTemplateManager, param5.get1x2FrontEntrance(this.random, param6), var10, param2.getRotated(Rotation.CLOCKWISE_90)
                    )
                );
            } else if (param4 == Direction.EAST && param3 == Direction.WEST) {
                BlockPos var11 = param1.relative(param2.rotate(Direction.WEST), 7);
                var11 = var11.relative(param2.rotate(Direction.SOUTH), 6);
                param0.add(
                    new WoodlandMansionPieces.WoodlandMansionPiece(
                        this.structureTemplateManager, param5.get1x2FrontEntrance(this.random, param6), var11, param2.getRotated(Rotation.COUNTERCLOCKWISE_90)
                    )
                );
            } else if (param4 == Direction.UP && param3 == Direction.EAST) {
                BlockPos var12 = param1.relative(param2.rotate(Direction.EAST), 15);
                param0.add(
                    new WoodlandMansionPieces.WoodlandMansionPiece(
                        this.structureTemplateManager, param5.get1x2Secret(this.random), var12, param2.getRotated(Rotation.CLOCKWISE_90)
                    )
                );
            } else if (param4 == Direction.UP && param3 == Direction.SOUTH) {
                BlockPos var13 = param1.relative(param2.rotate(Direction.EAST), 1);
                var13 = var13.relative(param2.rotate(Direction.NORTH), 0);
                param0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, param5.get1x2Secret(this.random), var13, param2));
            }

        }

        private void addRoom2x2(
            List<WoodlandMansionPieces.WoodlandMansionPiece> param0,
            BlockPos param1,
            Rotation param2,
            Direction param3,
            Direction param4,
            WoodlandMansionPieces.FloorRoomCollection param5
        ) {
            int var0 = 0;
            int var1 = 0;
            Rotation var2 = param2;
            Mirror var3 = Mirror.NONE;
            if (param4 == Direction.EAST && param3 == Direction.SOUTH) {
                var0 = -7;
            } else if (param4 == Direction.EAST && param3 == Direction.NORTH) {
                var0 = -7;
                var1 = 6;
                var3 = Mirror.LEFT_RIGHT;
            } else if (param4 == Direction.NORTH && param3 == Direction.EAST) {
                var0 = 1;
                var1 = 14;
                var2 = param2.getRotated(Rotation.COUNTERCLOCKWISE_90);
            } else if (param4 == Direction.NORTH && param3 == Direction.WEST) {
                var0 = 7;
                var1 = 14;
                var2 = param2.getRotated(Rotation.COUNTERCLOCKWISE_90);
                var3 = Mirror.LEFT_RIGHT;
            } else if (param4 == Direction.SOUTH && param3 == Direction.WEST) {
                var0 = 7;
                var1 = -8;
                var2 = param2.getRotated(Rotation.CLOCKWISE_90);
            } else if (param4 == Direction.SOUTH && param3 == Direction.EAST) {
                var0 = 1;
                var1 = -8;
                var2 = param2.getRotated(Rotation.CLOCKWISE_90);
                var3 = Mirror.LEFT_RIGHT;
            } else if (param4 == Direction.WEST && param3 == Direction.NORTH) {
                var0 = 15;
                var1 = 6;
                var2 = param2.getRotated(Rotation.CLOCKWISE_180);
            } else if (param4 == Direction.WEST && param3 == Direction.SOUTH) {
                var0 = 15;
                var3 = Mirror.FRONT_BACK;
            }

            BlockPos var4 = param1.relative(param2.rotate(Direction.EAST), var0);
            var4 = var4.relative(param2.rotate(Direction.SOUTH), var1);
            param0.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, param5.get2x2(this.random), var4, var2, var3));
        }

        private void addRoom2x2Secret(
            List<WoodlandMansionPieces.WoodlandMansionPiece> param0, BlockPos param1, Rotation param2, WoodlandMansionPieces.FloorRoomCollection param3
        ) {
            BlockPos var0 = param1.relative(param2.rotate(Direction.EAST), 1);
            param0.add(
                new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, param3.get2x2Secret(this.random), var0, param2, Mirror.NONE)
            );
        }
    }

    static class PlacementData {
        public Rotation rotation;
        public BlockPos position;
        public String wallType;
    }

    static class SecondFloorRoomCollection extends WoodlandMansionPieces.FloorRoomCollection {
        @Override
        public String get1x1(RandomSource param0) {
            return "1x1_b" + (param0.nextInt(4) + 1);
        }

        @Override
        public String get1x1Secret(RandomSource param0) {
            return "1x1_as" + (param0.nextInt(4) + 1);
        }

        @Override
        public String get1x2SideEntrance(RandomSource param0, boolean param1) {
            return param1 ? "1x2_c_stairs" : "1x2_c" + (param0.nextInt(4) + 1);
        }

        @Override
        public String get1x2FrontEntrance(RandomSource param0, boolean param1) {
            return param1 ? "1x2_d_stairs" : "1x2_d" + (param0.nextInt(5) + 1);
        }

        @Override
        public String get1x2Secret(RandomSource param0) {
            return "1x2_se" + (param0.nextInt(1) + 1);
        }

        @Override
        public String get2x2(RandomSource param0) {
            return "2x2_b" + (param0.nextInt(5) + 1);
        }

        @Override
        public String get2x2Secret(RandomSource param0) {
            return "2x2_s1";
        }
    }

    static class SimpleGrid {
        private final int[][] grid;
        final int width;
        final int height;
        private final int valueIfOutside;

        public SimpleGrid(int param0, int param1, int param2) {
            this.width = param0;
            this.height = param1;
            this.valueIfOutside = param2;
            this.grid = new int[param0][param1];
        }

        public void set(int param0, int param1, int param2) {
            if (param0 >= 0 && param0 < this.width && param1 >= 0 && param1 < this.height) {
                this.grid[param0][param1] = param2;
            }

        }

        public void set(int param0, int param1, int param2, int param3, int param4) {
            for(int var0 = param1; var0 <= param3; ++var0) {
                for(int var1 = param0; var1 <= param2; ++var1) {
                    this.set(var1, var0, param4);
                }
            }

        }

        public int get(int param0, int param1) {
            return param0 >= 0 && param0 < this.width && param1 >= 0 && param1 < this.height ? this.grid[param0][param1] : this.valueIfOutside;
        }

        public void setif(int param0, int param1, int param2, int param3) {
            if (this.get(param0, param1) == param2) {
                this.set(param0, param1, param3);
            }

        }

        public boolean edgesTo(int param0, int param1, int param2) {
            return this.get(param0 - 1, param1) == param2
                || this.get(param0 + 1, param1) == param2
                || this.get(param0, param1 + 1) == param2
                || this.get(param0, param1 - 1) == param2;
        }
    }

    static class ThirdFloorRoomCollection extends WoodlandMansionPieces.SecondFloorRoomCollection {
    }

    public static class WoodlandMansionPiece extends TemplateStructurePiece {
        public WoodlandMansionPiece(StructureTemplateManager param0, String param1, BlockPos param2, Rotation param3) {
            this(param0, param1, param2, param3, Mirror.NONE);
        }

        public WoodlandMansionPiece(StructureTemplateManager param0, String param1, BlockPos param2, Rotation param3, Mirror param4) {
            super(StructurePieceType.WOODLAND_MANSION_PIECE, 0, param0, makeLocation(param1), param1, makeSettings(param4, param3), param2);
        }

        public WoodlandMansionPiece(StructureTemplateManager param0, CompoundTag param1) {
            super(
                StructurePieceType.WOODLAND_MANSION_PIECE,
                param1,
                param0,
                param1x -> makeSettings(Mirror.valueOf(param1.getString("Mi")), Rotation.valueOf(param1.getString("Rot")))
            );
        }

        @Override
        protected ResourceLocation makeTemplateLocation() {
            return makeLocation(this.templateName);
        }

        private static ResourceLocation makeLocation(String param0) {
            return new ResourceLocation("woodland_mansion/" + param0);
        }

        private static StructurePlaceSettings makeSettings(Mirror param0, Rotation param1) {
            return new StructurePlaceSettings()
                .setIgnoreEntities(true)
                .setRotation(param1)
                .setMirror(param0)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext param0, CompoundTag param1) {
            super.addAdditionalSaveData(param0, param1);
            param1.putString("Rot", this.placeSettings.getRotation().name());
            param1.putString("Mi", this.placeSettings.getMirror().name());
        }

        @Override
        protected void handleDataMarker(String param0, BlockPos param1, ServerLevelAccessor param2, RandomSource param3, BoundingBox param4) {
            if (param0.startsWith("Chest")) {
                Rotation var0 = this.placeSettings.getRotation();
                BlockState var1 = Blocks.CHEST.defaultBlockState();
                if ("ChestWest".equals(param0)) {
                    var1 = var1.setValue(ChestBlock.FACING, var0.rotate(Direction.WEST));
                } else if ("ChestEast".equals(param0)) {
                    var1 = var1.setValue(ChestBlock.FACING, var0.rotate(Direction.EAST));
                } else if ("ChestSouth".equals(param0)) {
                    var1 = var1.setValue(ChestBlock.FACING, var0.rotate(Direction.SOUTH));
                } else if ("ChestNorth".equals(param0)) {
                    var1 = var1.setValue(ChestBlock.FACING, var0.rotate(Direction.NORTH));
                }

                this.createChest(param2, param4, param3, param1, BuiltInLootTables.WOODLAND_MANSION, var1);
            } else {
                List<Mob> var2 = new ArrayList<>();
                switch(param0) {
                    case "Mage":
                        var2.add(EntityType.EVOKER.create(param2.getLevel()));
                        break;
                    case "Warrior":
                        var2.add(EntityType.VINDICATOR.create(param2.getLevel()));
                        break;
                    case "Group of Allays":
                        int var3 = param2.getRandom().nextInt(3) + 1;

                        for(int var4 = 0; var4 < var3; ++var4) {
                            var2.add(EntityType.ALLAY.create(param2.getLevel()));
                        }
                        break;
                    default:
                        return;
                }

                for(Mob var5 : var2) {
                    if (var5 != null) {
                        var5.setPersistenceRequired();
                        var5.moveTo(param1, 0.0F, 0.0F);
                        var5.finalizeSpawn(param2, param2.getCurrentDifficultyAt(var5.blockPosition()), MobSpawnType.STRUCTURE, null, null);
                        param2.addFreshEntityWithPassengers(var5);
                        param2.setBlock(param1, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }

        }
    }
}
