package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnvilScreen extends AbstractContainerScreen<AnvilMenu> implements ContainerListener {
    private static final ResourceLocation ANVIL_LOCATION = new ResourceLocation("textures/gui/container/anvil.png");
    private EditBox name;

    public AnvilScreen(AnvilMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
    }

    @Override
    protected void init() {
        super.init();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        this.name = new EditBox(this.font, var0 + 62, var1 + 24, 103, 12, I18n.get("container.repair"));
        this.name.setCanLoseFocus(false);
        this.name.changeFocus(true);
        this.name.setTextColor(-1);
        this.name.setTextColorUneditable(-1);
        this.name.setBordered(false);
        this.name.setMaxLength(35);
        this.name.setResponder(this::onNameChanged);
        this.children.add(this.name);
        this.menu.addSlotListener(this);
        this.setInitialFocus(this.name);
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.name.getValue();
        this.init(param0, param1, param2);
        this.name.setValue(var0);
    }

    @Override
    public void removed() {
        super.removed();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.menu.removeSlotListener(this);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.minecraft.player.closeContainer();
        }

        return !this.name.keyPressed(param0, param1, param2) && !this.name.canConsumeInput() ? super.keyPressed(param0, param1, param2) : true;
    }

    @Override
    protected void renderLabels(int param0, int param1) {
        RenderSystem.disableBlend();
        this.font.draw(this.title.getColoredString(), 60.0F, 6.0F, 4210752);
        int var0 = this.menu.getCost();
        if (var0 > 0) {
            int var1 = 8453920;
            boolean var2 = true;
            String var3 = I18n.get("container.repair.cost", var0);
            if (var0 >= 40 && !this.minecraft.player.abilities.instabuild) {
                var3 = I18n.get("container.repair.expensive");
                var1 = 16736352;
            } else if (!this.menu.getSlot(2).hasItem()) {
                var2 = false;
            } else if (!this.menu.getSlot(2).mayPickup(this.inventory.player)) {
                var1 = 16736352;
            }

            if (var2) {
                int var4 = this.imageWidth - 8 - this.font.width(var3) - 2;
                int var5 = 69;
                fill(var4 - 2, 67, this.imageWidth - 8, 79, 1325400064);
                this.font.drawShadow(var3, (float)var4, 69.0F, var1);
            }
        }

    }

    private void onNameChanged(String param0) {
        if (!param0.isEmpty()) {
            String var0x = param0;
            Slot var1x = this.menu.getSlot(0);
            if (var1x != null && var1x.hasItem() && !var1x.getItem().hasCustomHoverName() && param0.equals(var1x.getItem().getHoverName().getString())) {
                var0x = "";
            }

            this.menu.setItemName(var0x);
            this.minecraft.player.connection.send(new ServerboundRenameItemPacket(var0x));
        }
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        super.render(param0, param1, param2);
        this.renderTooltip(param0, param1);
        RenderSystem.disableBlend();
        this.name.render(param0, param1, param2);
    }

    @Override
    protected void renderBg(float param0, int param1, int param2) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(ANVIL_LOCATION);
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        this.blit(var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        this.blit(var0 + 59, var1 + 20, 0, this.imageHeight + (this.menu.getSlot(0).hasItem() ? 0 : 16), 110, 16);
        if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(2).hasItem()) {
            this.blit(var0 + 99, var1 + 45, this.imageWidth, 0, 28, 21);
        }

    }

    @Override
    public void refreshContainer(AbstractContainerMenu param0, NonNullList<ItemStack> param1) {
        this.slotChanged(param0, 0, param0.getSlot(0).getItem());
    }

    @Override
    public void slotChanged(AbstractContainerMenu param0, int param1, ItemStack param2) {
        if (param1 == 0) {
            this.name.setValue(param2.isEmpty() ? "" : param2.getHoverName().getString());
            this.name.setEditable(!param2.isEmpty());
        }

    }

    @Override
    public void setContainerData(AbstractContainerMenu param0, int param1, int param2) {
    }
}
