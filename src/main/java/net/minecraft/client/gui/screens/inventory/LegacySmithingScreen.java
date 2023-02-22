package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LegacySmithingMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Deprecated(
    forRemoval = true
)
@OnlyIn(Dist.CLIENT)
public class LegacySmithingScreen extends ItemCombinerScreen<LegacySmithingMenu> {
    private static final ResourceLocation SMITHING_LOCATION = new ResourceLocation("textures/gui/container/legacy_smithing.png");

    public LegacySmithingScreen(LegacySmithingMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2, SMITHING_LOCATION);
        this.titleLabelX = 60;
        this.titleLabelY = 18;
    }

    @Override
    protected void renderErrorIcon(PoseStack param0, int param1, int param2) {
        if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(this.menu.getResultSlot()).hasItem()) {
            blit(param0, param1 + 99, param2 + 45, this.imageWidth, 0, 28, 21);
        }

    }
}
