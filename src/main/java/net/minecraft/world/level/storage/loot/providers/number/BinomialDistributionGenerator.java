package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public record BinomialDistributionGenerator(NumberProvider n, NumberProvider p) implements NumberProvider {
    public static final Codec<BinomialDistributionGenerator> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    NumberProviders.CODEC.fieldOf("n").forGetter(BinomialDistributionGenerator::n),
                    NumberProviders.CODEC.fieldOf("p").forGetter(BinomialDistributionGenerator::p)
                )
                .apply(param0, BinomialDistributionGenerator::new)
    );

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.BINOMIAL;
    }

    @Override
    public int getInt(LootContext param0) {
        int var0 = this.n.getInt(param0);
        float var1 = this.p.getFloat(param0);
        RandomSource var2 = param0.getRandom();
        int var3 = 0;

        for(int var4 = 0; var4 < var0; ++var4) {
            if (var2.nextFloat() < var1) {
                ++var3;
            }
        }

        return var3;
    }

    @Override
    public float getFloat(LootContext param0) {
        return (float)this.getInt(param0);
    }

    public static BinomialDistributionGenerator binomial(int param0, float param1) {
        return new BinomialDistributionGenerator(ConstantValue.exactly((float)param0), ConstantValue.exactly(param1));
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(this.n.getReferencedContextParams(), this.p.getReferencedContextParams());
    }
}
