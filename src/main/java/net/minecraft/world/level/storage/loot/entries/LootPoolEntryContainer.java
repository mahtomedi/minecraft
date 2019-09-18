package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public abstract class LootPoolEntryContainer implements ComposableEntryContainer {
    protected final LootItemCondition[] conditions;
    private final Predicate<LootContext> compositeCondition;

    protected LootPoolEntryContainer(LootItemCondition[] param0) {
        this.conditions = param0;
        this.compositeCondition = LootItemConditions.andConditions(param0);
    }

    public void validate(ValidationContext param0) {
        for(int var0 = 0; var0 < this.conditions.length; ++var0) {
            this.conditions[var0].validate(param0.forChild(".condition[" + var0 + "]"));
        }

    }

    protected final boolean canRun(LootContext param0) {
        return this.compositeCondition.test(param0);
    }

    public abstract static class Builder<T extends LootPoolEntryContainer.Builder<T>> implements ConditionUserBuilder<T> {
        private final List<LootItemCondition> conditions = Lists.newArrayList();

        protected abstract T getThis();

        public T when(LootItemCondition.Builder param0) {
            this.conditions.add(param0.build());
            return this.getThis();
        }

        public final T unwrap() {
            return this.getThis();
        }

        protected LootItemCondition[] getConditions() {
            return this.conditions.toArray(new LootItemCondition[0]);
        }

        public AlternativesEntry.Builder otherwise(LootPoolEntryContainer.Builder<?> param0) {
            return new AlternativesEntry.Builder(this, param0);
        }

        public abstract LootPoolEntryContainer build();
    }

    public abstract static class Serializer<T extends LootPoolEntryContainer> {
        private final ResourceLocation name;
        private final Class<T> clazz;

        protected Serializer(ResourceLocation param0, Class<T> param1) {
            this.name = param0;
            this.clazz = param1;
        }

        public ResourceLocation getName() {
            return this.name;
        }

        public Class<T> getContainerClass() {
            return this.clazz;
        }

        public abstract void serialize(JsonObject var1, T var2, JsonSerializationContext var3);

        public abstract T deserialize(JsonObject var1, JsonDeserializationContext var2, LootItemCondition[] var3);
    }
}
