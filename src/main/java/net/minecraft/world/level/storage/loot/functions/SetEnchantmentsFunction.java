package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetEnchantmentsFunction extends LootItemConditionalFunction {
    private final Map<Enchantment, NumberProvider> enchantments;
    private final boolean add;

    private SetEnchantmentsFunction(LootItemCondition[] param0, Map<Enchantment, NumberProvider> param1, boolean param2) {
        super(param0);
        this.enchantments = ImmutableMap.copyOf(param1);
        this.add = param2;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_ENCHANTMENTS;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.enchantments.values().stream().flatMap(param0 -> param0.getReferencedContextParams().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        Object2IntMap<Enchantment> var0 = new Object2IntOpenHashMap<>();
        this.enchantments.forEach((param2, param3) -> var0.put(param2, param3.getInt(param1)));
        if (param0.getItem() == Items.BOOK) {
            ItemStack var1 = new ItemStack(Items.ENCHANTED_BOOK);
            var0.forEach((param1x, param2) -> EnchantedBookItem.addEnchantment(var1, new EnchantmentInstance(param1x, param2)));
            return var1;
        } else {
            Map<Enchantment, Integer> var2 = EnchantmentHelper.getEnchantments(param0);
            if (this.add) {
                var0.forEach((param1x, param2) -> updateEnchantment(var2, param1x, Math.max(var2.getOrDefault(param1x, 0) + param2, 0)));
            } else {
                var0.forEach((param1x, param2) -> updateEnchantment(var2, param1x, Math.max(param2, 0)));
            }

            EnchantmentHelper.setEnchantments(var2, param0);
            return param0;
        }
    }

    private static void updateEnchantment(Map<Enchantment, Integer> param0, Enchantment param1, int param2) {
        if (param2 == 0) {
            param0.remove(param1);
        } else {
            param0.put(param1, param2);
        }

    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetEnchantmentsFunction> {
        public void serialize(JsonObject param0, SetEnchantmentsFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            JsonObject var0 = new JsonObject();
            param1.enchantments.forEach((param2x, param3) -> {
                ResourceLocation var0x = Registry.ENCHANTMENT.getKey(param2x);
                if (var0x == null) {
                    throw new IllegalArgumentException("Don't know how to serialize enchantment " + param2x);
                } else {
                    var0.add(var0x.toString(), param2.serialize(param3));
                }
            });
            param0.add("enchantments", var0);
            param0.addProperty("add", param1.add);
        }

        public SetEnchantmentsFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            Map<Enchantment, NumberProvider> var0 = Maps.newHashMap();
            if (param0.has("enchantments")) {
                JsonObject var1 = GsonHelper.getAsJsonObject(param0, "enchantments");

                for(Entry<String, JsonElement> var2 : var1.entrySet()) {
                    String var3 = var2.getKey();
                    JsonElement var4 = var2.getValue();
                    Enchantment var5 = Registry.ENCHANTMENT
                        .getOptional(new ResourceLocation(var3))
                        .orElseThrow(() -> new JsonSyntaxException("Unknown enchantment '" + var3 + "'"));
                    NumberProvider var6 = param1.deserialize(var4, NumberProvider.class);
                    var0.put(var5, var6);
                }
            }

            boolean var7 = GsonHelper.getAsBoolean(param0, "add", false);
            return new SetEnchantmentsFunction(param2, var0, var7);
        }
    }
}
