package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

public class UpdateOneTwentyOneEntityTypeTagsProvider extends IntrinsicHolderTagsProvider<EntityType<?>> {
    public UpdateOneTwentyOneEntityTypeTagsProvider(PackOutput param0, CompletableFuture<HolderLookup.Provider> param1) {
        super(param0, Registries.ENTITY_TYPE, param1, param0x -> param0x.builtInRegistryHolder().key());
    }

    @Override
    protected void addTags(HolderLookup.Provider param0) {
        this.tag(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(EntityType.BREEZE);
        this.tag(EntityTypeTags.DEFLECTS_ARROWS).add(EntityType.BREEZE);
        this.tag(EntityTypeTags.DEFLECTS_TRIDENTS).add(EntityType.BREEZE);
        this.tag(EntityTypeTags.CAN_TURN_IN_BOATS).add(EntityType.BREEZE);
    }
}
