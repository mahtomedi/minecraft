package net.minecraft.world.level.levelgen.structure;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class ShipwreckPieces {
    private static final BlockPos PIVOT = new BlockPos(4, 0, 15);
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

    public static void addPieces(
        StructureManager param0, BlockPos param1, Rotation param2, List<StructurePiece> param3, Random param4, ShipwreckConfiguration param5
    ) {
        ResourceLocation var0 = param5.isBeached
            ? STRUCTURE_LOCATION_BEACHED[param4.nextInt(STRUCTURE_LOCATION_BEACHED.length)]
            : STRUCTURE_LOCATION_OCEAN[param4.nextInt(STRUCTURE_LOCATION_OCEAN.length)];
        param3.add(new ShipwreckPieces.ShipwreckPiece(param0, var0, param1, param2, param5.isBeached));
    }

    public static class ShipwreckPiece extends TemplateStructurePiece {
        private final Rotation rotation;
        private final ResourceLocation templateLocation;
        private final boolean isBeached;

        public ShipwreckPiece(StructureManager param0, ResourceLocation param1, BlockPos param2, Rotation param3, boolean param4) {
            super(StructurePieceType.SHIPWRECK_PIECE, 0);
            this.templatePosition = param2;
            this.rotation = param3;
            this.templateLocation = param1;
            this.isBeached = param4;
            this.loadTemplate(param0);
        }

        public ShipwreckPiece(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.SHIPWRECK_PIECE, param1);
            this.templateLocation = new ResourceLocation(param1.getString("Template"));
            this.isBeached = param1.getBoolean("isBeached");
            this.rotation = Rotation.valueOf(param1.getString("Rot"));
            this.loadTemplate(param0);
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag param0) {
            super.addAdditionalSaveData(param0);
            param0.putString("Template", this.templateLocation.toString());
            param0.putBoolean("isBeached", this.isBeached);
            param0.putString("Rot", this.rotation.name());
        }

        private void loadTemplate(StructureManager param0) {
            StructureTemplate var0 = param0.getOrCreate(this.templateLocation);
            StructurePlaceSettings var1 = new StructurePlaceSettings()
                .setRotation(this.rotation)
                .setMirror(Mirror.NONE)
                .setRotationPivot(ShipwreckPieces.PIVOT)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
            this.setup(var0, this.templatePosition, var1);
        }

        @Override
        protected void handleDataMarker(String param0, BlockPos param1, LevelAccessor param2, Random param3, BoundingBox param4) {
            if ("map_chest".equals(param0)) {
                RandomizableContainerBlockEntity.setLootTable(param2, param3, param1.below(), BuiltInLootTables.SHIPWRECK_MAP);
            } else if ("treasure_chest".equals(param0)) {
                RandomizableContainerBlockEntity.setLootTable(param2, param3, param1.below(), BuiltInLootTables.SHIPWRECK_TREASURE);
            } else if ("supply_chest".equals(param0)) {
                RandomizableContainerBlockEntity.setLootTable(param2, param3, param1.below(), BuiltInLootTables.SHIPWRECK_SUPPLY);
            }

        }

        @Override
        public boolean postProcess(LevelAccessor param0, ChunkGenerator<?> param1, Random param2, BoundingBox param3, ChunkPos param4) {
            int var0 = 256;
            int var1 = 0;
            BlockPos var2 = this.templatePosition.offset(this.template.getSize().getX() - 1, 0, this.template.getSize().getZ() - 1);

            for(BlockPos var3 : BlockPos.betweenClosed(this.templatePosition, var2)) {
                int var4 = param0.getHeight(this.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG, var3.getX(), var3.getZ());
                var1 += var4;
                var0 = Math.min(var0, var4);
            }

            var1 /= this.template.getSize().getX() * this.template.getSize().getZ();
            int var5 = this.isBeached ? var0 - this.template.getSize().getY() / 2 - param2.nextInt(3) : var1;
            this.templatePosition = new BlockPos(this.templatePosition.getX(), var5, this.templatePosition.getZ());
            return super.postProcess(param0, param1, param2, param3, param4);
        }
    }
}
