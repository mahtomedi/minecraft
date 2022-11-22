package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanMonumentStructure extends Structure {
    public static final Codec<OceanMonumentStructure> CODEC = simpleCodec(OceanMonumentStructure::new);

    public OceanMonumentStructure(Structure.StructureSettings param0) {
        super(param0);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        int var0 = param0.chunkPos().getBlockX(9);
        int var1 = param0.chunkPos().getBlockZ(9);

        for(Holder<Biome> var3 : param0.biomeSource().getBiomesWithin(var0, param0.chunkGenerator().getSeaLevel(), var1, 29, param0.randomState().sampler())) {
            if (!var3.is(BiomeTags.REQUIRED_OCEAN_MONUMENT_SURROUNDING)) {
                return Optional.empty();
            }
        }

        return onTopOfChunkCenter(param0, Heightmap.Types.OCEAN_FLOOR_WG, param1 -> generatePieces(param1, param0));
    }

    private static StructurePiece createTopPiece(ChunkPos param0, WorldgenRandom param1) {
        int var0 = param0.getMinBlockX() - 29;
        int var1 = param0.getMinBlockZ() - 29;
        Direction var2 = Direction.Plane.HORIZONTAL.getRandomDirection(param1);
        return new OceanMonumentPieces.MonumentBuilding(param1, var0, var1, var2);
    }

    private static void generatePieces(StructurePiecesBuilder param0, Structure.GenerationContext param1) {
        param0.addPiece(createTopPiece(param1.chunkPos(), param1.random()));
    }

    public static PiecesContainer regeneratePiecesAfterLoad(ChunkPos param0, long param1, PiecesContainer param2) {
        if (param2.isEmpty()) {
            return param2;
        } else {
            WorldgenRandom var0 = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
            var0.setLargeFeatureSeed(param1, param0.x, param0.z);
            StructurePiece var1 = param2.pieces().get(0);
            BoundingBox var2 = var1.getBoundingBox();
            int var3 = var2.minX();
            int var4 = var2.minZ();
            Direction var5 = Direction.Plane.HORIZONTAL.getRandomDirection(var0);
            Direction var6 = (Direction)Objects.requireNonNullElse(var1.getOrientation(), var5);
            StructurePiece var7 = new OceanMonumentPieces.MonumentBuilding(var0, var3, var4, var6);
            StructurePiecesBuilder var8 = new StructurePiecesBuilder();
            var8.addPiece(var7);
            return var8.build();
        }
    }

    @Override
    public StructureType<?> type() {
        return StructureType.OCEAN_MONUMENT;
    }
}
