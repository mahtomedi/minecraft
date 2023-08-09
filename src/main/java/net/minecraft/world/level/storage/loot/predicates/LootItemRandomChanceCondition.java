package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;

public record LootItemRandomChanceCondition(float probability) implements LootItemCondition {
    public static final Codec<LootItemRandomChanceCondition> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(Codec.FLOAT.fieldOf("chance").forGetter(LootItemRandomChanceCondition::probability))
                .apply(param0, LootItemRandomChanceCondition::new)
    );

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.RANDOM_CHANCE;
    }

    public boolean test(LootContext param0) {
        return param0.getRandom().nextFloat() < this.probability;
    }

    public static LootItemCondition.Builder randomChance(float param0) {
        return () -> new LootItemRandomChanceCondition(param0);
    }
}
