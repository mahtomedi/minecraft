package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class OceanRuinFeature extends StructureFeature<OceanRuinConfiguration> {
    public OceanRuinFeature(Codec<OceanRuinConfiguration> param0) {
        super(param0);
    }

    @Override
    public StructureFeature.StructureStartFactory<OceanRuinConfiguration> getStartFactory() {
        return OceanRuinFeature.OceanRuinStart::new;
    }

    public static class OceanRuinStart extends StructureStart<OceanRuinConfiguration> {
        public OceanRuinStart(StructureFeature<OceanRuinConfiguration> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        public void generatePieces(ChunkGenerator param0, StructureManager param1, int param2, int param3, Biome param4, OceanRuinConfiguration param5) {
            int var0 = param2 * 16;
            int var1 = param3 * 16;
            BlockPos var2 = new BlockPos(var0, 90, var1);
            Rotation var3 = Rotation.getRandom(this.random);
            OceanRuinPieces.addPieces(param1, var2, var3, this.pieces, this.random, param5);
            this.calculateBoundingBox();
        }
    }

    public static enum Type implements StringRepresentable {
        WARM("warm"),
        COLD("cold");

        public static final Codec<OceanRuinFeature.Type> CODEC = StringRepresentable.fromEnum(OceanRuinFeature.Type::values, OceanRuinFeature.Type::byName);
        private static final Map<String, OceanRuinFeature.Type> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(OceanRuinFeature.Type::getName, param0 -> param0));
        private final String name;

        private Type(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        @Nullable
        public static OceanRuinFeature.Type byName(String param0) {
            return BY_NAME.get(param0);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
