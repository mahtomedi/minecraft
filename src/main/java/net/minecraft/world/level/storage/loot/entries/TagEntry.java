package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TagEntry extends LootPoolSingletonContainer {
    public static final Codec<TagEntry> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    TagKey.codec(Registries.ITEM).fieldOf("name").forGetter(param0x -> param0x.tag),
                    Codec.BOOL.fieldOf("expand").forGetter(param0x -> param0x.expand)
                )
                .and(singletonFields(param0))
                .apply(param0, TagEntry::new)
    );
    private final TagKey<Item> tag;
    private final boolean expand;

    private TagEntry(TagKey<Item> param0, boolean param1, int param2, int param3, List<LootItemCondition> param4, List<LootItemFunction> param5) {
        super(param2, param3, param4, param5);
        this.tag = param0;
        this.expand = param1;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.TAG;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> param0, LootContext param1) {
        BuiltInRegistries.ITEM.getTagOrEmpty(this.tag).forEach(param1x -> param0.accept(new ItemStack(param1x)));
    }

    private boolean expandTag(LootContext param0, Consumer<LootPoolEntry> param1) {
        if (!this.canRun(param0)) {
            return false;
        } else {
            for(final Holder<Item> var0 : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
                param1.accept(new LootPoolSingletonContainer.EntryBase() {
                    @Override
                    public void createItemStack(Consumer<ItemStack> param0, LootContext param1) {
                        param0.accept(new ItemStack(var0));
                    }
                });
            }

            return true;
        }
    }

    @Override
    public boolean expand(LootContext param0, Consumer<LootPoolEntry> param1) {
        return this.expand ? this.expandTag(param0, param1) : super.expand(param0, param1);
    }

    public static LootPoolSingletonContainer.Builder<?> tagContents(TagKey<Item> param0) {
        return simpleBuilder((param1, param2, param3, param4) -> new TagEntry(param0, false, param1, param2, param3, param4));
    }

    public static LootPoolSingletonContainer.Builder<?> expandTag(TagKey<Item> param0) {
        return simpleBuilder((param1, param2, param3, param4) -> new TagEntry(param0, true, param1, param2, param3, param4));
    }
}
