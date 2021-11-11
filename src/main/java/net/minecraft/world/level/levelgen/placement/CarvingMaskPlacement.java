package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.GenerationStep;

public class CarvingMaskPlacement extends PlacementModifier {
    public static final Codec<CarvingMaskPlacement> CODEC = GenerationStep.Carving.CODEC
        .fieldOf("step")
        .xmap(CarvingMaskPlacement::new, param0 -> param0.step)
        .codec();
    private final GenerationStep.Carving step;

    private CarvingMaskPlacement(GenerationStep.Carving param0) {
        this.step = param0;
    }

    public static CarvingMaskPlacement forStep(GenerationStep.Carving param0) {
        return new CarvingMaskPlacement(param0);
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext param0, Random param1, BlockPos param2) {
        ChunkPos var0 = new ChunkPos(param2);
        return param0.getCarvingMask(var0, this.step).stream(var0);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.CARVING_MASK_PLACEMENT;
    }
}
