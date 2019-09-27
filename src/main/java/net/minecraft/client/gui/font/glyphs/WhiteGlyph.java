package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.font.RawGlyph;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum WhiteGlyph implements RawGlyph {
    INSTANCE;

    private static final NativeImage IMAGE_DATA = Util.make(new NativeImage(NativeImage.Format.RGBA, 5, 8, false), param0 -> {
        for(int var0 = 0; var0 < 8; ++var0) {
            for(int var1 = 0; var1 < 5; ++var1) {
                if (var1 != 0 && var1 + 1 != 5 && var0 != 0 && var0 + 1 != 8) {
                    boolean var4 = false;
                } else {
                    boolean var10000 = true;
                }

                param0.setPixelRGBA(var1, var0, -1);
            }
        }

        param0.untrack();
    });

    @Override
    public int getPixelWidth() {
        return 5;
    }

    @Override
    public int getPixelHeight() {
        return 8;
    }

    @Override
    public float getAdvance() {
        return 6.0F;
    }

    @Override
    public float getOversample() {
        return 1.0F;
    }

    @Override
    public void upload(int param0, int param1) {
        IMAGE_DATA.upload(0, param0, param1, false);
    }

    @Override
    public boolean isColored() {
        return true;
    }
}
