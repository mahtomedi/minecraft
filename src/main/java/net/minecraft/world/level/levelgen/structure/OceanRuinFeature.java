package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
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
        public OceanRuinStart(StructureFeature<OceanRuinConfiguration> param0, ChunkPos param1, int param2, long param3) {
            super(param0, param1, param2, param3);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            Biome param4,
            OceanRuinConfiguration param5,
            LevelHeightAccessor param6
        ) {
            BlockPos var0 = new BlockPos(param3.getMinBlockX(), 90, param3.getMinBlockZ());
            Rotation var1 = Rotation.getRandom(this.random);
            OceanRuinPieces.addPieces(param2, var0, var1, this, this.random, param5);
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
