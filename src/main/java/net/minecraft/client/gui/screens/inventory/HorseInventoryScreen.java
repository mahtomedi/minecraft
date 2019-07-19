package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
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
    protected void renderLabels(int param0, int param1) {
        this.font.draw(this.title.getColoredString(), 8.0F, 6.0F, 4210752);
        this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0F, (float)(this.imageHeight - 96 + 2), 4210752);
    }

    @Override
    protected void renderBg(float param0, int param1, int param2) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(HORSE_INVENTORY_LOCATION);
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        this.blit(var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        if (this.horse instanceof AbstractChestedHorse) {
            AbstractChestedHorse var2 = (AbstractChestedHorse)this.horse;
            if (var2.hasChest()) {
                this.blit(var0 + 79, var1 + 17, 0, this.imageHeight, var2.getInventoryColumns() * 18, 54);
            }
        }

        if (this.horse.canBeSaddled()) {
            this.blit(var0 + 7, var1 + 35 - 18, 18, this.imageHeight + 54, 18, 18);
        }

        if (this.horse.wearsArmor()) {
            if (this.horse instanceof Llama) {
                this.blit(var0 + 7, var1 + 35, 36, this.imageHeight + 54, 18, 18);
            } else {
                this.blit(var0 + 7, var1 + 35, 0, this.imageHeight + 54, 18, 18);
            }
        }

        InventoryScreen.renderPlayerModel(var0 + 51, var1 + 60, 17, (float)(var0 + 51) - this.xMouse, (float)(var1 + 75 - 50) - this.yMouse, this.horse);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.xMouse = (float)param0;
        this.yMouse = (float)param1;
        super.render(param0, param1, param2);
        this.renderTooltip(param0, param1);
    }
}
