package com.mojang.realmsclient.util;

import com.google.common.collect.Maps;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
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
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsTextureManager {
    private static final Map<String, RealmsTextureManager.RealmsTexture> textures = Maps.newHashMap();
    private static final Map<String, Boolean> skinFetchStatus = Maps.newHashMap();
    private static final Map<String, String> fetchedSkins = Maps.newHashMap();
    private static final Logger LOGGER = LogManager.getLogger();

    public static void bindWorldTemplate(String param0, String param1) {
        if (param1 == null) {
            RealmsScreen.bind("textures/gui/presets/isles.png");
        } else {
            int var0 = getTextureId(param0, param1);
            RenderSystem.bindTexture(var0);
        }
    }

    public static void withBoundFace(String param0, Runnable param1) {
        RenderSystem.pushTextureAttributes();

        try {
            bindFace(param0);
            param1.run();
        } finally {
            RenderSystem.popAttributes();
        }

    }

    private static void bindDefaultFace(UUID param0) {
        RealmsScreen.bind((param0.hashCode() & 1) == 1 ? "minecraft:textures/entity/alex.png" : "minecraft:textures/entity/steve.png");
    }

    private static void bindFace(final String param0) {
        UUID var0 = UUIDTypeAdapter.fromString(param0);
        if (textures.containsKey(param0)) {
            RenderSystem.bindTexture(textures.get(param0).textureId);
        } else if (skinFetchStatus.containsKey(param0)) {
            if (!skinFetchStatus.get(param0)) {
                bindDefaultFace(var0);
            } else if (fetchedSkins.containsKey(param0)) {
                int var1 = getTextureId(param0, fetchedSkins.get(param0));
                RenderSystem.bindTexture(var1);
            } else {
                bindDefaultFace(var0);
            }

        } else {
            skinFetchStatus.put(param0, false);
            bindDefaultFace(var0);
            Thread var2 = new Thread("Realms Texture Downloader") {
                @Override
                public void run() {
                    Map<Type, MinecraftProfileTexture> var0 = RealmsUtil.getTextures(param0);
                    if (var0.containsKey(Type.SKIN)) {
                        MinecraftProfileTexture var1 = var0.get(Type.SKIN);
                        String var2 = var1.getUrl();
                        HttpURLConnection var3 = null;
                        RealmsTextureManager.LOGGER.debug("Downloading http texture from {}", var2);

                        try {
                            var3 = (HttpURLConnection)new URL(var2).openConnection(Realms.getProxy());
                            var3.setDoInput(true);
                            var3.setDoOutput(false);
                            var3.connect();
                            if (var3.getResponseCode() / 100 == 2) {
                                BufferedImage var4;
                                try {
                                    var4 = ImageIO.read(var3.getInputStream());
                                } catch (Exception var17) {
                                    RealmsTextureManager.skinFetchStatus.remove(param0);
                                    return;
                                } finally {
                                    IOUtils.closeQuietly(var3.getInputStream());
                                }

                                var4 = new SkinProcessor().process(var4);
                                ByteArrayOutputStream var7 = new ByteArrayOutputStream();
                                ImageIO.write(var4, "png", var7);
                                RealmsTextureManager.fetchedSkins.put(param0, DatatypeConverter.printBase64Binary(var7.toByteArray()));
                                RealmsTextureManager.skinFetchStatus.put(param0, true);
                                return;
                            }

                            RealmsTextureManager.skinFetchStatus.remove(param0);
                        } catch (Exception var19) {
                            RealmsTextureManager.LOGGER.error("Couldn't download http texture", (Throwable)var19);
                            RealmsTextureManager.skinFetchStatus.remove(param0);
                            return;
                        } finally {
                            if (var3 != null) {
                                var3.disconnect();
                            }

                        }

                    } else {
                        RealmsTextureManager.skinFetchStatus.put(param0, true);
                    }
                }
            };
            var2.setDaemon(true);
            var2.start();
        }
    }

    private static int getTextureId(String param0, String param1) {
        int var1;
        if (textures.containsKey(param0)) {
            RealmsTextureManager.RealmsTexture var0 = textures.get(param0);
            if (var0.image.equals(param1)) {
                return var0.textureId;
            }

            RenderSystem.deleteTexture(var0.textureId);
            var1 = var0.textureId;
        } else {
            var1 = RenderSystem.genTexture();
        }

        IntBuffer var3 = null;
        int var4 = 0;
        int var5 = 0;

        try {
            InputStream var6 = new ByteArrayInputStream(new Base64().decode(param1));

            BufferedImage var7;
            try {
                var7 = ImageIO.read(var6);
            } finally {
                IOUtils.closeQuietly(var6);
            }

            var4 = var7.getWidth();
            var5 = var7.getHeight();
            int[] var9 = new int[var4 * var5];
            var7.getRGB(0, 0, var4, var5, var9, 0, var4);
            var3 = ByteBuffer.allocateDirect(4 * var4 * var5).order(ByteOrder.nativeOrder()).asIntBuffer();
            var3.put(var9);
            ((Buffer)var3).flip();
        } catch (IOException var12) {
            var12.printStackTrace();
        }

        RenderSystem.activeTexture(33984);
        RenderSystem.bindTexture(var1);
        TextureUtil.initTexture(var3, var4, var5);
        textures.put(param0, new RealmsTextureManager.RealmsTexture(param1, var1));
        return var1;
    }

    @OnlyIn(Dist.CLIENT)
    public static class RealmsTexture {
        String image;
        int textureId;

        public RealmsTexture(String param0, int param1) {
            this.image = param0;
            this.textureId = param1;
        }
    }
}
