package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CyclingSlotBackground {
    private static final int ICON_CHANGE_TICK_RATE = 30;
    private static final int ICON_SIZE = 16;
    private static final int ICON_TRANSITION_TICK_DURATION = 4;
    private final int slotIndex;
    private List<ResourceLocation> icons = List.of();
    private int tick;
    private int iconIndex;

    public CyclingSlotBackground(int param0) {
        this.slotIndex = param0;
    }

    public void tick(List<ResourceLocation> param0) {
        if (!this.icons.equals(param0)) {
            this.icons = param0;
            this.iconIndex = 0;
        }

        if (!this.icons.isEmpty() && ++this.tick % 30 == 0) {
            this.iconIndex = (this.iconIndex + 1) % this.icons.size();
        }

    }

    public void render(AbstractContainerMenu param0, PoseStack param1, float param2, int param3, int param4) {
        Slot var0 = param0.getSlot(this.slotIndex);
        if (!this.icons.isEmpty() && !var0.hasItem()) {
            boolean var1 = this.icons.size() > 1 && this.tick >= 30;
            float var2 = var1 ? this.getIconTransitionTransparency(param2) : 1.0F;
            if (var2 < 1.0F) {
                int var3 = Math.floorMod(this.iconIndex - 1, this.icons.size());
                this.renderIcon(var0, this.icons.get(var3), 1.0F - var2, param1, param3, param4);
            }

            this.renderIcon(var0, this.icons.get(this.iconIndex), var2, param1, param3, param4);
        }
    }

    private void renderIcon(Slot param0, ResourceLocation param1, float param2, PoseStack param3, int param4, int param5) {
        TextureAtlasSprite var0 = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(param1);
        RenderSystem.setShaderTexture(0, var0.atlasLocation());
        GuiComponent.blit(param3, param4 + param0.x, param5 + param0.y, 0, 16, 16, var0, 1.0F, 1.0F, 1.0F, param2);
    }

    private float getIconTransitionTransparency(float param0) {
        float var0 = (float)(this.tick % 30) + param0;
        return Math.min(var0, 4.0F) / 4.0F;
    }
}
