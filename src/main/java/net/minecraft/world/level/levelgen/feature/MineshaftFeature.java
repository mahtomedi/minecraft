package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.MineShaftPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class MineshaftFeature extends StructureFeature<MineshaftConfiguration> {
    public MineshaftFeature(Codec<MineshaftConfiguration> param0) {
        super(param0);
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0,
        BiomeSource param1,
        long param2,
        WorldgenRandom param3,
        int param4,
        int param5,
        Biome param6,
        ChunkPos param7,
        MineshaftConfiguration param8,
        LevelHeightAccessor param9
    ) {
        param3.setLargeFeatureSeed(param2, param4, param5);
        double var0 = (double)param8.probability;
        return param3.nextDouble() < var0;
    }

    @Override
    public StructureFeature.StructureStartFactory<MineshaftConfiguration> getStartFactory() {
        return MineshaftFeature.MineShaftStart::new;
    }

    public static class MineShaftStart extends StructureStart<MineshaftConfiguration> {
        public MineShaftStart(StructureFeature<MineshaftConfiguration> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            int param3,
            int param4,
            Biome param5,
            MineshaftConfiguration param6,
            LevelHeightAccessor param7
        ) {
            MineShaftPieces.MineShaftRoom var0 = new MineShaftPieces.MineShaftRoom(
                0, this.random, SectionPos.sectionToBlockCoord(param3, 2), SectionPos.sectionToBlockCoord(param4, 2), param6.type
            );
            this.pieces.add(var0);
            var0.addChildren(var0, this.pieces, this.random);
            this.calculateBoundingBox();
            if (param6.type == MineshaftFeature.Type.MESA) {
                int var1 = -5;
                int var2 = param1.getSeaLevel() - this.boundingBox.y1 + this.boundingBox.getYSpan() / 2 - -5;
                this.boundingBox.move(0, var2, 0);

                for(StructurePiece var3 : this.pieces) {
                    var3.move(0, var2, 0);
                }
            } else {
                this.moveBelowSeaLevel(param1.getSeaLevel(), this.random, 10);
            }

        }
    }

    public static enum Type implements StringRepresentable {
        NORMAL("normal"),
        MESA("mesa");

        public static final Codec<MineshaftFeature.Type> CODEC = StringRepresentable.fromEnum(MineshaftFeature.Type::values, MineshaftFeature.Type::byName);
        private static final Map<String, MineshaftFeature.Type> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(MineshaftFeature.Type::getName, param0 -> param0));
        private final String name;

        private Type(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        private static MineshaftFeature.Type byName(String param0) {
            return BY_NAME.get(param0);
        }

        public static MineshaftFeature.Type byId(int param0) {
            return param0 >= 0 && param0 < values().length ? values()[param0] : NORMAL;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
