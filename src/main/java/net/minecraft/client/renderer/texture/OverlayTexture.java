package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OverlayTexture implements AutoCloseable {
    public static final int NO_OVERLAY = pack(0, 10);
    private final DynamicTexture texture = new DynamicTexture(24, 24, false);

    public OverlayTexture() {
        NativeImage var0 = this.texture.getPixels();

        for(int var1 = 0; var1 < 24; ++var1) {
            for(int var2 = 0; var2 < 24; ++var2) {
                if (var1 < 8) {
                    var0.setPixelRGBA(var2, var1, -1308622593);
                } else if (var1 < 16) {
                    int var3 = (int)((1.0F - (float)var2 / 15.0F * 0.75F) * 255.0F);
                    var0.setPixelRGBA(var2, var1, var3 << 24 | 16777215);
                } else {
                    var0.setPixelRGBA(var2, var1, -1291911168);
                }
            }
        }

        RenderSystem.activeTexture(33985);
        this.texture.bind();
        RenderSystem.matrixMode(5890);
        RenderSystem.loadIdentity();
        float var4 = 0.04347826F;
        RenderSystem.scalef(0.04347826F, 0.04347826F, 0.04347826F);
        RenderSystem.matrixMode(5888);
        this.texture.bind();
        var0.upload(0, 0, 0, 0, 0, var0.getWidth(), var0.getHeight(), false, true, false, false);
        RenderSystem.activeTexture(33984);
    }

    @Override
    public void close() {
        this.texture.close();
    }

    public void setupOverlayColor() {
        RenderSystem.setupOverlayColor(this.texture::getId, 24);
    }

    public static int u(float param0) {
        return (int)(param0 * 23.0F);
    }

    public static int v(boolean param0) {
        return v(param0, null);
    }

    public static int v(boolean param0, @Nullable DamageSource param1) {
        if (param0) {
            return param1 == DamageSource.FREEZE ? 19 : 3;
        } else {
            return 10;
        }
    }

    public static int pack(int param0, int param1) {
        return param0 | param1 << 16;
    }

    public static int pack(float param0, boolean param1) {
        return pack(u(param0), v(param1));
    }

    public void teardownOverlayColor() {
        RenderSystem.teardownOverlayColor();
    }
}
