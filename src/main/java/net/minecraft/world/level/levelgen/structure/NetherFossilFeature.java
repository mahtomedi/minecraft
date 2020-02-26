package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.RandomScatteredFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherFossilFeature extends RandomScatteredFeature<NoneFeatureConfiguration> {
    public NetherFossilFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    protected int getRandomSalt() {
        return 14357921;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return NetherFossilFeature.FeatureStart::new;
    }

    @Override
    public String getFeatureName() {
        return "Nether_Fossil";
    }

    @Override
    protected int getSpacing(ChunkGenerator<?> param0) {
        return 2;
    }

    @Override
    protected int getSeparation(ChunkGenerator<?> param0) {
        return 1;
    }

    @Override
    public int getLookupRange() {
        return 3;
    }

    public static class FeatureStart extends StructureStart {
        public FeatureStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
            int var0 = param2 * 16;
            int var1 = param3 * 16;
            int var2 = param0.getSeaLevel() + this.random.nextInt(126 - param0.getSeaLevel());
            NetherFossilPieces.addPieces(param1, this.pieces, this.random, new BlockPos(var0 + this.random.nextInt(16), var2, var1 + this.random.nextInt(16)));
            this.calculateBoundingBox();
        }
    }
}
