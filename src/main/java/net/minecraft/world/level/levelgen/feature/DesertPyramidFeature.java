package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.DesertPyramidPiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class DesertPyramidFeature extends StructureFeature<NoneFeatureConfiguration> {
    public DesertPyramidFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return DesertPyramidFeature.FeatureStart::new;
    }

    public static class FeatureStart extends StructureStart<NoneFeatureConfiguration> {
        public FeatureStart(StructureFeature<NoneFeatureConfiguration> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        public void generatePieces(ChunkGenerator param0, StructureManager param1, int param2, int param3, Biome param4, NoneFeatureConfiguration param5) {
            DesertPyramidPiece var0 = new DesertPyramidPiece(this.random, param2 * 16, param3 * 16);
            this.pieces.add(var0);
            this.calculateBoundingBox();
        }
    }
}
