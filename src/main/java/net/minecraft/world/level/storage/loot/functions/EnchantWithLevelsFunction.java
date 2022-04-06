package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class EnchantWithLevelsFunction extends LootItemConditionalFunction {
    final NumberProvider levels;
    final boolean treasure;

    EnchantWithLevelsFunction(LootItemCondition[] param0, NumberProvider param1, boolean param2) {
        super(param0);
        this.levels = param1;
        this.treasure = param2;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.ENCHANT_WITH_LEVELS;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.levels.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        RandomSource var0 = param1.getRandom();
        return EnchantmentHelper.enchantItem(var0, param0, this.levels.getInt(param1), this.treasure);
    }

    public static EnchantWithLevelsFunction.Builder enchantWithLevels(NumberProvider param0) {
        return new EnchantWithLevelsFunction.Builder(param0);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<EnchantWithLevelsFunction.Builder> {
        private final NumberProvider levels;
        private boolean treasure;

        public Builder(NumberProvider param0) {
            this.levels = param0;
        }

        protected EnchantWithLevelsFunction.Builder getThis() {
            return this;
        }

        public EnchantWithLevelsFunction.Builder allowTreasure() {
            this.treasure = true;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new EnchantWithLevelsFunction(this.getConditions(), this.levels, this.treasure);
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<EnchantWithLevelsFunction> {
        public void serialize(JsonObject param0, EnchantWithLevelsFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.add("levels", param2.serialize(param1.levels));
            param0.addProperty("treasure", param1.treasure);
        }

        public EnchantWithLevelsFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            NumberProvider var0 = GsonHelper.getAsObject(param0, "levels", param1, NumberProvider.class);
            boolean var1 = GsonHelper.getAsBoolean(param0, "treasure", false);
            return new EnchantWithLevelsFunction(param2, var0, var1);
        }
    }
}
