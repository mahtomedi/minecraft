package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MipmapGenerator {
    private static final int ALPHA_CUTOUT_CUTOFF = 96;
    private static final float[] POW22 = Util.make(new float[256], param0 -> {
        for(int var0 = 0; var0 < param0.length; ++var0) {
            param0[var0] = (float)Math.pow((double)((float)var0 / 255.0F), 2.2);
        }

    });

    private MipmapGenerator() {
    }

    public static NativeImage[] generateMipLevels(NativeImage[] param0, int param1) {
        if (param1 + 1 <= param0.length) {
            return param0;
        } else {
            NativeImage[] var0 = new NativeImage[param1 + 1];
            var0[0] = param0[0];
            boolean var1 = hasTransparentPixel(var0[0]);

            for(int var2 = 1; var2 <= param1; ++var2) {
                if (var2 < param0.length) {
                    var0[var2] = param0[var2];
                } else {
                    NativeImage var3 = var0[var2 - 1];
                    NativeImage var4 = new NativeImage(var3.getWidth() >> 1, var3.getHeight() >> 1, false);
                    int var5 = var4.getWidth();
                    int var6 = var4.getHeight();

                    for(int var7 = 0; var7 < var5; ++var7) {
                        for(int var8 = 0; var8 < var6; ++var8) {
                            var4.setPixelRGBA(
                                var7,
                                var8,
                                alphaBlend(
                                    var3.getPixelRGBA(var7 * 2 + 0, var8 * 2 + 0),
                                    var3.getPixelRGBA(var7 * 2 + 1, var8 * 2 + 0),
                                    var3.getPixelRGBA(var7 * 2 + 0, var8 * 2 + 1),
                                    var3.getPixelRGBA(var7 * 2 + 1, var8 * 2 + 1),
                                    var1
                                )
                            );
                        }
                    }

                    var0[var2] = var4;
                }
            }

            return var0;
        }
    }

    private static boolean hasTransparentPixel(NativeImage param0) {
        for(int var0 = 0; var0 < param0.getWidth(); ++var0) {
            for(int var1 = 0; var1 < param0.getHeight(); ++var1) {
                if (param0.getPixelRGBA(var0, var1) >> 24 == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private static int alphaBlend(int param0, int param1, int param2, int param3, boolean param4) {
        if (param4) {
            float var0 = 0.0F;
            float var1 = 0.0F;
            float var2 = 0.0F;
            float var3 = 0.0F;
            if (param0 >> 24 != 0) {
                var0 += getPow22(param0 >> 24);
                var1 += getPow22(param0 >> 16);
                var2 += getPow22(param0 >> 8);
                var3 += getPow22(param0 >> 0);
            }

            if (param1 >> 24 != 0) {
                var0 += getPow22(param1 >> 24);
                var1 += getPow22(param1 >> 16);
                var2 += getPow22(param1 >> 8);
                var3 += getPow22(param1 >> 0);
            }

            if (param2 >> 24 != 0) {
                var0 += getPow22(param2 >> 24);
                var1 += getPow22(param2 >> 16);
                var2 += getPow22(param2 >> 8);
                var3 += getPow22(param2 >> 0);
            }

            if (param3 >> 24 != 0) {
                var0 += getPow22(param3 >> 24);
                var1 += getPow22(param3 >> 16);
                var2 += getPow22(param3 >> 8);
                var3 += getPow22(param3 >> 0);
            }

            var0 /= 4.0F;
            var1 /= 4.0F;
            var2 /= 4.0F;
            var3 /= 4.0F;
            int var4 = (int)(Math.pow((double)var0, 0.45454545454545453) * 255.0);
            int var5 = (int)(Math.pow((double)var1, 0.45454545454545453) * 255.0);
            int var6 = (int)(Math.pow((double)var2, 0.45454545454545453) * 255.0);
            int var7 = (int)(Math.pow((double)var3, 0.45454545454545453) * 255.0);
            if (var4 < 96) {
                var4 = 0;
            }

            return var4 << 24 | var5 << 16 | var6 << 8 | var7;
        } else {
            int var8 = gammaBlend(param0, param1, param2, param3, 24);
            int var9 = gammaBlend(param0, param1, param2, param3, 16);
            int var10 = gammaBlend(param0, param1, param2, param3, 8);
            int var11 = gammaBlend(param0, param1, param2, param3, 0);
            return var8 << 24 | var9 << 16 | var10 << 8 | var11;
        }
    }

    private static int gammaBlend(int param0, int param1, int param2, int param3, int param4) {
        float var0 = getPow22(param0 >> param4);
        float var1 = getPow22(param1 >> param4);
        float var2 = getPow22(param2 >> param4);
        float var3 = getPow22(param3 >> param4);
        float var4 = (float)((double)((float)Math.pow((double)(var0 + var1 + var2 + var3) * 0.25, 0.45454545454545453)));
        return (int)((double)var4 * 255.0);
    }

    private static float getPow22(int param0) {
        return POW22[param0 & 0xFF];
    }
}
