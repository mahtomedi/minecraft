package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class LegacyUnicodeBitmapsProvider implements GlyphProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ResourceManager resourceManager;
    private final byte[] sizes;
    private final String texturePattern;
    private final Map<ResourceLocation, NativeImage> textures = Maps.newHashMap();

    public LegacyUnicodeBitmapsProvider(ResourceManager param0, byte[] param1, String param2) {
        this.resourceManager = param0;
        this.sizes = param1;
        this.texturePattern = param2;

        for(int var0 = 0; var0 < 256; ++var0) {
            char var1 = (char)(var0 * 256);
            ResourceLocation var2 = this.getSheetLocation(var1);

            try (
                Resource var3 = this.resourceManager.getResource(var2);
                NativeImage var4 = NativeImage.read(NativeImage.Format.RGBA, var3.getInputStream());
            ) {
                if (var4.getWidth() == 256 && var4.getHeight() == 256) {
                    for(int var5 = 0; var5 < 256; ++var5) {
                        byte var6 = param1[var1 + var5];
                        if (var6 != 0 && getLeft(var6) > getRight(var6)) {
                            param1[var1 + var5] = 0;
                        }
                    }
                    continue;
                }
            } catch (IOException var43) {
            }

            Arrays.fill(param1, var1, var1 + 256, (byte)0);
        }

    }

    @Override
    public void close() {
        this.textures.values().forEach(NativeImage::close);
    }

    private ResourceLocation getSheetLocation(char param0) {
        ResourceLocation var0 = new ResourceLocation(String.format(this.texturePattern, String.format("%02x", param0 / 256)));
        return new ResourceLocation(var0.getNamespace(), "textures/" + var0.getPath());
    }

    @Nullable
    @Override
    public RawGlyph getGlyph(char param0) {
        byte var0 = this.sizes[param0];
        if (var0 != 0) {
            NativeImage var1 = this.textures.computeIfAbsent(this.getSheetLocation(param0), this::loadTexture);
            if (var1 != null) {
                int var2 = getLeft(var0);
                return new LegacyUnicodeBitmapsProvider.Glyph(param0 % 16 * 16 + var2, (param0 & 255) / 16 * 16, getRight(var0) - var2, 16, var1);
            }
        }

        return null;
    }

    @Nullable
    private NativeImage loadTexture(ResourceLocation param0x) {
        try (Resource var0x = this.resourceManager.getResource(param0x)) {
            return NativeImage.read(NativeImage.Format.RGBA, var0x.getInputStream());
        } catch (IOException var16) {
            LOGGER.error("Couldn't load texture {}", param0x, var16);
            return null;
        }
    }

    private static int getLeft(byte param0) {
        return param0 >> 4 & 15;
    }

    private static int getRight(byte param0) {
        return (param0 & 15) + 1;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder implements GlyphProviderBuilder {
        private final ResourceLocation metadata;
        private final String texturePattern;

        public Builder(ResourceLocation param0, String param1) {
            this.metadata = param0;
            this.texturePattern = param1;
        }

        public static GlyphProviderBuilder fromJson(JsonObject param0) {
            return new LegacyUnicodeBitmapsProvider.Builder(
                new ResourceLocation(GsonHelper.getAsString(param0, "sizes")), GsonHelper.getAsString(param0, "template")
            );
        }

        @Nullable
        @Override
        public GlyphProvider create(ResourceManager param0) {
            try (Resource var0 = Minecraft.getInstance().getResourceManager().getResource(this.metadata)) {
                byte[] var1 = new byte[65536];
                var0.getInputStream().read(var1);
                return new LegacyUnicodeBitmapsProvider(param0, var1, this.texturePattern);
            } catch (IOException var17) {
                LegacyUnicodeBitmapsProvider.LOGGER.error("Cannot load {}, unicode glyphs will not render correctly", this.metadata);
                return null;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Glyph implements RawGlyph {
        private final int width;
        private final int height;
        private final int sourceX;
        private final int sourceY;
        private final NativeImage source;

        private Glyph(int param0, int param1, int param2, int param3, NativeImage param4) {
            this.width = param2;
            this.height = param3;
            this.sourceX = param0;
            this.sourceY = param1;
            this.source = param4;
        }

        @Override
        public float getOversample() {
            return 2.0F;
        }

        @Override
        public int getPixelWidth() {
            return this.width;
        }

        @Override
        public int getPixelHeight() {
            return this.height;
        }

        @Override
        public float getAdvance() {
            return (float)(this.width / 2 + 1);
        }

        @Override
        public void upload(int param0, int param1) {
            this.source.upload(0, param0, param1, this.sourceX, this.sourceY, this.width, this.height, false, false);
        }

        @Override
        public boolean isColored() {
            return this.source.format().components() > 1;
        }

        @Override
        public float getShadowOffset() {
            return 0.5F;
        }

        @Override
        public float getBoldOffset() {
            return 0.5F;
        }
    }
}
