package net.minecraft.tags;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

    @OnlyIn(Dist.CLIENT)
    public static void resetToEmpty() {
        HELPER.resetToEmpty();
    }

    public static TagCollection<Fluid> getAllTags() {
        return HELPER.getAllTags();
    }

    public static Set<ResourceLocation> getMissingTags(TagCollection<Fluid> param0) {
        return HELPER.getMissingTags(param0);
    }
}
