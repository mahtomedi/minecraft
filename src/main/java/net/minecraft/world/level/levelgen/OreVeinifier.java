package net.minecraft.world.level.levelgen;

import java.util.Random;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class OreVeinifier {
    private static final float RARITY = 1.0F;
    private static final float RIDGE_NOISE_FREQUENCY = 4.0F;
    private static final float THICKNESS = 0.08F;
    private static final float VEININESS_THRESHOLD = 0.5F;
    private static final double VEININESS_FREQUENCY = 1.5;
    private static final int EDGE_ROUNDOFF_BEGIN = 20;
    private static final double MAX_EDGE_ROUNDOFF = 0.2;
    private static final float VEIN_SOLIDNESS = 0.7F;
    private static final float MIN_RICHNESS = 0.1F;
    private static final float MAX_RICHNESS = 0.3F;
    private static final float MAX_RICHNESS_THRESHOLD = 0.6F;
    private static final float CHANCE_OF_RAW_ORE_BLOCK = 0.02F;
    private static final float SKIP_ORE_IF_GAP_NOISE_IS_BELOW = -0.3F;
    private final int veinMaxY;
    private final int veinMinY;
    private final BlockState normalBlock;
    private final NormalNoise veininessNoiseSource;
    private final NormalNoise veinANoiseSource;
    private final NormalNoise veinBNoiseSource;
    private final NormalNoise gapNoise;
    private final int cellWidth;
    private final int cellHeight;

    public OreVeinifier(long param0, BlockState param1, int param2, int param3, int param4) {
        Random var0 = new Random(param0);
        this.normalBlock = param1;
        this.veininessNoiseSource = NormalNoise.create(new SimpleRandomSource(var0.nextLong()), -8, 1.0);
        this.veinANoiseSource = NormalNoise.create(new SimpleRandomSource(var0.nextLong()), -7, 1.0);
        this.veinBNoiseSource = NormalNoise.create(new SimpleRandomSource(var0.nextLong()), -7, 1.0);
        this.gapNoise = NormalNoise.create(new SimpleRandomSource(0L), -5, 1.0);
        this.cellWidth = param2;
        this.cellHeight = param3;
        this.veinMaxY = Stream.of(OreVeinifier.VeinType.values()).mapToInt(param0x -> param0x.maxY).max().orElse(param4);
        this.veinMinY = Stream.of(OreVeinifier.VeinType.values()).mapToInt(param0x -> param0x.minY).min().orElse(param4);
    }

    public void fillVeininessNoiseColumn(double[] param0, int param1, int param2, int param3, int param4) {
        this.fillNoiseColumn(param0, param1, param2, this.veininessNoiseSource, 1.5, param3, param4);
    }

    public void fillNoiseColumnA(double[] param0, int param1, int param2, int param3, int param4) {
        this.fillNoiseColumn(param0, param1, param2, this.veinANoiseSource, 4.0, param3, param4);
    }

    public void fillNoiseColumnB(double[] param0, int param1, int param2, int param3, int param4) {
        this.fillNoiseColumn(param0, param1, param2, this.veinBNoiseSource, 4.0, param3, param4);
    }

    public void fillNoiseColumn(double[] param0, int param1, int param2, NormalNoise param3, double param4, int param5, int param6) {
        for(int var0 = 0; var0 < param6; ++var0) {
            int var1 = var0 + param5;
            int var2 = param1 * this.cellWidth;
            int var3 = var1 * this.cellHeight;
            int var4 = param2 * this.cellWidth;
            double var5;
            if (var3 >= this.veinMinY && var3 <= this.veinMaxY) {
                var5 = param3.getValue((double)var2 * param4, (double)var3 * param4, (double)var4 * param4);
            } else {
                var5 = 0.0;
            }

            param0[var0] = var5;
        }

    }

    public BlockState oreVeinify(RandomSource param0, int param1, int param2, int param3, double param4, double param5, double param6) {
        BlockState var0 = this.normalBlock;
        OreVeinifier.VeinType var1 = this.getVeinType(param4, param2);
        if (var1 == null) {
            return var0;
        } else if (param0.nextFloat() > 0.7F) {
            return var0;
        } else if (this.isVein(param5, param6)) {
            double var2 = Mth.clampedMap(Math.abs(param4), 0.5, 0.6F, 0.1F, 0.3F);
            if ((double)param0.nextFloat() < var2 && this.gapNoise.getValue((double)param1, (double)param2, (double)param3) > -0.3F) {
                return param0.nextFloat() < 0.02F ? var1.rawOreBlock : var1.ore;
            } else {
                return var1.filler;
            }
        } else {
            return var0;
        }
    }

    private boolean isVein(double param0, double param1) {
        double var0 = Math.abs(1.0 * param0) - 0.08F;
        double var1 = Math.abs(1.0 * param1) - 0.08F;
        return Math.max(var0, var1) < 0.0;
    }

    @Nullable
    private OreVeinifier.VeinType getVeinType(double param0, int param1) {
        OreVeinifier.VeinType var0 = param0 > 0.0 ? OreVeinifier.VeinType.COPPER : OreVeinifier.VeinType.IRON;
        int var1 = var0.maxY - param1;
        int var2 = param1 - var0.minY;
        if (var2 >= 0 && var1 >= 0) {
            int var3 = Math.min(var1, var2);
            double var4 = Mth.clampedMap((double)var3, 0.0, 20.0, -0.2, 0.0);
            return Math.abs(param0) + var4 < 0.5 ? null : var0;
        } else {
            return null;
        }
    }

    static enum VeinType {
        COPPER(Blocks.COPPER_ORE.defaultBlockState(), Blocks.RAW_COPPER_BLOCK.defaultBlockState(), Blocks.GRANITE.defaultBlockState(), 0, 50),
        IRON(Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), Blocks.RAW_IRON_BLOCK.defaultBlockState(), Blocks.TUFF.defaultBlockState(), -60, -8);

        private final BlockState ore;
        private final BlockState rawOreBlock;
        private final BlockState filler;
        private final int minY;
        private final int maxY;

        private VeinType(BlockState param0, BlockState param1, BlockState param2, int param3, int param4) {
            this.ore = param0;
            this.rawOreBlock = param1;
            this.filler = param2;
            this.minY = param3;
            this.maxY = param4;
        }
    }
}
