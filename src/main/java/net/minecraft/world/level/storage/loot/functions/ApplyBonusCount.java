package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import java.util.Random;
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
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyBonusCount extends LootItemConditionalFunction {
    static final Map<ResourceLocation, ApplyBonusCount.FormulaDeserializer> FORMULAS = Maps.newHashMap();
    final Enchantment enchantment;
    final ApplyBonusCount.Formula formula;

    ApplyBonusCount(LootItemCondition[] param0, Enchantment param1, ApplyBonusCount.Formula param2) {
        super(param0);
        this.enchantment = param1;
        this.formula = param2;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.APPLY_BONUS;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        ItemStack var0 = param1.getParamOrNull(LootContextParams.TOOL);
        if (var0 != null) {
            int var1 = EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, var0);
            int var2 = this.formula.calculateNewCount(param1.getRandom(), param0.getCount(), var1);
            param0.setCount(var2);
        }

        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> addBonusBinomialDistributionCount(Enchantment param0, float param1, int param2) {
        return simpleBuilder(param3 -> new ApplyBonusCount(param3, param0, new ApplyBonusCount.BinomialWithBonusCount(param2, param1)));
    }

    public static LootItemConditionalFunction.Builder<?> addOreBonusCount(Enchantment param0) {
        return simpleBuilder(param1 -> new ApplyBonusCount(param1, param0, new ApplyBonusCount.OreDrops()));
    }

    public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Enchantment param0) {
        return simpleBuilder(param1 -> new ApplyBonusCount(param1, param0, new ApplyBonusCount.UniformBonusCount(1)));
    }

    public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Enchantment param0, int param1) {
        return simpleBuilder(param2 -> new ApplyBonusCount(param2, param0, new ApplyBonusCount.UniformBonusCount(param1)));
    }

    static {
        FORMULAS.put(ApplyBonusCount.BinomialWithBonusCount.TYPE, ApplyBonusCount.BinomialWithBonusCount::deserialize);
        FORMULAS.put(ApplyBonusCount.OreDrops.TYPE, ApplyBonusCount.OreDrops::deserialize);
        FORMULAS.put(ApplyBonusCount.UniformBonusCount.TYPE, ApplyBonusCount.UniformBonusCount::deserialize);
    }

    static final class BinomialWithBonusCount implements ApplyBonusCount.Formula {
        public static final ResourceLocation TYPE = new ResourceLocation("binomial_with_bonus_count");
        private final int extraRounds;
        private final float probability;

        public BinomialWithBonusCount(int param0, float param1) {
            this.extraRounds = param0;
            this.probability = param1;
        }

        @Override
        public int calculateNewCount(Random param0, int param1, int param2) {
            for(int var0 = 0; var0 < param2 + this.extraRounds; ++var0) {
                if (param0.nextFloat() < this.probability) {
                    ++param1;
                }
            }

            return param1;
        }

        @Override
        public void serializeParams(JsonObject param0, JsonSerializationContext param1) {
            param0.addProperty("extra", this.extraRounds);
            param0.addProperty("probability", this.probability);
        }

        public static ApplyBonusCount.Formula deserialize(JsonObject param0, JsonDeserializationContext param1) {
            int var0 = GsonHelper.getAsInt(param0, "extra");
            float var1 = GsonHelper.getAsFloat(param0, "probability");
            return new ApplyBonusCount.BinomialWithBonusCount(var0, var1);
        }

        @Override
        public ResourceLocation getType() {
            return TYPE;
        }
    }

    interface Formula {
        int calculateNewCount(Random var1, int var2, int var3);

        void serializeParams(JsonObject var1, JsonSerializationContext var2);

        ResourceLocation getType();
    }

    interface FormulaDeserializer {
        ApplyBonusCount.Formula deserialize(JsonObject var1, JsonDeserializationContext var2);
    }

    static final class OreDrops implements ApplyBonusCount.Formula {
        public static final ResourceLocation TYPE = new ResourceLocation("ore_drops");

        @Override
        public int calculateNewCount(Random param0, int param1, int param2) {
            if (param2 > 0) {
                int var0 = param0.nextInt(param2 + 2) - 1;
                if (var0 < 0) {
                    var0 = 0;
                }

                return param1 * (var0 + 1);
            } else {
                return param1;
            }
        }

        @Override
        public void serializeParams(JsonObject param0, JsonSerializationContext param1) {
        }

        public static ApplyBonusCount.Formula deserialize(JsonObject param0, JsonDeserializationContext param1) {
            return new ApplyBonusCount.OreDrops();
        }

        @Override
        public ResourceLocation getType() {
            return TYPE;
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<ApplyBonusCount> {
        public void serialize(JsonObject param0, ApplyBonusCount param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.addProperty("enchantment", Registry.ENCHANTMENT.getKey(param1.enchantment).toString());
            param0.addProperty("formula", param1.formula.getType().toString());
            JsonObject var0 = new JsonObject();
            param1.formula.serializeParams(var0, param2);
            if (var0.size() > 0) {
                param0.add("parameters", var0);
            }

        }

        public ApplyBonusCount deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "enchantment"));
            Enchantment var1 = Registry.ENCHANTMENT.getOptional(var0).orElseThrow(() -> new JsonParseException("Invalid enchantment id: " + var0));
            ResourceLocation var2 = new ResourceLocation(GsonHelper.getAsString(param0, "formula"));
            ApplyBonusCount.FormulaDeserializer var3 = ApplyBonusCount.FORMULAS.get(var2);
            if (var3 == null) {
                throw new JsonParseException("Invalid formula id: " + var2);
            } else {
                ApplyBonusCount.Formula var4;
                if (param0.has("parameters")) {
                    var4 = var3.deserialize(GsonHelper.getAsJsonObject(param0, "parameters"), param1);
                } else {
                    var4 = var3.deserialize(new JsonObject(), param1);
                }

                return new ApplyBonusCount(param2, var1, var4);
            }
        }
    }

    static final class UniformBonusCount implements ApplyBonusCount.Formula {
        public static final ResourceLocation TYPE = new ResourceLocation("uniform_bonus_count");
        private final int bonusMultiplier;

        public UniformBonusCount(int param0) {
            this.bonusMultiplier = param0;
        }

        @Override
        public int calculateNewCount(Random param0, int param1, int param2) {
            return param1 + param0.nextInt(this.bonusMultiplier * param2 + 1);
        }

        @Override
        public void serializeParams(JsonObject param0, JsonSerializationContext param1) {
            param0.addProperty("bonusMultiplier", this.bonusMultiplier);
        }

        public static ApplyBonusCount.Formula deserialize(JsonObject param0, JsonDeserializationContext param1) {
            int var0 = GsonHelper.getAsInt(param0, "bonusMultiplier");
            return new ApplyBonusCount.UniformBonusCount(var0);
        }

        @Override
        public ResourceLocation getType() {
            return TYPE;
        }
    }
}
