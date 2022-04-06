package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class SwampHutStructure extends Structure {
    public static final Codec<SwampHutStructure> CODEC = simpleCodec(SwampHutStructure::new);

    public SwampHutStructure(Structure.StructureSettings param0) {
        super(param0);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        return onTopOfChunkCenter(param0, Heightmap.Types.WORLD_SURFACE_WG, param1 -> generatePieces(param1, param0));
    }

    private static void generatePieces(StructurePiecesBuilder param0, Structure.GenerationContext param1) {
        param0.addPiece(new SwampHutPiece(param1.random(), param1.chunkPos().getMinBlockX(), param1.chunkPos().getMinBlockZ()));
    }

    @Override
    public StructureType<?> type() {
        return StructureType.SWAMP_HUT;
    }
}
