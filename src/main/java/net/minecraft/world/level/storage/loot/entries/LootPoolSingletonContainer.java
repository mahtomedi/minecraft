package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.ArrayUtils;

public abstract class LootPoolSingletonContainer extends LootPoolEntryContainer {
    public static final int DEFAULT_WEIGHT = 1;
    public static final int DEFAULT_QUALITY = 0;
    protected final int weight;
    protected final int quality;
    protected final LootItemFunction[] functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    private final LootPoolEntry entry = new LootPoolSingletonContainer.EntryBase() {
        @Override
        public void createItemStack(Consumer<ItemStack> param0, LootContext param1) {
            LootPoolSingletonContainer.this.createItemStack(
                LootItemFunction.decorate(LootPoolSingletonContainer.this.compositeFunction, param0, param1), param1
            );
        }
    };

    protected LootPoolSingletonContainer(int param0, int param1, LootItemCondition[] param2, LootItemFunction[] param3) {
        super(param2);
        this.weight = param0;
        this.quality = param1;
        this.functions = param3;
        this.compositeFunction = LootItemFunctions.compose(param3);
    }

    @Override
    public void validate(ValidationContext param0) {
        super.validate(param0);

        for(int var0 = 0; var0 < this.functions.length; ++var0) {
            this.functions[var0].validate(param0.forChild(".functions[" + var0 + "]"));
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
        private final List<LootItemFunction> functions = Lists.newArrayList();

        public T apply(LootItemFunction.Builder param0) {
            this.functions.add(param0.build());
            return this.getThis();
        }

        protected LootItemFunction[] getFunctions() {
            return this.functions.toArray(new LootItemFunction[0]);
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

    public abstract class EntryBase implements LootPoolEntry {
        protected EntryBase() {
        }

        @Override
        public int getWeight(float param0) {
            return Math.max(Mth.floor((float)LootPoolSingletonContainer.this.weight + (float)LootPoolSingletonContainer.this.quality * param0), 0);
        }
    }

    @FunctionalInterface
    public interface EntryConstructor {
        LootPoolSingletonContainer build(int var1, int var2, LootItemCondition[] var3, LootItemFunction[] var4);
    }

    public abstract static class Serializer<T extends LootPoolSingletonContainer> extends LootPoolEntryContainer.Serializer<T> {
        public void serializeCustom(JsonObject param0, T param1, JsonSerializationContext param2) {
            if (param1.weight != 1) {
                param0.addProperty("weight", param1.weight);
            }

            if (param1.quality != 0) {
                param0.addProperty("quality", param1.quality);
            }

            if (!ArrayUtils.isEmpty((Object[])param1.functions)) {
                param0.add("functions", param2.serialize(param1.functions));
            }

        }

        public final T deserializeCustom(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            int var0 = GsonHelper.getAsInt(param0, "weight", 1);
            int var1 = GsonHelper.getAsInt(param0, "quality", 0);
            LootItemFunction[] var2 = GsonHelper.getAsObject(param0, "functions", new LootItemFunction[0], param1, LootItemFunction[].class);
            return this.deserialize(param0, param1, var0, var1, param2, var2);
        }

        protected abstract T deserialize(
            JsonObject var1, JsonDeserializationContext var2, int var3, int var4, LootItemCondition[] var5, LootItemFunction[] var6
        );
    }
}
