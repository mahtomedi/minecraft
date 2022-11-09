package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.decoration.PaintingVariants;

public class PaintingVariantTagsProvider extends TagsProvider<PaintingVariant> {
    public PaintingVariantTagsProvider(PackOutput param0, CompletableFuture<HolderLookup.Provider> param1) {
        super(param0, Registries.PAINTING_VARIANT, param1);
    }

    @Override
    protected void addTags(HolderLookup.Provider param0) {
        this.tag(PaintingVariantTags.PLACEABLE)
            .add(
                PaintingVariants.KEBAB,
                PaintingVariants.AZTEC,
                PaintingVariants.ALBAN,
                PaintingVariants.AZTEC2,
                PaintingVariants.BOMB,
                PaintingVariants.PLANT,
                PaintingVariants.WASTELAND,
                PaintingVariants.POOL,
                PaintingVariants.COURBET,
                PaintingVariants.SEA,
                PaintingVariants.SUNSET,
                PaintingVariants.CREEBET,
                PaintingVariants.WANDERER,
                PaintingVariants.GRAHAM,
                PaintingVariants.MATCH,
                PaintingVariants.BUST,
                PaintingVariants.STAGE,
                PaintingVariants.VOID,
                PaintingVariants.SKULL_AND_ROSES,
                PaintingVariants.WITHER,
                PaintingVariants.FIGHTERS,
                PaintingVariants.POINTER,
                PaintingVariants.PIGSCENE,
                PaintingVariants.BURNING_SKULL,
                PaintingVariants.SKELETON,
                PaintingVariants.DONKEY_KONG
            );
    }
}
