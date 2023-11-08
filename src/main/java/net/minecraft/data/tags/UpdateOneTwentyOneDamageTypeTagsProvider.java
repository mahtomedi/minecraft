package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

public class UpdateOneTwentyOneDamageTypeTagsProvider extends TagsProvider<DamageType> {
    public UpdateOneTwentyOneDamageTypeTagsProvider(PackOutput param0, CompletableFuture<HolderLookup.Provider> param1) {
        super(param0, Registries.DAMAGE_TYPE, param1);
    }

    @Override
    protected void addTags(HolderLookup.Provider param0) {
        this.tag(DamageTypeTags.BREEZE_IMMUNE_TO).add(DamageTypes.ARROW, DamageTypes.TRIDENT);
    }
}
