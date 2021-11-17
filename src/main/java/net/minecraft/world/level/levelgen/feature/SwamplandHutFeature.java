package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.SwamplandHutPiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class SwamplandHutFeature extends StructureFeature<NoneFeatureConfiguration> {
    public static final WeightedRandomList<MobSpawnSettings.SpawnerData> SWAMPHUT_ENEMIES = WeightedRandomList.create(
        new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1, 1)
    );
    public static final WeightedRandomList<MobSpawnSettings.SpawnerData> SWAMPHUT_ANIMALS = WeightedRandomList.create(
        new MobSpawnSettings.SpawnerData(EntityType.CAT, 1, 1, 1)
    );

    public SwamplandHutFeature(Codec<NoneFeatureConfiguration> param0) {
        super(
            param0,
            PieceGeneratorSupplier.simple(PieceGeneratorSupplier.checkForBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG), SwamplandHutFeature::generatePieces)
        );
    }

    private static void generatePieces(StructurePiecesBuilder param0x, PieceGenerator.Context<NoneFeatureConfiguration> param1) {
        param0x.addPiece(new SwamplandHutPiece(param1.random(), param1.chunkPos().getMinBlockX(), param1.chunkPos().getMinBlockZ()));
    }
}
