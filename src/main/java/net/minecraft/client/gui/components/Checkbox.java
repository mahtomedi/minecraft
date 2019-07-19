package net.minecraft.client.gui.components;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Checkbox extends AbstractButton {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
    boolean selected;

    public Checkbox(int param0, int param1, int param2, int param3, String param4, boolean param5) {
        super(param0, param1, param2, param3, param4);
        this.selected = param5;
    }

    @Override
    public void onPress() {
        this.selected = !this.selected;
    }

    public boolean selected() {
        return this.selected;
    }

    @Override
    public void renderButton(int param0, int param1, float param2) {
        Minecraft var0 = Minecraft.getInstance();
        var0.getTextureManager().bind(TEXTURE);
        GlStateManager.enableDepthTest();
        Font var1 = var0.font;
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        blit(this.x, this.y, 0.0F, this.selected ? 20.0F : 0.0F, 20, this.height, 32, 64);
        this.renderBg(var0, param0, param1);
        int var2 = 14737632;
        this.drawString(var1, this.getMessage(), this.x + 24, this.y + (this.height - 8) / 2, 14737632 | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}
