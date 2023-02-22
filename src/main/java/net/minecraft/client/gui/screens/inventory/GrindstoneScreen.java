package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GrindstoneScreen extends AbstractContainerScreen<GrindstoneMenu> {
    private static final ResourceLocation GRINDSTONE_LOCATION = new ResourceLocation("textures/gui/container/grindstone.png");

    public GrindstoneScreen(GrindstoneMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.renderBg(param0, param3, param1, param2);
        super.render(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
    }

    @Override
    protected void renderBg(PoseStack param0, float param1, int param2, int param3) {
        RenderSystem.setShaderTexture(0, GRINDSTONE_LOCATION);
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        blit(param0, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(2).hasItem()) {
            blit(param0, var0 + 92, var1 + 31, this.imageWidth, 0, 28, 21);
        }

    }
}
