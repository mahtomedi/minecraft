package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class DepthBasedReplacingBaseStoneSource implements BaseStoneSource {
    private static final int ALWAYS_REPLACE_BELOW_Y = -8;
    private static final int NEVER_REPLACE_ABOVE_Y = 0;
    private final WorldgenRandom random;
    private final long seed;
    private final BlockState normalBlock;
    private final BlockState replacementBlock;
    private final NoiseGeneratorSettings settings;

    public DepthBasedReplacingBaseStoneSource(long param0, BlockState param1, BlockState param2, NoiseGeneratorSettings param3) {
        this.random = new WorldgenRandom(param0);
        this.seed = param0;
        this.normalBlock = param1;
        this.replacementBlock = param2;
        this.settings = param3;
    }

    @Override
    public BlockState getBaseBlock(int param0, int param1, int param2) {
        if (!this.settings.isDeepslateEnabled()) {
            return this.normalBlock;
        } else if (param1 < -8) {
            return this.replacementBlock;
        } else if (param1 > 0) {
            return this.normalBlock;
        } else {
            double var0 = Mth.map((double)param1, -8.0, 0.0, 1.0, 0.0);
            this.random.setBaseStoneSeed(this.seed, param0, param1, param2);
            return (double)this.random.nextFloat() < var0 ? this.replacementBlock : this.normalBlock;
        }
    }
}
