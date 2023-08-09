package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItem extends LootPoolSingletonContainer {
    public static final Codec<LootItem> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("name").forGetter(param0x -> param0x.item))
                .and(singletonFields(param0))
                .apply(param0, LootItem::new)
    );
    private final Holder<Item> item;

    private LootItem(Holder<Item> param0, int param1, int param2, List<LootItemCondition> param3, List<LootItemFunction> param4) {
        super(param1, param2, param3, param4);
        this.item = param0;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.ITEM;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> param0, LootContext param1) {
        param0.accept(new ItemStack(this.item));
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableItem(ItemLike param0) {
        return simpleBuilder((param1, param2, param3, param4) -> new LootItem(param0.asItem().builtInRegistryHolder(), param1, param2, param3, param4));
    }
}
