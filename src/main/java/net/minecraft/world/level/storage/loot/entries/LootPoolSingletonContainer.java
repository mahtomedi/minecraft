package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products.P4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootPoolSingletonContainer extends LootPoolEntryContainer {
    public static final int DEFAULT_WEIGHT = 1;
    public static final int DEFAULT_QUALITY = 0;
    protected final int weight;
    protected final int quality;
    protected final List<LootItemFunction> functions;
    final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    private final LootPoolEntry entry = new LootPoolSingletonContainer.EntryBase() {
        @Override
        public void createItemStack(Consumer<ItemStack> param0, LootContext param1) {
            LootPoolSingletonContainer.this.createItemStack(
                LootItemFunction.decorate(LootPoolSingletonContainer.this.compositeFunction, param0, param1), param1
            );
        }
    };

    protected LootPoolSingletonContainer(int param0, int param1, List<LootItemCondition> param2, List<LootItemFunction> param3) {
        super(param2);
        this.weight = param0;
        this.quality = param1;
        this.functions = param3;
        this.compositeFunction = LootItemFunctions.compose(param3);
    }

    protected static <T extends LootPoolSingletonContainer> P4<Mu<T>, Integer, Integer, List<LootItemCondition>, List<LootItemFunction>> singletonFields(
        Instance<T> param0
    ) {
        return param0.group(
                ExtraCodecs.strictOptionalField(Codec.INT, "weight", 1).forGetter(param0x -> param0x.weight),
                ExtraCodecs.strictOptionalField(Codec.INT, "quality", 0).forGetter(param0x -> param0x.quality)
            )
            .and(commonFields(param0).t1())
            .and(ExtraCodecs.strictOptionalField(LootItemFunctions.CODEC.listOf(), "functions", List.of()).forGetter(param0x -> param0x.functions));
    }

    @Override
    public void validate(ValidationContext param0) {
        super.validate(param0);

        for(int var0 = 0; var0 < this.functions.size(); ++var0) {
            this.functions.get(var0).validate(param0.forChild(".functions[" + var0 + "]"));
        }

    }

    protected abstract void createItemStack(Consumer<ItemStack> var1, LootContext var2);

    @Override
    public boolean expand(LootContext param0, Consumer<LootPoolEntry> param1) {
        if (this.canRun(param0)) {
            param1.accept(this.entry);
            return true;
        } else {
            return false;
        }
    }

    public static LootPoolSingletonContainer.Builder<?> simpleBuilder(LootPoolSingletonContainer.EntryConstructor param0) {
        return new LootPoolSingletonContainer.DummyBuilder(param0);
    }

    public abstract static class Builder<T extends LootPoolSingletonContainer.Builder<T>>
        extends LootPoolEntryContainer.Builder<T>
        implements FunctionUserBuilder<T> {
        protected int weight = 1;
        protected int quality = 0;
        private final ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();

        public T apply(LootItemFunction.Builder param0) {
            this.functions.add(param0.build());
            return this.getThis();
        }

        protected List<LootItemFunction> getFunctions() {
            return this.functions.build();
        }

        public T setWeight(int param0) {
            this.weight = param0;
            return this.getThis();
        }

        public T setQuality(int param0) {
            this.quality = param0;
            return this.getThis();
        }
    }

    static class DummyBuilder extends LootPoolSingletonContainer.Builder<LootPoolSingletonContainer.DummyBuilder> {
        private final LootPoolSingletonContainer.EntryConstructor constructor;

        public DummyBuilder(LootPoolSingletonContainer.EntryConstructor param0) {
            this.constructor = param0;
        }

        protected LootPoolSingletonContainer.DummyBuilder getThis() {
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return this.constructor.build(this.weight, this.quality, this.getConditions(), this.getFunctions());
        }
    }

    protected abstract class EntryBase implements LootPoolEntry {
        @Override
        public int getWeight(float param0) {
            return Math.max(Mth.floor((float)LootPoolSingletonContainer.this.weight + (float)LootPoolSingletonContainer.this.quality * param0), 0);
        }
    }

    @FunctionalInterface
    protected interface EntryConstructor {
        LootPoolSingletonContainer build(int var1, int var2, List<LootItemCondition> var3, List<LootItemFunction> var4);
    }
}
