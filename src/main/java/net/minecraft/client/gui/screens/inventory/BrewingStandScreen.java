package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BrewingStandScreen extends AbstractContainerScreen<BrewingStandMenu> {
    private static final ResourceLocation BREWING_STAND_LOCATION = new ResourceLocation("textures/gui/container/brewing_stand.png");
    private static final int[] BUBBLELENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};

    public BrewingStandScreen(BrewingStandMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        super.render(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
    }

    @Override
    protected void renderBg(PoseStack param0, float param1, int param2, int param3) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(BREWING_STAND_LOCATION);
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        this.blit(param0, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        int var2 = this.menu.getFuel();
        int var3 = Mth.clamp((18 * var2 + 20 - 1) / 20, 0, 18);
        if (var3 > 0) {
            this.blit(param0, var0 + 60, var1 + 44, 176, 29, var3, 4);
        }

        int var4 = this.menu.getBrewingTicks();
        if (var4 > 0) {
            int var5 = (int)(28.0F * (1.0F - (float)var4 / 400.0F));
            if (var5 > 0) {
                this.blit(param0, var0 + 97, var1 + 16, 176, 0, 9, var5);
            }

            var5 = BUBBLELENGTHS[var4 / 2 % 7];
            if (var5 > 0) {
                this.blit(param0, var0 + 63, var1 + 14 + 29 - var5, 185, 29 - var5, 12, var5);
            }
        }

    }
}
