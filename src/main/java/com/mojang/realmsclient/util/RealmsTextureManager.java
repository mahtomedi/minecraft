package com.mojang.realmsclient.util;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsTextureManager {
    private static final Map<String, RealmsTextureManager.RealmsTexture> TEXTURES = Maps.newHashMap();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation TEMPLATE_ICON_LOCATION = new ResourceLocation("textures/gui/presets/isles.png");

    public static ResourceLocation worldTemplate(String param0, @Nullable String param1) {
        return param1 == null ? TEMPLATE_ICON_LOCATION : getTexture(param0, param1);
    }

    private static ResourceLocation getTexture(String param0, String param1) {
        RealmsTextureManager.RealmsTexture var0 = (RealmsTextureManager.RealmsTexture)TEXTURES.get(param0);
        if (var0 != null && var0.image().equals(param1)) {
            return var0.textureId;
        } else {
            NativeImage var1 = loadImage(param1);
            if (var1 == null) {
                ResourceLocation var2 = MissingTextureAtlasSprite.getLocation();
                TEXTURES.put(param0, new RealmsTextureManager.RealmsTexture(param1, var2));
                return var2;
            } else {
                ResourceLocation var3 = new ResourceLocation("realms", "dynamic/" + param0);
                Minecraft.getInstance().getTextureManager().register(var3, new DynamicTexture(var1));
                TEXTURES.put(param0, new RealmsTextureManager.RealmsTexture(param1, var3));
                return var3;
            }
        }
    }

    @Nullable
    private static NativeImage loadImage(String param0) {
        byte[] var0 = Base64.getDecoder().decode(param0);
        ByteBuffer var1 = MemoryUtil.memAlloc(var0.length);

        try {
            return NativeImage.read(var1.put(var0).flip());
        } catch (IOException var7) {
            LOGGER.warn("Failed to load world image: {}", param0, var7);
        } finally {
            MemoryUtil.memFree(var1);
        }

        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public static record RealmsTexture(String image, ResourceLocation textureId) {
    }
}
