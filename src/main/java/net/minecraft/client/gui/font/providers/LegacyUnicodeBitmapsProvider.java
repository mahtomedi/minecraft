package net.minecraft.client.gui.font.providers;

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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class LegacyUnicodeBitmapsProvider implements GlyphProvider {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int UNICODE_SHEETS = 256;
    private static final int CODEPOINTS_PER_SHEET = 256;
    private static final int TEXTURE_SIZE = 256;
    private static final byte NO_GLYPH = 0;
    private static final int TOTAL_CODEPOINTS = 65536;
    private final byte[] sizes;
    private final LegacyUnicodeBitmapsProvider.Sheet[] sheets = new LegacyUnicodeBitmapsProvider.Sheet[256];

    public LegacyUnicodeBitmapsProvider(ResourceManager param0, byte[] param1, String param2) {
        this.sizes = param1;
        Set<ResourceLocation> var0 = new HashSet<>();

        for(int var1 = 0; var1 < 256; ++var1) {
            int var2 = var1 * 256;
            var0.add(getSheetLocation(param2, var2));
        }

        String var3 = getCommonSearchPrefix(var0);
        Map<ResourceLocation, CompletableFuture<NativeImage>> var4 = new HashMap<>();
        param0.listResources(var3, var0::contains).forEach((param1x, param2x) -> var4.put(param1x, CompletableFuture.supplyAsync(() -> {
                try {
                    NativeImage var3x;
                    try (InputStream var0x = param2x.open()) {
                        var3x = NativeImage.read(NativeImage.Format.RGBA, var0x);
                    }

                    return var3x;
                } catch (IOException var7x) {
                    LOGGER.error("Failed to read resource {} from pack {}", param1x, param2x.sourcePackId());
                    return null;
                }
            }, Util.backgroundExecutor())));
        List<CompletableFuture<?>> var5 = new ArrayList<>(256);

        for(int var6 = 0; var6 < 256; ++var6) {
            int var7 = var6 * 256;
            int var8 = var6;
            ResourceLocation var9 = getSheetLocation(param2, var7);
            CompletableFuture<NativeImage> var10 = var4.get(var9);
            if (var10 != null) {
                var5.add(var10.thenAcceptAsync(param3 -> {
                    if (param3 != null) {
                        if (param3.getWidth() == 256 && param3.getHeight() == 256) {
                            for(int var0x = 0; var0x < 256; ++var0x) {
                                byte var1x = param1[var7 + var0x];
                                if (var1x != 0 && getLeft(var1x) > getRight(var1x)) {
                                    param1[var7 + var0x] = 0;
                                }
                            }

                            this.sheets[var8] = new LegacyUnicodeBitmapsProvider.Sheet(param1, param3);
                        } else {
                            param3.close();
                            Arrays.fill(param1, var7, var7 + 256, (byte)0);
                        }

                    }
                }, Util.backgroundExecutor()));
            }
        }

        CompletableFuture.allOf((CompletableFuture<?>[])var5.toArray(param0x -> new CompletableFuture[param0x])).join();
    }

    private static String getCommonSearchPrefix(Set<ResourceLocation> param0) {
        String var0 = StringUtils.getCommonPrefix(param0.stream().map(ResourceLocation::getPath).toArray(param0x -> new String[param0x]));
        int var1 = var0.lastIndexOf("/");
        return var1 == -1 ? "" : var0.substring(0, var1);
    }

    @Override
    public void close() {
        for(LegacyUnicodeBitmapsProvider.Sheet var0 : this.sheets) {
            if (var0 != null) {
                var0.close();
            }
        }

    }

    private static ResourceLocation getSheetLocation(String param0, int param1) {
        String var0 = String.format(Locale.ROOT, "%02x", param1 / 256);
        ResourceLocation var1 = new ResourceLocation(String.format(Locale.ROOT, param0, var0));
        return var1.withPrefix("textures/");
    }

    @Nullable
    @Override
    public GlyphInfo getGlyph(int param0) {
        if (param0 >= 0 && param0 < this.sizes.length) {
            int var0 = param0 / 256;
            LegacyUnicodeBitmapsProvider.Sheet var1 = this.sheets[var0];
            return var1 != null ? var1.getGlyph(param0) : null;
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

    static int getLeft(byte param0) {
        return param0 >> 4 & 15;
    }

    static int getRight(byte param0) {
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
                String.format(Locale.ROOT, var0, "");
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
                try (InputStream var0 = Minecraft.getInstance().getResourceManager().open(this.metadata)) {
                    byte[] var1 = var0.readNBytes(65536);
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

    @OnlyIn(Dist.CLIENT)
    static class Sheet implements AutoCloseable {
        private final byte[] sizes;
        private final NativeImage source;

        Sheet(byte[] param0, NativeImage param1) {
            this.sizes = param0;
            this.source = param1;
        }

        @Override
        public void close() {
            this.source.close();
        }

        @Nullable
        public GlyphInfo getGlyph(int param0) {
            byte var0 = this.sizes[param0];
            if (var0 != 0) {
                int var1 = LegacyUnicodeBitmapsProvider.getLeft(var0);
                return new LegacyUnicodeBitmapsProvider.Glyph(
                    param0 % 16 * 16 + var1, (param0 & 0xFF) / 16 * 16, LegacyUnicodeBitmapsProvider.getRight(var0) - var1, 16, this.source
                );
            } else {
                return null;
            }
        }
    }
}
