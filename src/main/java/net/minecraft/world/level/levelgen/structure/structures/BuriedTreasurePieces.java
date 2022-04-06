package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BuriedTreasurePieces {
    public static class BuriedTreasurePiece extends StructurePiece {
        public BuriedTreasurePiece(BlockPos param0) {
            super(StructurePieceType.BURIED_TREASURE_PIECE, 0, new BoundingBox(param0));
        }

        public BuriedTreasurePiece(CompoundTag param0) {
            super(StructurePieceType.BURIED_TREASURE_PIECE, param0);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext param0, CompoundTag param1) {
        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            int var0 = param0.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.boundingBox.minX(), this.boundingBox.minZ());
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos(this.boundingBox.minX(), var0, this.boundingBox.minZ());

            while(var1.getY() > param0.getMinBuildHeight()) {
                BlockState var2 = param0.getBlockState(var1);
                BlockState var3 = param0.getBlockState(var1.below());
                if (var3 == Blocks.SANDSTONE.defaultBlockState()
                    || var3 == Blocks.STONE.defaultBlockState()
                    || var3 == Blocks.ANDESITE.defaultBlockState()
                    || var3 == Blocks.GRANITE.defaultBlockState()
                    || var3 == Blocks.DIORITE.defaultBlockState()) {
                    BlockState var4 = !var2.isAir() && !this.isLiquid(var2) ? var2 : Blocks.SAND.defaultBlockState();

                    for(Direction var5 : Direction.values()) {
                        BlockPos var6 = var1.relative(var5);
                        BlockState var7 = param0.getBlockState(var6);
                        if (var7.isAir() || this.isLiquid(var7)) {
                            BlockPos var8 = var6.below();
                            BlockState var9 = param0.getBlockState(var8);
                            if ((var9.isAir() || this.isLiquid(var9)) && var5 != Direction.UP) {
                                param0.setBlock(var6, var3, 3);
                            } else {
                                param0.setBlock(var6, var4, 3);
                            }
                        }
                    }

                    this.boundingBox = new BoundingBox(var1);
                    this.createChest(param0, param4, param3, var1, BuiltInLootTables.BURIED_TREASURE, null);
                    return;
                }

                var1.move(0, -1, 0);
            }

        }

        private boolean isLiquid(BlockState param0) {
            return param0 == Blocks.WATER.defaultBlockState() || param0 == Blocks.LAVA.defaultBlockState();
        }
    }
}
