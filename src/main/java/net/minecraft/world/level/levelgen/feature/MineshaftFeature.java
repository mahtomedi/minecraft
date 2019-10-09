package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.MineShaftPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class MineshaftFeature extends StructureFeature<MineshaftConfiguration> {
    public MineshaftFeature(Function<Dynamic<?>, ? extends MineshaftConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean isFeatureChunk(BiomeManager param0, ChunkGenerator<?> param1, Random param2, int param3, int param4, Biome param5) {
        ((WorldgenRandom)param2).setLargeFeatureSeed(param1.getSeed(), param3, param4);
        if (param1.isBiomeValidStartForStructure(param5, this)) {
            MineshaftConfiguration var0 = param1.getStructureConfiguration(param5, this);
            double var1 = var0.probability;
            return param2.nextDouble() < var1;
        } else {
            return false;
        }
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return MineshaftFeature.MineShaftStart::new;
    }

    @Override
    public String getFeatureName() {
        return "Mineshaft";
    }

    @Override
    public int getLookupRange() {
        return 8;
    }

    public static class MineShaftStart extends StructureStart {
        public MineShaftStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
            MineshaftConfiguration var0 = param0.getStructureConfiguration(param4, Feature.MINESHAFT);
            MineShaftPieces.MineShaftRoom var1 = new MineShaftPieces.MineShaftRoom(0, this.random, (param2 << 4) + 2, (param3 << 4) + 2, var0.type);
            this.pieces.add(var1);
            var1.addChildren(var1, this.pieces, this.random);
            this.calculateBoundingBox();
            if (var0.type == MineshaftFeature.Type.MESA) {
                int var2 = -5;
                int var3 = param0.getSeaLevel() - this.boundingBox.y1 + this.boundingBox.getYSpan() / 2 - -5;
                this.boundingBox.move(0, var3, 0);

                for(StructurePiece var4 : this.pieces) {
                    var4.move(0, var3, 0);
                }
            } else {
                this.moveBelowSeaLevel(param0.getSeaLevel(), this.random, 10);
            }

        }
    }

    public static enum Type {
        NORMAL("normal"),
        MESA("mesa");

        private static final Map<String, MineshaftFeature.Type> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(MineshaftFeature.Type::getName, param0 -> param0));
        private final String name;

        private Type(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        public static MineshaftFeature.Type byName(String param0) {
            return BY_NAME.get(param0);
        }

        public static MineshaftFeature.Type byId(int param0) {
            return param0 >= 0 && param0 < values().length ? values()[param0] : NORMAL;
        }
    }
}
