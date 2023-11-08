package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

public class UpdateOneTwentyOneBiomeTagsProvider extends TagsProvider<Biome> {
    public UpdateOneTwentyOneBiomeTagsProvider(
        PackOutput param0, CompletableFuture<HolderLookup.Provider> param1, CompletableFuture<TagsProvider.TagLookup<Biome>> param2
    ) {
        super(param0, Registries.BIOME, param1, param2);
    }

    @Override
    protected void addTags(HolderLookup.Provider param0) {
        this.tag(BiomeTags.HAS_TRIAL_CHAMBERS).addTag(BiomeTags.IS_OVERWORLD);
    }
}
