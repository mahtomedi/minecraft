package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractButton extends AbstractWidget {
    protected static final int TEXTURE_Y_OFFSET = 46;
    protected static final int TEXTURE_WIDTH = 200;
    protected static final int TEXTURE_HEIGHT = 20;
    protected static final int TEXTURE_BORDER_X = 20;
    protected static final int TEXTURE_BORDER_Y = 4;
    protected static final int TEXT_MARGIN = 2;

    public AbstractButton(int param0, int param1, int param2, int param3, Component param4) {
        super(param0, param1, param2, param3, param4);
    }

    public abstract void onPress();

    @Override
    public void renderWidget(PoseStack param0, int param1, int param2, float param3) {
        Minecraft var0 = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        blitNineSliced(param0, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int var1 = this.active ? 16777215 : 10526880;
        this.renderString(param0, var0.font, var1 | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void renderString(PoseStack param0, Font param1, int param2) {
        this.renderScrollingString(param0, param1, 2, param2);
    }

    private int getTextureY() {
        int var0 = 1;
        if (!this.active) {
            var0 = 0;
        } else if (this.isHoveredOrFocused()) {
            var0 = 2;
        }

        return 46 + var0 * 20;
    }

    @Override
    public void onClick(double param0, double param1) {
        this.onPress();
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (!this.active || !this.visible) {
            return false;
        } else if (param0 != 257 && param0 != 32 && param0 != 335) {
            return false;
        } else {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
            return true;
        }
    }
}
