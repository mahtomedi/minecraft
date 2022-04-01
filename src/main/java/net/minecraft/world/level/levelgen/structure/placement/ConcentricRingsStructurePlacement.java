package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
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
    public boolean isFeatureChunk(ChunkGenerator param0, long param1, int param2, int param3) {
        List<ChunkPos> var0 = param0.getRingPositionsFor(this);
        return var0 == null ? false : var0.contains(new ChunkPos(param2, param3));
    }

    @Override
    public StructurePlacementType<?> type() {
        return StructurePlacementType.CONCENTRIC_RINGS;
    }
}
