package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record BonusLevelTableCondition(Holder<Enchantment> enchantment, List<Float> values) implements LootItemCondition {
    public static final Codec<BonusLevelTableCondition> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BuiltInRegistries.ENCHANTMENT.holderByNameCodec().fieldOf("enchantment").forGetter(BonusLevelTableCondition::enchantment),
                    Codec.FLOAT.listOf().fieldOf("chances").forGetter(BonusLevelTableCondition::values)
                )
                .apply(param0, BonusLevelTableCondition::new)
    );

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.TABLE_BONUS;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    public boolean test(LootContext param0) {
        ItemStack var0 = param0.getParamOrNull(LootContextParams.TOOL);
        int var1 = var0 != null ? EnchantmentHelper.getItemEnchantmentLevel(this.enchantment.value(), var0) : 0;
        float var2 = this.values.get(Math.min(var1, this.values.size() - 1));
        return param0.getRandom().nextFloat() < var2;
    }

    public static LootItemCondition.Builder bonusLevelFlatChance(Enchantment param0, float... param1) {
        List<Float> var0 = new ArrayList<>(param1.length);

        for(float var1 : param1) {
            var0.add(var1);
        }

        return () -> new BonusLevelTableCondition(param0.builtInRegistryHolder(), var0);
    }
}
