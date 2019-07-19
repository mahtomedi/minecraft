package net.minecraft.client.resources;

import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.FoliageColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FoliageColorReloadListener extends SimplePreparableReloadListener<int[]> {
    private static final ResourceLocation LOCATION = new ResourceLocation("textures/colormap/foliage.png");

    protected int[] prepare(ResourceManager param0, ProfilerFiller param1) {
        try {
            return LegacyStuffWrapper.getPixels(param0, LOCATION);
        } catch (IOException var4) {
            throw new IllegalStateException("Failed to load foliage color texture", var4);
        }
    }

    protected void apply(int[] param0, ResourceManager param1, ProfilerFiller param2) {
        FoliageColor.init(param0);
    }
}
