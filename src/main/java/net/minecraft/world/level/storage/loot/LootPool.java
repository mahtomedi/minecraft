package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.apache.commons.lang3.mutable.MutableInt;

public class LootPool {
    public static final Codec<LootPool> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    LootPoolEntries.CODEC.listOf().fieldOf("entries").forGetter(param0x -> param0x.entries),
                    ExtraCodecs.strictOptionalField(LootItemConditions.CODEC.listOf(), "conditions", List.of()).forGetter(param0x -> param0x.conditions),
                    ExtraCodecs.strictOptionalField(LootItemFunctions.CODEC.listOf(), "functions", List.of()).forGetter(param0x -> param0x.functions),
                    NumberProviders.CODEC.fieldOf("rolls").forGetter(param0x -> param0x.rolls),
                    NumberProviders.CODEC.fieldOf("bonus_rolls").orElse(ConstantValue.exactly(0.0F)).forGetter(param0x -> param0x.bonusRolls)
                )
                .apply(param0, LootPool::new)
    );
    private final List<LootPoolEntryContainer> entries;
    private final List<LootItemCondition> conditions;
    private final Predicate<LootContext> compositeCondition;
    private final List<LootItemFunction> functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    private final NumberProvider rolls;
    private final NumberProvider bonusRolls;

    LootPool(List<LootPoolEntryContainer> param0, List<LootItemCondition> param1, List<LootItemFunction> param2, NumberProvider param3, NumberProvider param4) {
        this.entries = param0;
        this.conditions = param1;
        this.compositeCondition = LootItemConditions.andConditions(param1);
        this.functions = param2;
        this.compositeFunction = LootItemFunctions.compose(param2);
        this.rolls = param3;
        this.bonusRolls = param4;
    }

    private void addRandomItem(Consumer<ItemStack> param0, LootContext param1) {
        RandomSource var0 = param1.getRandom();
        List<LootPoolEntry> var1 = Lists.newArrayList();
        MutableInt var2 = new MutableInt();

        for(LootPoolEntryContainer var3 : this.entries) {
            var3.expand(param1, param3 -> {
                int var0x = param3.getWeight(param1.getLuck());
                if (var0x > 0) {
                    var1.add(param3);
                    var2.add(var0x);
                }

            });
        }

        int var4 = var1.size();
        if (var2.intValue() != 0 && var4 != 0) {
            if (var4 == 1) {
                var1.get(0).createItemStack(param0, param1);
            } else {
                int var5 = var0.nextInt(var2.intValue());

                for(LootPoolEntry var6 : var1) {
                    var5 -= var6.getWeight(param1.getLuck());
                    if (var5 < 0) {
                        var6.createItemStack(param0, param1);
                        return;
                    }
                }

            }
        }
    }

    public void addRandomItems(Consumer<ItemStack> param0, LootContext param1) {
        if (this.compositeCondition.test(param1)) {
            Consumer<ItemStack> var0 = LootItemFunction.decorate(this.compositeFunction, param0, param1);
            int var1 = this.rolls.getInt(param1) + Mth.floor(this.bonusRolls.getFloat(param1) * param1.getLuck());

            for(int var2 = 0; var2 < var1; ++var2) {
                this.addRandomItem(var0, param1);
            }

        }
    }

    public void validate(ValidationContext param0) {
        for(int var0 = 0; var0 < this.conditions.size(); ++var0) {
            this.conditions.get(var0).validate(param0.forChild(".condition[" + var0 + "]"));
        }

        for(int var1 = 0; var1 < this.functions.size(); ++var1) {
            this.functions.get(var1).validate(param0.forChild(".functions[" + var1 + "]"));
        }

        for(int var2 = 0; var2 < this.entries.size(); ++var2) {
            this.entries.get(var2).validate(param0.forChild(".entries[" + var2 + "]"));
        }

        this.rolls.validate(param0.forChild(".rolls"));
        this.bonusRolls.validate(param0.forChild(".bonusRolls"));
    }

    public static LootPool.Builder lootPool() {
        return new LootPool.Builder();
    }

    public static class Builder implements FunctionUserBuilder<LootPool.Builder>, ConditionUserBuilder<LootPool.Builder> {
        private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();
        private final ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();
        private final ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();
        private NumberProvider rolls = ConstantValue.exactly(1.0F);
        private NumberProvider bonusRolls = ConstantValue.exactly(0.0F);

        public LootPool.Builder setRolls(NumberProvider param0) {
            this.rolls = param0;
            return this;
        }

        public LootPool.Builder unwrap() {
            return this;
        }

        public LootPool.Builder setBonusRolls(NumberProvider param0) {
            this.bonusRolls = param0;
            return this;
        }

        public LootPool.Builder add(LootPoolEntryContainer.Builder<?> param0) {
            this.entries.add(param0.build());
            return this;
        }

        public LootPool.Builder when(LootItemCondition.Builder param0) {
            this.conditions.add(param0.build());
            return this;
        }

        public LootPool.Builder apply(LootItemFunction.Builder param0) {
            this.functions.add(param0.build());
            return this;
        }

        public LootPool build() {
            return new LootPool(this.entries.build(), this.conditions.build(), this.functions.build(), this.rolls, this.bonusRolls);
        }
    }
}
