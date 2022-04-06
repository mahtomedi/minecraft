package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum SpecialGlyphs implements GlyphInfo {
    WHITE(() -> generate(5, 8, (param0, param1) -> -1)),
    MISSING(() -> {
        int var0 = 5;
        int var1 = 8;
        return generate(5, 8, (param0, param1) -> {
            boolean var0x = param0 == 0 || param0 + 1 == 5 || param1 == 0 || param1 + 1 == 8;
            return var0x ? -1 : 0;
        });
    });

    final NativeImage image;

    private static NativeImage generate(int param0, int param1, SpecialGlyphs.PixelProvider param2) {
        NativeImage var0 = new NativeImage(NativeImage.Format.RGBA, param0, param1, false);

        for(int var1 = 0; var1 < param1; ++var1) {
            for(int var2 = 0; var2 < param0; ++var2) {
                var0.setPixelRGBA(var2, var1, param2.getColor(var2, var1));
            }
        }

        var0.untrack();
        return var0;
    }

    private SpecialGlyphs(Supplier<NativeImage> param0) {
        this.image = param0.get();
    }

    @Override
    public float getAdvance() {
        return (float)(this.image.getWidth() + 1);
    }

    @Override
    public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> param0) {
        return param0.apply(new SheetGlyphInfo() {
            @Override
            public int getPixelWidth() {
                return SpecialGlyphs.this.image.getWidth();
            }

            @Override
            public int getPixelHeight() {
                return SpecialGlyphs.this.image.getHeight();
            }

            @Override
            public float getOversample() {
                return 1.0F;
            }

            @Override
            public void upload(int param0, int param1) {
                SpecialGlyphs.this.image.upload(0, param0, param1, false);
            }

            @Override
            public boolean isColored() {
                return true;
            }
        });
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    interface PixelProvider {
        int getColor(int var1, int var2);
    }
}
