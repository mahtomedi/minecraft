package net.minecraft.world.level.levelgen.structure;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;

public class SwamplandHutPiece extends ScatteredFeaturePiece {
    private boolean spawnedWitch;
    private boolean spawnedCat;

    public SwamplandHutPiece(Random param0, int param1, int param2) {
        super(StructurePieceType.SWAMPLAND_HUT, param1, 64, param2, 7, 7, 9, getRandomHorizontalDirection(param0));
    }

    public SwamplandHutPiece(ServerLevel param0, CompoundTag param1) {
        super(StructurePieceType.SWAMPLAND_HUT, param1);
        this.spawnedWitch = param1.getBoolean("Witch");
        this.spawnedCat = param1.getBoolean("Cat");
    }

    @Override
    protected void addAdditionalSaveData(ServerLevel param0, CompoundTag param1) {
        super.addAdditionalSaveData(param0, param1);
        param1.putBoolean("Witch", this.spawnedWitch);
        param1.putBoolean("Cat", this.spawnedCat);
    }

    @Override
    public boolean postProcess(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
    ) {
        if (!this.updateAverageGroundHeight(param0, param4, 0)) {
            return false;
        } else {
            this.generateBox(param0, param4, 1, 1, 1, 5, 1, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 4, 2, 5, 4, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 1, 0, 4, 1, 0, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 2, 2, 3, 3, 2, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 2, 3, 1, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 5, 2, 3, 5, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 2, 2, 7, 4, 3, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 0, 2, 1, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
            this.generateBox(param0, param4, 5, 0, 2, 5, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
            this.generateBox(param0, param4, 1, 0, 7, 1, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
            this.generateBox(param0, param4, 5, 0, 7, 5, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
            this.placeBlock(param0, Blocks.OAK_FENCE.defaultBlockState(), 2, 3, 2, param4);
            this.placeBlock(param0, Blocks.OAK_FENCE.defaultBlockState(), 3, 3, 7, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 1, 3, 4, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 5, 3, 4, param4);
            this.placeBlock(param0, Blocks.AIR.defaultBlockState(), 5, 3, 5, param4);
            this.placeBlock(param0, Blocks.POTTED_RED_MUSHROOM.defaultBlockState(), 1, 3, 5, param4);
            this.placeBlock(param0, Blocks.CRAFTING_TABLE.defaultBlockState(), 3, 2, 6, param4);
            this.placeBlock(param0, Blocks.CAULDRON.defaultBlockState(), 4, 2, 6, param4);
            this.placeBlock(param0, Blocks.OAK_FENCE.defaultBlockState(), 1, 2, 1, param4);
            this.placeBlock(param0, Blocks.OAK_FENCE.defaultBlockState(), 5, 2, 1, param4);
            BlockState var0 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
            BlockState var1 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
            BlockState var2 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
            BlockState var3 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
            this.generateBox(param0, param4, 0, 4, 1, 6, 4, 1, var0, var0, false);
            this.generateBox(param0, param4, 0, 4, 2, 0, 4, 7, var1, var1, false);
            this.generateBox(param0, param4, 6, 4, 2, 6, 4, 7, var2, var2, false);
            this.generateBox(param0, param4, 0, 4, 8, 6, 4, 8, var3, var3, false);
            this.placeBlock(param0, var0.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 0, 4, 1, param4);
            this.placeBlock(param0, var0.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 6, 4, 1, param4);
            this.placeBlock(param0, var3.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 0, 4, 8, param4);
            this.placeBlock(param0, var3.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 6, 4, 8, param4);

            for(int var4 = 2; var4 <= 7; var4 += 5) {
                for(int var5 = 1; var5 <= 5; var5 += 4) {
                    this.fillColumnDown(param0, Blocks.OAK_LOG.defaultBlockState(), var5, -1, var4, param4);
                }
            }

            if (!this.spawnedWitch) {
                BlockPos var6 = this.getWorldPos(2, 2, 5);
                if (param4.isInside(var6)) {
                    this.spawnedWitch = true;
                    Witch var7 = EntityType.WITCH.create(param0.getLevel());
                    var7.setPersistenceRequired();
                    var7.moveTo((double)var6.getX() + 0.5, (double)var6.getY(), (double)var6.getZ() + 0.5, 0.0F, 0.0F);
                    var7.finalizeSpawn(param0, param0.getCurrentDifficultyAt(var6), MobSpawnType.STRUCTURE, null, null);
                    param0.addFreshEntityWithPassengers(var7);
                }
            }

            this.spawnCat(param0, param4);
            return true;
        }
    }

    private void spawnCat(ServerLevelAccessor param0, BoundingBox param1) {
        if (!this.spawnedCat) {
            BlockPos var0 = this.getWorldPos(2, 2, 5);
            if (param1.isInside(var0)) {
                this.spawnedCat = true;
                Cat var1 = EntityType.CAT.create(param0.getLevel());
                var1.setPersistenceRequired();
                var1.moveTo((double)var0.getX() + 0.5, (double)var0.getY(), (double)var0.getZ() + 0.5, 0.0F, 0.0F);
                var1.finalizeSpawn(param0, param0.getCurrentDifficultyAt(var0), MobSpawnType.STRUCTURE, null, null);
                param0.addFreshEntityWithPassengers(var1);
            }
        }

    }
}
