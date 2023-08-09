package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
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
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetEnchantmentsFunction extends LootItemConditionalFunction {
    public static final Codec<SetEnchantmentsFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(
                    param0.group(
                        ExtraCodecs.strictOptionalField(
                                Codec.unboundedMap(BuiltInRegistries.ENCHANTMENT.holderByNameCodec(), NumberProviders.CODEC), "enchantments", Map.of()
                            )
                            .forGetter(param0x -> param0x.enchantments),
                        Codec.BOOL.fieldOf("add").orElse(false).forGetter(param0x -> param0x.add)
                    )
                )
                .apply(param0, SetEnchantmentsFunction::new)
    );
    private final Map<Holder<Enchantment>, NumberProvider> enchantments;
    private final boolean add;

    SetEnchantmentsFunction(List<LootItemCondition> param0, Map<Holder<Enchantment>, NumberProvider> param1, boolean param2) {
        super(param0);
        this.enchantments = Map.copyOf(param1);
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
        this.enchantments.forEach((param2, param3) -> var0.put(param2.value(), param3.getInt(param1)));
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

    public static class Builder extends LootItemConditionalFunction.Builder<SetEnchantmentsFunction.Builder> {
        private final ImmutableMap.Builder<Holder<Enchantment>, NumberProvider> enchantments = ImmutableMap.builder();
        private final boolean add;

        public Builder() {
            this(false);
        }

        public Builder(boolean param0) {
            this.add = param0;
        }

        protected SetEnchantmentsFunction.Builder getThis() {
            return this;
        }

        public SetEnchantmentsFunction.Builder withEnchantment(Enchantment param0, NumberProvider param1) {
            this.enchantments.put(param0.builtInRegistryHolder(), param1);
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetEnchantmentsFunction(this.getConditions(), this.enchantments.build(), this.add);
        }
    }
}
