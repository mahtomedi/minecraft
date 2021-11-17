package com.mojang.realmsclient.util;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.util.UUIDTypeAdapter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;

@OnlyIn(Dist.CLIENT)
public class RealmsTextureManager {
    private static final Map<String, RealmsTextureManager.RealmsTexture> TEXTURES = Maps.newHashMap();
    static final Map<String, Boolean> SKIN_FETCH_STATUS = Maps.newHashMap();
    static final Map<String, String> FETCHED_SKINS = Maps.newHashMap();
    static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation TEMPLATE_ICON_LOCATION = new ResourceLocation("textures/gui/presets/isles.png");

    public static void bindWorldTemplate(String param0, @Nullable String param1) {
        if (param1 == null) {
            RenderSystem.setShaderTexture(0, TEMPLATE_ICON_LOCATION);
        } else {
            int var0 = getTextureId(param0, param1);
            RenderSystem.setShaderTexture(0, var0);
        }
    }

    public static void withBoundFace(String param0, Runnable param1) {
        bindFace(param0);
        param1.run();
    }

    private static void bindDefaultFace(UUID param0) {
        RenderSystem.setShaderTexture(0, DefaultPlayerSkin.getDefaultSkin(param0));
    }

    private static void bindFace(final String param0) {
        UUID var0 = UUIDTypeAdapter.fromString(param0);
        if (TEXTURES.containsKey(param0)) {
            int var1 = TEXTURES.get(param0).textureId;
            RenderSystem.setShaderTexture(0, var1);
        } else if (SKIN_FETCH_STATUS.containsKey(param0)) {
            if (!SKIN_FETCH_STATUS.get(param0)) {
                bindDefaultFace(var0);
            } else if (FETCHED_SKINS.containsKey(param0)) {
                int var2 = getTextureId(param0, FETCHED_SKINS.get(param0));
                RenderSystem.setShaderTexture(0, var2);
            } else {
                bindDefaultFace(var0);
            }

        } else {
            SKIN_FETCH_STATUS.put(param0, false);
            bindDefaultFace(var0);
            Thread var3 = new Thread("Realms Texture Downloader") {
                @Override
                public void run() {
                    Map<Type, MinecraftProfileTexture> var0 = RealmsUtil.getTextures(param0);
                    if (var0.containsKey(Type.SKIN)) {
                        MinecraftProfileTexture var1 = var0.get(Type.SKIN);
                        String var2 = var1.getUrl();
                        HttpURLConnection var3 = null;
                        RealmsTextureManager.LOGGER.debug("Downloading http texture from {}", var2);

                        try {
                            var3 = (HttpURLConnection)new URL(var2).openConnection(Minecraft.getInstance().getProxy());
                            var3.setDoInput(true);
                            var3.setDoOutput(false);
                            var3.connect();
                            if (var3.getResponseCode() / 100 == 2) {
                                BufferedImage var4;
                                try {
                                    var4 = ImageIO.read(var3.getInputStream());
                                } catch (Exception var17) {
                                    RealmsTextureManager.SKIN_FETCH_STATUS.remove(param0);
                                    return;
                                } finally {
                                    IOUtils.closeQuietly(var3.getInputStream());
                                }

                                var4 = new SkinProcessor().process(var4);
                                ByteArrayOutputStream var7 = new ByteArrayOutputStream();
                                ImageIO.write(var4, "png", var7);
                                RealmsTextureManager.FETCHED_SKINS.put(param0, new Base64().encodeToString(var7.toByteArray()));
                                RealmsTextureManager.SKIN_FETCH_STATUS.put(param0, true);
                                return;
                            }

                            RealmsTextureManager.SKIN_FETCH_STATUS.remove(param0);
                        } catch (Exception var19) {
                            RealmsTextureManager.LOGGER.error("Couldn't download http texture", (Throwable)var19);
                            RealmsTextureManager.SKIN_FETCH_STATUS.remove(param0);
                            return;
                        } finally {
                            if (var3 != null) {
                                var3.disconnect();
                            }

                        }

                    } else {
                        RealmsTextureManager.SKIN_FETCH_STATUS.put(param0, true);
                    }
                }
            };
            var3.setDaemon(true);
            var3.start();
        }
    }

    private static int getTextureId(String param0, String param1) {
        RealmsTextureManager.RealmsTexture var0 = TEXTURES.get(param0);
        if (var0 != null && var0.image.equals(param1)) {
            return var0.textureId;
        } else {
            int var1;
            if (var0 != null) {
                var1 = var0.textureId;
            } else {
                var1 = GlStateManager._genTexture();
            }

            RealmsTextureManager.TextureData var3 = RealmsTextureManager.TextureData.load(param1);
            RenderSystem.activeTexture(33984);
            RenderSystem.bindTextureForSetup(var1);
            TextureUtil.initTexture(var3.data, var3.width, var3.height);
            TEXTURES.put(param0, new RealmsTextureManager.RealmsTexture(param1, var1));
            return var1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class RealmsTexture {
        final String image;
        final int textureId;

        public RealmsTexture(String param0, int param1) {
            this.image = param0;
            this.textureId = param1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class TextureData {
        final int width;
        final int height;
        final IntBuffer data;
        private static final Supplier<RealmsTextureManager.TextureData> MISSING = Suppliers.memoize(() -> {
            int var0 = 16;
            int var1 = 16;
            IntBuffer var2 = BufferUtils.createIntBuffer(256);
            int var3 = -16777216;
            int var4 = -524040;

            for(int var5 = 0; var5 < 16; ++var5) {
                for(int var6 = 0; var6 < 16; ++var6) {
                    if (var5 < 8 ^ var6 < 8) {
                        var2.put(var6 + var5 * 16, -524040);
                    } else {
                        var2.put(var6 + var5 * 16, -16777216);
                    }
                }
            }

            return new RealmsTextureManager.TextureData(16, 16, var2);
        });

        private TextureData(int param0, int param1, IntBuffer param2) {
            this.width = param0;
            this.height = param1;
            this.data = param2;
        }

        public static RealmsTextureManager.TextureData load(String param0) {
            try {
                InputStream var0 = new ByteArrayInputStream(new Base64().decode(param0));
                BufferedImage var1 = ImageIO.read(var0);
                if (var1 != null) {
                    int var2 = var1.getWidth();
                    int var3 = var1.getHeight();
                    int[] var4 = new int[var2 * var3];
                    var1.getRGB(0, 0, var2, var3, var4, 0, var2);
                    IntBuffer var5 = BufferUtils.createIntBuffer(var2 * var3);
                    var5.put(var4);
                    var5.flip();
                    return new RealmsTextureManager.TextureData(var2, var3, var5);
                }

                RealmsTextureManager.LOGGER.warn("Unknown image format: {}", param0);
            } catch (IOException var7) {
                RealmsTextureManager.LOGGER.warn("Failed to load world image: {}", param0, var7);
            }

            return MISSING.get();
        }
    }
}
