package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class BuriedTreasureStructure extends Structure {
    public static final Codec<BuriedTreasureStructure> CODEC = RecordCodecBuilder.create(param0 -> codec(param0).apply(param0, BuriedTreasureStructure::new));

    public BuriedTreasureStructure(HolderSet<Biome> param0, Map<MobCategory, StructureSpawnOverride> param1, GenerationStep.Decoration param2, boolean param3) {
        super(param0, param1, param2, param3);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        return onTopOfChunkCenter(param0, Heightmap.Types.OCEAN_FLOOR_WG, param1 -> generatePieces(param1, param0));
    }

    private static void generatePieces(StructurePiecesBuilder param0, Structure.GenerationContext param1) {
        BlockPos var0 = new BlockPos(param1.chunkPos().getBlockX(9), 90, param1.chunkPos().getBlockZ(9));
        param0.addPiece(new BuriedTreasurePieces.BuriedTreasurePiece(var0));
    }

    @Override
    public StructureType<?> type() {
        return StructureType.BURIED_TREASURE;
    }
}
