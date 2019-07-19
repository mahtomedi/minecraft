package net.minecraft.data.tags;

import java.nio.file.Path;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidTagsProvider extends TagsProvider<Fluid> {
    public FluidTagsProvider(DataGenerator param0) {
        super(param0, Registry.FLUID);
    }

    @Override
    protected void addTags() {
        this.tag(FluidTags.WATER).add(Fluids.WATER, Fluids.FLOWING_WATER);
        this.tag(FluidTags.LAVA).add(Fluids.LAVA, Fluids.FLOWING_LAVA);
    }

    @Override
    protected Path getPath(ResourceLocation param0) {
        return this.generator.getOutputFolder().resolve("data/" + param0.getNamespace() + "/tags/fluids/" + param0.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Fluid Tags";
    }

    @Override
    protected void useTags(TagCollection<Fluid> param0) {
        FluidTags.reset(param0);
    }
}
