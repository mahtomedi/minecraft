package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemCombinerScreen<T extends ItemCombinerMenu> extends AbstractContainerScreen<T> implements ContainerListener {
    private final ResourceLocation menuResource;

    public ItemCombinerScreen(T param0, Inventory param1, Component param2, ResourceLocation param3) {
        super(param0, param1, param2);
        this.menuResource = param3;
    }

    protected void subInit() {
    }

    @Override
    protected void init() {
        super.init();
        this.subInit();
        this.menu.addSlotListener(this);
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        super.render(param0, param1, param2, param3);
        RenderSystem.disableBlend();
        this.renderFg(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
    }

    protected void renderFg(PoseStack param0, int param1, int param2, float param3) {
    }

    @Override
    protected void renderBg(PoseStack param0, float param1, int param2, int param3) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(this.menuResource);
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        this.blit(param0, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        this.blit(param0, var0 + 59, var1 + 20, 0, this.imageHeight + (this.menu.getSlot(0).hasItem() ? 0 : 16), 110, 16);
        if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(2).hasItem()) {
            this.blit(param0, var0 + 99, var1 + 45, this.imageWidth, 0, 28, 21);
        }

    }

    @Override
    public void refreshContainer(AbstractContainerMenu param0, NonNullList<ItemStack> param1) {
        this.slotChanged(param0, 0, param0.getSlot(0).getItem());
    }

    @Override
    public void setContainerData(AbstractContainerMenu param0, int param1, int param2) {
    }

    @Override
    public void slotChanged(AbstractContainerMenu param0, int param1, ItemStack param2) {
    }
}
