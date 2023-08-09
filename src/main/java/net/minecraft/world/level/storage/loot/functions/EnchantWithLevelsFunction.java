package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class EnchantWithLevelsFunction extends LootItemConditionalFunction {
    public static final Codec<EnchantWithLevelsFunction> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(
                    param0.group(
                        NumberProviders.CODEC.fieldOf("levels").forGetter(param0x -> param0x.levels),
                        Codec.BOOL.fieldOf("treasure").orElse(false).forGetter(param0x -> param0x.treasure)
                    )
                )
                .apply(param0, EnchantWithLevelsFunction::new)
    );
    private final NumberProvider levels;
    private final boolean treasure;

    EnchantWithLevelsFunction(List<LootItemCondition> param0, NumberProvider param1, boolean param2) {
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
}
