package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class OceanMonumentPieces {
    private OceanMonumentPieces() {
    }

    static class FitDoubleXRoom implements OceanMonumentPieces.MonumentRoomFitter {
        @Override
        public boolean fits(OceanMonumentPieces.RoomDefinition param0) {
            return param0.hasOpening[Direction.EAST.get3DDataValue()] && !param0.connections[Direction.EAST.get3DDataValue()].claimed;
        }

        @Override
        public OceanMonumentPieces.OceanMonumentPiece create(Direction param0, OceanMonumentPieces.RoomDefinition param1, RandomSource param2) {
            param1.claimed = true;
            param1.connections[Direction.EAST.get3DDataValue()].claimed = true;
            return new OceanMonumentPieces.OceanMonumentDoubleXRoom(param0, param1);
        }
    }

    static class FitDoubleXYRoom implements OceanMonumentPieces.MonumentRoomFitter {
        @Override
        public boolean fits(OceanMonumentPieces.RoomDefinition param0) {
            if (param0.hasOpening[Direction.EAST.get3DDataValue()]
                && !param0.connections[Direction.EAST.get3DDataValue()].claimed
                && param0.hasOpening[Direction.UP.get3DDataValue()]
                && !param0.connections[Direction.UP.get3DDataValue()].claimed) {
                OceanMonumentPieces.RoomDefinition var0 = param0.connections[Direction.EAST.get3DDataValue()];
                return var0.hasOpening[Direction.UP.get3DDataValue()] && !var0.connections[Direction.UP.get3DDataValue()].claimed;
            } else {
                return false;
            }
        }

        @Override
        public OceanMonumentPieces.OceanMonumentPiece create(Direction param0, OceanMonumentPieces.RoomDefinition param1, RandomSource param2) {
            param1.claimed = true;
            param1.connections[Direction.EAST.get3DDataValue()].claimed = true;
            param1.connections[Direction.UP.get3DDataValue()].claimed = true;
            param1.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
            return new OceanMonumentPieces.OceanMonumentDoubleXYRoom(param0, param1);
        }
    }

    static class FitDoubleYRoom implements OceanMonumentPieces.MonumentRoomFitter {
        @Override
        public boolean fits(OceanMonumentPieces.RoomDefinition param0) {
            return param0.hasOpening[Direction.UP.get3DDataValue()] && !param0.connections[Direction.UP.get3DDataValue()].claimed;
        }

        @Override
        public OceanMonumentPieces.OceanMonumentPiece create(Direction param0, OceanMonumentPieces.RoomDefinition param1, RandomSource param2) {
            param1.claimed = true;
            param1.connections[Direction.UP.get3DDataValue()].claimed = true;
            return new OceanMonumentPieces.OceanMonumentDoubleYRoom(param0, param1);
        }
    }

    static class FitDoubleYZRoom implements OceanMonumentPieces.MonumentRoomFitter {
        @Override
        public boolean fits(OceanMonumentPieces.RoomDefinition param0) {
            if (param0.hasOpening[Direction.NORTH.get3DDataValue()]
                && !param0.connections[Direction.NORTH.get3DDataValue()].claimed
                && param0.hasOpening[Direction.UP.get3DDataValue()]
                && !param0.connections[Direction.UP.get3DDataValue()].claimed) {
                OceanMonumentPieces.RoomDefinition var0 = param0.connections[Direction.NORTH.get3DDataValue()];
                return var0.hasOpening[Direction.UP.get3DDataValue()] && !var0.connections[Direction.UP.get3DDataValue()].claimed;
            } else {
                return false;
            }
        }

        @Override
        public OceanMonumentPieces.OceanMonumentPiece create(Direction param0, OceanMonumentPieces.RoomDefinition param1, RandomSource param2) {
            param1.claimed = true;
            param1.connections[Direction.NORTH.get3DDataValue()].claimed = true;
            param1.connections[Direction.UP.get3DDataValue()].claimed = true;
            param1.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
            return new OceanMonumentPieces.OceanMonumentDoubleYZRoom(param0, param1);
        }
    }

    static class FitDoubleZRoom implements OceanMonumentPieces.MonumentRoomFitter {
        @Override
        public boolean fits(OceanMonumentPieces.RoomDefinition param0) {
            return param0.hasOpening[Direction.NORTH.get3DDataValue()] && !param0.connections[Direction.NORTH.get3DDataValue()].claimed;
        }

        @Override
        public OceanMonumentPieces.OceanMonumentPiece create(Direction param0, OceanMonumentPieces.RoomDefinition param1, RandomSource param2) {
            OceanMonumentPieces.RoomDefinition var0 = param1;
            if (!param1.hasOpening[Direction.NORTH.get3DDataValue()] || param1.connections[Direction.NORTH.get3DDataValue()].claimed) {
                var0 = param1.connections[Direction.SOUTH.get3DDataValue()];
            }

            var0.claimed = true;
            var0.connections[Direction.NORTH.get3DDataValue()].claimed = true;
            return new OceanMonumentPieces.OceanMonumentDoubleZRoom(param0, var0);
        }
    }

    static class FitSimpleRoom implements OceanMonumentPieces.MonumentRoomFitter {
        @Override
        public boolean fits(OceanMonumentPieces.RoomDefinition param0) {
            return true;
        }

        @Override
        public OceanMonumentPieces.OceanMonumentPiece create(Direction param0, OceanMonumentPieces.RoomDefinition param1, RandomSource param2) {
            param1.claimed = true;
            return new OceanMonumentPieces.OceanMonumentSimpleRoom(param0, param1, param2);
        }
    }

    static class FitSimpleTopRoom implements OceanMonumentPieces.MonumentRoomFitter {
        @Override
        public boolean fits(OceanMonumentPieces.RoomDefinition param0) {
            return !param0.hasOpening[Direction.WEST.get3DDataValue()]
                && !param0.hasOpening[Direction.EAST.get3DDataValue()]
                && !param0.hasOpening[Direction.NORTH.get3DDataValue()]
                && !param0.hasOpening[Direction.SOUTH.get3DDataValue()]
                && !param0.hasOpening[Direction.UP.get3DDataValue()];
        }

        @Override
        public OceanMonumentPieces.OceanMonumentPiece create(Direction param0, OceanMonumentPieces.RoomDefinition param1, RandomSource param2) {
            param1.claimed = true;
            return new OceanMonumentPieces.OceanMonumentSimpleTopRoom(param0, param1);
        }
    }

    public static class MonumentBuilding extends OceanMonumentPieces.OceanMonumentPiece {
        private static final int WIDTH = 58;
        private static final int HEIGHT = 22;
        private static final int DEPTH = 58;
        public static final int BIOME_RANGE_CHECK = 29;
        private static final int TOP_POSITION = 61;
        private OceanMonumentPieces.RoomDefinition sourceRoom;
        private OceanMonumentPieces.RoomDefinition coreRoom;
        private final List<OceanMonumentPieces.OceanMonumentPiece> childPieces = Lists.newArrayList();

        public MonumentBuilding(RandomSource param0, int param1, int param2, Direction param3) {
            super(StructurePieceType.OCEAN_MONUMENT_BUILDING, param3, 0, makeBoundingBox(param1, 39, param2, param3, 58, 23, 58));
            this.setOrientation(param3);
            List<OceanMonumentPieces.RoomDefinition> var0 = this.generateRoomGraph(param0);
            this.sourceRoom.claimed = true;
            this.childPieces.add(new OceanMonumentPieces.OceanMonumentEntryRoom(param3, this.sourceRoom));
            this.childPieces.add(new OceanMonumentPieces.OceanMonumentCoreRoom(param3, this.coreRoom));
            List<OceanMonumentPieces.MonumentRoomFitter> var1 = Lists.newArrayList();
            var1.add(new OceanMonumentPieces.FitDoubleXYRoom());
            var1.add(new OceanMonumentPieces.FitDoubleYZRoom());
            var1.add(new OceanMonumentPieces.FitDoubleZRoom());
            var1.add(new OceanMonumentPieces.FitDoubleXRoom());
            var1.add(new OceanMonumentPieces.FitDoubleYRoom());
            var1.add(new OceanMonumentPieces.FitSimpleTopRoom());
            var1.add(new OceanMonumentPieces.FitSimpleRoom());

            for(OceanMonumentPieces.RoomDefinition var2 : var0) {
                if (!var2.claimed && !var2.isSpecial()) {
                    for(OceanMonumentPieces.MonumentRoomFitter var3 : var1) {
                        if (var3.fits(var2)) {
                            this.childPieces.add(var3.create(param3, var2, param0));
                            break;
                        }
                    }
                }
            }

            BlockPos var4 = this.getWorldPos(9, 0, 22);

            for(OceanMonumentPieces.OceanMonumentPiece var5 : this.childPieces) {
                var5.getBoundingBox().move(var4);
            }

            BoundingBox var6 = BoundingBox.fromCorners(this.getWorldPos(1, 1, 1), this.getWorldPos(23, 8, 21));
            BoundingBox var7 = BoundingBox.fromCorners(this.getWorldPos(34, 1, 1), this.getWorldPos(56, 8, 21));
            BoundingBox var8 = BoundingBox.fromCorners(this.getWorldPos(22, 13, 22), this.getWorldPos(35, 17, 35));
            int var9 = param0.nextInt();
            this.childPieces.add(new OceanMonumentPieces.OceanMonumentWingRoom(param3, var6, var9++));
            this.childPieces.add(new OceanMonumentPieces.OceanMonumentWingRoom(param3, var7, var9++));
            this.childPieces.add(new OceanMonumentPieces.OceanMonumentPenthouse(param3, var8));
        }

        public MonumentBuilding(CompoundTag param0) {
            super(StructurePieceType.OCEAN_MONUMENT_BUILDING, param0);
        }

        private List<OceanMonumentPieces.RoomDefinition> generateRoomGraph(RandomSource param0) {
            OceanMonumentPieces.RoomDefinition[] var0 = new OceanMonumentPieces.RoomDefinition[75];

            for(int var1 = 0; var1 < 5; ++var1) {
                for(int var2 = 0; var2 < 4; ++var2) {
                    int var3 = 0;
                    int var4 = getRoomIndex(var1, 0, var2);
                    var0[var4] = new OceanMonumentPieces.RoomDefinition(var4);
                }
            }

            for(int var5 = 0; var5 < 5; ++var5) {
                for(int var6 = 0; var6 < 4; ++var6) {
                    int var7 = 1;
                    int var8 = getRoomIndex(var5, 1, var6);
                    var0[var8] = new OceanMonumentPieces.RoomDefinition(var8);
                }
            }

            for(int var9 = 1; var9 < 4; ++var9) {
                for(int var10 = 0; var10 < 2; ++var10) {
                    int var11 = 2;
                    int var12 = getRoomIndex(var9, 2, var10);
                    var0[var12] = new OceanMonumentPieces.RoomDefinition(var12);
                }
            }

            this.sourceRoom = var0[GRIDROOM_SOURCE_INDEX];

            for(int var13 = 0; var13 < 5; ++var13) {
                for(int var14 = 0; var14 < 5; ++var14) {
                    for(int var15 = 0; var15 < 3; ++var15) {
                        int var16 = getRoomIndex(var13, var15, var14);
                        if (var0[var16] != null) {
                            for(Direction var17 : Direction.values()) {
                                int var18 = var13 + var17.getStepX();
                                int var19 = var15 + var17.getStepY();
                                int var20 = var14 + var17.getStepZ();
                                if (var18 >= 0 && var18 < 5 && var20 >= 0 && var20 < 5 && var19 >= 0 && var19 < 3) {
                                    int var21 = getRoomIndex(var18, var19, var20);
                                    if (var0[var21] != null) {
                                        if (var20 == var14) {
                                            var0[var16].setConnection(var17, var0[var21]);
                                        } else {
                                            var0[var16].setConnection(var17.getOpposite(), var0[var21]);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            OceanMonumentPieces.RoomDefinition var22 = new OceanMonumentPieces.RoomDefinition(1003);
            OceanMonumentPieces.RoomDefinition var23 = new OceanMonumentPieces.RoomDefinition(1001);
            OceanMonumentPieces.RoomDefinition var24 = new OceanMonumentPieces.RoomDefinition(1002);
            var0[GRIDROOM_TOP_CONNECT_INDEX].setConnection(Direction.UP, var22);
            var0[GRIDROOM_LEFTWING_CONNECT_INDEX].setConnection(Direction.SOUTH, var23);
            var0[GRIDROOM_RIGHTWING_CONNECT_INDEX].setConnection(Direction.SOUTH, var24);
            var22.claimed = true;
            var23.claimed = true;
            var24.claimed = true;
            this.sourceRoom.isSource = true;
            this.coreRoom = var0[getRoomIndex(param0.nextInt(4), 0, 2)];
            this.coreRoom.claimed = true;
            this.coreRoom.connections[Direction.EAST.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.NORTH.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.UP.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
            this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
            ObjectArrayList<OceanMonumentPieces.RoomDefinition> var25 = new ObjectArrayList<>();

            for(OceanMonumentPieces.RoomDefinition var26 : var0) {
                if (var26 != null) {
                    var26.updateOpenings();
                    var25.add(var26);
                }
            }

            var22.updateOpenings();
            Util.shuffle(var25, param0);
            int var27 = 1;

            for(OceanMonumentPieces.RoomDefinition var28 : var25) {
                int var29 = 0;
                int var30 = 0;

                while(var29 < 2 && var30 < 5) {
                    ++var30;
                    int var31 = param0.nextInt(6);
                    if (var28.hasOpening[var31]) {
                        int var32 = Direction.from3DDataValue(var31).getOpposite().get3DDataValue();
                        var28.hasOpening[var31] = false;
                        var28.connections[var31].hasOpening[var32] = false;
                        if (var28.findSource(var27++) && var28.connections[var31].findSource(var27++)) {
                            ++var29;
                        } else {
                            var28.hasOpening[var31] = true;
                            var28.connections[var31].hasOpening[var32] = true;
                        }
                    }
                }
            }

            var25.add(var22);
            var25.add(var23);
            var25.add(var24);
            return var25;
        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            int var0 = Math.max(param0.getSeaLevel(), 64) - this.boundingBox.minY();
            this.generateWaterBox(param0, param4, 0, 0, 0, 58, var0, 58);
            this.generateWing(false, 0, param0, param3, param4);
            this.generateWing(true, 33, param0, param3, param4);
            this.generateEntranceArchs(param0, param3, param4);
            this.generateEntranceWall(param0, param3, param4);
            this.generateRoofPiece(param0, param3, param4);
            this.generateLowerWall(param0, param3, param4);
            this.generateMiddleWall(param0, param3, param4);
            this.generateUpperWall(param0, param3, param4);

            for(int var1 = 0; var1 < 7; ++var1) {
                int var2 = 0;

                while(var2 < 7) {
                    if (var2 == 0 && var1 == 3) {
                        var2 = 6;
                    }

                    int var3 = var1 * 9;
                    int var4 = var2 * 9;

                    for(int var5 = 0; var5 < 4; ++var5) {
                        for(int var6 = 0; var6 < 4; ++var6) {
                            this.placeBlock(param0, BASE_LIGHT, var3 + var5, 0, var4 + var6, param4);
                            this.fillColumnDown(param0, BASE_LIGHT, var3 + var5, -1, var4 + var6, param4);
                        }
                    }

                    if (var1 != 0 && var1 != 6) {
                        var2 += 6;
                    } else {
                        ++var2;
                    }
                }
            }

            for(int var7 = 0; var7 < 5; ++var7) {
                this.generateWaterBox(param0, param4, -1 - var7, 0 + var7 * 2, -1 - var7, -1 - var7, 23, 58 + var7);
                this.generateWaterBox(param0, param4, 58 + var7, 0 + var7 * 2, -1 - var7, 58 + var7, 23, 58 + var7);
                this.generateWaterBox(param0, param4, 0 - var7, 0 + var7 * 2, -1 - var7, 57 + var7, 23, -1 - var7);
                this.generateWaterBox(param0, param4, 0 - var7, 0 + var7 * 2, 58 + var7, 57 + var7, 23, 58 + var7);
            }

            for(OceanMonumentPieces.OceanMonumentPiece var8 : this.childPieces) {
                if (var8.getBoundingBox().intersects(param4)) {
                    var8.postProcess(param0, param1, param2, param3, param4, param5, param6);
                }
            }

        }

        private void generateWing(boolean param0, int param1, WorldGenLevel param2, RandomSource param3, BoundingBox param4) {
            int var0 = 24;
            if (this.chunkIntersects(param4, param1, 0, param1 + 23, 20)) {
                this.generateBox(param2, param4, param1 + 0, 0, 0, param1 + 24, 0, 20, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(param2, param4, param1 + 0, 1, 0, param1 + 24, 10, 20);

                for(int var1 = 0; var1 < 4; ++var1) {
                    this.generateBox(param2, param4, param1 + var1, var1 + 1, var1, param1 + var1, var1 + 1, 20, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param2, param4, param1 + var1 + 7, var1 + 5, var1 + 7, param1 + var1 + 7, var1 + 5, 20, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param2, param4, param1 + 17 - var1, var1 + 5, var1 + 7, param1 + 17 - var1, var1 + 5, 20, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param2, param4, param1 + 24 - var1, var1 + 1, var1, param1 + 24 - var1, var1 + 1, 20, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param2, param4, param1 + var1 + 1, var1 + 1, var1, param1 + 23 - var1, var1 + 1, var1, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(
                        param2, param4, param1 + var1 + 8, var1 + 5, var1 + 7, param1 + 16 - var1, var1 + 5, var1 + 7, BASE_LIGHT, BASE_LIGHT, false
                    );
                }

                this.generateBox(param2, param4, param1 + 4, 4, 4, param1 + 6, 4, 20, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param2, param4, param1 + 7, 4, 4, param1 + 17, 4, 6, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param2, param4, param1 + 18, 4, 4, param1 + 20, 4, 20, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param2, param4, param1 + 11, 8, 11, param1 + 13, 8, 20, BASE_GRAY, BASE_GRAY, false);
                this.placeBlock(param2, DOT_DECO_DATA, param1 + 12, 9, 12, param4);
                this.placeBlock(param2, DOT_DECO_DATA, param1 + 12, 9, 15, param4);
                this.placeBlock(param2, DOT_DECO_DATA, param1 + 12, 9, 18, param4);
                int var2 = param1 + (param0 ? 19 : 5);
                int var3 = param1 + (param0 ? 5 : 19);

                for(int var4 = 20; var4 >= 5; var4 -= 3) {
                    this.placeBlock(param2, DOT_DECO_DATA, var2, 5, var4, param4);
                }

                for(int var5 = 19; var5 >= 7; var5 -= 3) {
                    this.placeBlock(param2, DOT_DECO_DATA, var3, 5, var5, param4);
                }

                for(int var6 = 0; var6 < 4; ++var6) {
                    int var7 = param0 ? param1 + 24 - (17 - var6 * 3) : param1 + 17 - var6 * 3;
                    this.placeBlock(param2, DOT_DECO_DATA, var7, 5, 5, param4);
                }

                this.placeBlock(param2, DOT_DECO_DATA, var3, 5, 5, param4);
                this.generateBox(param2, param4, param1 + 11, 1, 12, param1 + 13, 7, 12, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param2, param4, param1 + 12, 1, 11, param1 + 12, 7, 13, BASE_GRAY, BASE_GRAY, false);
            }

        }

        private void generateEntranceArchs(WorldGenLevel param0, RandomSource param1, BoundingBox param2) {
            if (this.chunkIntersects(param2, 22, 5, 35, 17)) {
                this.generateWaterBox(param0, param2, 25, 0, 0, 32, 8, 20);

                for(int var0 = 0; var0 < 4; ++var0) {
                    this.generateBox(param0, param2, 24, 2, 5 + var0 * 4, 24, 4, 5 + var0 * 4, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param2, 22, 4, 5 + var0 * 4, 23, 4, 5 + var0 * 4, BASE_LIGHT, BASE_LIGHT, false);
                    this.placeBlock(param0, BASE_LIGHT, 25, 5, 5 + var0 * 4, param2);
                    this.placeBlock(param0, BASE_LIGHT, 26, 6, 5 + var0 * 4, param2);
                    this.placeBlock(param0, LAMP_BLOCK, 26, 5, 5 + var0 * 4, param2);
                    this.generateBox(param0, param2, 33, 2, 5 + var0 * 4, 33, 4, 5 + var0 * 4, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param2, 34, 4, 5 + var0 * 4, 35, 4, 5 + var0 * 4, BASE_LIGHT, BASE_LIGHT, false);
                    this.placeBlock(param0, BASE_LIGHT, 32, 5, 5 + var0 * 4, param2);
                    this.placeBlock(param0, BASE_LIGHT, 31, 6, 5 + var0 * 4, param2);
                    this.placeBlock(param0, LAMP_BLOCK, 31, 5, 5 + var0 * 4, param2);
                    this.generateBox(param0, param2, 27, 6, 5 + var0 * 4, 30, 6, 5 + var0 * 4, BASE_GRAY, BASE_GRAY, false);
                }
            }

        }

        private void generateEntranceWall(WorldGenLevel param0, RandomSource param1, BoundingBox param2) {
            if (this.chunkIntersects(param2, 15, 20, 42, 21)) {
                this.generateBox(param0, param2, 15, 0, 21, 42, 0, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(param0, param2, 26, 1, 21, 31, 3, 21);
                this.generateBox(param0, param2, 21, 12, 21, 36, 12, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 17, 11, 21, 40, 11, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 16, 10, 21, 41, 10, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 15, 7, 21, 42, 9, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 16, 6, 21, 41, 6, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 17, 5, 21, 40, 5, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 21, 4, 21, 36, 4, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 22, 3, 21, 26, 3, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 31, 3, 21, 35, 3, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 23, 2, 21, 25, 2, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 32, 2, 21, 34, 2, 21, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 28, 4, 20, 29, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
                this.placeBlock(param0, BASE_LIGHT, 27, 3, 21, param2);
                this.placeBlock(param0, BASE_LIGHT, 30, 3, 21, param2);
                this.placeBlock(param0, BASE_LIGHT, 26, 2, 21, param2);
                this.placeBlock(param0, BASE_LIGHT, 31, 2, 21, param2);
                this.placeBlock(param0, BASE_LIGHT, 25, 1, 21, param2);
                this.placeBlock(param0, BASE_LIGHT, 32, 1, 21, param2);

                for(int var0 = 0; var0 < 7; ++var0) {
                    this.placeBlock(param0, BASE_BLACK, 28 - var0, 6 + var0, 21, param2);
                    this.placeBlock(param0, BASE_BLACK, 29 + var0, 6 + var0, 21, param2);
                }

                for(int var1 = 0; var1 < 4; ++var1) {
                    this.placeBlock(param0, BASE_BLACK, 28 - var1, 9 + var1, 21, param2);
                    this.placeBlock(param0, BASE_BLACK, 29 + var1, 9 + var1, 21, param2);
                }

                this.placeBlock(param0, BASE_BLACK, 28, 12, 21, param2);
                this.placeBlock(param0, BASE_BLACK, 29, 12, 21, param2);

                for(int var2 = 0; var2 < 3; ++var2) {
                    this.placeBlock(param0, BASE_BLACK, 22 - var2 * 2, 8, 21, param2);
                    this.placeBlock(param0, BASE_BLACK, 22 - var2 * 2, 9, 21, param2);
                    this.placeBlock(param0, BASE_BLACK, 35 + var2 * 2, 8, 21, param2);
                    this.placeBlock(param0, BASE_BLACK, 35 + var2 * 2, 9, 21, param2);
                }

                this.generateWaterBox(param0, param2, 15, 13, 21, 42, 15, 21);
                this.generateWaterBox(param0, param2, 15, 1, 21, 15, 6, 21);
                this.generateWaterBox(param0, param2, 16, 1, 21, 16, 5, 21);
                this.generateWaterBox(param0, param2, 17, 1, 21, 20, 4, 21);
                this.generateWaterBox(param0, param2, 21, 1, 21, 21, 3, 21);
                this.generateWaterBox(param0, param2, 22, 1, 21, 22, 2, 21);
                this.generateWaterBox(param0, param2, 23, 1, 21, 24, 1, 21);
                this.generateWaterBox(param0, param2, 42, 1, 21, 42, 6, 21);
                this.generateWaterBox(param0, param2, 41, 1, 21, 41, 5, 21);
                this.generateWaterBox(param0, param2, 37, 1, 21, 40, 4, 21);
                this.generateWaterBox(param0, param2, 36, 1, 21, 36, 3, 21);
                this.generateWaterBox(param0, param2, 33, 1, 21, 34, 1, 21);
                this.generateWaterBox(param0, param2, 35, 1, 21, 35, 2, 21);
            }

        }

        private void generateRoofPiece(WorldGenLevel param0, RandomSource param1, BoundingBox param2) {
            if (this.chunkIntersects(param2, 21, 21, 36, 36)) {
                this.generateBox(param0, param2, 21, 0, 22, 36, 0, 36, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(param0, param2, 21, 1, 22, 36, 23, 36);

                for(int var0 = 0; var0 < 4; ++var0) {
                    this.generateBox(param0, param2, 21 + var0, 13 + var0, 21 + var0, 36 - var0, 13 + var0, 21 + var0, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param2, 21 + var0, 13 + var0, 36 - var0, 36 - var0, 13 + var0, 36 - var0, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param2, 21 + var0, 13 + var0, 22 + var0, 21 + var0, 13 + var0, 35 - var0, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param2, 36 - var0, 13 + var0, 22 + var0, 36 - var0, 13 + var0, 35 - var0, BASE_LIGHT, BASE_LIGHT, false);
                }

                this.generateBox(param0, param2, 25, 16, 25, 32, 16, 32, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 25, 17, 25, 25, 19, 25, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param2, 32, 17, 25, 32, 19, 25, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param2, 25, 17, 32, 25, 19, 32, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param2, 32, 17, 32, 32, 19, 32, BASE_LIGHT, BASE_LIGHT, false);
                this.placeBlock(param0, BASE_LIGHT, 26, 20, 26, param2);
                this.placeBlock(param0, BASE_LIGHT, 27, 21, 27, param2);
                this.placeBlock(param0, LAMP_BLOCK, 27, 20, 27, param2);
                this.placeBlock(param0, BASE_LIGHT, 26, 20, 31, param2);
                this.placeBlock(param0, BASE_LIGHT, 27, 21, 30, param2);
                this.placeBlock(param0, LAMP_BLOCK, 27, 20, 30, param2);
                this.placeBlock(param0, BASE_LIGHT, 31, 20, 31, param2);
                this.placeBlock(param0, BASE_LIGHT, 30, 21, 30, param2);
                this.placeBlock(param0, LAMP_BLOCK, 30, 20, 30, param2);
                this.placeBlock(param0, BASE_LIGHT, 31, 20, 26, param2);
                this.placeBlock(param0, BASE_LIGHT, 30, 21, 27, param2);
                this.placeBlock(param0, LAMP_BLOCK, 30, 20, 27, param2);
                this.generateBox(param0, param2, 28, 21, 27, 29, 21, 27, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 27, 21, 28, 27, 21, 29, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 28, 21, 30, 29, 21, 30, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 30, 21, 28, 30, 21, 29, BASE_GRAY, BASE_GRAY, false);
            }

        }

        private void generateLowerWall(WorldGenLevel param0, RandomSource param1, BoundingBox param2) {
            if (this.chunkIntersects(param2, 0, 21, 6, 58)) {
                this.generateBox(param0, param2, 0, 0, 21, 6, 0, 57, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(param0, param2, 0, 1, 21, 6, 7, 57);
                this.generateBox(param0, param2, 4, 4, 21, 6, 4, 53, BASE_GRAY, BASE_GRAY, false);

                for(int var0 = 0; var0 < 4; ++var0) {
                    this.generateBox(param0, param2, var0, var0 + 1, 21, var0, var0 + 1, 57 - var0, BASE_LIGHT, BASE_LIGHT, false);
                }

                for(int var1 = 23; var1 < 53; var1 += 3) {
                    this.placeBlock(param0, DOT_DECO_DATA, 5, 5, var1, param2);
                }

                this.placeBlock(param0, DOT_DECO_DATA, 5, 5, 52, param2);

                for(int var2 = 0; var2 < 4; ++var2) {
                    this.generateBox(param0, param2, var2, var2 + 1, 21, var2, var2 + 1, 57 - var2, BASE_LIGHT, BASE_LIGHT, false);
                }

                this.generateBox(param0, param2, 4, 1, 52, 6, 3, 52, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 5, 1, 51, 5, 3, 53, BASE_GRAY, BASE_GRAY, false);
            }

            if (this.chunkIntersects(param2, 51, 21, 58, 58)) {
                this.generateBox(param0, param2, 51, 0, 21, 57, 0, 57, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(param0, param2, 51, 1, 21, 57, 7, 57);
                this.generateBox(param0, param2, 51, 4, 21, 53, 4, 53, BASE_GRAY, BASE_GRAY, false);

                for(int var3 = 0; var3 < 4; ++var3) {
                    this.generateBox(param0, param2, 57 - var3, var3 + 1, 21, 57 - var3, var3 + 1, 57 - var3, BASE_LIGHT, BASE_LIGHT, false);
                }

                for(int var4 = 23; var4 < 53; var4 += 3) {
                    this.placeBlock(param0, DOT_DECO_DATA, 52, 5, var4, param2);
                }

                this.placeBlock(param0, DOT_DECO_DATA, 52, 5, 52, param2);
                this.generateBox(param0, param2, 51, 1, 52, 53, 3, 52, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 52, 1, 51, 52, 3, 53, BASE_GRAY, BASE_GRAY, false);
            }

            if (this.chunkIntersects(param2, 0, 51, 57, 57)) {
                this.generateBox(param0, param2, 7, 0, 51, 50, 0, 57, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(param0, param2, 7, 1, 51, 50, 10, 57);

                for(int var5 = 0; var5 < 4; ++var5) {
                    this.generateBox(param0, param2, var5 + 1, var5 + 1, 57 - var5, 56 - var5, var5 + 1, 57 - var5, BASE_LIGHT, BASE_LIGHT, false);
                }
            }

        }

        private void generateMiddleWall(WorldGenLevel param0, RandomSource param1, BoundingBox param2) {
            if (this.chunkIntersects(param2, 7, 21, 13, 50)) {
                this.generateBox(param0, param2, 7, 0, 21, 13, 0, 50, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(param0, param2, 7, 1, 21, 13, 10, 50);
                this.generateBox(param0, param2, 11, 8, 21, 13, 8, 53, BASE_GRAY, BASE_GRAY, false);

                for(int var0 = 0; var0 < 4; ++var0) {
                    this.generateBox(param0, param2, var0 + 7, var0 + 5, 21, var0 + 7, var0 + 5, 54, BASE_LIGHT, BASE_LIGHT, false);
                }

                for(int var1 = 21; var1 <= 45; var1 += 3) {
                    this.placeBlock(param0, DOT_DECO_DATA, 12, 9, var1, param2);
                }
            }

            if (this.chunkIntersects(param2, 44, 21, 50, 54)) {
                this.generateBox(param0, param2, 44, 0, 21, 50, 0, 50, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(param0, param2, 44, 1, 21, 50, 10, 50);
                this.generateBox(param0, param2, 44, 8, 21, 46, 8, 53, BASE_GRAY, BASE_GRAY, false);

                for(int var2 = 0; var2 < 4; ++var2) {
                    this.generateBox(param0, param2, 50 - var2, var2 + 5, 21, 50 - var2, var2 + 5, 54, BASE_LIGHT, BASE_LIGHT, false);
                }

                for(int var3 = 21; var3 <= 45; var3 += 3) {
                    this.placeBlock(param0, DOT_DECO_DATA, 45, 9, var3, param2);
                }
            }

            if (this.chunkIntersects(param2, 8, 44, 49, 54)) {
                this.generateBox(param0, param2, 14, 0, 44, 43, 0, 50, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(param0, param2, 14, 1, 44, 43, 10, 50);

                for(int var4 = 12; var4 <= 45; var4 += 3) {
                    this.placeBlock(param0, DOT_DECO_DATA, var4, 9, 45, param2);
                    this.placeBlock(param0, DOT_DECO_DATA, var4, 9, 52, param2);
                    if (var4 == 12 || var4 == 18 || var4 == 24 || var4 == 33 || var4 == 39 || var4 == 45) {
                        this.placeBlock(param0, DOT_DECO_DATA, var4, 9, 47, param2);
                        this.placeBlock(param0, DOT_DECO_DATA, var4, 9, 50, param2);
                        this.placeBlock(param0, DOT_DECO_DATA, var4, 10, 45, param2);
                        this.placeBlock(param0, DOT_DECO_DATA, var4, 10, 46, param2);
                        this.placeBlock(param0, DOT_DECO_DATA, var4, 10, 51, param2);
                        this.placeBlock(param0, DOT_DECO_DATA, var4, 10, 52, param2);
                        this.placeBlock(param0, DOT_DECO_DATA, var4, 11, 47, param2);
                        this.placeBlock(param0, DOT_DECO_DATA, var4, 11, 50, param2);
                        this.placeBlock(param0, DOT_DECO_DATA, var4, 12, 48, param2);
                        this.placeBlock(param0, DOT_DECO_DATA, var4, 12, 49, param2);
                    }
                }

                for(int var5 = 0; var5 < 3; ++var5) {
                    this.generateBox(param0, param2, 8 + var5, 5 + var5, 54, 49 - var5, 5 + var5, 54, BASE_GRAY, BASE_GRAY, false);
                }

                this.generateBox(param0, param2, 11, 8, 54, 46, 8, 54, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param2, 14, 8, 44, 43, 8, 53, BASE_GRAY, BASE_GRAY, false);
            }

        }

        private void generateUpperWall(WorldGenLevel param0, RandomSource param1, BoundingBox param2) {
            if (this.chunkIntersects(param2, 14, 21, 20, 43)) {
                this.generateBox(param0, param2, 14, 0, 21, 20, 0, 43, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(param0, param2, 14, 1, 22, 20, 14, 43);
                this.generateBox(param0, param2, 18, 12, 22, 20, 12, 39, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 18, 12, 21, 20, 12, 21, BASE_LIGHT, BASE_LIGHT, false);

                for(int var0 = 0; var0 < 4; ++var0) {
                    this.generateBox(param0, param2, var0 + 14, var0 + 9, 21, var0 + 14, var0 + 9, 43 - var0, BASE_LIGHT, BASE_LIGHT, false);
                }

                for(int var1 = 23; var1 <= 39; var1 += 3) {
                    this.placeBlock(param0, DOT_DECO_DATA, 19, 13, var1, param2);
                }
            }

            if (this.chunkIntersects(param2, 37, 21, 43, 43)) {
                this.generateBox(param0, param2, 37, 0, 21, 43, 0, 43, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(param0, param2, 37, 1, 22, 43, 14, 43);
                this.generateBox(param0, param2, 37, 12, 22, 39, 12, 39, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param2, 37, 12, 21, 39, 12, 21, BASE_LIGHT, BASE_LIGHT, false);

                for(int var2 = 0; var2 < 4; ++var2) {
                    this.generateBox(param0, param2, 43 - var2, var2 + 9, 21, 43 - var2, var2 + 9, 43 - var2, BASE_LIGHT, BASE_LIGHT, false);
                }

                for(int var3 = 23; var3 <= 39; var3 += 3) {
                    this.placeBlock(param0, DOT_DECO_DATA, 38, 13, var3, param2);
                }
            }

            if (this.chunkIntersects(param2, 15, 37, 42, 43)) {
                this.generateBox(param0, param2, 21, 0, 37, 36, 0, 43, BASE_GRAY, BASE_GRAY, false);
                this.generateWaterBox(param0, param2, 21, 1, 37, 36, 14, 43);
                this.generateBox(param0, param2, 21, 12, 37, 36, 12, 39, BASE_GRAY, BASE_GRAY, false);

                for(int var4 = 0; var4 < 4; ++var4) {
                    this.generateBox(param0, param2, 15 + var4, var4 + 9, 43 - var4, 42 - var4, var4 + 9, 43 - var4, BASE_LIGHT, BASE_LIGHT, false);
                }

                for(int var5 = 21; var5 <= 36; var5 += 3) {
                    this.placeBlock(param0, DOT_DECO_DATA, var5, 13, 38, param2);
                }
            }

        }
    }

    interface MonumentRoomFitter {
        boolean fits(OceanMonumentPieces.RoomDefinition var1);

        OceanMonumentPieces.OceanMonumentPiece create(Direction var1, OceanMonumentPieces.RoomDefinition var2, RandomSource var3);
    }

    public static class OceanMonumentCoreRoom extends OceanMonumentPieces.OceanMonumentPiece {
        public OceanMonumentCoreRoom(Direction param0, OceanMonumentPieces.RoomDefinition param1) {
            super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, 1, param0, param1, 2, 2, 2);
        }

        public OceanMonumentCoreRoom(CompoundTag param0) {
            super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, param0);
        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            this.generateBoxOnFillOnly(param0, param4, 1, 8, 0, 14, 8, 14, BASE_GRAY);
            int var0 = 7;
            BlockState var1 = BASE_LIGHT;
            this.generateBox(param0, param4, 0, 7, 0, 0, 7, 15, var1, var1, false);
            this.generateBox(param0, param4, 15, 7, 0, 15, 7, 15, var1, var1, false);
            this.generateBox(param0, param4, 1, 7, 0, 15, 7, 0, var1, var1, false);
            this.generateBox(param0, param4, 1, 7, 15, 14, 7, 15, var1, var1, false);

            for(int var2 = 1; var2 <= 6; ++var2) {
                var1 = BASE_LIGHT;
                if (var2 == 2 || var2 == 6) {
                    var1 = BASE_GRAY;
                }

                for(int var4 = 0; var4 <= 15; var4 += 15) {
                    this.generateBox(param0, param4, var4, var2, 0, var4, var2, 1, var1, var1, false);
                    this.generateBox(param0, param4, var4, var2, 6, var4, var2, 9, var1, var1, false);
                    this.generateBox(param0, param4, var4, var2, 14, var4, var2, 15, var1, var1, false);
                }

                this.generateBox(param0, param4, 1, var2, 0, 1, var2, 0, var1, var1, false);
                this.generateBox(param0, param4, 6, var2, 0, 9, var2, 0, var1, var1, false);
                this.generateBox(param0, param4, 14, var2, 0, 14, var2, 0, var1, var1, false);
                this.generateBox(param0, param4, 1, var2, 15, 14, var2, 15, var1, var1, false);
            }

            this.generateBox(param0, param4, 6, 3, 6, 9, 6, 9, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(param0, param4, 7, 4, 7, 8, 5, 8, Blocks.GOLD_BLOCK.defaultBlockState(), Blocks.GOLD_BLOCK.defaultBlockState(), false);

            for(int var5 = 3; var5 <= 6; var5 += 3) {
                for(int var6 = 6; var6 <= 9; var6 += 3) {
                    this.placeBlock(param0, LAMP_BLOCK, var6, var5, 6, param4);
                    this.placeBlock(param0, LAMP_BLOCK, var6, var5, 9, param4);
                }
            }

            this.generateBox(param0, param4, 5, 1, 6, 5, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 1, 9, 5, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 10, 1, 6, 10, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 10, 1, 9, 10, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 6, 1, 5, 6, 2, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 9, 1, 5, 9, 2, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 6, 1, 10, 6, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 9, 1, 10, 9, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 2, 5, 5, 6, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 2, 10, 5, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 10, 2, 5, 10, 6, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 10, 2, 10, 10, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 7, 1, 5, 7, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 10, 7, 1, 10, 7, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 7, 9, 5, 7, 14, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 10, 7, 9, 10, 7, 14, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 7, 5, 6, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 7, 10, 6, 7, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 9, 7, 5, 14, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 9, 7, 10, 14, 7, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 2, 1, 2, 2, 1, 3, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 3, 1, 2, 3, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 13, 1, 2, 13, 1, 3, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 12, 1, 2, 12, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 2, 1, 12, 2, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 3, 1, 13, 3, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 13, 1, 12, 13, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 12, 1, 13, 12, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
        }
    }

    public static class OceanMonumentDoubleXRoom extends OceanMonumentPieces.OceanMonumentPiece {
        public OceanMonumentDoubleXRoom(Direction param0, OceanMonumentPieces.RoomDefinition param1) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, 1, param0, param1, 2, 1, 1);
        }

        public OceanMonumentDoubleXRoom(CompoundTag param0) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, param0);
        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            OceanMonumentPieces.RoomDefinition var0 = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
            OceanMonumentPieces.RoomDefinition var1 = this.roomDefinition;
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(param0, param4, 8, 0, var0.hasOpening[Direction.DOWN.get3DDataValue()]);
                this.generateDefaultFloor(param0, param4, 0, 0, var1.hasOpening[Direction.DOWN.get3DDataValue()]);
            }

            if (var1.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(param0, param4, 1, 4, 1, 7, 4, 6, BASE_GRAY);
            }

            if (var0.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(param0, param4, 8, 4, 1, 14, 4, 6, BASE_GRAY);
            }

            this.generateBox(param0, param4, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 15, 3, 0, 15, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 3, 0, 15, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 3, 7, 14, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 0, 2, 0, 0, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(param0, param4, 15, 2, 0, 15, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(param0, param4, 1, 2, 0, 15, 2, 0, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(param0, param4, 1, 2, 7, 14, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(param0, param4, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 15, 1, 0, 15, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 1, 0, 15, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 1, 7, 14, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 1, 0, 10, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 6, 2, 0, 9, 2, 3, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(param0, param4, 5, 3, 0, 10, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(param0, LAMP_BLOCK, 6, 2, 3, param4);
            this.placeBlock(param0, LAMP_BLOCK, 9, 2, 3, param4);
            if (var1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 3, 1, 0, 4, 2, 0);
            }

            if (var1.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 3, 1, 7, 4, 2, 7);
            }

            if (var1.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 0, 1, 3, 0, 2, 4);
            }

            if (var0.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 11, 1, 0, 12, 2, 0);
            }

            if (var0.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 11, 1, 7, 12, 2, 7);
            }

            if (var0.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 15, 1, 3, 15, 2, 4);
            }

        }
    }

    public static class OceanMonumentDoubleXYRoom extends OceanMonumentPieces.OceanMonumentPiece {
        public OceanMonumentDoubleXYRoom(Direction param0, OceanMonumentPieces.RoomDefinition param1) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_XY_ROOM, 1, param0, param1, 2, 2, 1);
        }

        public OceanMonumentDoubleXYRoom(CompoundTag param0) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_XY_ROOM, param0);
        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            OceanMonumentPieces.RoomDefinition var0 = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
            OceanMonumentPieces.RoomDefinition var1 = this.roomDefinition;
            OceanMonumentPieces.RoomDefinition var2 = var1.connections[Direction.UP.get3DDataValue()];
            OceanMonumentPieces.RoomDefinition var3 = var0.connections[Direction.UP.get3DDataValue()];
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(param0, param4, 8, 0, var0.hasOpening[Direction.DOWN.get3DDataValue()]);
                this.generateDefaultFloor(param0, param4, 0, 0, var1.hasOpening[Direction.DOWN.get3DDataValue()]);
            }

            if (var2.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(param0, param4, 1, 8, 1, 7, 8, 6, BASE_GRAY);
            }

            if (var3.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(param0, param4, 8, 8, 1, 14, 8, 6, BASE_GRAY);
            }

            for(int var4 = 1; var4 <= 7; ++var4) {
                BlockState var5 = BASE_LIGHT;
                if (var4 == 2 || var4 == 6) {
                    var5 = BASE_GRAY;
                }

                this.generateBox(param0, param4, 0, var4, 0, 0, var4, 7, var5, var5, false);
                this.generateBox(param0, param4, 15, var4, 0, 15, var4, 7, var5, var5, false);
                this.generateBox(param0, param4, 1, var4, 0, 15, var4, 0, var5, var5, false);
                this.generateBox(param0, param4, 1, var4, 7, 14, var4, 7, var5, var5, false);
            }

            this.generateBox(param0, param4, 2, 1, 3, 2, 7, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 3, 1, 2, 4, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 3, 1, 5, 4, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 13, 1, 3, 13, 7, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 11, 1, 2, 12, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 11, 1, 5, 12, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 1, 3, 5, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 10, 1, 3, 10, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 7, 2, 10, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 5, 2, 5, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 10, 5, 2, 10, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 5, 5, 5, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 10, 5, 5, 10, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(param0, BASE_LIGHT, 6, 6, 2, param4);
            this.placeBlock(param0, BASE_LIGHT, 9, 6, 2, param4);
            this.placeBlock(param0, BASE_LIGHT, 6, 6, 5, param4);
            this.placeBlock(param0, BASE_LIGHT, 9, 6, 5, param4);
            this.generateBox(param0, param4, 5, 4, 3, 6, 4, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 9, 4, 3, 10, 4, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(param0, LAMP_BLOCK, 5, 4, 2, param4);
            this.placeBlock(param0, LAMP_BLOCK, 5, 4, 5, param4);
            this.placeBlock(param0, LAMP_BLOCK, 10, 4, 2, param4);
            this.placeBlock(param0, LAMP_BLOCK, 10, 4, 5, param4);
            if (var1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 3, 1, 0, 4, 2, 0);
            }

            if (var1.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 3, 1, 7, 4, 2, 7);
            }

            if (var1.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 0, 1, 3, 0, 2, 4);
            }

            if (var0.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 11, 1, 0, 12, 2, 0);
            }

            if (var0.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 11, 1, 7, 12, 2, 7);
            }

            if (var0.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 15, 1, 3, 15, 2, 4);
            }

            if (var2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 3, 5, 0, 4, 6, 0);
            }

            if (var2.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 3, 5, 7, 4, 6, 7);
            }

            if (var2.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 0, 5, 3, 0, 6, 4);
            }

            if (var3.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 11, 5, 0, 12, 6, 0);
            }

            if (var3.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 11, 5, 7, 12, 6, 7);
            }

            if (var3.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 15, 5, 3, 15, 6, 4);
            }

        }
    }

    public static class OceanMonumentDoubleYRoom extends OceanMonumentPieces.OceanMonumentPiece {
        public OceanMonumentDoubleYRoom(Direction param0, OceanMonumentPieces.RoomDefinition param1) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, 1, param0, param1, 1, 2, 1);
        }

        public OceanMonumentDoubleYRoom(CompoundTag param0) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, param0);
        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(param0, param4, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            }

            OceanMonumentPieces.RoomDefinition var0 = this.roomDefinition.connections[Direction.UP.get3DDataValue()];
            if (var0.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(param0, param4, 1, 8, 1, 6, 8, 6, BASE_GRAY);
            }

            this.generateBox(param0, param4, 0, 4, 0, 0, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 7, 4, 0, 7, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 4, 0, 6, 4, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 4, 7, 6, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 2, 4, 1, 2, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 4, 2, 1, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 4, 1, 5, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 6, 4, 2, 6, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 2, 4, 5, 2, 4, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 4, 5, 1, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 4, 5, 5, 4, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 6, 4, 5, 6, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
            OceanMonumentPieces.RoomDefinition var1 = this.roomDefinition;

            for(int var2 = 1; var2 <= 5; var2 += 4) {
                int var3 = 0;
                if (var1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                    this.generateBox(param0, param4, 2, var2, var3, 2, var2 + 2, var3, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, 5, var2, var3, 5, var2 + 2, var3, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, 3, var2 + 2, var3, 4, var2 + 2, var3, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(param0, param4, 0, var2, var3, 7, var2 + 2, var3, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, 0, var2 + 1, var3, 7, var2 + 1, var3, BASE_GRAY, BASE_GRAY, false);
                }

                int var13 = 7;
                if (var1.hasOpening[Direction.NORTH.get3DDataValue()]) {
                    this.generateBox(param0, param4, 2, var2, var13, 2, var2 + 2, var13, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, 5, var2, var13, 5, var2 + 2, var13, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, 3, var2 + 2, var13, 4, var2 + 2, var13, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(param0, param4, 0, var2, var13, 7, var2 + 2, var13, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, 0, var2 + 1, var13, 7, var2 + 1, var13, BASE_GRAY, BASE_GRAY, false);
                }

                int var4 = 0;
                if (var1.hasOpening[Direction.WEST.get3DDataValue()]) {
                    this.generateBox(param0, param4, var4, var2, 2, var4, var2 + 2, 2, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, var4, var2, 5, var4, var2 + 2, 5, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, var4, var2 + 2, 3, var4, var2 + 2, 4, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(param0, param4, var4, var2, 0, var4, var2 + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, var4, var2 + 1, 0, var4, var2 + 1, 7, BASE_GRAY, BASE_GRAY, false);
                }

                int var14 = 7;
                if (var1.hasOpening[Direction.EAST.get3DDataValue()]) {
                    this.generateBox(param0, param4, var14, var2, 2, var14, var2 + 2, 2, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, var14, var2, 5, var14, var2 + 2, 5, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, var14, var2 + 2, 3, var14, var2 + 2, 4, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(param0, param4, var14, var2, 0, var14, var2 + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, var14, var2 + 1, 0, var14, var2 + 1, 7, BASE_GRAY, BASE_GRAY, false);
                }

                var1 = var0;
            }

        }
    }

    public static class OceanMonumentDoubleYZRoom extends OceanMonumentPieces.OceanMonumentPiece {
        public OceanMonumentDoubleYZRoom(Direction param0, OceanMonumentPieces.RoomDefinition param1) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_YZ_ROOM, 1, param0, param1, 1, 2, 2);
        }

        public OceanMonumentDoubleYZRoom(CompoundTag param0) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_YZ_ROOM, param0);
        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            OceanMonumentPieces.RoomDefinition var0 = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
            OceanMonumentPieces.RoomDefinition var1 = this.roomDefinition;
            OceanMonumentPieces.RoomDefinition var2 = var0.connections[Direction.UP.get3DDataValue()];
            OceanMonumentPieces.RoomDefinition var3 = var1.connections[Direction.UP.get3DDataValue()];
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(param0, param4, 0, 8, var0.hasOpening[Direction.DOWN.get3DDataValue()]);
                this.generateDefaultFloor(param0, param4, 0, 0, var1.hasOpening[Direction.DOWN.get3DDataValue()]);
            }

            if (var3.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(param0, param4, 1, 8, 1, 6, 8, 7, BASE_GRAY);
            }

            if (var2.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(param0, param4, 1, 8, 8, 6, 8, 14, BASE_GRAY);
            }

            for(int var4 = 1; var4 <= 7; ++var4) {
                BlockState var5 = BASE_LIGHT;
                if (var4 == 2 || var4 == 6) {
                    var5 = BASE_GRAY;
                }

                this.generateBox(param0, param4, 0, var4, 0, 0, var4, 15, var5, var5, false);
                this.generateBox(param0, param4, 7, var4, 0, 7, var4, 15, var5, var5, false);
                this.generateBox(param0, param4, 1, var4, 0, 6, var4, 0, var5, var5, false);
                this.generateBox(param0, param4, 1, var4, 15, 6, var4, 15, var5, var5, false);
            }

            for(int var6 = 1; var6 <= 7; ++var6) {
                BlockState var7 = BASE_BLACK;
                if (var6 == 2 || var6 == 6) {
                    var7 = LAMP_BLOCK;
                }

                this.generateBox(param0, param4, 3, var6, 7, 4, var6, 8, var7, var7, false);
            }

            if (var1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 3, 1, 0, 4, 2, 0);
            }

            if (var1.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 7, 1, 3, 7, 2, 4);
            }

            if (var1.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 0, 1, 3, 0, 2, 4);
            }

            if (var0.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 3, 1, 15, 4, 2, 15);
            }

            if (var0.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 0, 1, 11, 0, 2, 12);
            }

            if (var0.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 7, 1, 11, 7, 2, 12);
            }

            if (var3.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 3, 5, 0, 4, 6, 0);
            }

            if (var3.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 7, 5, 3, 7, 6, 4);
                this.generateBox(param0, param4, 5, 4, 2, 6, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 6, 1, 2, 6, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 6, 1, 5, 6, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (var3.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 0, 5, 3, 0, 6, 4);
                this.generateBox(param0, param4, 1, 4, 2, 2, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 1, 1, 2, 1, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 1, 1, 5, 1, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (var2.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 3, 5, 15, 4, 6, 15);
            }

            if (var2.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 0, 5, 11, 0, 6, 12);
                this.generateBox(param0, param4, 1, 4, 10, 2, 4, 13, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 1, 1, 10, 1, 3, 10, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 1, 1, 13, 1, 3, 13, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (var2.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 7, 5, 11, 7, 6, 12);
                this.generateBox(param0, param4, 5, 4, 10, 6, 4, 13, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 6, 1, 10, 6, 3, 10, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 6, 1, 13, 6, 3, 13, BASE_LIGHT, BASE_LIGHT, false);
            }

        }
    }

    public static class OceanMonumentDoubleZRoom extends OceanMonumentPieces.OceanMonumentPiece {
        public OceanMonumentDoubleZRoom(Direction param0, OceanMonumentPieces.RoomDefinition param1) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, 1, param0, param1, 1, 1, 2);
        }

        public OceanMonumentDoubleZRoom(CompoundTag param0) {
            super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, param0);
        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            OceanMonumentPieces.RoomDefinition var0 = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
            OceanMonumentPieces.RoomDefinition var1 = this.roomDefinition;
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(param0, param4, 0, 8, var0.hasOpening[Direction.DOWN.get3DDataValue()]);
                this.generateDefaultFloor(param0, param4, 0, 0, var1.hasOpening[Direction.DOWN.get3DDataValue()]);
            }

            if (var1.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(param0, param4, 1, 4, 1, 6, 4, 7, BASE_GRAY);
            }

            if (var0.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(param0, param4, 1, 4, 8, 6, 4, 14, BASE_GRAY);
            }

            this.generateBox(param0, param4, 0, 3, 0, 0, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 7, 3, 0, 7, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 3, 0, 7, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 3, 15, 6, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 0, 2, 0, 0, 2, 15, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(param0, param4, 7, 2, 0, 7, 2, 15, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(param0, param4, 1, 2, 0, 7, 2, 0, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(param0, param4, 1, 2, 15, 6, 2, 15, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(param0, param4, 0, 1, 0, 0, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 7, 1, 0, 7, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 1, 0, 7, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 1, 15, 6, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 1, 1, 1, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 6, 1, 1, 6, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 3, 1, 1, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 6, 3, 1, 6, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 1, 13, 1, 1, 14, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 6, 1, 13, 6, 1, 14, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 3, 13, 1, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 6, 3, 13, 6, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 2, 1, 6, 2, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 1, 6, 5, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 2, 1, 9, 2, 3, 9, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 1, 9, 5, 3, 9, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 3, 2, 6, 4, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 3, 2, 9, 4, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 2, 2, 7, 2, 2, 8, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 2, 7, 5, 2, 8, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(param0, LAMP_BLOCK, 2, 2, 5, param4);
            this.placeBlock(param0, LAMP_BLOCK, 5, 2, 5, param4);
            this.placeBlock(param0, LAMP_BLOCK, 2, 2, 10, param4);
            this.placeBlock(param0, LAMP_BLOCK, 5, 2, 10, param4);
            this.placeBlock(param0, BASE_LIGHT, 2, 3, 5, param4);
            this.placeBlock(param0, BASE_LIGHT, 5, 3, 5, param4);
            this.placeBlock(param0, BASE_LIGHT, 2, 3, 10, param4);
            this.placeBlock(param0, BASE_LIGHT, 5, 3, 10, param4);
            if (var1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 3, 1, 0, 4, 2, 0);
            }

            if (var1.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 7, 1, 3, 7, 2, 4);
            }

            if (var1.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 0, 1, 3, 0, 2, 4);
            }

            if (var0.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 3, 1, 15, 4, 2, 15);
            }

            if (var0.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 0, 1, 11, 0, 2, 12);
            }

            if (var0.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 7, 1, 11, 7, 2, 12);
            }

        }
    }

    public static class OceanMonumentEntryRoom extends OceanMonumentPieces.OceanMonumentPiece {
        public OceanMonumentEntryRoom(Direction param0, OceanMonumentPieces.RoomDefinition param1) {
            super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, 1, param0, param1, 1, 1, 1);
        }

        public OceanMonumentEntryRoom(CompoundTag param0) {
            super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, param0);
        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            this.generateBox(param0, param4, 0, 3, 0, 2, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 0, 2, 0, 1, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 6, 2, 0, 7, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 0, 1, 7, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 1, 0, 2, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 5, 1, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 3, 1, 7, 4, 2, 7);
            }

            if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 0, 1, 3, 1, 2, 4);
            }

            if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 6, 1, 3, 7, 2, 4);
            }

        }
    }

    public static class OceanMonumentPenthouse extends OceanMonumentPieces.OceanMonumentPiece {
        public OceanMonumentPenthouse(Direction param0, BoundingBox param1) {
            super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, param0, 1, param1);
        }

        public OceanMonumentPenthouse(CompoundTag param0) {
            super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, param0);
        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            this.generateBox(param0, param4, 2, -1, 2, 11, -1, 11, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 0, -1, 0, 1, -1, 11, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(param0, param4, 12, -1, 0, 13, -1, 11, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(param0, param4, 2, -1, 0, 11, -1, 1, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(param0, param4, 2, -1, 12, 11, -1, 13, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(param0, param4, 0, 0, 0, 0, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 13, 0, 0, 13, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 0, 0, 12, 0, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 0, 13, 12, 0, 13, BASE_LIGHT, BASE_LIGHT, false);

            for(int var0 = 2; var0 <= 11; var0 += 3) {
                this.placeBlock(param0, LAMP_BLOCK, 0, 0, var0, param4);
                this.placeBlock(param0, LAMP_BLOCK, 13, 0, var0, param4);
                this.placeBlock(param0, LAMP_BLOCK, var0, 0, 0, param4);
            }

            this.generateBox(param0, param4, 2, 0, 3, 4, 0, 9, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 9, 0, 3, 11, 0, 9, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 4, 0, 9, 9, 0, 11, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(param0, BASE_LIGHT, 5, 0, 8, param4);
            this.placeBlock(param0, BASE_LIGHT, 8, 0, 8, param4);
            this.placeBlock(param0, BASE_LIGHT, 10, 0, 10, param4);
            this.placeBlock(param0, BASE_LIGHT, 3, 0, 10, param4);
            this.generateBox(param0, param4, 3, 0, 3, 3, 0, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(param0, param4, 10, 0, 3, 10, 0, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(param0, param4, 6, 0, 10, 7, 0, 10, BASE_BLACK, BASE_BLACK, false);
            int var1 = 3;

            for(int var2 = 0; var2 < 2; ++var2) {
                for(int var3 = 2; var3 <= 8; var3 += 3) {
                    this.generateBox(param0, param4, var1, 0, var3, var1, 2, var3, BASE_LIGHT, BASE_LIGHT, false);
                }

                var1 = 10;
            }

            this.generateBox(param0, param4, 5, 0, 10, 5, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 8, 0, 10, 8, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 6, -1, 7, 7, -1, 8, BASE_BLACK, BASE_BLACK, false);
            this.generateWaterBox(param0, param4, 6, -1, 3, 7, -1, 4);
            this.spawnElder(param0, param4, 6, 1, 6);
        }
    }

    protected abstract static class OceanMonumentPiece extends StructurePiece {
        protected static final BlockState BASE_GRAY = Blocks.PRISMARINE.defaultBlockState();
        protected static final BlockState BASE_LIGHT = Blocks.PRISMARINE_BRICKS.defaultBlockState();
        protected static final BlockState BASE_BLACK = Blocks.DARK_PRISMARINE.defaultBlockState();
        protected static final BlockState DOT_DECO_DATA = BASE_LIGHT;
        protected static final BlockState LAMP_BLOCK = Blocks.SEA_LANTERN.defaultBlockState();
        protected static final boolean DO_FILL = true;
        protected static final BlockState FILL_BLOCK = Blocks.WATER.defaultBlockState();
        protected static final Set<Block> FILL_KEEP = ImmutableSet.<Block>builder()
            .add(Blocks.ICE)
            .add(Blocks.PACKED_ICE)
            .add(Blocks.BLUE_ICE)
            .add(FILL_BLOCK.getBlock())
            .build();
        protected static final int GRIDROOM_WIDTH = 8;
        protected static final int GRIDROOM_DEPTH = 8;
        protected static final int GRIDROOM_HEIGHT = 4;
        protected static final int GRID_WIDTH = 5;
        protected static final int GRID_DEPTH = 5;
        protected static final int GRID_HEIGHT = 3;
        protected static final int GRID_FLOOR_COUNT = 25;
        protected static final int GRID_SIZE = 75;
        protected static final int GRIDROOM_SOURCE_INDEX = getRoomIndex(2, 0, 0);
        protected static final int GRIDROOM_TOP_CONNECT_INDEX = getRoomIndex(2, 2, 0);
        protected static final int GRIDROOM_LEFTWING_CONNECT_INDEX = getRoomIndex(0, 1, 0);
        protected static final int GRIDROOM_RIGHTWING_CONNECT_INDEX = getRoomIndex(4, 1, 0);
        protected static final int LEFTWING_INDEX = 1001;
        protected static final int RIGHTWING_INDEX = 1002;
        protected static final int PENTHOUSE_INDEX = 1003;
        protected OceanMonumentPieces.RoomDefinition roomDefinition;

        protected static int getRoomIndex(int param0, int param1, int param2) {
            return param1 * 25 + param2 * 5 + param0;
        }

        public OceanMonumentPiece(StructurePieceType param0, Direction param1, int param2, BoundingBox param3) {
            super(param0, param2, param3);
            this.setOrientation(param1);
        }

        protected OceanMonumentPiece(
            StructurePieceType param0, int param1, Direction param2, OceanMonumentPieces.RoomDefinition param3, int param4, int param5, int param6
        ) {
            super(param0, param1, makeBoundingBox(param2, param3, param4, param5, param6));
            this.setOrientation(param2);
            this.roomDefinition = param3;
        }

        private static BoundingBox makeBoundingBox(Direction param0, OceanMonumentPieces.RoomDefinition param1, int param2, int param3, int param4) {
            int var0 = param1.index;
            int var1 = var0 % 5;
            int var2 = var0 / 5 % 5;
            int var3 = var0 / 25;
            BoundingBox var4 = makeBoundingBox(0, 0, 0, param0, param2 * 8, param3 * 4, param4 * 8);
            switch(param0) {
                case NORTH:
                    var4.move(var1 * 8, var3 * 4, -(var2 + param4) * 8 + 1);
                    break;
                case SOUTH:
                    var4.move(var1 * 8, var3 * 4, var2 * 8);
                    break;
                case WEST:
                    var4.move(-(var2 + param4) * 8 + 1, var3 * 4, var1 * 8);
                    break;
                case EAST:
                default:
                    var4.move(var2 * 8, var3 * 4, var1 * 8);
            }

            return var4;
        }

        public OceanMonumentPiece(StructurePieceType param0, CompoundTag param1) {
            super(param0, param1);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext param0, CompoundTag param1) {
        }

        protected void generateWaterBox(WorldGenLevel param0, BoundingBox param1, int param2, int param3, int param4, int param5, int param6, int param7) {
            for(int var0 = param3; var0 <= param6; ++var0) {
                for(int var1 = param2; var1 <= param5; ++var1) {
                    for(int var2 = param4; var2 <= param7; ++var2) {
                        BlockState var3 = this.getBlock(param0, var1, var0, var2, param1);
                        if (!FILL_KEEP.contains(var3.getBlock())) {
                            if (this.getWorldY(var0) >= param0.getSeaLevel() && var3 != FILL_BLOCK) {
                                this.placeBlock(param0, Blocks.AIR.defaultBlockState(), var1, var0, var2, param1);
                            } else {
                                this.placeBlock(param0, FILL_BLOCK, var1, var0, var2, param1);
                            }
                        }
                    }
                }
            }

        }

        protected void generateDefaultFloor(WorldGenLevel param0, BoundingBox param1, int param2, int param3, boolean param4) {
            if (param4) {
                this.generateBox(param0, param1, param2 + 0, 0, param3 + 0, param2 + 2, 0, param3 + 8 - 1, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param1, param2 + 5, 0, param3 + 0, param2 + 8 - 1, 0, param3 + 8 - 1, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param1, param2 + 3, 0, param3 + 0, param2 + 4, 0, param3 + 2, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param1, param2 + 3, 0, param3 + 5, param2 + 4, 0, param3 + 8 - 1, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param1, param2 + 3, 0, param3 + 2, param2 + 4, 0, param3 + 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param1, param2 + 3, 0, param3 + 5, param2 + 4, 0, param3 + 5, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param1, param2 + 2, 0, param3 + 3, param2 + 2, 0, param3 + 4, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param1, param2 + 5, 0, param3 + 3, param2 + 5, 0, param3 + 4, BASE_LIGHT, BASE_LIGHT, false);
            } else {
                this.generateBox(param0, param1, param2 + 0, 0, param3 + 0, param2 + 8 - 1, 0, param3 + 8 - 1, BASE_GRAY, BASE_GRAY, false);
            }

        }

        protected void generateBoxOnFillOnly(
            WorldGenLevel param0, BoundingBox param1, int param2, int param3, int param4, int param5, int param6, int param7, BlockState param8
        ) {
            for(int var0 = param3; var0 <= param6; ++var0) {
                for(int var1 = param2; var1 <= param5; ++var1) {
                    for(int var2 = param4; var2 <= param7; ++var2) {
                        if (this.getBlock(param0, var1, var0, var2, param1) == FILL_BLOCK) {
                            this.placeBlock(param0, param8, var1, var0, var2, param1);
                        }
                    }
                }
            }

        }

        protected boolean chunkIntersects(BoundingBox param0, int param1, int param2, int param3, int param4) {
            int var0 = this.getWorldX(param1, param2);
            int var1 = this.getWorldZ(param1, param2);
            int var2 = this.getWorldX(param3, param4);
            int var3 = this.getWorldZ(param3, param4);
            return param0.intersects(Math.min(var0, var2), Math.min(var1, var3), Math.max(var0, var2), Math.max(var1, var3));
        }

        protected void spawnElder(WorldGenLevel param0, BoundingBox param1, int param2, int param3, int param4) {
            BlockPos var0 = this.getWorldPos(param2, param3, param4);
            if (param1.isInside(var0)) {
                ElderGuardian var1 = EntityType.ELDER_GUARDIAN.create(param0.getLevel());
                if (var1 != null) {
                    var1.heal(var1.getMaxHealth());
                    var1.moveTo((double)var0.getX() + 0.5, (double)var0.getY(), (double)var0.getZ() + 0.5, 0.0F, 0.0F);
                    var1.finalizeSpawn(param0, param0.getCurrentDifficultyAt(var1.blockPosition()), MobSpawnType.STRUCTURE, null, null);
                    param0.addFreshEntityWithPassengers(var1);
                }
            }

        }
    }

    public static class OceanMonumentSimpleRoom extends OceanMonumentPieces.OceanMonumentPiece {
        private int mainDesign;

        public OceanMonumentSimpleRoom(Direction param0, OceanMonumentPieces.RoomDefinition param1, RandomSource param2) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, 1, param0, param1, 1, 1, 1);
            this.mainDesign = param2.nextInt(3);
        }

        public OceanMonumentSimpleRoom(CompoundTag param0) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, param0);
        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(param0, param4, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            }

            if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(param0, param4, 1, 4, 1, 6, 4, 6, BASE_GRAY);
            }

            boolean var0 = this.mainDesign != 0
                && param3.nextBoolean()
                && !this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]
                && !this.roomDefinition.hasOpening[Direction.UP.get3DDataValue()]
                && this.roomDefinition.countOpenings() > 1;
            if (this.mainDesign == 0) {
                this.generateBox(param0, param4, 0, 1, 0, 2, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 0, 3, 0, 2, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 0, 2, 0, 0, 2, 2, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param4, 1, 2, 0, 2, 2, 0, BASE_GRAY, BASE_GRAY, false);
                this.placeBlock(param0, LAMP_BLOCK, 1, 2, 1, param4);
                this.generateBox(param0, param4, 5, 1, 0, 7, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 5, 3, 0, 7, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 7, 2, 0, 7, 2, 2, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param4, 5, 2, 0, 6, 2, 0, BASE_GRAY, BASE_GRAY, false);
                this.placeBlock(param0, LAMP_BLOCK, 6, 2, 1, param4);
                this.generateBox(param0, param4, 0, 1, 5, 2, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 0, 3, 5, 2, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 0, 2, 5, 0, 2, 7, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param4, 1, 2, 7, 2, 2, 7, BASE_GRAY, BASE_GRAY, false);
                this.placeBlock(param0, LAMP_BLOCK, 1, 2, 6, param4);
                this.generateBox(param0, param4, 5, 1, 5, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 5, 3, 5, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 7, 2, 5, 7, 2, 7, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param4, 5, 2, 7, 6, 2, 7, BASE_GRAY, BASE_GRAY, false);
                this.placeBlock(param0, LAMP_BLOCK, 6, 2, 6, param4);
                if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                    this.generateBox(param0, param4, 3, 3, 0, 4, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(param0, param4, 3, 3, 0, 4, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, 3, 2, 0, 4, 2, 0, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(param0, param4, 3, 1, 0, 4, 1, 1, BASE_LIGHT, BASE_LIGHT, false);
                }

                if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                    this.generateBox(param0, param4, 3, 3, 7, 4, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(param0, param4, 3, 3, 6, 4, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, 3, 2, 7, 4, 2, 7, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(param0, param4, 3, 1, 6, 4, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                }

                if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                    this.generateBox(param0, param4, 0, 3, 3, 0, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(param0, param4, 0, 3, 3, 1, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, 0, 2, 3, 0, 2, 4, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(param0, param4, 0, 1, 3, 1, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
                }

                if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                    this.generateBox(param0, param4, 7, 3, 3, 7, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
                } else {
                    this.generateBox(param0, param4, 6, 3, 3, 7, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, 7, 2, 3, 7, 2, 4, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(param0, param4, 6, 1, 3, 7, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
                }
            } else if (this.mainDesign == 1) {
                this.generateBox(param0, param4, 2, 1, 2, 2, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 2, 1, 5, 2, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 5, 1, 5, 5, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 5, 1, 2, 5, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
                this.placeBlock(param0, LAMP_BLOCK, 2, 2, 2, param4);
                this.placeBlock(param0, LAMP_BLOCK, 2, 2, 5, param4);
                this.placeBlock(param0, LAMP_BLOCK, 5, 2, 5, param4);
                this.placeBlock(param0, LAMP_BLOCK, 5, 2, 2, param4);
                this.generateBox(param0, param4, 0, 1, 0, 1, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 0, 1, 1, 0, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 0, 1, 7, 1, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 0, 1, 6, 0, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 6, 1, 7, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 7, 1, 6, 7, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 6, 1, 0, 7, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 7, 1, 1, 7, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
                this.placeBlock(param0, BASE_GRAY, 1, 2, 0, param4);
                this.placeBlock(param0, BASE_GRAY, 0, 2, 1, param4);
                this.placeBlock(param0, BASE_GRAY, 1, 2, 7, param4);
                this.placeBlock(param0, BASE_GRAY, 0, 2, 6, param4);
                this.placeBlock(param0, BASE_GRAY, 6, 2, 7, param4);
                this.placeBlock(param0, BASE_GRAY, 7, 2, 6, param4);
                this.placeBlock(param0, BASE_GRAY, 6, 2, 0, param4);
                this.placeBlock(param0, BASE_GRAY, 7, 2, 1, param4);
                if (!this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                    this.generateBox(param0, param4, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, 1, 2, 0, 6, 2, 0, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(param0, param4, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
                }

                if (!this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                    this.generateBox(param0, param4, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, 1, 2, 7, 6, 2, 7, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(param0, param4, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                }

                if (!this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                    this.generateBox(param0, param4, 0, 3, 1, 0, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, 0, 2, 1, 0, 2, 6, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(param0, param4, 0, 1, 1, 0, 1, 6, BASE_LIGHT, BASE_LIGHT, false);
                }

                if (!this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                    this.generateBox(param0, param4, 7, 3, 1, 7, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, 7, 2, 1, 7, 2, 6, BASE_GRAY, BASE_GRAY, false);
                    this.generateBox(param0, param4, 7, 1, 1, 7, 1, 6, BASE_LIGHT, BASE_LIGHT, false);
                }
            } else if (this.mainDesign == 2) {
                this.generateBox(param0, param4, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 0, 2, 0, 0, 2, 7, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(param0, param4, 7, 2, 0, 7, 2, 7, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(param0, param4, 1, 2, 0, 6, 2, 0, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(param0, param4, 1, 2, 7, 6, 2, 7, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(param0, param4, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 7, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 0, 1, 3, 0, 2, 4, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(param0, param4, 7, 1, 3, 7, 2, 4, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(param0, param4, 3, 1, 0, 4, 2, 0, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(param0, param4, 3, 1, 7, 4, 2, 7, BASE_BLACK, BASE_BLACK, false);
                if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                    this.generateWaterBox(param0, param4, 3, 1, 0, 4, 2, 0);
                }

                if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
                    this.generateWaterBox(param0, param4, 3, 1, 7, 4, 2, 7);
                }

                if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
                    this.generateWaterBox(param0, param4, 0, 1, 3, 0, 2, 4);
                }

                if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
                    this.generateWaterBox(param0, param4, 7, 1, 3, 7, 2, 4);
                }
            }

            if (var0) {
                this.generateBox(param0, param4, 3, 1, 3, 4, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 3, 2, 3, 4, 2, 4, BASE_GRAY, BASE_GRAY, false);
                this.generateBox(param0, param4, 3, 3, 3, 4, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            }

        }
    }

    public static class OceanMonumentSimpleTopRoom extends OceanMonumentPieces.OceanMonumentPiece {
        public OceanMonumentSimpleTopRoom(Direction param0, OceanMonumentPieces.RoomDefinition param1) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, 1, param0, param1, 1, 1, 1);
        }

        public OceanMonumentSimpleTopRoom(CompoundTag param0) {
            super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, param0);
        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            if (this.roomDefinition.index / 25 > 0) {
                this.generateDefaultFloor(param0, param4, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            }

            if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
                this.generateBoxOnFillOnly(param0, param4, 1, 4, 1, 6, 4, 6, BASE_GRAY);
            }

            for(int var0 = 1; var0 <= 6; ++var0) {
                for(int var1 = 1; var1 <= 6; ++var1) {
                    if (param3.nextInt(3) != 0) {
                        int var2 = 2 + (param3.nextInt(4) == 0 ? 0 : 1);
                        BlockState var3 = Blocks.WET_SPONGE.defaultBlockState();
                        this.generateBox(param0, param4, var0, var2, var1, var0, 3, var1, var3, var3, false);
                    }
                }
            }

            this.generateBox(param0, param4, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 0, 2, 0, 0, 2, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(param0, param4, 7, 2, 0, 7, 2, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(param0, param4, 1, 2, 0, 6, 2, 0, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(param0, param4, 1, 2, 7, 6, 2, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(param0, param4, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 7, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(param0, param4, 0, 1, 3, 0, 2, 4, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(param0, param4, 7, 1, 3, 7, 2, 4, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(param0, param4, 3, 1, 0, 4, 2, 0, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(param0, param4, 3, 1, 7, 4, 2, 7, BASE_BLACK, BASE_BLACK, false);
            if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
                this.generateWaterBox(param0, param4, 3, 1, 0, 4, 2, 0);
            }

        }
    }

    public static class OceanMonumentWingRoom extends OceanMonumentPieces.OceanMonumentPiece {
        private int mainDesign;

        public OceanMonumentWingRoom(Direction param0, BoundingBox param1, int param2) {
            super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, param0, 1, param1);
            this.mainDesign = param2 & 1;
        }

        public OceanMonumentWingRoom(CompoundTag param0) {
            super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, param0);
        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            if (this.mainDesign == 0) {
                for(int var0 = 0; var0 < 4; ++var0) {
                    this.generateBox(param0, param4, 10 - var0, 3 - var0, 20 - var0, 12 + var0, 3 - var0, 20, BASE_LIGHT, BASE_LIGHT, false);
                }

                this.generateBox(param0, param4, 7, 0, 6, 15, 0, 16, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 6, 0, 6, 6, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 16, 0, 6, 16, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 7, 1, 7, 7, 1, 20, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 15, 1, 7, 15, 1, 20, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 7, 1, 6, 9, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 13, 1, 6, 15, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 8, 1, 7, 9, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 13, 1, 7, 14, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 9, 0, 5, 13, 0, 5, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 10, 0, 7, 12, 0, 7, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(param0, param4, 8, 0, 10, 8, 0, 12, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(param0, param4, 14, 0, 10, 14, 0, 12, BASE_BLACK, BASE_BLACK, false);

                for(int var1 = 18; var1 >= 7; var1 -= 3) {
                    this.placeBlock(param0, LAMP_BLOCK, 6, 3, var1, param4);
                    this.placeBlock(param0, LAMP_BLOCK, 16, 3, var1, param4);
                }

                this.placeBlock(param0, LAMP_BLOCK, 10, 0, 10, param4);
                this.placeBlock(param0, LAMP_BLOCK, 12, 0, 10, param4);
                this.placeBlock(param0, LAMP_BLOCK, 10, 0, 12, param4);
                this.placeBlock(param0, LAMP_BLOCK, 12, 0, 12, param4);
                this.placeBlock(param0, LAMP_BLOCK, 8, 3, 6, param4);
                this.placeBlock(param0, LAMP_BLOCK, 14, 3, 6, param4);
                this.placeBlock(param0, BASE_LIGHT, 4, 2, 4, param4);
                this.placeBlock(param0, LAMP_BLOCK, 4, 1, 4, param4);
                this.placeBlock(param0, BASE_LIGHT, 4, 0, 4, param4);
                this.placeBlock(param0, BASE_LIGHT, 18, 2, 4, param4);
                this.placeBlock(param0, LAMP_BLOCK, 18, 1, 4, param4);
                this.placeBlock(param0, BASE_LIGHT, 18, 0, 4, param4);
                this.placeBlock(param0, BASE_LIGHT, 4, 2, 18, param4);
                this.placeBlock(param0, LAMP_BLOCK, 4, 1, 18, param4);
                this.placeBlock(param0, BASE_LIGHT, 4, 0, 18, param4);
                this.placeBlock(param0, BASE_LIGHT, 18, 2, 18, param4);
                this.placeBlock(param0, LAMP_BLOCK, 18, 1, 18, param4);
                this.placeBlock(param0, BASE_LIGHT, 18, 0, 18, param4);
                this.placeBlock(param0, BASE_LIGHT, 9, 7, 20, param4);
                this.placeBlock(param0, BASE_LIGHT, 13, 7, 20, param4);
                this.generateBox(param0, param4, 6, 0, 21, 7, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 15, 0, 21, 16, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
                this.spawnElder(param0, param4, 11, 2, 16);
            } else if (this.mainDesign == 1) {
                this.generateBox(param0, param4, 9, 3, 18, 13, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 9, 0, 18, 9, 2, 18, BASE_LIGHT, BASE_LIGHT, false);
                this.generateBox(param0, param4, 13, 0, 18, 13, 2, 18, BASE_LIGHT, BASE_LIGHT, false);
                int var2 = 9;
                int var3 = 20;
                int var4 = 5;

                for(int var5 = 0; var5 < 2; ++var5) {
                    this.placeBlock(param0, BASE_LIGHT, var2, 6, 20, param4);
                    this.placeBlock(param0, LAMP_BLOCK, var2, 5, 20, param4);
                    this.placeBlock(param0, BASE_LIGHT, var2, 4, 20, param4);
                    var2 = 13;
                }

                this.generateBox(param0, param4, 7, 3, 7, 15, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
                int var14 = 10;

                for(int var6 = 0; var6 < 2; ++var6) {
                    this.generateBox(param0, param4, var14, 0, 10, var14, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, var14, 0, 12, var14, 6, 12, BASE_LIGHT, BASE_LIGHT, false);
                    this.placeBlock(param0, LAMP_BLOCK, var14, 0, 10, param4);
                    this.placeBlock(param0, LAMP_BLOCK, var14, 0, 12, param4);
                    this.placeBlock(param0, LAMP_BLOCK, var14, 4, 10, param4);
                    this.placeBlock(param0, LAMP_BLOCK, var14, 4, 12, param4);
                    var14 = 12;
                }

                var14 = 8;

                for(int var7 = 0; var7 < 2; ++var7) {
                    this.generateBox(param0, param4, var14, 0, 7, var14, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
                    this.generateBox(param0, param4, var14, 0, 14, var14, 2, 14, BASE_LIGHT, BASE_LIGHT, false);
                    var14 = 14;
                }

                this.generateBox(param0, param4, 8, 3, 8, 8, 3, 13, BASE_BLACK, BASE_BLACK, false);
                this.generateBox(param0, param4, 14, 3, 8, 14, 3, 13, BASE_BLACK, BASE_BLACK, false);
                this.spawnElder(param0, param4, 11, 5, 13);
            }

        }
    }

    static class RoomDefinition {
        final int index;
        final OceanMonumentPieces.RoomDefinition[] connections = new OceanMonumentPieces.RoomDefinition[6];
        final boolean[] hasOpening = new boolean[6];
        boolean claimed;
        boolean isSource;
        private int scanIndex;

        public RoomDefinition(int param0) {
            this.index = param0;
        }

        public void setConnection(Direction param0, OceanMonumentPieces.RoomDefinition param1) {
            this.connections[param0.get3DDataValue()] = param1;
            param1.connections[param0.getOpposite().get3DDataValue()] = this;
        }

        public void updateOpenings() {
            for(int var0 = 0; var0 < 6; ++var0) {
                this.hasOpening[var0] = this.connections[var0] != null;
            }

        }

        public boolean findSource(int param0) {
            if (this.isSource) {
                return true;
            } else {
                this.scanIndex = param0;

                for(int var0 = 0; var0 < 6; ++var0) {
                    if (this.connections[var0] != null
                        && this.hasOpening[var0]
                        && this.connections[var0].scanIndex != param0
                        && this.connections[var0].findSource(param0)) {
                        return true;
                    }
                }

                return false;
            }
        }

        public boolean isSpecial() {
            return this.index >= 75;
        }

        public int countOpenings() {
            int var0 = 0;

            for(int var1 = 0; var1 < 6; ++var1) {
                if (this.hasOpening[var1]) {
                    ++var0;
                }
            }

            return var0;
        }
    }
}
