package net.minecraft.world.level.levelgen.structure;

import java.util.Map;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class ShipwreckPieces {
    static final BlockPos PIVOT = new BlockPos(4, 0, 15);
    private static final ResourceLocation[] STRUCTURE_LOCATION_BEACHED = new ResourceLocation[]{
        new ResourceLocation("shipwreck/with_mast"),
        new ResourceLocation("shipwreck/sideways_full"),
        new ResourceLocation("shipwreck/sideways_fronthalf"),
        new ResourceLocation("shipwreck/sideways_backhalf"),
        new ResourceLocation("shipwreck/rightsideup_full"),
        new ResourceLocation("shipwreck/rightsideup_fronthalf"),
        new ResourceLocation("shipwreck/rightsideup_backhalf"),
        new ResourceLocation("shipwreck/with_mast_degraded"),
        new ResourceLocation("shipwreck/rightsideup_full_degraded"),
        new ResourceLocation("shipwreck/rightsideup_fronthalf_degraded"),
        new ResourceLocation("shipwreck/rightsideup_backhalf_degraded")
    };
    private static final ResourceLocation[] STRUCTURE_LOCATION_OCEAN = new ResourceLocation[]{
        new ResourceLocation("shipwreck/with_mast"),
        new ResourceLocation("shipwreck/upsidedown_full"),
        new ResourceLocation("shipwreck/upsidedown_fronthalf"),
        new ResourceLocation("shipwreck/upsidedown_backhalf"),
        new ResourceLocation("shipwreck/sideways_full"),
        new ResourceLocation("shipwreck/sideways_fronthalf"),
        new ResourceLocation("shipwreck/sideways_backhalf"),
        new ResourceLocation("shipwreck/rightsideup_full"),
        new ResourceLocation("shipwreck/rightsideup_fronthalf"),
        new ResourceLocation("shipwreck/rightsideup_backhalf"),
        new ResourceLocation("shipwreck/with_mast_degraded"),
        new ResourceLocation("shipwreck/upsidedown_full_degraded"),
        new ResourceLocation("shipwreck/upsidedown_fronthalf_degraded"),
        new ResourceLocation("shipwreck/upsidedown_backhalf_degraded"),
        new ResourceLocation("shipwreck/sideways_full_degraded"),
        new ResourceLocation("shipwreck/sideways_fronthalf_degraded"),
        new ResourceLocation("shipwreck/sideways_backhalf_degraded"),
        new ResourceLocation("shipwreck/rightsideup_full_degraded"),
        new ResourceLocation("shipwreck/rightsideup_fronthalf_degraded"),
        new ResourceLocation("shipwreck/rightsideup_backhalf_degraded")
    };
    static final Map<String, ResourceLocation> MARKERS_TO_LOOT = Map.of(
        "map_chest",
        BuiltInLootTables.SHIPWRECK_MAP,
        "treasure_chest",
        BuiltInLootTables.SHIPWRECK_TREASURE,
        "supply_chest",
        BuiltInLootTables.SHIPWRECK_SUPPLY
    );

    public static void addPieces(
        StructureManager param0, BlockPos param1, Rotation param2, StructurePieceAccessor param3, Random param4, ShipwreckConfiguration param5
    ) {
        ResourceLocation var0 = Util.getRandom(param5.isBeached ? STRUCTURE_LOCATION_BEACHED : STRUCTURE_LOCATION_OCEAN, param4);
        param3.addPiece(new ShipwreckPieces.ShipwreckPiece(param0, var0, param1, param2, param5.isBeached));
    }

    public static class ShipwreckPiece extends TemplateStructurePiece {
        private final boolean isBeached;

        public ShipwreckPiece(StructureManager param0, ResourceLocation param1, BlockPos param2, Rotation param3, boolean param4) {
            super(StructurePieceType.SHIPWRECK_PIECE, 0, param0, param1, param1.toString(), makeSettings(param3), param2);
            this.isBeached = param4;
        }

        public ShipwreckPiece(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.SHIPWRECK_PIECE, param1, param0, param1x -> makeSettings(Rotation.valueOf(param1.getString("Rot"))));
            this.isBeached = param1.getBoolean("isBeached");
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext param0, CompoundTag param1) {
            super.addAdditionalSaveData(param0, param1);
            param1.putBoolean("isBeached", this.isBeached);
            param1.putString("Rot", this.placeSettings.getRotation().name());
        }

        private static StructurePlaceSettings makeSettings(Rotation param0) {
            return new StructurePlaceSettings()
                .setRotation(param0)
                .setMirror(Mirror.NONE)
                .setRotationPivot(ShipwreckPieces.PIVOT)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        }

        @Override
        protected void handleDataMarker(String param0, BlockPos param1, ServerLevelAccessor param2, Random param3, BoundingBox param4) {
            ResourceLocation var0 = ShipwreckPieces.MARKERS_TO_LOOT.get(param0);
            if (var0 != null) {
                RandomizableContainerBlockEntity.setLootTable(param2, param3, param1.below(), var0);
            }

        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            int var0 = param0.getMaxBuildHeight();
            int var1 = 0;
            Vec3i var2 = this.template.getSize();
            Heightmap.Types var3 = this.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
            int var4 = var2.getX() * var2.getZ();
            if (var4 == 0) {
                var1 = param0.getHeight(var3, this.templatePosition.getX(), this.templatePosition.getZ());
            } else {
                BlockPos var5 = this.templatePosition.offset(var2.getX() - 1, 0, var2.getZ() - 1);

                for(BlockPos var6 : BlockPos.betweenClosed(this.templatePosition, var5)) {
                    int var7 = param0.getHeight(var3, var6.getX(), var6.getZ());
                    var1 += var7;
                    var0 = Math.min(var0, var7);
                }

                var1 /= var4;
            }

            int var8 = this.isBeached ? var0 - var2.getY() / 2 - param3.nextInt(3) : var1;
            this.templatePosition = new BlockPos(this.templatePosition.getX(), var8, this.templatePosition.getZ());
            super.postProcess(param0, param1, param2, param3, param4, param5, param6);
        }
    }
}
