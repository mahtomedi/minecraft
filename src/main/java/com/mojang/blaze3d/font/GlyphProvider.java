package com.mojang.blaze3d.font;

import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.Closeable;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface GlyphProvider extends Closeable {
    @Override
    default void close() {
    }

    @Nullable
    default RawGlyph getGlyph(int param0) {
        return null;
    }

    IntSet getSupportedGlyphs();
}
