package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public class InSquarePlacement extends PlacementModifier {
    private static final InSquarePlacement INSTANCE = new InSquarePlacement();
    public static final Codec<InSquarePlacement> CODEC = Codec.unit(() -> INSTANCE);

    public static InSquarePlacement spread() {
        return INSTANCE;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext param0, RandomSource param1, BlockPos param2) {
        int var0 = param1.nextInt(16) + param2.getX();
        int var1 = param1.nextInt(16) + param2.getZ();
        return Stream.of(new BlockPos(var0, param2.getY(), var1));
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.IN_SQUARE;
    }
}
