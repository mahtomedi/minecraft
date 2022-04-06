package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.Products.P4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseProvider extends NoiseBasedStateProvider {
    public static final Codec<NoiseProvider> CODEC = RecordCodecBuilder.create(param0 -> noiseProviderCodec(param0).apply(param0, NoiseProvider::new));
    protected final List<BlockState> states;

    protected static <P extends NoiseProvider> P4<Mu<P>, Long, NormalNoise.NoiseParameters, Float, List<BlockState>> noiseProviderCodec(Instance<P> param0) {
        return noiseCodec(param0).and(Codec.list(BlockState.CODEC).fieldOf("states").forGetter(param0x -> param0x.states));
    }

    public NoiseProvider(long param0, NormalNoise.NoiseParameters param1, float param2, List<BlockState> param3) {
        super(param0, param1, param2);
        this.states = param3;
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.NOISE_PROVIDER;
    }

    @Override
    public BlockState getState(RandomSource param0, BlockPos param1) {
        return this.getRandomState(this.states, param1, (double)this.scale);
    }

    protected BlockState getRandomState(List<BlockState> param0, BlockPos param1, double param2) {
        double var0 = this.getNoiseValue(param1, param2);
        return this.getRandomState(param0, var0);
    }

    protected BlockState getRandomState(List<BlockState> param0, double param1) {
        double var0 = Mth.clamp((1.0 + param1) / 2.0, 0.0, 0.9999);
        return param0.get((int)(var0 * (double)param0.size()));
    }
}
