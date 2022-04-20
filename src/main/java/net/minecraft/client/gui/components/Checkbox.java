package net.minecraft.client.gui.components;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Checkbox extends AbstractButton {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
    private static final int TEXT_COLOR = 14737632;
    private boolean selected;
    private final boolean showLabel;

    public Checkbox(int param0, int param1, int param2, int param3, Component param4, boolean param5) {
        this(param0, param1, param2, param3, param4, param5, true);
    }

    public Checkbox(int param0, int param1, int param2, int param3, Component param4, boolean param5, boolean param6) {
        super(param0, param1, param2, param3, param4);
        this.selected = param5;
        this.showLabel = param6;
    }

    @Override
    public void onPress() {
        this.selected = !this.selected;
    }

    public boolean selected() {
        return this.selected;
    }

    @Override
    public void updateNarration(NarrationElementOutput param0) {
        param0.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                param0.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.checkbox.usage.focused"));
            } else {
                param0.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.checkbox.usage.hovered"));
            }
        }

    }

    @Override
    public void renderButton(PoseStack param0, int param1, int param2, float param3) {
        Minecraft var0 = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableDepthTest();
        Font var1 = var0.font;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        blit(param0, this.x, this.y, this.isFocused() ? 20.0F : 0.0F, this.selected ? 20.0F : 0.0F, 20, this.height, 64, 64);
        this.renderBg(param0, var0, param1, param2);
        if (this.showLabel) {
            drawString(param0, var1, this.getMessage(), this.x + 24, this.y + (this.height - 8) / 2, 14737632 | Mth.ceil(this.alpha * 255.0F) << 24);
        }

    }
}
