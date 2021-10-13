package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class DualNoiseProvider extends NoiseProvider {
    public static final Codec<DualNoiseProvider> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    InclusiveRange.codec(Codec.INT, 1, 64).fieldOf("variety").forGetter(param0x -> param0x.variety),
                    NormalNoise.NoiseParameters.CODEC.fieldOf("slow_noise").forGetter(param0x -> param0x.slowNoiseParameters),
                    ExtraCodecs.POSITIVE_FLOAT.fieldOf("slow_scale").forGetter(param0x -> param0x.slowScale)
                )
                .and(noiseProviderCodec(param0))
                .apply(param0, DualNoiseProvider::new)
    );
    private final InclusiveRange<Integer> variety;
    private final NormalNoise.NoiseParameters slowNoiseParameters;
    private final float slowScale;
    private final NormalNoise slowNoise;

    public DualNoiseProvider(
        InclusiveRange<Integer> param0,
        NormalNoise.NoiseParameters param1,
        float param2,
        long param3,
        NormalNoise.NoiseParameters param4,
        float param5,
        List<BlockState> param6
    ) {
        super(param3, param4, param5, param6);
        this.variety = param0;
        this.slowNoiseParameters = param1;
        this.slowScale = param2;
        this.slowNoise = NormalNoise.createLegacy(new WorldgenRandom(new LegacyRandomSource(param3)), param1);
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.DUAL_NOISE_PROVIDER;
    }

    @Override
    public BlockState getState(Random param0, BlockPos param1) {
        double var0 = this.getSlowNoiseValue(param1);
        int var1 = (int)Mth.clampedMap(var0, -1.0, 1.0, (double)this.variety.minInclusive().intValue(), (double)(this.variety.maxInclusive() + 1));
        List<BlockState> var2 = Lists.newArrayListWithCapacity(var1);

        for(int var3 = 0; var3 < var1; ++var3) {
            var2.add(this.getRandomState(this.states, this.getSlowNoiseValue(param1.offset(var3 * 54545, 0, var3 * 34234))));
        }

        return this.getRandomState(var2, param1, (double)this.scale);
    }

    protected double getSlowNoiseValue(BlockPos param0) {
        return this.slowNoise
            .getValue(
                (double)((float)param0.getX() * this.slowScale),
                (double)((float)param0.getY() * this.slowScale),
                (double)((float)param0.getZ() * this.slowScale)
            );
    }
}
