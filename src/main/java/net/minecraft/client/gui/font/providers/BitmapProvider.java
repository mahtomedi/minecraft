package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BitmapProvider implements GlyphProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private final NativeImage image;
    private final Char2ObjectMap<BitmapProvider.Glyph> glyphs;

    public BitmapProvider(NativeImage param0, Char2ObjectMap<BitmapProvider.Glyph> param1) {
        this.image = param0;
        this.glyphs = param1;
    }

    @Override
    public void close() {
        this.image.close();
    }

    @Nullable
    @Override
    public RawGlyph getGlyph(char param0) {
        return this.glyphs.get(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder implements GlyphProviderBuilder {
        private final ResourceLocation texture;
        private final List<String> chars;
        private final int height;
        private final int ascent;

        public Builder(ResourceLocation param0, int param1, int param2, List<String> param3) {
            this.texture = new ResourceLocation(param0.getNamespace(), "textures/" + param0.getPath());
            this.chars = param3;
            this.height = param1;
            this.ascent = param2;
        }

        public static BitmapProvider.Builder fromJson(JsonObject param0) {
            int var0 = GsonHelper.getAsInt(param0, "height", 8);
            int var1 = GsonHelper.getAsInt(param0, "ascent");
            if (var1 > var0) {
                throw new JsonParseException("Ascent " + var1 + " higher than height " + var0);
            } else {
                List<String> var2 = Lists.newArrayList();
                JsonArray var3 = GsonHelper.getAsJsonArray(param0, "chars");

                for(int var4 = 0; var4 < var3.size(); ++var4) {
                    String var5 = GsonHelper.convertToString(var3.get(var4), "chars[" + var4 + "]");
                    if (var4 > 0) {
                        int var6 = var5.length();
                        int var7 = var2.get(0).length();
                        if (var6 != var7) {
                            throw new JsonParseException(
                                "Elements of chars have to be the same length (found: " + var6 + ", expected: " + var7 + "), pad with space or \\u0000"
                            );
                        }
                    }

                    var2.add(var5);
                }

                if (!var2.isEmpty() && !var2.get(0).isEmpty()) {
                    return new BitmapProvider.Builder(new ResourceLocation(GsonHelper.getAsString(param0, "file")), var0, var1, var2);
                } else {
                    throw new JsonParseException("Expected to find data in chars, found none.");
                }
            }
        }

        @Nullable
        @Override
        public GlyphProvider create(ResourceManager param0) {
            try (Resource var0 = param0.getResource(this.texture)) {
                NativeImage var1 = NativeImage.read(NativeImage.Format.RGBA, var0.getInputStream());
                int var2 = var1.getWidth();
                int var3 = var1.getHeight();
                int var4 = var2 / this.chars.get(0).length();
                int var5 = var3 / this.chars.size();
                float var6 = (float)this.height / (float)var5;
                Char2ObjectMap<BitmapProvider.Glyph> var7 = new Char2ObjectOpenHashMap<>();

                for(int var8 = 0; var8 < this.chars.size(); ++var8) {
                    String var9 = this.chars.get(var8);

                    for(int var10 = 0; var10 < var9.length(); ++var10) {
                        char var11 = var9.charAt(var10);
                        if (var11 != 0 && var11 != ' ') {
                            int var12 = this.getActualGlyphWidth(var1, var4, var5, var10, var8);
                            BitmapProvider.Glyph var13 = var7.put(
                                var11,
                                new BitmapProvider.Glyph(
                                    var6, var1, var10 * var4, var8 * var5, var4, var5, (int)(0.5 + (double)((float)var12 * var6)) + 1, this.ascent
                                )
                            );
                            if (var13 != null) {
                                BitmapProvider.LOGGER.warn("Codepoint '{}' declared multiple times in {}", Integer.toHexString(var11), this.texture);
                            }
                        }
                    }
                }

                return new BitmapProvider(var1, var7);
            } catch (IOException var27) {
                throw new RuntimeException(var27.getMessage());
            }
        }

        private int getActualGlyphWidth(NativeImage param0, int param1, int param2, int param3, int param4) {
            int var0;
            for(var0 = param1 - 1; var0 >= 0; --var0) {
                int var1 = param3 * param1 + var0;

                for(int var2 = 0; var2 < param2; ++var2) {
                    int var3 = param4 * param2 + var2;
                    if (param0.getLuminanceOrAlpha(var1, var3) != 0) {
                        return var0 + 1;
                    }
                }
            }

            return var0 + 1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static final class Glyph implements RawGlyph {
        private final float scale;
        private final NativeImage image;
        private final int offsetX;
        private final int offsetY;
        private final int width;
        private final int height;
        private final int advance;
        private final int ascent;

        private Glyph(float param0, NativeImage param1, int param2, int param3, int param4, int param5, int param6, int param7) {
            this.scale = param0;
            this.image = param1;
            this.offsetX = param2;
            this.offsetY = param3;
            this.width = param4;
            this.height = param5;
            this.advance = param6;
            this.ascent = param7;
        }

        @Override
        public float getOversample() {
            return 1.0F / this.scale;
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
            return (float)this.advance;
        }

        @Override
        public float getBearingY() {
            return RawGlyph.super.getBearingY() + 7.0F - (float)this.ascent;
        }

        @Override
        public void upload(int param0, int param1) {
            this.image.upload(0, param0, param1, this.offsetX, this.offsetY, this.width, this.height, false);
        }

        @Override
        public boolean isColored() {
            return this.image.format().components() > 1;
        }
    }
}
