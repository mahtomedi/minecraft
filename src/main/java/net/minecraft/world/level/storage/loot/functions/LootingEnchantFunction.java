package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootingEnchantFunction extends LootItemConditionalFunction {
    private final RandomValueBounds value;
    private final int limit;

    private LootingEnchantFunction(LootItemCondition[] param0, RandomValueBounds param1, int param2) {
        super(param0);
        this.value = param1;
        this.limit = param2;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.KILLER_ENTITY);
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

            float var2 = (float)var1 * this.value.getFloat(param1.getRandom());
            param0.grow(Math.round(var2));
            if (this.hasLimit() && param0.getCount() > this.limit) {
                param0.setCount(this.limit);
            }
        }

        return param0;
    }

    public static LootingEnchantFunction.Builder lootingMultiplier(RandomValueBounds param0) {
        return new LootingEnchantFunction.Builder(param0);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<LootingEnchantFunction.Builder> {
        private final RandomValueBounds count;
        private int limit = 0;

        public Builder(RandomValueBounds param0) {
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

    public static class Serializer extends LootItemConditionalFunction.Serializer<LootingEnchantFunction> {
        protected Serializer() {
            super(new ResourceLocation("looting_enchant"), LootingEnchantFunction.class);
        }

        public void serialize(JsonObject param0, LootingEnchantFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.add("count", param2.serialize(param1.value));
            if (param1.hasLimit()) {
                param0.add("limit", param2.serialize(param1.limit));
            }

        }

        public LootingEnchantFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            int var0 = GsonHelper.getAsInt(param0, "limit", 0);
            return new LootingEnchantFunction(param2, GsonHelper.getAsObject(param0, "count", param1, RandomValueBounds.class), var0);
        }
    }
}
