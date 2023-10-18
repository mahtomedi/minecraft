package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
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
    }
}
