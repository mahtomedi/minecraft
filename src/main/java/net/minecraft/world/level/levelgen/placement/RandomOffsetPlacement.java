package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;

public class RandomOffsetPlacement extends PlacementModifier {
    public static final Codec<RandomOffsetPlacement> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    IntProvider.codec(-16, 16).fieldOf("xz_spread").forGetter(param0x -> param0x.xzSpread),
                    IntProvider.codec(-16, 16).fieldOf("y_spread").forGetter(param0x -> param0x.ySpread)
                )
                .apply(param0, RandomOffsetPlacement::new)
    );
    private final IntProvider xzSpread;
    private final IntProvider ySpread;

    public static RandomOffsetPlacement of(IntProvider param0, IntProvider param1) {
        return new RandomOffsetPlacement(param0, param1);
    }

    public static RandomOffsetPlacement vertical(IntProvider param0) {
        return new RandomOffsetPlacement(ConstantInt.of(0), param0);
    }

    public static RandomOffsetPlacement horizontal(IntProvider param0) {
        return new RandomOffsetPlacement(param0, ConstantInt.of(0));
    }

    private RandomOffsetPlacement(IntProvider param0, IntProvider param1) {
        this.xzSpread = param0;
        this.ySpread = param1;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext param0, RandomSource param1, BlockPos param2) {
        int var0 = param2.getX() + this.xzSpread.sample(param1);
        int var1 = param2.getY() + this.ySpread.sample(param1);
        int var2 = param2.getZ() + this.xzSpread.sample(param1);
        return Stream.of(new BlockPos(var0, var1, var2));
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.RANDOM_OFFSET;
    }
}
