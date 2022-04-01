package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.chunk.ChunkGenerator;

public interface StructurePlacement {
    Codec<StructurePlacement> CODEC = Registry.STRUCTURE_PLACEMENT_TYPE.byNameCodec().dispatch(StructurePlacement::type, StructurePlacementType::codec);

    boolean isFeatureChunk(ChunkGenerator var1, long var2, int var4, int var5);

    StructurePlacementType<?> type();
}
