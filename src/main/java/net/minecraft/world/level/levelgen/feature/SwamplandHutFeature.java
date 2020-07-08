package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.SwamplandHutPiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class SwamplandHutFeature extends StructureFeature<NoneFeatureConfiguration> {
    private static final List<Biome.SpawnerData> SWAMPHUT_ENEMIES = Lists.newArrayList(new Biome.SpawnerData(EntityType.WITCH, 1, 1, 1));
    private static final List<Biome.SpawnerData> SWAMPHUT_ANIMALS = Lists.newArrayList(new Biome.SpawnerData(EntityType.CAT, 1, 1, 1));

    public SwamplandHutFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return SwamplandHutFeature.FeatureStart::new;
    }

    @Override
    public List<Biome.SpawnerData> getSpecialEnemies() {
        return SWAMPHUT_ENEMIES;
    }

    @Override
    public List<Biome.SpawnerData> getSpecialAnimals() {
        return SWAMPHUT_ANIMALS;
    }

    public static class FeatureStart extends StructureStart<NoneFeatureConfiguration> {
        public FeatureStart(StructureFeature<NoneFeatureConfiguration> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        public void generatePieces(
            RegistryAccess param0, ChunkGenerator param1, StructureManager param2, int param3, int param4, Biome param5, NoneFeatureConfiguration param6
        ) {
            SwamplandHutPiece var0 = new SwamplandHutPiece(this.random, param3 * 16, param4 * 16);
            this.pieces.add(var0);
            this.calculateBoundingBox();
        }
    }
}
