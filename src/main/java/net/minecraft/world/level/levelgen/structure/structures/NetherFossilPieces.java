package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class NetherFossilPieces {
    private static final ResourceLocation[] FOSSILS = new ResourceLocation[]{
        new ResourceLocation("nether_fossils/fossil_1"),
        new ResourceLocation("nether_fossils/fossil_2"),
        new ResourceLocation("nether_fossils/fossil_3"),
        new ResourceLocation("nether_fossils/fossil_4"),
        new ResourceLocation("nether_fossils/fossil_5"),
        new ResourceLocation("nether_fossils/fossil_6"),
        new ResourceLocation("nether_fossils/fossil_7"),
        new ResourceLocation("nether_fossils/fossil_8"),
        new ResourceLocation("nether_fossils/fossil_9"),
        new ResourceLocation("nether_fossils/fossil_10"),
        new ResourceLocation("nether_fossils/fossil_11"),
        new ResourceLocation("nether_fossils/fossil_12"),
        new ResourceLocation("nether_fossils/fossil_13"),
        new ResourceLocation("nether_fossils/fossil_14")
    };

    public static void addPieces(StructureTemplateManager param0, StructurePieceAccessor param1, RandomSource param2, BlockPos param3) {
        Rotation var0 = Rotation.getRandom(param2);
        param1.addPiece(new NetherFossilPieces.NetherFossilPiece(param0, Util.getRandom(FOSSILS, param2), param3, var0));
    }

    public static class NetherFossilPiece extends TemplateStructurePiece {
        public NetherFossilPiece(StructureTemplateManager param0, ResourceLocation param1, BlockPos param2, Rotation param3) {
            super(StructurePieceType.NETHER_FOSSIL, 0, param0, param1, param1.toString(), makeSettings(param3), param2);
        }

        public NetherFossilPiece(StructureTemplateManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FOSSIL, param1, param0, param1x -> makeSettings(Rotation.valueOf(param1.getString("Rot"))));
        }

        private static StructurePlaceSettings makeSettings(Rotation param0) {
            return new StructurePlaceSettings().setRotation(param0).setMirror(Mirror.NONE).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext param0, CompoundTag param1) {
            super.addAdditionalSaveData(param0, param1);
            param1.putString("Rot", this.placeSettings.getRotation().name());
        }

        @Override
        protected void handleDataMarker(String param0, BlockPos param1, ServerLevelAccessor param2, RandomSource param3, BoundingBox param4) {
        }

        @Override
        public void postProcess(
            WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, BlockPos param6
        ) {
            param4.encapsulate(this.template.getBoundingBox(this.placeSettings, this.templatePosition));
            super.postProcess(param0, param1, param2, param3, param4, param5, param6);
        }
    }
}
