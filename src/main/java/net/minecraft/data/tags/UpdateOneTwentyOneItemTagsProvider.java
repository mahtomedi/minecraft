package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class UpdateOneTwentyOneItemTagsProvider extends ItemTagsProvider {
    public UpdateOneTwentyOneItemTagsProvider(
        PackOutput param0,
        CompletableFuture<HolderLookup.Provider> param1,
        CompletableFuture<TagsProvider.TagLookup<Item>> param2,
        CompletableFuture<TagsProvider.TagLookup<Block>> param3
    ) {
        super(param0, param1, param2, param3);
    }

    @Override
    protected void addTags(HolderLookup.Provider param0) {
        this.tag(ItemTags.STAIRS).add(Items.TUFF_STAIRS, Items.POLISHED_TUFF_STAIRS, Items.TUFF_BRICK_STAIRS);
        this.tag(ItemTags.SLABS).add(Items.TUFF_SLAB, Items.POLISHED_TUFF_SLAB, Items.TUFF_BRICK_SLAB);
        this.tag(ItemTags.WALLS).add(Items.TUFF_WALL, Items.POLISHED_TUFF_WALL, Items.TUFF_BRICK_WALL);
    }
}
