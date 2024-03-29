package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerContents extends LootItemConditionalFunction {
    public static final Codec<SetContainerContents> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(
                    param0.group(
                        BuiltInRegistries.BLOCK_ENTITY_TYPE.holderByNameCodec().fieldOf("type").forGetter(param0x -> param0x.type),
                        LootPoolEntries.CODEC.listOf().fieldOf("entries").forGetter(param0x -> param0x.entries)
                    )
                )
                .apply(param0, SetContainerContents::new)
    );
    private final Holder<BlockEntityType<?>> type;
    private final List<LootPoolEntryContainer> entries;

    SetContainerContents(List<LootItemCondition> param0, Holder<BlockEntityType<?>> param1, List<LootPoolEntryContainer> param2) {
        super(param0);
        this.type = param1;
        this.entries = List.copyOf(param2);
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
            this.entries
                .forEach(
                    param2 -> param2.expand(param1, param2x -> param2x.createItemStack(LootTable.createStackSplitter(param1.getLevel(), var0::add), param1))
                );
            CompoundTag var1 = new CompoundTag();
            ContainerHelper.saveAllItems(var1, var0);
            CompoundTag var2 = BlockItem.getBlockEntityData(param0);
            if (var2 == null) {
                var2 = var1;
            } else {
                var2.merge(var1);
            }

            BlockItem.setBlockEntityData(param0, this.type.value(), var2);
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
        private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();
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
            return new SetContainerContents(this.getConditions(), this.type.builtInRegistryHolder(), this.entries.build());
        }
    }
}
