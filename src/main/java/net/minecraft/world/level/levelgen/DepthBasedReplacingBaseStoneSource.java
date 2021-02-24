package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class DepthBasedReplacingBaseStoneSource implements BaseStoneSource {
    private final WorldgenRandom random;
    private final long seed;
    private final BlockState normalBlock;
    private final BlockState replacementBlock;

    public DepthBasedReplacingBaseStoneSource(long param0, BlockState param1, BlockState param2) {
        this.random = new WorldgenRandom(param0);
        this.seed = param0;
        this.normalBlock = param1;
        this.replacementBlock = param2;
    }

    @Override
    public BlockState getBaseStone(int param0, int param1, int param2, NoiseGeneratorSettings param3) {
        if (!param3.isDeepslateEnabled()) {
            return this.normalBlock;
        } else {
            this.random.setBaseStoneSeed(this.seed, param0, param1, param2);
            double var0 = Mth.clampedMap((double)param1, -8.0, 0.0, 1.0, 0.0);
            return (double)this.random.nextFloat() < var0 ? this.replacementBlock : this.normalBlock;
        }
    }
}
