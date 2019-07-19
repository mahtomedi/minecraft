package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.JunglePyramidPiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class JunglePyramidFeature extends RandomScatteredFeature<NoneFeatureConfiguration> {
    public JunglePyramidFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public String getFeatureName() {
        return "Jungle_Pyramid";
    }

    @Override
    public int getLookupRange() {
        return 3;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return JunglePyramidFeature.FeatureStart::new;
    }

    @Override
    protected int getRandomSalt() {
        return 14357619;
    }

    public static class FeatureStart extends StructureStart {
        public FeatureStart(StructureFeature<?> param0, int param1, int param2, Biome param3, BoundingBox param4, int param5, long param6) {
            super(param0, param1, param2, param3, param4, param5, param6);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
            JunglePyramidPiece var0 = new JunglePyramidPiece(this.random, param2 * 16, param3 * 16);
            this.pieces.add(var0);
            this.calculateBoundingBox();
        }
    }
}
