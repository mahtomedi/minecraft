package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ShipwreckPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ShipwreckFeature extends RandomScatteredFeature<ShipwreckConfiguration> {
    public ShipwreckFeature(Function<Dynamic<?>, ? extends ShipwreckConfiguration> param0) {
        super(param0);
    }

    @Override
    public String getFeatureName() {
        return "Shipwreck";
    }

    @Override
    public int getLookupRange() {
        return 3;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return ShipwreckFeature.FeatureStart::new;
    }

    @Override
    protected int getRandomSalt(ChunkGeneratorSettings param0) {
        return 165745295;
    }

    @Override
    protected int getSpacing(ChunkGeneratorSettings param0) {
        return param0.getShipwreckSpacing();
    }

    @Override
    protected int getSeparation(ChunkGeneratorSettings param0) {
        return param0.getShipwreckSeparation();
    }

    public static class FeatureStart extends StructureStart {
        public FeatureStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator param0, StructureManager param1, int param2, int param3, Biome param4) {
            ShipwreckConfiguration var0 = param0.getStructureConfiguration(param4, Feature.SHIPWRECK);
            Rotation var1 = Rotation.getRandom(this.random);
            BlockPos var2 = new BlockPos(param2 * 16, 90, param3 * 16);
            ShipwreckPieces.addPieces(param1, var2, var1, this.pieces, this.random, var0);
            this.calculateBoundingBox();
        }
    }
}
