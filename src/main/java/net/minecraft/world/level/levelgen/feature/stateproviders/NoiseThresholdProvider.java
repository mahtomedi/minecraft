package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseThresholdProvider extends NoiseBasedStateProvider {
    public static final Codec<NoiseThresholdProvider> CODEC = RecordCodecBuilder.create(
        param0 -> noiseCodec(param0)
                .and(
                    param0.group(
                        Codec.floatRange(-1.0F, 1.0F).fieldOf("threshold").forGetter(param0x -> param0x.threshold),
                        Codec.floatRange(0.0F, 1.0F).fieldOf("high_chance").forGetter(param0x -> param0x.highChance),
                        BlockState.CODEC.fieldOf("default_state").forGetter(param0x -> param0x.defaultState),
                        Codec.list(BlockState.CODEC).fieldOf("low_states").forGetter(param0x -> param0x.lowStates),
                        Codec.list(BlockState.CODEC).fieldOf("high_states").forGetter(param0x -> param0x.highStates)
                    )
                )
                .apply(param0, NoiseThresholdProvider::new)
    );
    private final float threshold;
    private final float highChance;
    private final BlockState defaultState;
    private final List<BlockState> lowStates;
    private final List<BlockState> highStates;

    public NoiseThresholdProvider(
        long param0,
        NormalNoise.NoiseParameters param1,
        float param2,
        float param3,
        float param4,
        BlockState param5,
        List<BlockState> param6,
        List<BlockState> param7
    ) {
        super(param0, param1, param2);
        this.threshold = param3;
        this.highChance = param4;
        this.defaultState = param5;
        this.lowStates = param6;
        this.highStates = param7;
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.NOISE_THRESHOLD_PROVIDER;
    }

    @Override
    public BlockState getState(RandomSource param0, BlockPos param1) {
        double var0 = this.getNoiseValue(param1, (double)this.scale);
        if (var0 < (double)this.threshold) {
            return Util.getRandom(this.lowStates, param0);
        } else {
            return param0.nextFloat() < this.highChance ? Util.getRandom(this.highStates, param0) : this.defaultState;
        }
    }
}
