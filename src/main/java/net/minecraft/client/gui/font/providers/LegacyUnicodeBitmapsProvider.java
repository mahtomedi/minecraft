package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class LegacyUnicodeBitmapsProvider implements GlyphProvider {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int UNICODE_SHEETS = 256;
    private static final int CODEPOINTS_PER_SHEET = 256;
    private static final int TEXTURE_SIZE = 256;
    private static final byte NO_GLYPH = 0;
    private final ResourceManager resourceManager;
    private final byte[] sizes;
    private final String texturePattern;
    private final Map<ResourceLocation, NativeImage> textures = Maps.newHashMap();

    public LegacyUnicodeBitmapsProvider(ResourceManager param0, byte[] param1, String param2) {
        this.resourceManager = param0;
        this.sizes = param1;
        this.texturePattern = param2;

        for(int var0 = 0; var0 < 256; ++var0) {
            int var1 = var0 * 256;
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
            } catch (IOException var15) {
            }

            Arrays.fill(param1, var1, var1 + 256, (byte)0);
        }

    }

    @Override
    public void close() {
        this.textures.values().forEach(NativeImage::close);
    }

    private ResourceLocation getSheetLocation(int param0) {
        ResourceLocation var0 = new ResourceLocation(String.format(this.texturePattern, String.format("%02x", param0 / 256)));
        return new ResourceLocation(var0.getNamespace(), "textures/" + var0.getPath());
    }

    @Nullable
    @Override
    public GlyphInfo getGlyph(int param0) {
        if (param0 >= 0 && param0 < this.sizes.length) {
            byte var0 = this.sizes[param0];
            if (var0 != 0) {
                NativeImage var1 = this.textures.computeIfAbsent(this.getSheetLocation(param0), this::loadTexture);
                if (var1 != null) {
                    int var2 = getLeft(var0);
                    return new LegacyUnicodeBitmapsProvider.Glyph(param0 % 16 * 16 + var2, (param0 & 0xFF) / 16 * 16, getRight(var0) - var2, 16, var1);
                }
            }

            return null;
        } else {
            return null;
        }
    }

    @Override
    public IntSet getSupportedGlyphs() {
        IntSet var0 = new IntOpenHashSet();

        for(int var1 = 0; var1 < this.sizes.length; ++var1) {
            if (this.sizes[var1] != 0) {
                var0.add(var1);
            }
        }

        return var0;
    }

    @Nullable
    private NativeImage loadTexture(ResourceLocation param0x) {
        try {
            NativeImage var3;
            try (Resource var0x = this.resourceManager.getResource(param0x)) {
                var3 = NativeImage.read(NativeImage.Format.RGBA, var0x.getInputStream());
            }

            return var3;
        } catch (IOException var7) {
            LOGGER.error("Couldn't load texture {}", param0x, var7);
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
            return new LegacyUnicodeBitmapsProvider.Builder(new ResourceLocation(GsonHelper.getAsString(param0, "sizes")), getTemplate(param0));
        }

        private static String getTemplate(JsonObject param0) {
            String var0 = GsonHelper.getAsString(param0, "template");

            try {
                String.format(var0, "");
                return var0;
            } catch (IllegalFormatException var3) {
                throw new JsonParseException("Invalid legacy unicode template supplied, expected single '%s': " + var0);
            }
        }

        @Nullable
        @Override
        public GlyphProvider create(ResourceManager param0) {
            try {
                LegacyUnicodeBitmapsProvider var4;
                try (Resource var0 = Minecraft.getInstance().getResourceManager().getResource(this.metadata)) {
                    byte[] var1 = var0.getInputStream().readNBytes(65536);
                    var4 = new LegacyUnicodeBitmapsProvider(param0, var1, this.texturePattern);
                }

                return var4;
            } catch (IOException var7) {
                LegacyUnicodeBitmapsProvider.LOGGER.error("Cannot load {}, unicode glyphs will not render correctly", this.metadata);
                return null;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record Glyph(int sourceX, int sourceY, int width, int height, NativeImage source) implements GlyphInfo {
        @Override
        public float getAdvance() {
            return (float)(this.width / 2 + 1);
        }

        @Override
        public float getShadowOffset() {
            return 0.5F;
        }

        @Override
        public float getBoldOffset() {
            return 0.5F;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> param0) {
            return param0.apply(new SheetGlyphInfo() {
                @Override
                public float getOversample() {
                    return 2.0F;
                }

                @Override
                public int getPixelWidth() {
                    return Glyph.this.width;
                }

                @Override
                public int getPixelHeight() {
                    return Glyph.this.height;
                }

                @Override
                public void upload(int param0, int param1) {
                    Glyph.this.source.upload(0, param0, param1, Glyph.this.sourceX, Glyph.this.sourceY, Glyph.this.width, Glyph.this.height, false, false);
                }

                @Override
                public boolean isColored() {
                    return Glyph.this.source.format().components() > 1;
                }
            });
        }
    }
}
