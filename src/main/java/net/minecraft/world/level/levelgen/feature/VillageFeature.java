package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class VillageFeature extends StructureFeature<JigsawConfiguration> {
    public VillageFeature(Function<Dynamic<?>, ? extends JigsawConfiguration> param0) {
        super(param0);
    }

    @Override
    protected int getSpacing(DimensionType param0, ChunkGeneratorSettings param1) {
        return param1.getVillagesSpacing();
    }

    @Override
    protected int getSeparation(DimensionType param0, ChunkGeneratorSettings param1) {
        return param1.getVillagesSeparation();
    }

    @Override
    protected int getRandomSalt(ChunkGeneratorSettings param0) {
        return 10387312;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return VillageFeature.FeatureStart::new;
    }

    @Override
    public String getFeatureName() {
        return "Village";
    }

    @Override
    public int getLookupRange() {
        return 8;
    }

    public static class FeatureStart extends BeardedStructureStart {
        public FeatureStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
            JigsawConfiguration var0 = param0.getStructureConfiguration(param4, Feature.VILLAGE);
            BlockPos var1 = new BlockPos(param2 * 16, 0, param3 * 16);
            VillagePieces.addPieces(param0, param1, var1, this.pieces, this.random, var0);
            this.calculateBoundingBox();
        }
    }
}
