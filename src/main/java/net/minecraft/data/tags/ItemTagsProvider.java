package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public abstract class ItemTagsProvider extends IntrinsicHolderTagsProvider<Item> {
    private final Function<TagKey<Block>, TagBuilder> blockTags;

    public ItemTagsProvider(PackOutput param0, CompletableFuture<HolderLookup.Provider> param1, TagsProvider<Block> param2) {
        super(param0, Registries.ITEM, param1, param0x -> param0x.builtInRegistryHolder().key());
        this.blockTags = param2::getOrCreateRawBuilder;
    }

    protected void copy(TagKey<Block> param0, TagKey<Item> param1) {
        TagBuilder var0 = this.getOrCreateRawBuilder(param1);
        TagBuilder var1 = this.blockTags.apply(param0);
        var1.build().forEach(var0::add);
    }
}
