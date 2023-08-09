package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EmptyLootItem extends LootPoolSingletonContainer {
    public static final Codec<EmptyLootItem> CODEC = RecordCodecBuilder.create(param0 -> singletonFields(param0).apply(param0, EmptyLootItem::new));

    private EmptyLootItem(int param0, int param1, List<LootItemCondition> param2, List<LootItemFunction> param3) {
        super(param0, param1, param2, param3);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.EMPTY;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> param0, LootContext param1) {
    }

    public static LootPoolSingletonContainer.Builder<?> emptyItem() {
        return simpleBuilder(EmptyLootItem::new);
    }
}
