package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.IglooPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class IglooFeature extends RandomScatteredFeature<NoneFeatureConfiguration> {
    public IglooFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public String getFeatureName() {
        return "Igloo";
    }

    @Override
    public int getLookupRange() {
        return 3;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return IglooFeature.FeatureStart::new;
    }

    @Override
    protected int getRandomSalt(ChunkGeneratorSettings param0) {
        return 14357618;
    }

    public static class FeatureStart extends StructureStart {
        public FeatureStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
            NoneFeatureConfiguration var0 = param0.getStructureConfiguration(param4, Feature.IGLOO);
            int var1 = param2 * 16;
            int var2 = param3 * 16;
            BlockPos var3 = new BlockPos(var1, 90, var2);
            Rotation var4 = Rotation.getRandom(this.random);
            IglooPieces.addPieces(param1, var3, var4, this.pieces, this.random, var0);
            this.calculateBoundingBox();
        }
    }
}
