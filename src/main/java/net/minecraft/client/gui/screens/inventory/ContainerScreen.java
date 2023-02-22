package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ContainerScreen extends AbstractContainerScreen<ChestMenu> implements MenuAccess<ChestMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");
    private final int containerRows;

    public ContainerScreen(ChestMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
        this.passEvents = false;
        int var0 = 222;
        int var1 = 114;
        this.containerRows = param0.getRowCount();
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        super.render(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
    }

    @Override
    protected void renderBg(PoseStack param0, float param1, int param2, int param3) {
        RenderSystem.setShaderTexture(0, CONTAINER_BACKGROUND);
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        blit(param0, var0, var1, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
        blit(param0, var0, var1 + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }
}
