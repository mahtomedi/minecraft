package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableProblemCollector;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class CompositeEntryBase extends LootPoolEntryContainer {
    protected final LootPoolEntryContainer[] children;
    private final ComposableEntryContainer composedChildren;

    protected CompositeEntryBase(LootPoolEntryContainer[] param0, LootItemCondition[] param1) {
        super(param1);
        this.children = param0;
        this.composedChildren = this.compose(param0);
    }

    @Override
    public void validate(
        LootTableProblemCollector param0, Function<ResourceLocation, LootTable> param1, Set<ResourceLocation> param2, LootContextParamSet param3
    ) {
        super.validate(param0, param1, param2, param3);
        if (this.children.length == 0) {
            param0.reportProblem("Empty children list");
        }

        for(int var0 = 0; var0 < this.children.length; ++var0) {
            this.children[var0].validate(param0.forChild(".entry[" + var0 + "]"), param1, param2, param3);
        }

    }

    protected abstract ComposableEntryContainer compose(ComposableEntryContainer[] var1);

    @Override
    public final boolean expand(LootContext param0, Consumer<LootPoolEntry> param1) {
        return !this.canRun(param0) ? false : this.composedChildren.expand(param0, param1);
    }

    public static <T extends CompositeEntryBase> CompositeEntryBase.Serializer<T> createSerializer(
        ResourceLocation param0, Class<T> param1, final CompositeEntryBase.CompositeEntryConstructor<T> param2
    ) {
        return new CompositeEntryBase.Serializer<T>(param0, param1) {
            @Override
            protected T deserialize(JsonObject param0, JsonDeserializationContext param1, LootPoolEntryContainer[] param2x, LootItemCondition[] param3) {
                return param2.create(param2, param3);
            }
        };
    }

    @FunctionalInterface
    public interface CompositeEntryConstructor<T extends CompositeEntryBase> {
        T create(LootPoolEntryContainer[] var1, LootItemCondition[] var2);
    }

    public abstract static class Serializer<T extends CompositeEntryBase> extends LootPoolEntryContainer.Serializer<T> {
        public Serializer(ResourceLocation param0, Class<T> param1) {
            super(param0, param1);
        }

        public void serialize(JsonObject param0, T param1, JsonSerializationContext param2) {
            param0.add("children", param2.serialize(param1.children));
        }

        public final T deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            LootPoolEntryContainer[] var0 = GsonHelper.getAsObject(param0, "children", param1, LootPoolEntryContainer[].class);
            return this.deserialize(param0, param1, var0, param2);
        }

        protected abstract T deserialize(JsonObject var1, JsonDeserializationContext var2, LootPoolEntryContainer[] var3, LootItemCondition[] var4);
    }
}
