package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.SwamplandHutPiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class SwamplandHutFeature extends StructureFeature<NoneFeatureConfiguration> {
    private static final List<MobSpawnSettings.SpawnerData> SWAMPHUT_ENEMIES = ImmutableList.of(new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1, 1));
    private static final List<MobSpawnSettings.SpawnerData> SWAMPHUT_ANIMALS = ImmutableList.of(new MobSpawnSettings.SpawnerData(EntityType.CAT, 1, 1, 1));

    public SwamplandHutFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return SwamplandHutFeature.FeatureStart::new;
    }

    @Override
    public List<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
        return SWAMPHUT_ENEMIES;
    }

    @Override
    public List<MobSpawnSettings.SpawnerData> getSpecialAnimals() {
        return SWAMPHUT_ANIMALS;
    }

    public static class FeatureStart extends StructureStart<NoneFeatureConfiguration> {
        public FeatureStart(StructureFeature<NoneFeatureConfiguration> param0, ChunkPos param1, BoundingBox param2, int param3, long param4) {
            super(param0, param1, param2, param3, param4);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            Biome param4,
            NoneFeatureConfiguration param5,
            LevelHeightAccessor param6
        ) {
            SwamplandHutPiece var0 = new SwamplandHutPiece(this.random, param3.getMinBlockX(), param3.getMinBlockZ());
            this.pieces.add(var0);
            this.calculateBoundingBox();
        }
    }
}
