package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;

public class CountPlacement extends RepeatingPlacement {
    public static final Codec<CountPlacement> CODEC = IntProvider.codec(0, 256).fieldOf("count").xmap(CountPlacement::new, param0 -> param0.count).codec();
    private final IntProvider count;

    private CountPlacement(IntProvider param0) {
        this.count = param0;
    }

    public static CountPlacement of(IntProvider param0) {
        return new CountPlacement(param0);
    }

    public static CountPlacement of(int param0) {
        return of(ConstantInt.of(param0));
    }

    @Override
    protected int count(RandomSource param0, BlockPos param1) {
        return this.count.sample(param0);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.COUNT;
    }
}
