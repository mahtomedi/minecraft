package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class BonusLevelTableCondition implements LootItemCondition {
    private final Enchantment enchantment;
    private final float[] values;

    private BonusLevelTableCondition(Enchantment param0, float[] param1) {
        this.enchantment = param0;
        this.values = param1;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    public boolean test(LootContext param0) {
        ItemStack var0 = param0.getParamOrNull(LootContextParams.TOOL);
        int var1 = var0 != null ? EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, var0) : 0;
        float var2 = this.values[Math.min(var1, this.values.length - 1)];
        return param0.getRandom().nextFloat() < var2;
    }

    public static LootItemCondition.Builder bonusLevelFlatChance(Enchantment param0, float... param1) {
        return () -> new BonusLevelTableCondition(param0, param1);
    }

    public static class Serializer extends LootItemCondition.Serializer<BonusLevelTableCondition> {
        public Serializer() {
            super(new ResourceLocation("table_bonus"), BonusLevelTableCondition.class);
        }

        public void serialize(JsonObject param0, BonusLevelTableCondition param1, JsonSerializationContext param2) {
            param0.addProperty("enchantment", Registry.ENCHANTMENT.getKey(param1.enchantment).toString());
            param0.add("chances", param2.serialize(param1.values));
        }

        public BonusLevelTableCondition deserialize(JsonObject param0, JsonDeserializationContext param1) {
            ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "enchantment"));
            Enchantment var1 = Registry.ENCHANTMENT.getOptional(var0).orElseThrow(() -> new JsonParseException("Invalid enchantment id: " + var0));
            float[] var2 = GsonHelper.getAsObject(param0, "chances", param1, float[].class);
            return new BonusLevelTableCondition(var1, var2);
        }
    }
}
