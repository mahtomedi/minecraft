package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public record UniformGenerator(NumberProvider min, NumberProvider max) implements NumberProvider {
    public static final Codec<UniformGenerator> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    NumberProviders.CODEC.fieldOf("min").forGetter(UniformGenerator::min),
                    NumberProviders.CODEC.fieldOf("max").forGetter(UniformGenerator::max)
                )
                .apply(param0, UniformGenerator::new)
    );

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.UNIFORM;
    }

    public static UniformGenerator between(float param0, float param1) {
        return new UniformGenerator(ConstantValue.exactly(param0), ConstantValue.exactly(param1));
    }

    @Override
    public int getInt(LootContext param0) {
        return Mth.nextInt(param0.getRandom(), this.min.getInt(param0), this.max.getInt(param0));
    }

    @Override
    public float getFloat(LootContext param0) {
        return Mth.nextFloat(param0.getRandom(), this.min.getFloat(param0), this.max.getFloat(param0));
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(this.min.getReferencedContextParams(), this.max.getReferencedContextParams());
    }
}
