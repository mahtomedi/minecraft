package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
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
        public FeatureStart(StructureFeature<NoneFeatureConfiguration> param0, ChunkPos param1, int param2, long param3) {
            super(param0, param1, param2, param3);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            NoneFeatureConfiguration param4,
            LevelHeightAccessor param5,
            Predicate<Biome> param6
        ) {
            if (StructureFeature.validBiomeOnTop(param1, param5, param6, Heightmap.Types.WORLD_SURFACE_WG, param3.getMiddleBlockX(), param3.getMiddleBlockZ())) {
                if (StructureFeature.getLowestY(param1, 12, 15, param3, param5) >= param1.getSeaLevel()) {
                    JunglePyramidPiece var0 = new JunglePyramidPiece(this.random, param3.getMinBlockX(), param3.getMinBlockZ());
                    this.addPiece(var0);
                }
            }
        }
    }
}
