package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.gui.font.glyphs.MissingGlyph;
import net.minecraft.client.gui.font.glyphs.WhiteGlyph;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class FontSet implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final EmptyGlyph SPACE_GLYPH = new EmptyGlyph();
    private static final GlyphInfo SPACE_INFO = () -> 4.0F;
    private static final Random RANDOM = new Random();
    private final TextureManager textureManager;
    private final ResourceLocation name;
    private BakedGlyph missingGlyph;
    private BakedGlyph whiteGlyph;
    private final List<GlyphProvider> providers = Lists.newArrayList();
    private final Char2ObjectMap<BakedGlyph> glyphs = new Char2ObjectOpenHashMap<>();
    private final Char2ObjectMap<GlyphInfo> glyphInfos = new Char2ObjectOpenHashMap<>();
    private final Int2ObjectMap<CharList> glyphsByWidth = new Int2ObjectOpenHashMap<>();
    private final List<FontTexture> textures = Lists.newArrayList();

    public FontSet(TextureManager param0, ResourceLocation param1) {
        this.textureManager = param0;
        this.name = param1;
    }

    public void reload(List<GlyphProvider> param0) {
        for(GlyphProvider var0 : this.providers) {
            var0.close();
        }

        this.providers.clear();
        this.closeTextures();
        this.textures.clear();
        this.glyphs.clear();
        this.glyphInfos.clear();
        this.glyphsByWidth.clear();
        this.missingGlyph = this.stitch(MissingGlyph.INSTANCE);
        this.whiteGlyph = this.stitch(WhiteGlyph.INSTANCE);
        Set<GlyphProvider> var1 = Sets.newHashSet();

        for(char var2 = 0; var2 < '\uffff'; ++var2) {
            for(GlyphProvider var3 : param0) {
                GlyphInfo var4 = (GlyphInfo)(var2 == ' ' ? SPACE_INFO : var3.getGlyph(var2));
                if (var4 != null) {
                    var1.add(var3);
                    if (var4 != MissingGlyph.INSTANCE) {
                        this.glyphsByWidth.computeIfAbsent(Mth.ceil(var4.getAdvance(false)), param0x -> new CharArrayList()).add(var2);
                    }
                    break;
                }
            }
        }

        param0.stream().filter(var1::contains).forEach(this.providers::add);
    }

    @Override
    public void close() {
        this.closeTextures();
    }

    public void closeTextures() {
        for(FontTexture var0 : this.textures) {
            var0.close();
        }

    }

    public GlyphInfo getGlyphInfo(char param0) {
        return this.glyphInfos.computeIfAbsent(param0, param0x -> (GlyphInfo)(param0x == 32 ? SPACE_INFO : this.getRaw((char)param0x)));
    }

    private RawGlyph getRaw(char param0) {
        for(GlyphProvider var0 : this.providers) {
            RawGlyph var1 = var0.getGlyph(param0);
            if (var1 != null) {
                return var1;
            }
        }

        return MissingGlyph.INSTANCE;
    }

    public BakedGlyph getGlyph(char param0) {
        return this.glyphs.computeIfAbsent(param0, param0x -> (BakedGlyph)(param0x == 32 ? SPACE_GLYPH : this.stitch(this.getRaw((char)param0x))));
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
        CharList var0 = this.glyphsByWidth.get(Mth.ceil(param0.getAdvance(false)));
        return var0 != null && !var0.isEmpty() ? this.getGlyph(var0.get(RANDOM.nextInt(var0.size()))) : this.missingGlyph;
    }

    public BakedGlyph whiteGlyph() {
        return this.whiteGlyph;
    }
}
