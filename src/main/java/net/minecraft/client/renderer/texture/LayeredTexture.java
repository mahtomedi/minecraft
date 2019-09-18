package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.AbstractTexture;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class LayeredTexture extends AbstractTexture {
    private static final Logger LOGGER = LogManager.getLogger();
    public final List<String> layerPaths;

    public LayeredTexture(String... param0) {
        this.layerPaths = Lists.newArrayList(param0);
        if (this.layerPaths.isEmpty()) {
            throw new IllegalStateException("Layered texture with no layers.");
        }
    }

    @Override
    public void load(ResourceManager param0) throws IOException {
        Iterator<String> var0 = this.layerPaths.iterator();
        String var1 = var0.next();

        try (Resource var2 = param0.getResource(new ResourceLocation(var1))) {
            NativeImage var3 = NativeImage.read(var2.getInputStream());

            while(var0.hasNext()) {
                String var4 = var0.next();
                if (var4 != null) {
                    try (
                        Resource var5 = param0.getResource(new ResourceLocation(var4));
                        NativeImage var6 = NativeImage.read(var5.getInputStream());
                    ) {
                        for(int var7 = 0; var7 < var6.getHeight(); ++var7) {
                            for(int var8 = 0; var8 < var6.getWidth(); ++var8) {
                                var3.blendPixel(var8, var7, var6.getPixelRGBA(var8, var7));
                            }
                        }
                    }
                }
            }

            if (!RenderSystem.isOnRenderThreadOrInit()) {
                RenderSystem.recordRenderCall(() -> this.doLoad(var3));
            } else {
                this.doLoad(var3);
            }
        } catch (IOException var65) {
            LOGGER.error("Couldn't load layered image", (Throwable)var65);
        }

    }

    private void doLoad(NativeImage param0) {
        TextureUtil.prepareImage(this.getId(), param0.getWidth(), param0.getHeight());
        param0.upload(0, 0, 0, true);
    }
}
