package net.minecraft.world.level.levelgen.feature;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class VillagePieces {
    public static void bootstrap() {
        PlainVillagePools.bootstrap();
        SnowyVillagePools.bootstrap();
        SavannaVillagePools.bootstrap();
        DesertVillagePools.bootstrap();
        TaigaVillagePools.bootstrap();
    }

    public static void addPieces(
        ChunkGenerator param0, StructureManager param1, BlockPos param2, List<StructurePiece> param3, WorldgenRandom param4, JigsawConfiguration param5
    ) {
        bootstrap();
        JigsawPlacement.addPieces(param5.startPool, param5.size, VillagePieces.VillagePiece::new, param0, param1, param2, param3, param4, true, true);
    }

    public static class VillagePiece extends PoolElementStructurePiece {
        public VillagePiece(StructureManager param0, StructurePoolElement param1, BlockPos param2, int param3, Rotation param4, BoundingBox param5) {
            super(StructurePieceType.VILLAGE, param0, param1, param2, param3, param4, param5);
        }

        public VillagePiece(StructureManager param0, CompoundTag param1) {
            super(param0, param1, StructurePieceType.VILLAGE);
        }
    }
}
