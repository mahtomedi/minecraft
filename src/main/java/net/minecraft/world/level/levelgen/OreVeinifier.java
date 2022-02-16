package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class OreVeinifier {
    private static final float VEININESS_THRESHOLD = 0.4F;
    private static final int EDGE_ROUNDOFF_BEGIN = 20;
    private static final double MAX_EDGE_ROUNDOFF = 0.2;
    private static final float VEIN_SOLIDNESS = 0.7F;
    private static final float MIN_RICHNESS = 0.1F;
    private static final float MAX_RICHNESS = 0.3F;
    private static final float MAX_RICHNESS_THRESHOLD = 0.6F;
    private static final float CHANCE_OF_RAW_ORE_BLOCK = 0.02F;
    private static final float SKIP_ORE_IF_GAP_NOISE_IS_BELOW = -0.3F;

    private OreVeinifier() {
    }

    protected static NoiseChunk.BlockStateFiller create(DensityFunction param0, DensityFunction param1, DensityFunction param2, PositionalRandomFactory param3) {
        BlockState var0 = null;
        return param5 -> {
            double var0x = param0.compute(param5);
            int var1x = param5.blockY();
            OreVeinifier.VeinType var2x = var0x > 0.0 ? OreVeinifier.VeinType.COPPER : OreVeinifier.VeinType.IRON;
            double var3x = Math.abs(var0x);
            int var4x = var2x.maxY - var1x;
            int var5 = var1x - var2x.minY;
            if (var5 >= 0 && var4x >= 0) {
                int var6 = Math.min(var4x, var5);
                double var7 = Mth.clampedMap((double)var6, 0.0, 20.0, -0.2, 0.0);
                if (var3x + var7 < 0.4F) {
                    return var0;
                } else {
                    RandomSource var8 = param3.at(param5.blockX(), var1x, param5.blockZ());
                    if (var8.nextFloat() > 0.7F) {
                        return var0;
                    } else if (param1.compute(param5) >= 0.0) {
                        return var0;
                    } else {
                        double var9 = Mth.clampedMap(var3x, 0.4F, 0.6F, 0.1F, 0.3F);
                        if ((double)var8.nextFloat() < var9 && param2.compute(param5) > -0.3F) {
                            return var8.nextFloat() < 0.02F ? var2x.rawOreBlock : var2x.ore;
                        } else {
                            return var2x.filler;
                        }
                    }
                }
            } else {
                return var0;
            }
        };
    }

    protected static enum VeinType {
        COPPER(Blocks.COPPER_ORE.defaultBlockState(), Blocks.RAW_COPPER_BLOCK.defaultBlockState(), Blocks.GRANITE.defaultBlockState(), 0, 50),
        IRON(Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), Blocks.RAW_IRON_BLOCK.defaultBlockState(), Blocks.TUFF.defaultBlockState(), -60, -8);

        final BlockState ore;
        final BlockState rawOreBlock;
        final BlockState filler;
        protected final int minY;
        protected final int maxY;

        private VeinType(BlockState param0, BlockState param1, BlockState param2, int param3, int param4) {
            this.ore = param0;
            this.rawOreBlock = param1;
            this.filler = param2;
            this.minY = param3;
            this.maxY = param4;
        }
    }
}
