package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanMonumentFeature extends StructureFeature<NoneFeatureConfiguration> {
    public static final WeightedRandomList<MobSpawnSettings.SpawnerData> MONUMENT_ENEMIES = WeightedRandomList.create(
        new MobSpawnSettings.SpawnerData(EntityType.GUARDIAN, 1, 2, 4)
    );

    public OceanMonumentFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0, OceanMonumentFeature::generatePieces);
    }

    @Override
    protected boolean linearSeparation() {
        return false;
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0,
        BiomeSource param1,
        long param2,
        WorldgenRandom param3,
        ChunkPos param4,
        ChunkPos param5,
        NoneFeatureConfiguration param6,
        LevelHeightAccessor param7
    ) {
        int var0 = param4.getBlockX(9);
        int var1 = param4.getBlockZ(9);

        for(Biome var3 : param1.getBiomesWithin(var0, param0.getSeaLevel(), var1, 29, param0.climateSampler())) {
            if (var3.getBiomeCategory() != Biome.BiomeCategory.OCEAN && var3.getBiomeCategory() != Biome.BiomeCategory.RIVER) {
                return false;
            }
        }

        return true;
    }

    private static StructurePiece createTopPiece(ChunkPos param0, WorldgenRandom param1) {
        int var0 = param0.getMinBlockX() - 29;
        int var1 = param0.getMinBlockZ() - 29;
        Direction var2 = Direction.Plane.HORIZONTAL.getRandomDirection(param1);
        return new OceanMonumentPieces.MonumentBuilding(param1, var0, var1, var2);
    }

    private static void generatePieces(StructurePiecesBuilder param0x, NoneFeatureConfiguration param1, PieceGenerator.Context param2) {
        generatePieces(param0x, param2);
    }

    private static void generatePieces(StructurePiecesBuilder param0, PieceGenerator.Context param1) {
        if (param1.validBiomeOnTop(Heightmap.Types.OCEAN_FLOOR_WG)) {
            param0.addPiece(createTopPiece(param1.chunkPos(), param1.random()));
        }

    }

    public static PiecesContainer regeneratePiecesAfterLoad(ChunkPos param0, long param1, PiecesContainer param2) {
        if (param2.isEmpty()) {
            return param2;
        } else {
            WorldgenRandom var0 = new WorldgenRandom();
            var0.setLargeFeatureSeed(param1, param0.x, param0.z);
            StructurePiece var1 = createTopPiece(param0, var0);
            StructurePiecesBuilder var2 = new StructurePiecesBuilder();
            var2.addPiece(var1);
            return var2.build();
        }
    }
}
