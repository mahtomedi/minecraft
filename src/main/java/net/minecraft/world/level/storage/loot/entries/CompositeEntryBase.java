package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
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
    public void validate(ValidationContext param0) {
        super.validate(param0);
        if (this.children.length == 0) {
            param0.reportProblem("Empty children list");
        }

        for(int var0 = 0; var0 < this.children.length; ++var0) {
            this.children[var0].validate(param0.forChild(".entry[" + var0 + "]"));
        }

    }

    protected abstract ComposableEntryContainer compose(ComposableEntryContainer[] var1);

    @Override
    public final boolean expand(LootContext param0, Consumer<LootPoolEntry> param1) {
        return !this.canRun(param0) ? false : this.composedChildren.expand(param0, param1);
    }

    public static <T extends CompositeEntryBase> LootPoolEntryContainer.Serializer<T> createSerializer(
        final CompositeEntryBase.CompositeEntryConstructor<T> param0
    ) {
        return new LootPoolEntryContainer.Serializer<T>() {
            public void serializeCustom(JsonObject param0x, T param1, JsonSerializationContext param2) {
                param0.add("children", param2.serialize(param1.children));
            }

            public final T deserializeCustom(JsonObject param0x, JsonDeserializationContext param1, LootItemCondition[] param2) {
                LootPoolEntryContainer[] var0 = GsonHelper.getAsObject(param0, "children", param1, LootPoolEntryContainer[].class);
                return param0.create(var0, param2);
            }
        };
    }

    @FunctionalInterface
    public interface CompositeEntryConstructor<T extends CompositeEntryBase> {
        T create(LootPoolEntryContainer[] var1, LootItemCondition[] var2);
    }
}
