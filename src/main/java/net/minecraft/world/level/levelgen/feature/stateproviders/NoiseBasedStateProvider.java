package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public abstract class NoiseBasedStateProvider extends BlockStateProvider {
    protected final long seed;
    protected final NormalNoise.NoiseParameters parameters;
    protected final float scale;
    protected final NormalNoise noise;

    protected static <P extends NoiseBasedStateProvider> P3<Mu<P>, Long, NormalNoise.NoiseParameters, Float> noiseCodec(Instance<P> param0) {
        return param0.group(
            Codec.LONG.fieldOf("seed").forGetter(param0x -> param0x.seed),
            NormalNoise.NoiseParameters.DIRECT_CODEC.fieldOf("noise").forGetter(param0x -> param0x.parameters),
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("scale").forGetter(param0x -> param0x.scale)
        );
    }

    protected NoiseBasedStateProvider(long param0, NormalNoise.NoiseParameters param1, float param2) {
        this.seed = param0;
        this.parameters = param1;
        this.scale = param2;
        this.noise = NormalNoise.create(new WorldgenRandom(new LegacyRandomSource(param0)), param1);
    }

    protected double getNoiseValue(BlockPos param0, double param1) {
        return this.noise.getValue((double)param0.getX() * param1, (double)param0.getY() * param1, (double)param0.getZ() * param1);
    }
}
