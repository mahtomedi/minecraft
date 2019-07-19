package net.minecraft.client.resources;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LegacyStuffWrapper {
    @Deprecated
    public static int[] getPixels(ResourceManager param0, ResourceLocation param1) throws IOException {
        int[] var6;
        try (
            Resource var0 = param0.getResource(param1);
            NativeImage var1 = NativeImage.read(var0.getInputStream());
        ) {
            var6 = var1.makePixelArray();
        }

        return var6;
    }
}
