package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ImageWidget extends AbstractWidget {
    private final ResourceLocation imageLocation;

    public ImageWidget(int param0, int param1, ResourceLocation param2) {
        this(0, 0, param0, param1, param2);
    }

    public ImageWidget(int param0, int param1, int param2, int param3, ResourceLocation param4) {
        super(param0, param1, param2, param3, Component.empty());
        this.imageLocation = param4;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput param0) {
    }

    @Override
    public void renderWidget(PoseStack param0, int param1, int param2, float param3) {
        RenderSystem.setShaderTexture(0, this.imageLocation);
        int var0 = this.getWidth();
        int var1 = this.getHeight();
        GuiComponent.blit(param0, this.getX(), this.getY(), 0.0F, 0.0F, var0, var1, var0, var1);
    }
}
