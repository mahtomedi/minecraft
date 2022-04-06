package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanRuinStructure extends Structure {
    public static final Codec<OceanRuinStructure> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    settingsCodec(param0),
                    OceanRuinStructure.Type.CODEC.fieldOf("biome_temp").forGetter(param0x -> param0x.biomeTemp),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("large_probability").forGetter(param0x -> param0x.largeProbability),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("cluster_probability").forGetter(param0x -> param0x.clusterProbability)
                )
                .apply(param0, OceanRuinStructure::new)
    );
    public final OceanRuinStructure.Type biomeTemp;
    public final float largeProbability;
    public final float clusterProbability;

    public OceanRuinStructure(Structure.StructureSettings param0, OceanRuinStructure.Type param1, float param2, float param3) {
        super(param0);
        this.biomeTemp = param1;
        this.largeProbability = param2;
        this.clusterProbability = param3;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        return onTopOfChunkCenter(param0, Heightmap.Types.OCEAN_FLOOR_WG, param1 -> this.generatePieces(param1, param0));
    }

    private void generatePieces(StructurePiecesBuilder param0, Structure.GenerationContext param1) {
        BlockPos var0 = new BlockPos(param1.chunkPos().getMinBlockX(), 90, param1.chunkPos().getMinBlockZ());
        Rotation var1 = Rotation.getRandom(param1.random());
        OceanRuinPieces.addPieces(param1.structureTemplateManager(), var0, var1, param0, param1.random(), this);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.OCEAN_RUIN;
    }

    public static enum Type implements StringRepresentable {
        WARM("warm"),
        COLD("cold");

        public static final Codec<OceanRuinStructure.Type> CODEC = StringRepresentable.fromEnum(OceanRuinStructure.Type::values);
        private final String name;

        private Type(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
