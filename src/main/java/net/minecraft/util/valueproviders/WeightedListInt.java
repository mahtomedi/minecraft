package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;

public class WeightedListInt extends IntProvider {
    public static final Codec<WeightedListInt> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(SimpleWeightedRandomList.wrappedCodec(IntProvider.CODEC).fieldOf("distribution").forGetter(param0x -> param0x.distribution))
                .apply(param0, WeightedListInt::new)
    );
    private final SimpleWeightedRandomList<IntProvider> distribution;
    private final int minValue;
    private final int maxValue;

    public WeightedListInt(SimpleWeightedRandomList<IntProvider> param0) {
        this.distribution = param0;
        List<WeightedEntry.Wrapper<IntProvider>> var0 = param0.unwrap();
        int var1 = Integer.MAX_VALUE;
        int var2 = Integer.MIN_VALUE;

        for(WeightedEntry.Wrapper<IntProvider> var3 : var0) {
            int var4 = var3.getData().getMinValue();
            int var5 = var3.getData().getMaxValue();
            var1 = Math.min(var1, var4);
            var2 = Math.max(var2, var5);
        }

        this.minValue = var1;
        this.maxValue = var2;
    }

    @Override
    public int sample(RandomSource param0) {
        return this.distribution.getRandomValue(param0).orElseThrow(IllegalStateException::new).sample(param0);
    }

    @Override
    public int getMinValue() {
        return this.minValue;
    }

    @Override
    public int getMaxValue() {
        return this.maxValue;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.WEIGHTED_LIST;
    }
}
