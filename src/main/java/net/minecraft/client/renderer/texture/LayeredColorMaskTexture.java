package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.IOException;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class LayeredColorMaskTexture extends AbstractTexture {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ResourceLocation baseLayerResource;
    private final List<String> layerMaskPaths;
    private final List<DyeColor> layerColors;

    public LayeredColorMaskTexture(ResourceLocation param0, List<String> param1, List<DyeColor> param2) {
        this.baseLayerResource = param0;
        this.layerMaskPaths = param1;
        this.layerColors = param2;
    }

    @Override
    public void load(ResourceManager param0) throws IOException {
        try (
            Resource var0 = param0.getResource(this.baseLayerResource);
            NativeImage var1 = NativeImage.read(var0.getInputStream());
            NativeImage var2 = new NativeImage(var1.getWidth(), var1.getHeight(), false);
        ) {
            var2.copyFrom(var1);

            for(int var3 = 0; var3 < 17 && var3 < this.layerMaskPaths.size() && var3 < this.layerColors.size(); ++var3) {
                String var4 = this.layerMaskPaths.get(var3);
                if (var4 != null) {
                    try (
                        Resource var5 = param0.getResource(new ResourceLocation(var4));
                        NativeImage var6 = NativeImage.read(var5.getInputStream());
                    ) {
                        int var7 = this.layerColors.get(var3).getTextureDiffuseColorBGR();
                        if (var6.getWidth() == var2.getWidth() && var6.getHeight() == var2.getHeight()) {
                            for(int var8 = 0; var8 < var6.getHeight(); ++var8) {
                                for(int var9 = 0; var9 < var6.getWidth(); ++var9) {
                                    int var10 = var6.getPixelRGBA(var9, var8);
                                    if ((var10 & 0xFF000000) != 0) {
                                        int var11 = (var10 & 0xFF) << 24 & 0xFF000000;
                                        int var12 = var1.getPixelRGBA(var9, var8);
                                        int var13 = Mth.colorMultiply(var12, var7) & 16777215;
                                        var2.blendPixel(var9, var8, var11 | var13);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            TextureUtil.prepareImage(this.getId(), var2.getWidth(), var2.getHeight());
            GlStateManager.pixelTransfer(3357, Float.MAX_VALUE);
            var2.upload(0, 0, 0, false);
            GlStateManager.pixelTransfer(3357, 0.0F);
        } catch (IOException var150) {
            LOGGER.error("Couldn't load layered color mask image", (Throwable)var150);
        }

    }
}
