package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class EnchantRandomlyFunction extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Codec<HolderSet<Enchantment>> ENCHANTMENT_SET_CODEC = BuiltInRegistries.ENCHANTMENT
        .holderByNameCodec()
        .listOf()
        .xmap(HolderSet::direct, param0 -> param0.stream().toList());
    public static final Codec<EnchantRandomlyFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(ExtraCodecs.strictOptionalField(ENCHANTMENT_SET_CODEC, "enchantments").forGetter(param0x -> param0x.enchantments))
                .apply(param0, EnchantRandomlyFunction::new)
    );
    private final Optional<HolderSet<Enchantment>> enchantments;

    EnchantRandomlyFunction(List<LootItemCondition> param0, Optional<HolderSet<Enchantment>> param1) {
        super(param0);
        this.enchantments = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.ENCHANT_RANDOMLY;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        RandomSource var0 = param1.getRandom();
        Optional<Holder<Enchantment>> var1 = this.enchantments
            .<Holder.Reference<Enchantment>>flatMap(param1x -> param1x.getRandomElement(var0))
            .or(
                () -> {
                    boolean var0x = param0.is(Items.BOOK);
                    List<Holder.Reference<Enchantment>> var1x = BuiltInRegistries.ENCHANTMENT
                        .holders()
                        .filter(param0x -> param0x.value().isDiscoverable())
                        .filter(param2 -> var0x || param2.value().canEnchant(param0))
                        .toList();
                    return Util.getRandomSafe(var1x, var0);
                }
            );
        if (var1.isEmpty()) {
            LOGGER.warn("Couldn't find a compatible enchantment for {}", param0);
            return param0;
        } else {
            return enchantItem(param0, var1.get().value(), var0);
        }
    }

    private static ItemStack enchantItem(ItemStack param0, Enchantment param1, RandomSource param2) {
        int var0 = Mth.nextInt(param2, param1.getMinLevel(), param1.getMaxLevel());
        if (param0.is(Items.BOOK)) {
            param0 = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantedBookItem.addEnchantment(param0, new EnchantmentInstance(param1, var0));
        } else {
            param0.enchant(param1, var0);
        }

        return param0;
    }

    public static EnchantRandomlyFunction.Builder randomEnchantment() {
        return new EnchantRandomlyFunction.Builder();
    }

    public static LootItemConditionalFunction.Builder<?> randomApplicableEnchantment() {
        return simpleBuilder(param0 -> new EnchantRandomlyFunction(param0, Optional.empty()));
    }

    public static class Builder extends LootItemConditionalFunction.Builder<EnchantRandomlyFunction.Builder> {
        private final List<Holder<Enchantment>> enchantments = new ArrayList<>();

        protected EnchantRandomlyFunction.Builder getThis() {
            return this;
        }

        public EnchantRandomlyFunction.Builder withEnchantment(Enchantment param0) {
            this.enchantments.add(param0.builtInRegistryHolder());
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new EnchantRandomlyFunction(
                this.getConditions(), this.enchantments.isEmpty() ? Optional.empty() : Optional.of(HolderSet.direct(this.enchantments))
            );
        }
    }
}
