package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MobSkinTextureProcessor implements HttpTextureProcessor {
    @Override
    public NativeImage process(NativeImage param0) {
        boolean var0 = param0.getHeight() == 32;
        if (var0) {
            NativeImage var1 = new NativeImage(64, 64, true);
            var1.copyFrom(param0);
            param0.close();
            param0 = var1;
            var1.fillRect(0, 32, 64, 32, 0);
            var1.copyRect(4, 16, 16, 32, 4, 4, true, false);
            var1.copyRect(8, 16, 16, 32, 4, 4, true, false);
            var1.copyRect(0, 20, 24, 32, 4, 12, true, false);
            var1.copyRect(4, 20, 16, 32, 4, 12, true, false);
            var1.copyRect(8, 20, 8, 32, 4, 12, true, false);
            var1.copyRect(12, 20, 16, 32, 4, 12, true, false);
            var1.copyRect(44, 16, -8, 32, 4, 4, true, false);
            var1.copyRect(48, 16, -8, 32, 4, 4, true, false);
            var1.copyRect(40, 20, 0, 32, 4, 12, true, false);
            var1.copyRect(44, 20, -8, 32, 4, 12, true, false);
            var1.copyRect(48, 20, -16, 32, 4, 12, true, false);
            var1.copyRect(52, 20, -8, 32, 4, 12, true, false);
        }

        setNoAlpha(param0, 0, 0, 32, 16);
        if (var0) {
            doLegacyTransparencyHack(param0, 32, 0, 64, 32);
        }

        setNoAlpha(param0, 0, 16, 64, 32);
        setNoAlpha(param0, 16, 48, 48, 64);
        return param0;
    }

    @Override
    public void onTextureDownloaded() {
    }

    private static void doLegacyTransparencyHack(NativeImage param0, int param1, int param2, int param3, int param4) {
        for(int var0 = param1; var0 < param3; ++var0) {
            for(int var1 = param2; var1 < param4; ++var1) {
                int var2 = param0.getPixelRGBA(var0, var1);
                if ((var2 >> 24 & 0xFF) < 128) {
                    return;
                }
            }
        }

        for(int var3 = param1; var3 < param3; ++var3) {
            for(int var4 = param2; var4 < param4; ++var4) {
                param0.setPixelRGBA(var3, var4, param0.getPixelRGBA(var3, var4) & 16777215);
            }
        }

    }

    private static void setNoAlpha(NativeImage param0, int param1, int param2, int param3, int param4) {
        for(int var0 = param1; var0 < param3; ++var0) {
            for(int var1 = param2; var1 < param4; ++var1) {
                param0.setPixelRGBA(var0, var1, param0.getPixelRGBA(var0, var1) | 0xFF000000);
            }
        }

    }
}
