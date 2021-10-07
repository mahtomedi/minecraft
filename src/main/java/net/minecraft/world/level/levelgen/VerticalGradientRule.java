package net.minecraft.world.level.levelgen;

import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.material.WorldGenMaterialRule;
import org.apache.commons.lang3.Validate;

public class VerticalGradientRule implements WorldGenMaterialRule {
    private final PositionalRandomFactory randomFactory;
    @Nullable
    private final BlockState lowerState;
    @Nullable
    private final BlockState upperState;
    private final int alwaysLowerAtAndBelow;
    private final int alwaysUpperAtAndAbove;

    public VerticalGradientRule(PositionalRandomFactory param0, @Nullable BlockState param1, @Nullable BlockState param2, int param3, int param4) {
        this.randomFactory = param0;
        this.lowerState = param1;
        this.upperState = param2;
        this.alwaysLowerAtAndBelow = param3;
        this.alwaysUpperAtAndAbove = param4;
        Validate.isTrue(param3 < param4, "Below bounds (" + param3 + ") need to be smaller than above bounds (" + param4 + ")");
    }

    @Nullable
    @Override
    public BlockState apply(NoiseChunk param0, int param1, int param2, int param3) {
        if (param2 <= this.alwaysLowerAtAndBelow) {
            return this.lowerState;
        } else if (param2 >= this.alwaysUpperAtAndAbove) {
            return this.upperState;
        } else {
            double var0 = Mth.map((double)param2, (double)this.alwaysLowerAtAndBelow, (double)this.alwaysUpperAtAndAbove, 1.0, 0.0);
            RandomSource var1 = this.randomFactory.at(param1, param2, param3);
            return (double)var1.nextFloat() < var0 ? this.lowerState : this.upperState;
        }
    }
}
