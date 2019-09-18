package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.commons.lang3.ArrayUtils;

public abstract class LootItemConditionalFunction implements LootItemFunction {
    protected final LootItemCondition[] predicates;
    private final Predicate<LootContext> compositePredicates;

    protected LootItemConditionalFunction(LootItemCondition[] param0) {
        this.predicates = param0;
        this.compositePredicates = LootItemConditions.andConditions(param0);
    }

    public final ItemStack apply(ItemStack param0, LootContext param1) {
        return this.compositePredicates.test(param1) ? this.run(param0, param1) : param0;
    }

    protected abstract ItemStack run(ItemStack var1, LootContext var2);

    @Override
    public void validate(ValidationContext param0) {
        LootItemFunction.super.validate(param0);

        for(int var0 = 0; var0 < this.predicates.length; ++var0) {
            this.predicates[var0].validate(param0.forChild(".conditions[" + var0 + "]"));
        }

    }

    protected static LootItemConditionalFunction.Builder<?> simpleBuilder(Function<LootItemCondition[], LootItemFunction> param0) {
        return new LootItemConditionalFunction.DummyBuilder(param0);
    }

    public abstract static class Builder<T extends LootItemConditionalFunction.Builder<T>> implements LootItemFunction.Builder, ConditionUserBuilder<T> {
        private final List<LootItemCondition> conditions = Lists.newArrayList();

        public T when(LootItemCondition.Builder param0) {
            this.conditions.add(param0.build());
            return this.getThis();
        }

        public final T unwrap() {
            return this.getThis();
        }

        protected abstract T getThis();

        protected LootItemCondition[] getConditions() {
            return this.conditions.toArray(new LootItemCondition[0]);
        }
    }

    static final class DummyBuilder extends LootItemConditionalFunction.Builder<LootItemConditionalFunction.DummyBuilder> {
        private final Function<LootItemCondition[], LootItemFunction> constructor;

        public DummyBuilder(Function<LootItemCondition[], LootItemFunction> param0) {
            this.constructor = param0;
        }

        protected LootItemConditionalFunction.DummyBuilder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return this.constructor.apply(this.getConditions());
        }
    }

    public abstract static class Serializer<T extends LootItemConditionalFunction> extends LootItemFunction.Serializer<T> {
        public Serializer(ResourceLocation param0, Class<T> param1) {
            super(param0, param1);
        }

        public void serialize(JsonObject param0, T param1, JsonSerializationContext param2) {
            if (!ArrayUtils.isEmpty((Object[])param1.predicates)) {
                param0.add("conditions", param2.serialize(param1.predicates));
            }

        }

        public final T deserialize(JsonObject param0, JsonDeserializationContext param1) {
            LootItemCondition[] var0 = GsonHelper.getAsObject(param0, "conditions", new LootItemCondition[0], param1, LootItemCondition[].class);
            return this.deserialize(param0, param1, var0);
        }

        public abstract T deserialize(JsonObject var1, JsonDeserializationContext var2, LootItemCondition[] var3);
    }
}
