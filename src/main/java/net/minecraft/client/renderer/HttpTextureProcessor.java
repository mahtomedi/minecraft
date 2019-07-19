package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface HttpTextureProcessor {
    NativeImage process(NativeImage var1);

    void onTextureDownloaded();
}
