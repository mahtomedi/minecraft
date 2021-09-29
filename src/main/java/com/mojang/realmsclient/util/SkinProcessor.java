package com.mojang.realmsclient.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkinProcessor {
    private int[] pixels;
    private int width;
    private int height;

    @Nullable
    public BufferedImage process(@Nullable BufferedImage param0) {
        if (param0 == null) {
            return null;
        } else {
            this.width = 64;
            this.height = 64;
            BufferedImage var0 = new BufferedImage(this.width, this.height, 2);
            Graphics var1 = var0.getGraphics();
            var1.drawImage(param0, 0, 0, null);
            boolean var2 = param0.getHeight() == 32;
            if (var2) {
                var1.setColor(new Color(0, 0, 0, 0));
                var1.fillRect(0, 32, 64, 32);
                var1.drawImage(var0, 24, 48, 20, 52, 4, 16, 8, 20, null);
                var1.drawImage(var0, 28, 48, 24, 52, 8, 16, 12, 20, null);
                var1.drawImage(var0, 20, 52, 16, 64, 8, 20, 12, 32, null);
                var1.drawImage(var0, 24, 52, 20, 64, 4, 20, 8, 32, null);
                var1.drawImage(var0, 28, 52, 24, 64, 0, 20, 4, 32, null);
                var1.drawImage(var0, 32, 52, 28, 64, 12, 20, 16, 32, null);
                var1.drawImage(var0, 40, 48, 36, 52, 44, 16, 48, 20, null);
                var1.drawImage(var0, 44, 48, 40, 52, 48, 16, 52, 20, null);
                var1.drawImage(var0, 36, 52, 32, 64, 48, 20, 52, 32, null);
                var1.drawImage(var0, 40, 52, 36, 64, 44, 20, 48, 32, null);
                var1.drawImage(var0, 44, 52, 40, 64, 40, 20, 44, 32, null);
                var1.drawImage(var0, 48, 52, 44, 64, 52, 20, 56, 32, null);
            }

            var1.dispose();
            this.pixels = ((DataBufferInt)var0.getRaster().getDataBuffer()).getData();
            this.setNoAlpha(0, 0, 32, 16);
            if (var2) {
                this.doLegacyTransparencyHack(32, 0, 64, 32);
            }

            this.setNoAlpha(0, 16, 64, 32);
            this.setNoAlpha(16, 48, 48, 64);
            return var0;
        }
    }

    private void doLegacyTransparencyHack(int param0, int param1, int param2, int param3) {
        for(int var0 = param0; var0 < param2; ++var0) {
            for(int var1 = param1; var1 < param3; ++var1) {
                int var2 = this.pixels[var0 + var1 * this.width];
                if ((var2 >> 24 & 0xFF) < 128) {
                    return;
                }
            }
        }

        for(int var3 = param0; var3 < param2; ++var3) {
            for(int var4 = param1; var4 < param3; ++var4) {
                this.pixels[var3 + var4 * this.width] &= 16777215;
            }
        }

    }

    private void setNoAlpha(int param0, int param1, int param2, int param3) {
        for(int var0 = param0; var0 < param2; ++var0) {
            for(int var1 = param1; var1 < param3; ++var1) {
                this.pixels[var0 + var1 * this.width] |= -16777216;
            }
        }

    }
}
