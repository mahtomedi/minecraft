package net.minecraft.tags;

import net.minecraft.world.level.material.Fluid;

public class FluidTags {
    private static final StaticTagHelper<Fluid> HELPER = new StaticTagHelper<>();
    public static final Tag.Named<Fluid> WATER = bind("water");
    public static final Tag.Named<Fluid> LAVA = bind("lava");

    private static Tag.Named<Fluid> bind(String param0) {
        return HELPER.bind(param0);
    }

    public static void reset(TagCollection<Fluid> param0) {
        HELPER.reset(param0);
    }

    public static TagCollection<Fluid> getAllTags() {
        return HELPER.getAllTags();
    }
}
