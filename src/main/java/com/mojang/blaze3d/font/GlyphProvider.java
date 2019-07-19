package com.mojang.blaze3d.font;

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
    default RawGlyph getGlyph(char param0) {
        return null;
    }
}
