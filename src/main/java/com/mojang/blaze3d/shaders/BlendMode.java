package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Locale;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlendMode {
    private static BlendMode lastApplied;
    private final int srcColorFactor;
    private final int srcAlphaFactor;
    private final int dstColorFactor;
    private final int dstAlphaFactor;
    private final int blendFunc;
    private final boolean separateBlend;
    private final boolean opaque;

    private BlendMode(boolean param0, boolean param1, int param2, int param3, int param4, int param5, int param6) {
        this.separateBlend = param0;
        this.srcColorFactor = param2;
        this.dstColorFactor = param3;
        this.srcAlphaFactor = param4;
        this.dstAlphaFactor = param5;
        this.opaque = param1;
        this.blendFunc = param6;
    }

    public BlendMode() {
        this(false, true, 1, 0, 1, 0, 32774);
    }

    public BlendMode(int param0, int param1, int param2) {
        this(false, false, param0, param1, param0, param1, param2);
    }

    public BlendMode(int param0, int param1, int param2, int param3, int param4) {
        this(true, false, param0, param1, param2, param3, param4);
    }

    public void apply() {
        if (!this.equals(lastApplied)) {
            if (lastApplied == null || this.opaque != lastApplied.isOpaque()) {
                lastApplied = this;
                if (this.opaque) {
                    GlStateManager.disableBlend();
                    return;
                }

                GlStateManager.enableBlend();
            }

            GlStateManager.blendEquation(this.blendFunc);
            if (this.separateBlend) {
                GlStateManager.blendFuncSeparate(this.srcColorFactor, this.dstColorFactor, this.srcAlphaFactor, this.dstAlphaFactor);
            } else {
                GlStateManager.blendFunc(this.srcColorFactor, this.dstColorFactor);
            }

        }
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof BlendMode)) {
            return false;
        } else {
            BlendMode var0 = (BlendMode)param0;
            if (this.blendFunc != var0.blendFunc) {
                return false;
            } else if (this.dstAlphaFactor != var0.dstAlphaFactor) {
                return false;
            } else if (this.dstColorFactor != var0.dstColorFactor) {
                return false;
            } else if (this.opaque != var0.opaque) {
                return false;
            } else if (this.separateBlend != var0.separateBlend) {
                return false;
            } else if (this.srcAlphaFactor != var0.srcAlphaFactor) {
                return false;
            } else {
                return this.srcColorFactor == var0.srcColorFactor;
            }
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.srcColorFactor;
        var0 = 31 * var0 + this.srcAlphaFactor;
        var0 = 31 * var0 + this.dstColorFactor;
        var0 = 31 * var0 + this.dstAlphaFactor;
        var0 = 31 * var0 + this.blendFunc;
        var0 = 31 * var0 + (this.separateBlend ? 1 : 0);
        return 31 * var0 + (this.opaque ? 1 : 0);
    }

    public boolean isOpaque() {
        return this.opaque;
    }

    public static int stringToBlendFunc(String param0) {
        String var0 = param0.trim().toLowerCase(Locale.ROOT);
        if ("add".equals(var0)) {
            return 32774;
        } else if ("subtract".equals(var0)) {
            return 32778;
        } else if ("reversesubtract".equals(var0)) {
            return 32779;
        } else if ("reverse_subtract".equals(var0)) {
            return 32779;
        } else if ("min".equals(var0)) {
            return 32775;
        } else {
            return "max".equals(var0) ? 32776 : 32774;
        }
    }

    public static int stringToBlendFactor(String param0) {
        String var0 = param0.trim().toLowerCase(Locale.ROOT);
        var0 = var0.replaceAll("_", "");
        var0 = var0.replaceAll("one", "1");
        var0 = var0.replaceAll("zero", "0");
        var0 = var0.replaceAll("minus", "-");
        if ("0".equals(var0)) {
            return 0;
        } else if ("1".equals(var0)) {
            return 1;
        } else if ("srccolor".equals(var0)) {
            return 768;
        } else if ("1-srccolor".equals(var0)) {
            return 769;
        } else if ("dstcolor".equals(var0)) {
            return 774;
        } else if ("1-dstcolor".equals(var0)) {
            return 775;
        } else if ("srcalpha".equals(var0)) {
            return 770;
        } else if ("1-srcalpha".equals(var0)) {
            return 771;
        } else if ("dstalpha".equals(var0)) {
            return 772;
        } else {
            return "1-dstalpha".equals(var0) ? 773 : -1;
        }
    }
}
