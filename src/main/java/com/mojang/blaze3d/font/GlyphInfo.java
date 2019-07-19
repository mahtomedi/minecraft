package com.mojang.blaze3d.font;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface GlyphInfo {
    float getAdvance();

    default float getAdvance(boolean param0) {
        return this.getAdvance() + (param0 ? this.getBoldOffset() : 0.0F);
    }

    default float getBearingX() {
        return 0.0F;
    }

    default float getBoldOffset() {
        return 1.0F;
    }

    default float getShadowOffset() {
        return 1.0F;
    }
}
