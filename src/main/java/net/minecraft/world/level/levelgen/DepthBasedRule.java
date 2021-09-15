package net.minecraft.world.level.levelgen;

import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.material.WorldGenMaterialRule;

public class DepthBasedRule implements WorldGenMaterialRule {
    private static final int ALWAYS_REPLACE_BELOW_Y = -8;
    private static final int NEVER_REPLACE_ABOVE_Y = 0;
    private final PositionalRandomFactory randomFactory;
    private final BlockState state;

    public DepthBasedRule(PositionalRandomFactory param0, BlockState param1) {
        this.randomFactory = param0;
        this.state = param1;
    }

    @Nullable
    @Override
    public BlockState apply(NoiseChunk param0, int param1, int param2, int param3) {
        if (param2 < -8) {
            return this.state;
        } else if (param2 > 0) {
            return null;
        } else {
            double var0 = (double)Mth.map((float)param2, -8.0F, 0.0F, 1.0F, 0.0F);
            RandomSource var1 = this.randomFactory.at(param1, param2, param3);
            return (double)var1.nextFloat() < var0 ? this.state : null;
        }
    }
}
