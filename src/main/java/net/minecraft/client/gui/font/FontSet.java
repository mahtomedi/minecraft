package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.gui.font.glyphs.MissingGlyph;
import net.minecraft.client.gui.font.glyphs.WhiteGlyph;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FontSet implements AutoCloseable {
    private static final EmptyGlyph SPACE_GLYPH = new EmptyGlyph();
    private static final GlyphInfo SPACE_INFO = () -> 4.0F;
    private static final GlyphInfo ZERO_WIDTH_NO_JOIN_INFO = () -> 0.0F;
    private static final int ZERO_WIDTH_NO_JOIN_CODEPOINT = 8204;
    private static final Random RANDOM = new Random();
    private final TextureManager textureManager;
    private final ResourceLocation name;
    private BakedGlyph missingGlyph;
    private BakedGlyph whiteGlyph;
    private final List<GlyphProvider> providers = Lists.newArrayList();
    private final Int2ObjectMap<BakedGlyph> glyphs = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<GlyphInfo> glyphInfos = new Int2ObjectOpenHashMap<>();
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
        this.missingGlyph = this.stitch(MissingGlyph.INSTANCE);
        this.whiteGlyph = this.stitch(WhiteGlyph.INSTANCE);
        IntSet var0 = new IntOpenHashSet();

        for(GlyphProvider var1 : param0) {
            var0.addAll(var1.getSupportedGlyphs());
        }

        Set<GlyphProvider> var2 = Sets.newHashSet();
        var0.forEach(param2 -> {
            for(GlyphProvider var0x : param0) {
                GlyphInfo var1x = this.getGlyphInfoForSpace(param2);
                if (var1x == null) {
                    var1x = var0x.getGlyph(param2);
                }

                if (var1x != null) {
                    var2.add(var0x);
                    if (var1x != MissingGlyph.INSTANCE) {
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

    @Nullable
    private GlyphInfo getGlyphInfoForSpace(int param0) {
        return switch(param0) {
            case 32 -> SPACE_INFO;
            case 8204 -> ZERO_WIDTH_NO_JOIN_INFO;
            default -> null;
        };
    }

    public GlyphInfo getGlyphInfo(int param0) {
        return this.glyphInfos.computeIfAbsent(param0, param0x -> {
            GlyphInfo var0 = this.getGlyphInfoForSpace(param0x);
            return (GlyphInfo)(var0 == null ? this.getRaw(param0x) : var0);
        });
    }

    private RawGlyph getRaw(int param0) {
        for(GlyphProvider var0 : this.providers) {
            RawGlyph var1 = var0.getGlyph(param0);
            if (var1 != null) {
                return var1;
            }
        }

        return MissingGlyph.INSTANCE;
    }

    public BakedGlyph getGlyph(int param0) {
        return this.glyphs.computeIfAbsent(param0, param0x -> {
            return (BakedGlyph)(switch(param0x) {
                case 32, 8204 -> SPACE_GLYPH;
                default -> this.stitch(this.getRaw(param0x));
            });
        });
    }

    private BakedGlyph stitch(RawGlyph param0) {
        for(FontTexture var0 : this.textures) {
            BakedGlyph var1 = var0.add(param0);
            if (var1 != null) {
                return var1;
            }
        }

        FontTexture var2 = new FontTexture(new ResourceLocation(this.name.getNamespace(), this.name.getPath() + "/" + this.textures.size()), param0.isColored());
        this.textures.add(var2);
        this.textureManager.register(var2.getName(), var2);
        BakedGlyph var3 = var2.add(param0);
        return var3 == null ? this.missingGlyph : var3;
    }

    public BakedGlyph getRandomGlyph(GlyphInfo param0) {
        IntList var0 = this.glyphsByWidth.get(Mth.ceil(param0.getAdvance(false)));
        return var0 != null && !var0.isEmpty() ? this.getGlyph(var0.getInt(RANDOM.nextInt(var0.size()))) : this.missingGlyph;
    }

    public BakedGlyph whiteGlyph() {
        return this.whiteGlyph;
    }
}
