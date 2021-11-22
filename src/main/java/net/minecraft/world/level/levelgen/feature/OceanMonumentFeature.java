package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanMonumentFeature extends StructureFeature<NoneFeatureConfiguration> {
    public static final WeightedRandomList<MobSpawnSettings.SpawnerData> MONUMENT_ENEMIES = WeightedRandomList.create(
        new MobSpawnSettings.SpawnerData(EntityType.GUARDIAN, 1, 2, 4)
    );

    public OceanMonumentFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0, PieceGeneratorSupplier.simple(OceanMonumentFeature::checkLocation, OceanMonumentFeature::generatePieces));
    }

    @Override
    protected boolean linearSeparation() {
        return false;
    }

    private static boolean checkLocation(PieceGeneratorSupplier.Context<NoneFeatureConfiguration> param0x) {
        int var0 = param0x.chunkPos().getBlockX(9);
        int var1 = param0x.chunkPos().getBlockZ(9);

        for(Biome var3 : param0x.biomeSource()
            .getBiomesWithin(var0, param0x.chunkGenerator().getSeaLevel(), var1, 29, param0x.chunkGenerator().climateSampler())) {
            if (var3.getBiomeCategory() != Biome.BiomeCategory.OCEAN && var3.getBiomeCategory() != Biome.BiomeCategory.RIVER) {
                return false;
            }
        }

        return param0x.validBiomeOnTop(Heightmap.Types.OCEAN_FLOOR_WG);
    }

    private static StructurePiece createTopPiece(ChunkPos param0, WorldgenRandom param1) {
        int var0 = param0.getMinBlockX() - 29;
        int var1 = param0.getMinBlockZ() - 29;
        Direction var2 = Direction.Plane.HORIZONTAL.getRandomDirection(param1);
        return new OceanMonumentPieces.MonumentBuilding(param1, var0, var1, var2);
    }

    private static void generatePieces(StructurePiecesBuilder param0x, PieceGenerator.Context<NoneFeatureConfiguration> param1) {
        param0x.addPiece(createTopPiece(param1.chunkPos(), param1.random()));
    }

    public static PiecesContainer regeneratePiecesAfterLoad(ChunkPos param0, long param1, PiecesContainer param2) {
        if (param2.isEmpty()) {
            return param2;
        } else {
            WorldgenRandom var0 = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
            var0.setLargeFeatureSeed(param1, param0.x, param0.z);
            StructurePiece var1 = param2.pieces().get(0);
            BoundingBox var2 = var1.getBoundingBox();
            int var3 = var2.minX();
            int var4 = var2.minZ();
            Direction var5 = Direction.Plane.HORIZONTAL.getRandomDirection(var0);
            Direction var6 = Objects.requireNonNullElse(var1.getOrientation(), var5);
            StructurePiece var7 = new OceanMonumentPieces.MonumentBuilding(var0, var3, var4, var6);
            StructurePiecesBuilder var8 = new StructurePiecesBuilder();
            var8.addPiece(var7);
            return var8.build();
        }
    }
}
