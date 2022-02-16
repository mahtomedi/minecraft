package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;

public record ConcentricRingsStructurePlacement(int distance, int spread, int count) implements StructurePlacement {
    public static final Codec<ConcentricRingsStructurePlacement> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.intRange(0, 1023).fieldOf("distance").forGetter(ConcentricRingsStructurePlacement::distance),
                    Codec.intRange(0, 1023).fieldOf("spread").forGetter(ConcentricRingsStructurePlacement::spread),
                    Codec.intRange(1, 4095).fieldOf("count").forGetter(ConcentricRingsStructurePlacement::count)
                )
                .apply(param0, ConcentricRingsStructurePlacement::new)
    );

    @Override
    public boolean isFeatureChunk(ChunkGenerator param0, int param1, int param2) {
        return param0.getRingPositionsFor(this).contains(new ChunkPos(param1, param2));
    }

    @Override
    public StructurePlacementType<?> type() {
        return StructurePlacementType.CONCENTRIC_RINGS;
    }
}