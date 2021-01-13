package net.minecraft.world.level.levelgen.structure;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BuriedTreasurePieces {
    public static class BuriedTreasurePiece extends StructurePiece {
        public BuriedTreasurePiece(BlockPos param0) {
            super(StructurePieceType.BURIED_TREASURE_PIECE, 0);
            this.boundingBox = new BoundingBox(param0.getX(), param0.getY(), param0.getZ(), param0.getX(), param0.getY(), param0.getZ());
        }

        public BuriedTreasurePiece(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.BURIED_TREASURE_PIECE, param1);
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag param0) {
        }

        @Override
        public boolean postProcess(
            WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            int var0 = param0.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.boundingBox.x0, this.boundingBox.z0);
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos(this.boundingBox.x0, var0, this.boundingBox.z0);

            while(var1.getY() > 0) {
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

                    this.boundingBox = new BoundingBox(var1.getX(), var1.getY(), var1.getZ(), var1.getX(), var1.getY(), var1.getZ());
                    return this.createChest(param0, param4, param3, var1, BuiltInLootTables.BURIED_TREASURE, null);
                }

                var1.move(0, -1, 0);
            }

            return false;
        }

        private boolean isLiquid(BlockState param0) {
            return param0 == Blocks.WATER.defaultBlockState() || param0 == Blocks.LAVA.defaultBlockState();
        }
    }
}
