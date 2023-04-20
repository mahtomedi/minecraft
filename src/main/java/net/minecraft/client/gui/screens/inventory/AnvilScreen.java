package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnvilScreen extends ItemCombinerScreen<AnvilMenu> {
    private static final ResourceLocation ANVIL_LOCATION = new ResourceLocation("textures/gui/container/anvil.png");
    private static final Component TOO_EXPENSIVE_TEXT = Component.translatable("container.repair.expensive");
    private EditBox name;
    private final Player player;

    public AnvilScreen(AnvilMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2, ANVIL_LOCATION);
        this.player = param1.player;
        this.titleLabelX = 60;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.name.tick();
    }

    @Override
    protected void subInit() {
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        this.name = new EditBox(this.font, var0 + 62, var1 + 24, 103, 12, Component.translatable("container.repair"));
        this.name.setCanLoseFocus(false);
        this.name.setTextColor(-1);
        this.name.setTextColorUneditable(-1);
        this.name.setBordered(false);
        this.name.setMaxLength(50);
        this.name.setResponder(this::onNameChanged);
        this.name.setValue("");
        this.addWidget(this.name);
        this.setInitialFocus(this.name);
        this.name.setEditable(false);
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.name.getValue();
        this.init(param0, param1, param2);
        this.name.setValue(var0);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.minecraft.player.closeContainer();
        }

        return !this.name.keyPressed(param0, param1, param2) && !this.name.canConsumeInput() ? super.keyPressed(param0, param1, param2) : true;
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
    protected void renderLabels(GuiGraphics param0, int param1, int param2) {
        super.renderLabels(param0, param1, param2);
        int var0 = this.menu.getCost();
        if (var0 > 0) {
            int var1 = 8453920;
            Component var2;
            if (var0 >= 40 && !this.minecraft.player.getAbilities().instabuild) {
                var2 = TOO_EXPENSIVE_TEXT;
                var1 = 16736352;
            } else if (!this.menu.getSlot(2).hasItem()) {
                var2 = null;
            } else {
                var2 = Component.translatable("container.repair.cost", var0);
                if (!this.menu.getSlot(2).mayPickup(this.player)) {
                    var1 = 16736352;
                }
            }

            if (var2 != null) {
                int var5 = this.imageWidth - 8 - this.font.width(var2) - 2;
                int var6 = 69;
                param0.fill(var5 - 2, 67, this.imageWidth - 8, 79, 1325400064);
                param0.drawString(this.font, var2, var5, 69, var1);
            }
        }

    }

    @Override
    protected void renderBg(GuiGraphics param0, float param1, int param2, int param3) {
        super.renderBg(param0, param1, param2, param3);
        param0.blit(ANVIL_LOCATION, this.leftPos + 59, this.topPos + 20, 0, this.imageHeight + (this.menu.getSlot(0).hasItem() ? 0 : 16), 110, 16);
    }

    @Override
    public void renderFg(GuiGraphics param0, int param1, int param2, float param3) {
        this.name.render(param0, param1, param2, param3);
    }

    @Override
    protected void renderErrorIcon(GuiGraphics param0, int param1, int param2) {
        if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(this.menu.getResultSlot()).hasItem()) {
            param0.blit(ANVIL_LOCATION, param1 + 99, param2 + 45, this.imageWidth, 0, 28, 21);
        }

    }

    @Override
    public void slotChanged(AbstractContainerMenu param0, int param1, ItemStack param2) {
        if (param1 == 0) {
            this.name.setValue(param2.isEmpty() ? "" : param2.getHoverName().getString());
            this.name.setEditable(!param2.isEmpty());
            this.setFocused(this.name);
        }

    }
}
