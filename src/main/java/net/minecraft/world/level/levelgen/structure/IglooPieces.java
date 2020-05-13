package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class IglooPieces {
    private static final ResourceLocation STRUCTURE_LOCATION_IGLOO = new ResourceLocation("igloo/top");
    private static final ResourceLocation STRUCTURE_LOCATION_LADDER = new ResourceLocation("igloo/middle");
    private static final ResourceLocation STRUCTURE_LOCATION_LABORATORY = new ResourceLocation("igloo/bottom");
    private static final Map<ResourceLocation, BlockPos> PIVOTS = ImmutableMap.of(
        STRUCTURE_LOCATION_IGLOO, new BlockPos(3, 5, 5), STRUCTURE_LOCATION_LADDER, new BlockPos(1, 3, 1), STRUCTURE_LOCATION_LABORATORY, new BlockPos(3, 6, 7)
    );
    private static final Map<ResourceLocation, BlockPos> OFFSETS = ImmutableMap.of(
        STRUCTURE_LOCATION_IGLOO, BlockPos.ZERO, STRUCTURE_LOCATION_LADDER, new BlockPos(2, -3, 4), STRUCTURE_LOCATION_LABORATORY, new BlockPos(0, -3, -2)
    );

    public static void addPieces(
        StructureManager param0, BlockPos param1, Rotation param2, List<StructurePiece> param3, Random param4, NoneFeatureConfiguration param5
    ) {
        if (param4.nextDouble() < 0.5) {
            int var0 = param4.nextInt(8) + 4;
            param3.add(new IglooPieces.IglooPiece(param0, STRUCTURE_LOCATION_LABORATORY, param1, param2, var0 * 3));

            for(int var1 = 0; var1 < var0 - 1; ++var1) {
                param3.add(new IglooPieces.IglooPiece(param0, STRUCTURE_LOCATION_LADDER, param1, param2, var1 * 3));
            }
        }

        param3.add(new IglooPieces.IglooPiece(param0, STRUCTURE_LOCATION_IGLOO, param1, param2, 0));
    }

    public static class IglooPiece extends TemplateStructurePiece {
        private final ResourceLocation templateLocation;
        private final Rotation rotation;

        public IglooPiece(StructureManager param0, ResourceLocation param1, BlockPos param2, Rotation param3, int param4) {
            super(StructurePieceType.IGLOO, 0);
            this.templateLocation = param1;
            BlockPos var0 = IglooPieces.OFFSETS.get(param1);
            this.templatePosition = param2.offset(var0.getX(), var0.getY() - param4, var0.getZ());
            this.rotation = param3;
            this.loadTemplate(param0);
        }

        public IglooPiece(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.IGLOO, param1);
            this.templateLocation = new ResourceLocation(param1.getString("Template"));
            this.rotation = Rotation.valueOf(param1.getString("Rot"));
            this.loadTemplate(param0);
        }

        private void loadTemplate(StructureManager param0) {
            StructureTemplate var0 = param0.getOrCreate(this.templateLocation);
            StructurePlaceSettings var1 = new StructurePlaceSettings()
                .setRotation(this.rotation)
                .setMirror(Mirror.NONE)
                .setRotationPivot(IglooPieces.PIVOTS.get(this.templateLocation))
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
            this.setup(var0, this.templatePosition, var1);
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag param0) {
            super.addAdditionalSaveData(param0);
            param0.putString("Template", this.templateLocation.toString());
            param0.putString("Rot", this.rotation.name());
        }

        @Override
        protected void handleDataMarker(String param0, BlockPos param1, LevelAccessor param2, Random param3, BoundingBox param4) {
            if ("chest".equals(param0)) {
                param2.setBlock(param1, Blocks.AIR.defaultBlockState(), 3);
                BlockEntity var0 = param2.getBlockEntity(param1.below());
                if (var0 instanceof ChestBlockEntity) {
                    ((ChestBlockEntity)var0).setLootTable(BuiltInLootTables.IGLOO_CHEST, param3.nextLong());
                }

            }
        }

        @Override
        public boolean postProcess(
            WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            StructurePlaceSettings var0 = new StructurePlaceSettings()
                .setRotation(this.rotation)
                .setMirror(Mirror.NONE)
                .setRotationPivot(IglooPieces.PIVOTS.get(this.templateLocation))
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
            BlockPos var1 = IglooPieces.OFFSETS.get(this.templateLocation);
            BlockPos var2 = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(var0, new BlockPos(3 - var1.getX(), 0, 0 - var1.getZ())));
            int var3 = param0.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var2.getX(), var2.getZ());
            BlockPos var4 = this.templatePosition;
            this.templatePosition = this.templatePosition.offset(0, var3 - 90 - 1, 0);
            boolean var5 = super.postProcess(param0, param1, param2, param3, param4, param5, param6);
            if (this.templateLocation.equals(IglooPieces.STRUCTURE_LOCATION_IGLOO)) {
                BlockPos var6 = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(var0, new BlockPos(3, 0, 5)));
                BlockState var7 = param0.getBlockState(var6.below());
                if (!var7.isAir() && !var7.is(Blocks.LADDER)) {
                    param0.setBlock(var6, Blocks.SNOW_BLOCK.defaultBlockState(), 3);
                }
            }

            this.templatePosition = var4;
            return var5;
        }
    }
}
