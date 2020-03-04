package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SmithingScreen extends ItemCombinerScreen<SmithingMenu> {
    private static final ResourceLocation SMITHING_LOCATION = new ResourceLocation("textures/gui/container/smithing.png");

    public SmithingScreen(SmithingMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2, SMITHING_LOCATION);
    }

    @Override
    protected void renderLabels(int param0, int param1) {
        RenderSystem.disableBlend();
        this.font.draw(this.title.getColoredString(), 40.0F, 20.0F, 4210752);
    }
}
