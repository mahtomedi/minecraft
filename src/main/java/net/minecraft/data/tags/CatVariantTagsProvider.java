package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.CatVariantTags;
import net.minecraft.world.entity.animal.CatVariant;

public class CatVariantTagsProvider extends TagsProvider<CatVariant> {
    public CatVariantTagsProvider(PackOutput param0, CompletableFuture<HolderLookup.Provider> param1) {
        super(param0, Registry.CAT_VARIANT_REGISTRY, param1);
    }

    @Override
    protected void addTags(HolderLookup.Provider param0) {
        this.tag(CatVariantTags.DEFAULT_SPAWNS)
            .add(
                CatVariant.TABBY,
                CatVariant.BLACK,
                CatVariant.RED,
                CatVariant.SIAMESE,
                CatVariant.BRITISH_SHORTHAIR,
                CatVariant.CALICO,
                CatVariant.PERSIAN,
                CatVariant.RAGDOLL,
                CatVariant.WHITE,
                CatVariant.JELLIE
            );
        this.tag(CatVariantTags.FULL_MOON_SPAWNS).addTag(CatVariantTags.DEFAULT_SPAWNS).add(CatVariant.ALL_BLACK);
    }
}
