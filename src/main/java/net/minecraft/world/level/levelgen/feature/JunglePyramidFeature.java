package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.JunglePyramidPiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class JunglePyramidFeature extends StructureFeature<NoneFeatureConfiguration> {
    public JunglePyramidFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return JunglePyramidFeature.FeatureStart::new;
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
            JunglePyramidPiece var0 = new JunglePyramidPiece(this.random, param3.getMinBlockX(), param3.getMinBlockZ());
            this.pieces.add(var0);
            this.calculateBoundingBox();
        }
    }
}
