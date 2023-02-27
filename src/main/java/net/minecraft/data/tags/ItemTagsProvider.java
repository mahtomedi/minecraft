package net.minecraft.data.tags;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public abstract class ItemTagsProvider extends IntrinsicHolderTagsProvider<Item> {
    private final CompletableFuture<TagsProvider.TagLookup<Block>> blockTags;
    private final Map<TagKey<Block>, TagKey<Item>> tagsToCopy = new HashMap();

    public ItemTagsProvider(PackOutput param0, CompletableFuture<HolderLookup.Provider> param1, CompletableFuture<TagsProvider.TagLookup<Block>> param2) {
        super(param0, Registries.ITEM, param1, param0x -> param0x.builtInRegistryHolder().key());
        this.blockTags = param2;
    }

    public ItemTagsProvider(
        PackOutput param0,
        CompletableFuture<HolderLookup.Provider> param1,
        CompletableFuture<TagsProvider.TagLookup<Item>> param2,
        CompletableFuture<TagsProvider.TagLookup<Block>> param3
    ) {
        super(param0, Registries.ITEM, param1, param2, param0x -> param0x.builtInRegistryHolder().key());
        this.blockTags = param3;
    }

    protected void copy(TagKey<Block> param0, TagKey<Item> param1) {
        this.tagsToCopy.put(param0, param1);
    }

    @Override
    protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
        return super.createContentsProvider().thenCombineAsync(this.blockTags, (param0, param1) -> {
            this.tagsToCopy.forEach((param1x, param2) -> {
                TagBuilder var0 = this.getOrCreateRawBuilder(param2);
                Optional<TagBuilder> var1x = param1.apply(param1x);
                ((TagBuilder)var1x.orElseThrow(() -> new IllegalStateException("Missing block tag " + param2.location()))).build().forEach(var0::add);
            });
            return param0;
        });
    }
}
