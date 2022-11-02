package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidTagsProvider extends IntrinsicHolderTagsProvider<Fluid> {
    public FluidTagsProvider(PackOutput param0, CompletableFuture<HolderLookup.Provider> param1) {
        super(param0, Registry.FLUID_REGISTRY, param1, param0x -> param0x.builtInRegistryHolder().key());
    }

    @Override
    protected void addTags(HolderLookup.Provider param0) {
        this.tag(FluidTags.WATER).add(Fluids.WATER, Fluids.FLOWING_WATER);
        this.tag(FluidTags.LAVA).add(Fluids.LAVA, Fluids.FLOWING_LAVA);
    }
}
