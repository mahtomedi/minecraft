package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseInventoryScreen extends AbstractContainerScreen<HorseInventoryMenu> {
    private static final ResourceLocation HORSE_INVENTORY_LOCATION = new ResourceLocation("textures/gui/container/horse.png");
    private final AbstractHorse horse;
    private float xMouse;
    private float yMouse;

    public HorseInventoryScreen(HorseInventoryMenu param0, Inventory param1, AbstractHorse param2) {
        super(param0, param1, param2.getDisplayName());
        this.horse = param2;
        this.passEvents = false;
    }

    @Override
    protected void renderBg(PoseStack param0, float param1, int param2, int param3) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, HORSE_INVENTORY_LOCATION);
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        this.blit(param0, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        if (this.horse instanceof AbstractChestedHorse var2 && var2.hasChest()) {
            this.blit(param0, var0 + 79, var1 + 17, 0, this.imageHeight, var2.getInventoryColumns() * 18, 54);
        }

        if (this.horse.isSaddleable()) {
            this.blit(param0, var0 + 7, var1 + 35 - 18, 18, this.imageHeight + 54, 18, 18);
        }

        if (this.horse.canWearArmor()) {
            if (this.horse instanceof Llama) {
                this.blit(param0, var0 + 7, var1 + 35, 36, this.imageHeight + 54, 18, 18);
            } else {
                this.blit(param0, var0 + 7, var1 + 35, 0, this.imageHeight + 54, 18, 18);
            }
        }

        InventoryScreen.renderEntityInInventory(var0 + 51, var1 + 60, 17, (float)(var0 + 51) - this.xMouse, (float)(var1 + 75 - 50) - this.yMouse, this.horse);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.xMouse = (float)param1;
        this.yMouse = (float)param2;
        super.render(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
    }
}
