package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.Dynamic;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.RandomScatteredFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class OceanRuinFeature extends RandomScatteredFeature<OceanRuinConfiguration> {
    public OceanRuinFeature(Function<Dynamic<?>, ? extends OceanRuinConfiguration> param0) {
        super(param0);
    }

    @Override
    public String getFeatureName() {
        return "Ocean_Ruin";
    }

    @Override
    public int getLookupRange() {
        return 3;
    }

    @Override
    protected int getSpacing(DimensionType param0, ChunkGeneratorSettings param1) {
        return param1.getOceanRuinSpacing();
    }

    @Override
    protected int getSeparation(DimensionType param0, ChunkGeneratorSettings param1) {
        return param1.getOceanRuinSeparation();
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return OceanRuinFeature.OceanRuinStart::new;
    }

    @Override
    protected int getRandomSalt(ChunkGeneratorSettings param0) {
        return 14357621;
    }

    public static class OceanRuinStart extends StructureStart {
        public OceanRuinStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
            OceanRuinConfiguration var0 = param0.getStructureConfiguration(param4, Feature.OCEAN_RUIN);
            int var1 = param2 * 16;
            int var2 = param3 * 16;
            BlockPos var3 = new BlockPos(var1, 90, var2);
            Rotation var4 = Rotation.getRandom(this.random);
            OceanRuinPieces.addPieces(param1, var3, var4, this.pieces, this.random, var0);
            this.calculateBoundingBox();
        }
    }

    public static enum Type {
        WARM("warm"),
        COLD("cold");

        private static final Map<String, OceanRuinFeature.Type> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(OceanRuinFeature.Type::getName, param0 -> param0));
        private final String name;

        private Type(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        public static OceanRuinFeature.Type byName(String param0) {
            return BY_NAME.get(param0);
        }
    }
}
