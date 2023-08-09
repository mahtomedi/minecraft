package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class LootingEnchantFunction extends LootItemConditionalFunction {
    public static final int NO_LIMIT = 0;
    public static final Codec<LootingEnchantFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(
                    param0.group(
                        NumberProviders.CODEC.fieldOf("count").forGetter(param0x -> param0x.value),
                        ExtraCodecs.strictOptionalField(Codec.INT, "limit", 0).forGetter(param0x -> param0x.limit)
                    )
                )
                .apply(param0, LootingEnchantFunction::new)
    );
    private final NumberProvider value;
    private final int limit;

    LootingEnchantFunction(List<LootItemCondition> param0, NumberProvider param1, int param2) {
        super(param0);
        this.value = param1;
        this.limit = param2;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.LOOTING_ENCHANT;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(ImmutableSet.of(LootContextParams.KILLER_ENTITY), this.value.getReferencedContextParams());
    }

    private boolean hasLimit() {
        return this.limit > 0;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        Entity var0 = param1.getParamOrNull(LootContextParams.KILLER_ENTITY);
        if (var0 instanceof LivingEntity) {
            int var1 = EnchantmentHelper.getMobLooting((LivingEntity)var0);
            if (var1 == 0) {
                return param0;
            }

            float var2 = (float)var1 * this.value.getFloat(param1);
            param0.grow(Math.round(var2));
            if (this.hasLimit() && param0.getCount() > this.limit) {
                param0.setCount(this.limit);
            }
        }

        return param0;
    }

    public static LootingEnchantFunction.Builder lootingMultiplier(NumberProvider param0) {
        return new LootingEnchantFunction.Builder(param0);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<LootingEnchantFunction.Builder> {
        private final NumberProvider count;
        private int limit = 0;

        public Builder(NumberProvider param0) {
            this.count = param0;
        }

        protected LootingEnchantFunction.Builder getThis() {
            return this;
        }

        public LootingEnchantFunction.Builder setLimit(int param0) {
            this.limit = param0;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootingEnchantFunction(this.getConditions(), this.count, this.limit);
        }
    }
}
