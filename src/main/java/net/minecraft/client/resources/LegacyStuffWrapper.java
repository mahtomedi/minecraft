package net.minecraft.client.resources;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LegacyStuffWrapper {
    @Deprecated
    public static int[] getPixels(ResourceManager param0, ResourceLocation param1) throws IOException {
        int[] var4;
        try (
            InputStream var0 = param0.open(param1);
            NativeImage var1 = NativeImage.read(var0);
        ) {
            var4 = var1.makePixelArray();
        }

        return var4;
    }
}
