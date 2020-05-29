package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerContents extends LootItemConditionalFunction {
    private final List<LootPoolEntryContainer> entries;

    private SetContainerContents(LootItemCondition[] param0, List<LootPoolEntryContainer> param1) {
        super(param0);
        this.entries = ImmutableList.copyOf(param1);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_CONTENTS;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        if (param0.isEmpty()) {
            return param0;
        } else {
            NonNullList<ItemStack> var0 = NonNullList.create();
            this.entries.forEach(param2 -> param2.expand(param1, param2x -> param2x.createItemStack(LootTable.createStackSplitter(var0::add), param1)));
            CompoundTag var1 = new CompoundTag();
            ContainerHelper.saveAllItems(var1, var0);
            CompoundTag var2 = param0.getOrCreateTag();
            var2.put("BlockEntityTag", var1.merge(var2.getCompound("BlockEntityTag")));
            return param0;
        }
    }

    @Override
    public void validate(ValidationContext param0) {
        super.validate(param0);

        for(int var0 = 0; var0 < this.entries.size(); ++var0) {
            this.entries.get(var0).validate(param0.forChild(".entry[" + var0 + "]"));
        }

    }

    public static SetContainerContents.Builder setContents() {
        return new SetContainerContents.Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<SetContainerContents.Builder> {
        private final List<LootPoolEntryContainer> entries = Lists.newArrayList();

        protected SetContainerContents.Builder getThis() {
            return this;
        }

        public SetContainerContents.Builder withEntry(LootPoolEntryContainer.Builder<?> param0) {
            this.entries.add(param0.build());
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetContainerContents(this.getConditions(), this.entries);
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerContents> {
        public void serialize(JsonObject param0, SetContainerContents param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.add("entries", param2.serialize(param1.entries));
        }

        public SetContainerContents deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            LootPoolEntryContainer[] var0 = GsonHelper.getAsObject(param0, "entries", param1, LootPoolEntryContainer[].class);
            return new SetContainerContents(param2, Arrays.asList(var0));
        }
    }
}
