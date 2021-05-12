package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class IglooPieces {
    public static final int GENERATION_HEIGHT = 90;
    static final ResourceLocation STRUCTURE_LOCATION_IGLOO = new ResourceLocation("igloo/top");
    private static final ResourceLocation STRUCTURE_LOCATION_LADDER = new ResourceLocation("igloo/middle");
    private static final ResourceLocation STRUCTURE_LOCATION_LABORATORY = new ResourceLocation("igloo/bottom");
    static final Map<ResourceLocation, BlockPos> PIVOTS = ImmutableMap.of(
        STRUCTURE_LOCATION_IGLOO, new BlockPos(3, 5, 5), STRUCTURE_LOCATION_LADDER, new BlockPos(1, 3, 1), STRUCTURE_LOCATION_LABORATORY, new BlockPos(3, 6, 7)
    );
    static final Map<ResourceLocation, BlockPos> OFFSETS = ImmutableMap.of(
        STRUCTURE_LOCATION_IGLOO, BlockPos.ZERO, STRUCTURE_LOCATION_LADDER, new BlockPos(2, -3, 4), STRUCTURE_LOCATION_LABORATORY, new BlockPos(0, -3, -2)
    );

    public static void addPieces(StructureManager param0, BlockPos param1, Rotation param2, StructurePieceAccessor param3, Random param4) {
        if (param4.nextDouble() < 0.5) {
            int var0 = param4.nextInt(8) + 4;
            param3.addPiece(new IglooPieces.IglooPiece(param0, STRUCTURE_LOCATION_LABORATORY, param1, param2, var0 * 3));

            for(int var1 = 0; var1 < var0 - 1; ++var1) {
                param3.addPiece(new IglooPieces.IglooPiece(param0, STRUCTURE_LOCATION_LADDER, param1, param2, var1 * 3));
            }
        }

        param3.addPiece(new IglooPieces.IglooPiece(param0, STRUCTURE_LOCATION_IGLOO, param1, param2, 0));
    }

    public static class IglooPiece extends TemplateStructurePiece {
        public IglooPiece(StructureManager param0, ResourceLocation param1, BlockPos param2, Rotation param3, int param4) {
            super(StructurePieceType.IGLOO, 0, param0, param1, param1.toString(), makeSettings(param3, param1), makePosition(param1, param2, param4));
        }

        public IglooPiece(ServerLevel param0, CompoundTag param1) {
            super(StructurePieceType.IGLOO, param1, param0, param1x -> makeSettings(Rotation.valueOf(param1.getString("Rot")), param1x));
        }

        private static StructurePlaceSettings makeSettings(Rotation param0, ResourceLocation param1) {
            return new StructurePlaceSettings()
                .setRotation(param0)
                .setMirror(Mirror.NONE)
                .setRotationPivot(IglooPieces.PIVOTS.get(param1))
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        private static BlockPos makePosition(ResourceLocation param0, BlockPos param1, int param2) {
            return param1.offset(IglooPieces.OFFSETS.get(param0)).below(param2);
        }

        @Override
        protected void addAdditionalSaveData(ServerLevel param0, CompoundTag param1) {
            super.addAdditionalSaveData(param0, param1);
            param1.putString("Rot", this.placeSettings.getRotation().name());
        }

        @Override
        protected void handleDataMarker(String param0, BlockPos param1, ServerLevelAccessor param2, Random param3, BoundingBox param4) {
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
            ResourceLocation var0 = new ResourceLocation(this.templateName);
            StructurePlaceSettings var1 = makeSettings(this.placeSettings.getRotation(), var0);
            BlockPos var2 = IglooPieces.OFFSETS.get(var0);
            BlockPos var3 = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(var1, new BlockPos(3 - var2.getX(), 0, -var2.getZ())));
            int var4 = param0.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var3.getX(), var3.getZ());
            BlockPos var5 = this.templatePosition;
            this.templatePosition = this.templatePosition.offset(0, var4 - 90 - 1, 0);
            boolean var6 = super.postProcess(param0, param1, param2, param3, param4, param5, param6);
            if (var0.equals(IglooPieces.STRUCTURE_LOCATION_IGLOO)) {
                BlockPos var7 = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(var1, new BlockPos(3, 0, 5)));
                BlockState var8 = param0.getBlockState(var7.below());
                if (!var8.isAir() && !var8.is(Blocks.LADDER)) {
                    param0.setBlock(var7, Blocks.SNOW_BLOCK.defaultBlockState(), 3);
                }
            }

            this.templatePosition = var5;
            return var6;
        }
    }
}
