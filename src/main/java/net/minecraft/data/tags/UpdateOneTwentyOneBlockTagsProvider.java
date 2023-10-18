package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class UpdateOneTwentyOneBlockTagsProvider extends IntrinsicHolderTagsProvider<Block> {
    public UpdateOneTwentyOneBlockTagsProvider(
        PackOutput param0, CompletableFuture<HolderLookup.Provider> param1, CompletableFuture<TagsProvider.TagLookup<Block>> param2
    ) {
        super(param0, Registries.BLOCK, param1, param2, param0x -> param0x.builtInRegistryHolder().key());
    }

    @Override
    protected void addTags(HolderLookup.Provider param0) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(Blocks.CRAFTER);
        this.tag(BlockTags.NEEDS_STONE_TOOL).add(Blocks.CRAFTER);
    }
}
