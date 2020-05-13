package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MultiJigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class BastionPieces {
    public static final ImmutableMap<String, Integer> POOLS = ImmutableMap.<String, Integer>builder()
        .put("bastion/units/base", 60)
        .put("bastion/hoglin_stable/origin", 60)
        .put("bastion/treasure/starters", 60)
        .put("bastion/bridge/start", 60)
        .build();

    public static void bootstrap() {
        BastionHousingUnitsPools.bootstrap();
        BastionHoglinStablePools.bootstrap();
        BastionTreasureRoomPools.bootstrap();
        BastionBridgePools.bootstrap();
        BastionSharedPools.bootstrap();
    }

    public static void addPieces(
        ChunkGenerator param0, StructureManager param1, BlockPos param2, List<StructurePiece> param3, WorldgenRandom param4, MultiJigsawConfiguration param5
    ) {
        bootstrap();
        JigsawConfiguration var0 = param5.getRandomPool(param4);
        JigsawPlacement.addPieces(var0.startPool, var0.size, BastionPieces.BastionPiece::new, param0, param1, param2, param3, param4, false, false);
    }

    public static class BastionPiece extends PoolElementStructurePiece {
        public BastionPiece(StructureManager param0, StructurePoolElement param1, BlockPos param2, int param3, Rotation param4, BoundingBox param5) {
            super(StructurePieceType.BASTION_REMNANT, param0, param1, param2, param3, param4, param5);
        }

        public BastionPiece(StructureManager param0, CompoundTag param1) {
            super(param0, param1, StructurePieceType.BASTION_REMNANT);
        }
    }
}
