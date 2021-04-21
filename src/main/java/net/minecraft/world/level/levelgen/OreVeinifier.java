package net.minecraft.world.level.levelgen;

import java.util.Random;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class OreVeinifier {
    private static final double RARITY = 1.0;
    private static final double RIDGE_NOISE_FREQUENCY = 4.0;
    private static final double THICKNESS = 0.08;
    private static final double VEININESS_THRESHOLD = 0.3;
    private static final float VEIN_SOLIDNESS = 0.7F;
    private static final double ORE_PROPORTION_VS_BASESTONE = 0.3;
    private static final double VEININESS_FREQUENCY = 0.00390625;
    private final int veinMaxY;
    private final int veinMinY;
    private final BlockState normalBlock;
    private final NormalNoise veininessNoiseSource;
    private final NormalNoise veinANoiseSource;
    private final NormalNoise veinBNoiseSource;
    private final int cellWidth;
    private final int cellHeight;

    public OreVeinifier(long param0, BlockState param1, int param2, int param3, int param4) {
        Random var0 = new Random(param0);
        this.normalBlock = param1;
        this.veininessNoiseSource = NormalNoise.create(new SimpleRandomSource(var0.nextLong()), 0, 1.0);
        this.veinANoiseSource = NormalNoise.create(new SimpleRandomSource(var0.nextLong()), -7, 1.0);
        this.veinBNoiseSource = NormalNoise.create(new SimpleRandomSource(var0.nextLong()), -7, 1.0);
        this.cellWidth = param2;
        this.cellHeight = param3;
        this.veinMaxY = Stream.of(OreVeinifier.VeinType.values()).mapToInt(param0x -> param0x.maxY).max().orElse(param4);
        this.veinMinY = Stream.of(OreVeinifier.VeinType.values()).mapToInt(param0x -> param0x.minY).min().orElse(param4);
    }

    public void fillVeininessNoiseColumn(double[] param0, int param1, int param2, int param3, int param4) {
        this.fillNoiseColumn(param0, param1, param2, this.veininessNoiseSource, 0.00390625, param3, param4);
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

    public BlockState oreVeinify(RandomSource param0, int param1, double param2, double param3, double param4) {
        BlockState var0 = this.normalBlock;
        OreVeinifier.VeinType var1 = this.getVeinType(param2);
        if (var1 != null && param1 >= var1.minY && param1 <= var1.maxY) {
            if (param0.nextFloat() > 0.7F) {
                return var0;
            } else if (this.isVein(param3, param4)) {
                return (double)param0.nextFloat() < 0.3 ? var1.ore : var1.filler;
            } else {
                return var0;
            }
        } else {
            return var0;
        }
    }

    private boolean isVein(double param0, double param1) {
        double var0 = Math.abs(1.0 * param0) - 0.08;
        double var1 = Math.abs(1.0 * param1) - 0.08;
        return Math.max(var0, var1) < 0.0;
    }

    @Nullable
    private OreVeinifier.VeinType getVeinType(double param0) {
        if (Math.abs(param0) < 0.3) {
            return null;
        } else {
            return param0 > 0.0 ? OreVeinifier.VeinType.COPPER : OreVeinifier.VeinType.IRON;
        }
    }

    static enum VeinType {
        COPPER(Blocks.COPPER_ORE.defaultBlockState(), Blocks.GRANITE.defaultBlockState(), 0, 50),
        IRON(Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), Blocks.TUFF.defaultBlockState(), -60, -8);

        private final BlockState ore;
        private final BlockState filler;
        private final int minY;
        private final int maxY;

        private VeinType(BlockState param0, BlockState param1, int param2, int param3) {
            this.ore = param0;
            this.filler = param1;
            this.minY = param2;
            this.maxY = param3;
        }
    }
}
