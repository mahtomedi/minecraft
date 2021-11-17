package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanRuinFeature extends StructureFeature<OceanRuinConfiguration> {
    public OceanRuinFeature(Codec<OceanRuinConfiguration> param0) {
        super(
            param0, PieceGeneratorSupplier.simple(PieceGeneratorSupplier.checkForBiomeOnTop(Heightmap.Types.OCEAN_FLOOR_WG), OceanRuinFeature::generatePieces)
        );
    }

    private static void generatePieces(StructurePiecesBuilder param0x, PieceGenerator.Context<OceanRuinConfiguration> param1) {
        BlockPos var0 = new BlockPos(param1.chunkPos().getMinBlockX(), 90, param1.chunkPos().getMinBlockZ());
        Rotation var1 = Rotation.getRandom(param1.random());
        OceanRuinPieces.addPieces(param1.structureManager(), var0, var1, param0x, param1.random(), param1.config());
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
