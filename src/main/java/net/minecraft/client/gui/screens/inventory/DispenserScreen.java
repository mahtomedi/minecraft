package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DispenserScreen extends AbstractContainerScreen<DispenserMenu> {
    private static final ResourceLocation CONTAINER_LOCATION = new ResourceLocation("textures/gui/container/dispenser.png");

    public DispenserScreen(DispenserMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        super.render(param0, param1, param2);
        this.renderTooltip(param0, param1);
    }

    @Override
    protected void renderLabels(int param0, int param1) {
        String var0 = this.title.getColoredString();
        this.font.draw(var0, (float)(this.imageWidth / 2 - this.font.width(var0) / 2), 6.0F, 4210752);
        this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0F, (float)(this.imageHeight - 96 + 2), 4210752);
    }

    @Override
    protected void renderBg(float param0, int param1, int param2) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(CONTAINER_LOCATION);
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        this.blit(var0, var1, 0, 0, this.imageWidth, this.imageHeight);
    }
}
