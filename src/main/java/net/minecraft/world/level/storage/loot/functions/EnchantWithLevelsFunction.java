package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomIntGenerator;
import net.minecraft.world.level.storage.loot.RandomIntGenerators;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EnchantWithLevelsFunction extends LootItemConditionalFunction {
    private final RandomIntGenerator levels;
    private final boolean treasure;

    private EnchantWithLevelsFunction(LootItemCondition[] param0, RandomIntGenerator param1, boolean param2) {
        super(param0);
        this.levels = param1;
        this.treasure = param2;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        Random var0 = param1.getRandom();
        return EnchantmentHelper.enchantItem(var0, param0, this.levels.getInt(var0), this.treasure);
    }

    public static EnchantWithLevelsFunction.Builder enchantWithLevels(RandomIntGenerator param0) {
        return new EnchantWithLevelsFunction.Builder(param0);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<EnchantWithLevelsFunction.Builder> {
        private final RandomIntGenerator levels;
        private boolean treasure;

        public Builder(RandomIntGenerator param0) {
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
        public Serializer() {
            super(new ResourceLocation("enchant_with_levels"), EnchantWithLevelsFunction.class);
        }

        public void serialize(JsonObject param0, EnchantWithLevelsFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.add("levels", RandomIntGenerators.serialize(param1.levels, param2));
            param0.addProperty("treasure", param1.treasure);
        }

        public EnchantWithLevelsFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            RandomIntGenerator var0 = RandomIntGenerators.deserialize(param0.get("levels"), param1);
            boolean var1 = GsonHelper.getAsBoolean(param0, "treasure", false);
            return new EnchantWithLevelsFunction(param2, var0, var1);
        }
    }
}
