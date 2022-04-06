package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class WeightedListHeight extends HeightProvider {
    public static final Codec<WeightedListHeight> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(SimpleWeightedRandomList.wrappedCodec(HeightProvider.CODEC).fieldOf("distribution").forGetter(param0x -> param0x.distribution))
                .apply(param0, WeightedListHeight::new)
    );
    private final SimpleWeightedRandomList<HeightProvider> distribution;

    public WeightedListHeight(SimpleWeightedRandomList<HeightProvider> param0) {
        this.distribution = param0;
    }

    @Override
    public int sample(RandomSource param0, WorldGenerationContext param1) {
        return this.distribution.getRandomValue(param0).orElseThrow(IllegalStateException::new).sample(param0, param1);
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.WEIGHTED_LIST;
    }
}
