package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetLoreFunction extends LootItemConditionalFunction {
    final boolean replace;
    final List<Component> lore;
    @Nullable
    final LootContext.EntityTarget resolutionContext;

    public SetLoreFunction(LootItemCondition[] param0, boolean param1, List<Component> param2, @Nullable LootContext.EntityTarget param3) {
        super(param0);
        this.replace = param1;
        this.lore = ImmutableList.copyOf(param2);
        this.resolutionContext = param3;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_LORE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.resolutionContext != null ? ImmutableSet.of(this.resolutionContext.getParam()) : ImmutableSet.of();
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        ListTag var0 = this.getLoreTag(param0, !this.lore.isEmpty());
        if (var0 != null) {
            if (this.replace) {
                var0.clear();
            }

            UnaryOperator<Component> var1 = SetNameFunction.createResolver(param1, this.resolutionContext);
            this.lore.stream().map(var1).map(Component.Serializer::toJson).map(StringTag::valueOf).forEach(var0::add);
        }

        return param0;
    }

    @Nullable
    private ListTag getLoreTag(ItemStack param0, boolean param1) {
        CompoundTag var0;
        if (param0.hasTag()) {
            var0 = param0.getTag();
        } else {
            if (!param1) {
                return null;
            }

            var0 = new CompoundTag();
            param0.setTag(var0);
        }

        CompoundTag var3;
        if (var0.contains("display", 10)) {
            var3 = var0.getCompound("display");
        } else {
            if (!param1) {
                return null;
            }

            var3 = new CompoundTag();
            var0.put("display", var3);
        }

        if (var3.contains("Lore", 9)) {
            return var3.getList("Lore", 8);
        } else if (param1) {
            ListTag var6 = new ListTag();
            var3.put("Lore", var6);
            return var6;
        } else {
            return null;
        }
    }

    public static SetLoreFunction.Builder setLore() {
        return new SetLoreFunction.Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<SetLoreFunction.Builder> {
        private boolean replace;
        private LootContext.EntityTarget resolutionContext;
        private final List<Component> lore = Lists.newArrayList();

        public SetLoreFunction.Builder setReplace(boolean param0) {
            this.replace = param0;
            return this;
        }

        public SetLoreFunction.Builder setResolutionContext(LootContext.EntityTarget param0) {
            this.resolutionContext = param0;
            return this;
        }

        public SetLoreFunction.Builder addLine(Component param0) {
            this.lore.add(param0);
            return this;
        }

        protected SetLoreFunction.Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetLoreFunction(this.getConditions(), this.replace, this.lore, this.resolutionContext);
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetLoreFunction> {
        public void serialize(JsonObject param0, SetLoreFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.addProperty("replace", param1.replace);
            JsonArray var0 = new JsonArray();

            for(Component var1 : param1.lore) {
                var0.add(Component.Serializer.toJsonTree(var1));
            }

            param0.add("lore", var0);
            if (param1.resolutionContext != null) {
                param0.add("entity", param2.serialize(param1.resolutionContext));
            }

        }

        public SetLoreFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            boolean var0 = GsonHelper.getAsBoolean(param0, "replace", false);
            List<Component> var1 = Streams.stream(GsonHelper.getAsJsonArray(param0, "lore"))
                .map(Component.Serializer::fromJson)
                .collect(ImmutableList.toImmutableList());
            LootContext.EntityTarget var2 = GsonHelper.getAsObject(param0, "entity", null, param1, LootContext.EntityTarget.class);
            return new SetLoreFunction(param2, var0, var1, var2);
        }
    }
}
