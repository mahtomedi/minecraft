package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class DesertPyramidPiece extends ScatteredFeaturePiece {
    public static final int WIDTH = 21;
    public static final int DEPTH = 21;
    private final boolean[] hasPlacedChest = new boolean[4];

    public DesertPyramidPiece(RandomSource param0, int param1, int param2) {
        super(StructurePieceType.DESERT_PYRAMID_PIECE, param1, 64, param2, 21, 15, 21, getRandomHorizontalDirection(param0));
    }

    public DesertPyramidPiece(CompoundTag param0) {
        super(StructurePieceType.DESERT_PYRAMID_PIECE, param0);
        this.hasPlacedChest[0] = param0.getBoolean("hasPlacedChest0");
        this.hasPlacedChest[1] = param0.getBoolean("hasPlacedChest1");
        this.hasPlacedChest[2] = param0.getBoolean("hasPlacedChest2");
        this.hasPlacedChest[3] = param0.getBoolean("hasPlacedChest3");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext param0, CompoundTag param1) {
        super.addAdditionalSaveData(param0, param1);
        param1.putBoolean("hasPlacedChest0", this.hasPlacedChest[0]);
        param1.putBoolean("hasPlacedChest1", this.hasPlacedChest[1]);
        param1.putBoolean("hasPlacedChest2", this.hasPlacedChest[2]);
        param1.putBoolean("hasPlacedChest3", this.hasPlacedChest[3]);
    }

    @Override
    public void postProcess(
        WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
    ) {
        if (this.updateHeightPositionToLowestGroundHeight(param0, -param3.nextInt(3))) {
            this.generateBox(
                param0, param4, 0, -4, 0, this.width - 1, 0, this.depth - 1, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false
            );

            for(int var0 = 1; var0 <= 9; ++var0) {
                this.generateBox(
                    param0,
                    param4,
                    var0,
                    var0,
                    var0,
                    this.width - 1 - var0,
                    var0,
                    this.depth - 1 - var0,
                    Blocks.SANDSTONE.defaultBlockState(),
                    Blocks.SANDSTONE.defaultBlockState(),
                    false
                );
                this.generateBox(
                    param0,
                    param4,
                    var0 + 1,
                    var0,
                    var0 + 1,
                    this.width - 2 - var0,
                    var0,
                    this.depth - 2 - var0,
                    Blocks.AIR.defaultBlockState(),
                    Blocks.AIR.defaultBlockState(),
                    false
                );
            }

            for(int var1 = 0; var1 < this.width; ++var1) {
                for(int var2 = 0; var2 < this.depth; ++var2) {
                    int var3 = -5;
                    this.fillColumnDown(param0, Blocks.SANDSTONE.defaultBlockState(), var1, -5, var2, param4);
                }
            }

            BlockState var4 = Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
            BlockState var5 = Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
            BlockState var6 = Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
            BlockState var7 = Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
            this.generateBox(param0, param4, 0, 0, 0, 4, 9, 4, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 10, 1, 3, 10, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
            this.placeBlock(param0, var4, 2, 10, 0, param4);
            this.placeBlock(param0, var5, 2, 10, 4, param4);
            this.placeBlock(param0, var6, 0, 10, 2, param4);
            this.placeBlock(param0, var7, 4, 10, 2, param4);
            this.generateBox(
                param0, param4, this.width - 5, 0, 0, this.width - 1, 9, 4, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false
            );
            this.generateBox(
                param0, param4, this.width - 4, 10, 1, this.width - 2, 10, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false
            );
            this.placeBlock(param0, var4, this.width - 3, 10, 0, param4);
            this.placeBlock(param0, var5, this.width - 3, 10, 4, param4);
            this.placeBlock(param0, var6, this.width - 5, 10, 2, param4);
            this.placeBlock(param0, var7, this.width - 1, 10, 2, param4);
            this.generateBox(param0, param4, 8, 0, 0, 12, 4, 4, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 9, 1, 0, 11, 3, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), 9, 1, 1, param4);
            this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), 9, 2, 1, param4);
            this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), 9, 3, 1, param4);
            this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), 10, 3, 1, param4);
            this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), 11, 3, 1, param4);
            this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), 11, 2, 1, param4);
            this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), 11, 1, 1, param4);
            this.generateBox(param0, param4, 4, 1, 1, 8, 3, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 4, 1, 2, 8, 2, 2, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 12, 1, 1, 16, 3, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 12, 1, 2, 16, 2, 2, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(
                param0, param4, 5, 4, 5, this.width - 6, 4, this.depth - 6, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false
            );
            this.generateBox(param0, param4, 9, 4, 9, 11, 4, 11, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, 8, 1, 8, 8, 3, 8, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
            this.generateBox(param0, param4, 12, 1, 8, 12, 3, 8, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
            this.generateBox(param0, param4, 8, 1, 12, 8, 3, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
            this.generateBox(param0, param4, 12, 1, 12, 12, 3, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 1, 5, 4, 4, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
            this.generateBox(
                param0, param4, this.width - 5, 1, 5, this.width - 2, 4, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false
            );
            this.generateBox(param0, param4, 6, 7, 9, 6, 7, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
            this.generateBox(
                param0, param4, this.width - 7, 7, 9, this.width - 7, 7, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false
            );
            this.generateBox(param0, param4, 5, 5, 9, 5, 7, 11, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
            this.generateBox(
                param0,
                param4,
                this.width - 6,
                5,
                9,
                this.width - 6,
                7,
                11,
                Blocks.CUT_SANDSTONE.defaultBlockState(),
                Blocks.CUT_SANDSTONE.defaultBlockState(),
                false
            );
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 5, 5, 10, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 5, 6, 10, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 6, 6, 10, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), this.width - 6, 5, 10, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), this.width - 6, 6, 10, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), this.width - 7, 6, 10, param4);
            this.generateBox(param0, param4, 2, 4, 4, 2, 6, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, this.width - 3, 4, 4, this.width - 3, 6, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.placeBlock(param0, var4, 2, 4, 5, param4);
            this.placeBlock(param0, var4, 2, 3, 4, param4);
            this.placeBlock(param0, var4, this.width - 3, 4, 5, param4);
            this.placeBlock(param0, var4, this.width - 3, 3, 4, param4);
            this.generateBox(param0, param4, 1, 1, 3, 2, 2, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
            this.generateBox(
                param0, param4, this.width - 3, 1, 3, this.width - 2, 2, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false
            );
            this.placeBlock(param0, Blocks.SANDSTONE.defaultBlockState(), 1, 1, 2, param4);
            this.placeBlock(param0, Blocks.SANDSTONE.defaultBlockState(), this.width - 2, 1, 2, param4);
            this.placeBlock(param0, Blocks.SANDSTONE_SLAB.defaultBlockState(), 1, 2, 2, param4);
            this.placeBlock(param0, Blocks.SANDSTONE_SLAB.defaultBlockState(), this.width - 2, 2, 2, param4);
            this.placeBlock(param0, var7, 2, 1, 2, param4);
            this.placeBlock(param0, var6, this.width - 3, 1, 2, param4);
            this.generateBox(param0, param4, 4, 3, 5, 4, 3, 17, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
            this.generateBox(
                param0, param4, this.width - 5, 3, 5, this.width - 5, 3, 17, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false
            );
            this.generateBox(param0, param4, 3, 1, 5, 4, 2, 16, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(param0, param4, this.width - 6, 1, 5, this.width - 5, 2, 16, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);

            for(int var8 = 5; var8 <= 17; var8 += 2) {
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), 4, 1, var8, param4);
                this.placeBlock(param0, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 4, 2, var8, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), this.width - 5, 1, var8, param4);
                this.placeBlock(param0, Blocks.CHISELED_SANDSTONE.defaultBlockState(), this.width - 5, 2, var8, param4);
            }

            this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 7, param4);
            this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 8, param4);
            this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 9, 0, 9, param4);
            this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 11, 0, 9, param4);
            this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 8, 0, 10, param4);
            this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 12, 0, 10, param4);
            this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 7, 0, 10, param4);
            this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 13, 0, 10, param4);
            this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 9, 0, 11, param4);
            this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 11, 0, 11, param4);
            this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 12, param4);
            this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 13, param4);
            this.placeBlock(param0, Blocks.BLUE_TERRACOTTA.defaultBlockState(), 10, 0, 10, param4);

            for(int var9 = 0; var9 <= this.width - 1; var9 += this.width - 1) {
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var9, 2, 1, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var9, 2, 2, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var9, 2, 3, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var9, 3, 1, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var9, 3, 2, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var9, 3, 3, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var9, 4, 1, param4);
                this.placeBlock(param0, Blocks.CHISELED_SANDSTONE.defaultBlockState(), var9, 4, 2, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var9, 4, 3, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var9, 5, 1, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var9, 5, 2, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var9, 5, 3, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var9, 6, 1, param4);
                this.placeBlock(param0, Blocks.CHISELED_SANDSTONE.defaultBlockState(), var9, 6, 2, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var9, 6, 3, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var9, 7, 1, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var9, 7, 2, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var9, 7, 3, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var9, 8, 1, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var9, 8, 2, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var9, 8, 3, param4);
            }

            for(int var10 = 2; var10 <= this.width - 3; var10 += this.width - 3 - 2) {
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var10 - 1, 2, 0, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var10, 2, 0, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var10 + 1, 2, 0, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var10 - 1, 3, 0, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var10, 3, 0, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var10 + 1, 3, 0, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var10 - 1, 4, 0, param4);
                this.placeBlock(param0, Blocks.CHISELED_SANDSTONE.defaultBlockState(), var10, 4, 0, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var10 + 1, 4, 0, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var10 - 1, 5, 0, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var10, 5, 0, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var10 + 1, 5, 0, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var10 - 1, 6, 0, param4);
                this.placeBlock(param0, Blocks.CHISELED_SANDSTONE.defaultBlockState(), var10, 6, 0, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var10 + 1, 6, 0, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var10 - 1, 7, 0, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var10, 7, 0, param4);
                this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), var10 + 1, 7, 0, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var10 - 1, 8, 0, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var10, 8, 0, param4);
                this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), var10 + 1, 8, 0, param4);
            }

            this.generateBox(param0, param4, 8, 4, 0, 12, 6, 0, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 8, 6, 0, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 12, 6, 0, param4);
            this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 9, 5, 0, param4);
            this.placeBlock(param0, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 10, 5, 0, param4);
            this.placeBlock(param0, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 11, 5, 0, param4);
            this.generateBox(param0, param4, 8, -14, 8, 12, -11, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
            this.generateBox(
                param0, param4, 8, -10, 8, 12, -10, 12, Blocks.CHISELED_SANDSTONE.defaultBlockState(), Blocks.CHISELED_SANDSTONE.defaultBlockState(), false
            );
            this.generateBox(param0, param4, 8, -9, 8, 12, -9, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
            this.generateBox(param0, param4, 8, -8, 8, 12, -1, 12, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
            this.generateBox(param0, param4, 9, -11, 9, 11, -1, 11, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.placeBlock(param0, Blocks.STONE_PRESSURE_PLATE.defaultBlockState(), 10, -11, 10, param4);
            this.generateBox(param0, param4, 9, -13, 9, 11, -13, 11, Blocks.TNT.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 8, -11, 10, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 8, -10, 10, param4);
            this.placeBlock(param0, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 7, -10, 10, param4);
            this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), 7, -11, 10, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 12, -11, 10, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 12, -10, 10, param4);
            this.placeBlock(param0, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 13, -10, 10, param4);
            this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), 13, -11, 10, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 10, -11, 8, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 10, -10, 8, param4);
            this.placeBlock(param0, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 10, -10, 7, param4);
            this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), 10, -11, 7, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 10, -11, 12, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 10, -10, 12, param4);
            this.placeBlock(param0, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 10, -10, 13, param4);
            this.placeBlock(param0, Blocks.CUT_SANDSTONE.defaultBlockState(), 10, -11, 13, param4);

            for(Direction var11 : Direction.Plane.HORIZONTAL) {
                if (!this.hasPlacedChest[var11.get2DDataValue()]) {
                    int var12 = var11.getStepX() * 2;
                    int var13 = var11.getStepZ() * 2;
                    this.hasPlacedChest[var11.get2DDataValue()] = this.createChest(
                        param0, param4, param3, 10 + var12, -11, 10 + var13, BuiltInLootTables.DESERT_PYRAMID
                    );
                }
            }

        }
    }
}
