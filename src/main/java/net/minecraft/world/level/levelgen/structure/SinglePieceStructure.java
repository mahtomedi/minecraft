package net.minecraft.world.level.levelgen.structure;

import java.util.Optional;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public abstract class SinglePieceStructure extends Structure {
    private final SinglePieceStructure.PieceConstructor constructor;
    private final int width;
    private final int depth;

    protected SinglePieceStructure(SinglePieceStructure.PieceConstructor param0, int param1, int param2, Structure.StructureSettings param3) {
        super(param3);
        this.constructor = param0;
        this.width = param1;
        this.depth = param2;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        return getLowestY(param0, this.width, this.depth) < param0.chunkGenerator().getSeaLevel()
            ? Optional.empty()
            : onTopOfChunkCenter(param0, Heightmap.Types.WORLD_SURFACE_WG, param1 -> this.generatePieces(param1, param0));
    }

    private void generatePieces(StructurePiecesBuilder param0, Structure.GenerationContext param1) {
        ChunkPos var0 = param1.chunkPos();
        param0.addPiece(this.constructor.construct(param1.random(), var0.getMinBlockX(), var0.getMinBlockZ()));
    }

    @FunctionalInterface
    protected interface PieceConstructor {
        StructurePiece construct(WorldgenRandom var1, int var2, int var3);
    }
}
