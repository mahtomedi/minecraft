package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
public abstract class ItemCombinerScreen<T extends ItemCombinerMenu> extends AbstractContainerScreen<T> implements ContainerListener {
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
        this.renderFg(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
    }

    protected void renderFg(PoseStack param0, int param1, int param2, float param3) {
    }

    @Override
    protected void renderBg(PoseStack param0, float param1, int param2, int param3) {
        RenderSystem.setShaderTexture(0, this.menuResource);
        blit(param0, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        this.renderErrorIcon(param0, this.leftPos, this.topPos);
    }

    protected abstract void renderErrorIcon(PoseStack var1, int var2, int var3);

    @Override
    public void dataChanged(AbstractContainerMenu param0, int param1, int param2) {
    }

    @Override
    public void slotChanged(AbstractContainerMenu param0, int param1, ItemStack param2) {
    }
}
