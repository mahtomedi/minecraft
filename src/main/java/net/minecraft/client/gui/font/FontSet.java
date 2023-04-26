package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FontSet implements AutoCloseable {
    private static final RandomSource RANDOM = RandomSource.create();
    private static final float LARGE_FORWARD_ADVANCE = 32.0F;
    private final TextureManager textureManager;
    private final ResourceLocation name;
    private BakedGlyph missingGlyph;
    private BakedGlyph whiteGlyph;
    private final List<GlyphProvider> providers = Lists.newArrayList();
    private final CodepointMap<BakedGlyph> glyphs = new CodepointMap<>(param0x -> new BakedGlyph[param0x], param0x -> new BakedGlyph[param0x][]);
    private final CodepointMap<FontSet.GlyphInfoFilter> glyphInfos = new CodepointMap<>(
        param0x -> new FontSet.GlyphInfoFilter[param0x], param0x -> new FontSet.GlyphInfoFilter[param0x][]
    );
    private final Int2ObjectMap<IntList> glyphsByWidth = new Int2ObjectOpenHashMap<>();
    private final List<FontTexture> textures = Lists.newArrayList();

    public FontSet(TextureManager param0, ResourceLocation param1) {
        this.textureManager = param0;
        this.name = param1;
    }

    public void reload(List<GlyphProvider> param0) {
        this.closeProviders();
        this.closeTextures();
        this.glyphs.clear();
        this.glyphInfos.clear();
        this.glyphsByWidth.clear();
        this.missingGlyph = SpecialGlyphs.MISSING.bake(this::stitch);
        this.whiteGlyph = SpecialGlyphs.WHITE.bake(this::stitch);
        IntSet var0 = new IntOpenHashSet();

        for(GlyphProvider var1 : param0) {
            var0.addAll(var1.getSupportedGlyphs());
        }

        Set<GlyphProvider> var2 = Sets.newHashSet();
        var0.forEach(param2 -> {
            for(GlyphProvider var0x : param0) {
                GlyphInfo var1x = var0x.getGlyph(param2);
                if (var1x != null) {
                    var2.add(var0x);
                    if (var1x != SpecialGlyphs.MISSING) {
                        this.glyphsByWidth.computeIfAbsent(Mth.ceil(var1x.getAdvance(false)), param0x -> new IntArrayList()).add(param2);
                    }
                    break;
                }
            }

        });
        param0.stream().filter(var2::contains).forEach(this.providers::add);
    }

    @Override
    public void close() {
        this.closeProviders();
        this.closeTextures();
    }

    private void closeProviders() {
        for(GlyphProvider var0 : this.providers) {
            var0.close();
        }

        this.providers.clear();
    }

    private void closeTextures() {
        for(FontTexture var0 : this.textures) {
            var0.close();
        }

        this.textures.clear();
    }

    private static boolean hasFishyAdvance(GlyphInfo param0) {
        float var0 = param0.getAdvance(false);
        if (!(var0 < 0.0F) && !(var0 > 32.0F)) {
            float var1 = param0.getAdvance(true);
            return var1 < 0.0F || var1 > 32.0F;
        } else {
            return true;
        }
    }

    private FontSet.GlyphInfoFilter computeGlyphInfo(int param0) {
        GlyphInfo var0 = null;

        for(GlyphProvider var1 : this.providers) {
            GlyphInfo var2 = var1.getGlyph(param0);
            if (var2 != null) {
                if (var0 == null) {
                    var0 = var2;
                }

                if (!hasFishyAdvance(var2)) {
                    return new FontSet.GlyphInfoFilter(var0, var2);
                }
            }
        }

        return var0 != null ? new FontSet.GlyphInfoFilter(var0, SpecialGlyphs.MISSING) : FontSet.GlyphInfoFilter.MISSING;
    }

    public GlyphInfo getGlyphInfo(int param0, boolean param1) {
        return this.glyphInfos.computeIfAbsent(param0, this::computeGlyphInfo).select(param1);
    }

    private BakedGlyph computeBakedGlyph(int param0) {
        for(GlyphProvider var0 : this.providers) {
            GlyphInfo var1 = var0.getGlyph(param0);
            if (var1 != null) {
                return var1.bake(this::stitch);
            }
        }

        return this.missingGlyph;
    }

    public BakedGlyph getGlyph(int param0) {
        return this.glyphs.computeIfAbsent(param0, this::computeBakedGlyph);
    }

    private BakedGlyph stitch(SheetGlyphInfo param0x) {
        for(FontTexture var0x : this.textures) {
            BakedGlyph var1x = var0x.add(param0x);
            if (var1x != null) {
                return var1x;
            }
        }

        ResourceLocation var2x = this.name.withSuffix("/" + this.textures.size());
        boolean var3 = param0x.isColored();
        GlyphRenderTypes var4 = var3 ? GlyphRenderTypes.createForColorTexture(var2x) : GlyphRenderTypes.createForIntensityTexture(var2x);
        FontTexture var5 = new FontTexture(var4, var3);
        this.textures.add(var5);
        this.textureManager.register(var2x, var5);
        BakedGlyph var6 = var5.add(param0x);
        return var6 == null ? this.missingGlyph : var6;
    }

    public BakedGlyph getRandomGlyph(GlyphInfo param0) {
        IntList var0 = this.glyphsByWidth.get(Mth.ceil(param0.getAdvance(false)));
        return var0 != null && !var0.isEmpty() ? this.getGlyph(var0.getInt(RANDOM.nextInt(var0.size()))) : this.missingGlyph;
    }

    public BakedGlyph whiteGlyph() {
        return this.whiteGlyph;
    }

    @OnlyIn(Dist.CLIENT)
    static record GlyphInfoFilter(GlyphInfo glyphInfo, GlyphInfo glyphInfoNotFishy) {
        static final FontSet.GlyphInfoFilter MISSING = new FontSet.GlyphInfoFilter(SpecialGlyphs.MISSING, SpecialGlyphs.MISSING);

        GlyphInfo select(boolean param0) {
            return param0 ? this.glyphInfoNotFishy : this.glyphInfo;
        }
    }
}
