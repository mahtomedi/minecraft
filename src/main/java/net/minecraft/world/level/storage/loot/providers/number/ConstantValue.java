package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;

public record ConstantValue(float value) implements NumberProvider {
    public static final Codec<ConstantValue> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(Codec.FLOAT.fieldOf("value").forGetter(ConstantValue::value)).apply(param0, ConstantValue::new)
    );
    public static final Codec<ConstantValue> INLINE_CODEC = Codec.FLOAT.xmap(ConstantValue::new, ConstantValue::value);

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.CONSTANT;
    }

    @Override
    public float getFloat(LootContext param0) {
        return this.value;
    }

    public static ConstantValue exactly(float param0) {
        return new ConstantValue(param0);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            return Float.compare(((ConstantValue)param0).value, this.value) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.value != 0.0F ? Float.floatToIntBits(this.value) : 0;
    }
}
