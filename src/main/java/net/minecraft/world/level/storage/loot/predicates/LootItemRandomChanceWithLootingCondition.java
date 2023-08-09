package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record LootItemRandomChanceWithLootingCondition(float percent, float lootingMultiplier) implements LootItemCondition {
    public static final Codec<LootItemRandomChanceWithLootingCondition> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.FLOAT.fieldOf("chance").forGetter(LootItemRandomChanceWithLootingCondition::percent),
                    Codec.FLOAT.fieldOf("looting_multiplier").forGetter(LootItemRandomChanceWithLootingCondition::lootingMultiplier)
                )
                .apply(param0, LootItemRandomChanceWithLootingCondition::new)
    );

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.RANDOM_CHANCE_WITH_LOOTING;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.KILLER_ENTITY);
    }

    public boolean test(LootContext param0) {
        Entity var0 = param0.getParamOrNull(LootContextParams.KILLER_ENTITY);
        int var1 = 0;
        if (var0 instanceof LivingEntity) {
            var1 = EnchantmentHelper.getMobLooting((LivingEntity)var0);
        }

        return param0.getRandom().nextFloat() < this.percent + (float)var1 * this.lootingMultiplier;
    }

    public static LootItemCondition.Builder randomChanceAndLootingBoost(float param0, float param1) {
        return () -> new LootItemRandomChanceWithLootingCondition(param0, param1);
    }
}
