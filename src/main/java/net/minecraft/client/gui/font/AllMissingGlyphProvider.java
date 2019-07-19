package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.glyphs.MissingGlyph;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AllMissingGlyphProvider implements GlyphProvider {
    @Nullable
    @Override
    public RawGlyph getGlyph(char param0) {
        return MissingGlyph.INSTANCE;
    }
}
