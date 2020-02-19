package net.minecraft.world.level.levelgen.structure;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BedrockBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

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

    public static void addPieces(StructureManager param0, List<StructurePiece> param1, Random param2, BlockPos param3) {
        Rotation var0 = Rotation.getRandom(param2);
        param1.add(new NetherFossilPieces.NetherFossilPiece(param0, FOSSILS[param2.nextInt(FOSSILS.length)], param3, var0));
    }

    public static class NetherFossilPiece extends TemplateStructurePiece {
        private final ResourceLocation templateLocation;
        private final Rotation rotation;

        public NetherFossilPiece(StructureManager param0, ResourceLocation param1, BlockPos param2, Rotation param3) {
            super(StructurePieceType.NETHER_FOSSIL, 0);
            this.templateLocation = param1;
            this.templatePosition = param2;
            this.rotation = param3;
            this.loadTemplate(param0);
        }

        public NetherFossilPiece(StructureManager param0, CompoundTag param1) {
            super(StructurePieceType.NETHER_FOSSIL, param1);
            this.templateLocation = new ResourceLocation(param1.getString("Template"));
            this.rotation = Rotation.valueOf(param1.getString("Rot"));
            this.loadTemplate(param0);
        }

        private void loadTemplate(StructureManager param0) {
            StructureTemplate var0 = param0.getOrCreate(this.templateLocation);
            StructurePlaceSettings var1 = new StructurePlaceSettings()
                .setRotation(this.rotation)
                .setMirror(Mirror.NONE)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
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
        }

        @Override
        public boolean postProcess(LevelAccessor param0, ChunkGenerator<?> param1, Random param2, BoundingBox param3, ChunkPos param4) {
            while(param0.getBlockState(this.templatePosition.above()).getBlock() != Blocks.BEDROCK && this.templatePosition.getY() < 128) {
                BlockPos var0 = this.templatePosition.below();
                BlockState var1 = param0.getBlockState(var0);
                if (!param0.getBlockState(this.templatePosition).isAir()
                    || var1.getBlock() != Blocks.SOUL_SAND && !var1.isFaceSturdy(param0, var0, Direction.UP)) {
                    this.templatePosition = this.templatePosition.above();
                    continue;
                }
                break;
            }

            if (param0.getBlockState(this.templatePosition.above()).getBlock() instanceof BedrockBlock) {
                return false;
            } else {
                param3.expand(this.template.getBoundingBox(this.placeSettings, this.templatePosition));
                return super.postProcess(param0, param1, param2, param3, param4);
            }
        }
    }
}