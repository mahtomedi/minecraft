package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerContents extends LootItemConditionalFunction {
    final List<LootPoolEntryContainer> entries;
    final BlockEntityType<?> type;

    SetContainerContents(LootItemCondition[] param0, BlockEntityType<?> param1, List<LootPoolEntryContainer> param2) {
        super(param0);
        this.type = param1;
        this.entries = ImmutableList.copyOf(param2);
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
            this.entries.forEach(param2 -> param2.expand(param1, param2x -> param2x.createItemStack(LootTable.createStackSplitter(param1, var0::add), param1)));
            CompoundTag var1 = new CompoundTag();
            ContainerHelper.saveAllItems(var1, var0);
            CompoundTag var2 = BlockItem.getBlockEntityData(param0);
            if (var2 == null) {
                var2 = var1;
            } else {
                var2.merge(var1);
            }

            BlockItem.setBlockEntityData(param0, this.type, var2);
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

    public static SetContainerContents.Builder setContents(BlockEntityType<?> param0) {
        return new SetContainerContents.Builder(param0);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<SetContainerContents.Builder> {
        private final List<LootPoolEntryContainer> entries = Lists.newArrayList();
        private final BlockEntityType<?> type;

        public Builder(BlockEntityType<?> param0) {
            this.type = param0;
        }

        protected SetContainerContents.Builder getThis() {
            return this;
        }

        public SetContainerContents.Builder withEntry(LootPoolEntryContainer.Builder<?> param0) {
            this.entries.add(param0.build());
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetContainerContents(this.getConditions(), this.type, this.entries);
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerContents> {
        public void serialize(JsonObject param0, SetContainerContents param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.addProperty("type", Registry.BLOCK_ENTITY_TYPE.getKey(param1.type).toString());
            param0.add("entries", param2.serialize(param1.entries));
        }

        public SetContainerContents deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            LootPoolEntryContainer[] var0 = GsonHelper.getAsObject(param0, "entries", param1, LootPoolEntryContainer[].class);
            ResourceLocation var1 = new ResourceLocation(GsonHelper.getAsString(param0, "type"));
            BlockEntityType<?> var2 = Registry.BLOCK_ENTITY_TYPE
                .getOptional(var1)
                .orElseThrow(() -> new JsonSyntaxException("Unknown block entity type id '" + var1 + "'"));
            return new SetContainerContents(param2, var2, Arrays.asList(var0));
        }
    }
}
