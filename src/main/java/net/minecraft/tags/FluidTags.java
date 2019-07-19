package net.minecraft.tags;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class FluidTags {
    private static TagCollection<Fluid> source = new TagCollection<>(param0 -> Optional.empty(), "", false, "");
    private static int resetCount;
    public static final Tag<Fluid> WATER = bind("water");
    public static final Tag<Fluid> LAVA = bind("lava");

    public static void reset(TagCollection<Fluid> param0) {
        source = param0;
        ++resetCount;
    }

    private static Tag<Fluid> bind(String param0) {
        return new FluidTags.Wrapper(new ResourceLocation(param0));
    }

    public static class Wrapper extends Tag<Fluid> {
        private int check = -1;
        private Tag<Fluid> actual;

        public Wrapper(ResourceLocation param0) {
            super(param0);
        }

        public boolean contains(Fluid param0) {
            if (this.check != FluidTags.resetCount) {
                this.actual = FluidTags.source.getTagOrEmpty(this.getId());
                this.check = FluidTags.resetCount;
            }

            return this.actual.contains(param0);
        }

        @Override
        public Collection<Fluid> getValues() {
            if (this.check != FluidTags.resetCount) {
                this.actual = FluidTags.source.getTagOrEmpty(this.getId());
                this.check = FluidTags.resetCount;
            }

            return this.actual.getValues();
        }

        @Override
        public Collection<Tag.Entry<Fluid>> getSource() {
            if (this.check != FluidTags.resetCount) {
                this.actual = FluidTags.source.getTagOrEmpty(this.getId());
                this.check = FluidTags.resetCount;
            }

            return this.actual.getSource();
        }
    }
}
