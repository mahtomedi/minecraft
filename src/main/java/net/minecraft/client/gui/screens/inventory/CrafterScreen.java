package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CrafterSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CrafterScreen extends AbstractContainerScreen<CrafterMenu> {
    private static final ResourceLocation DISABLED_SLOT_LOCATION_SPRITE = new ResourceLocation("container/crafter/disabled_slot");
    private static final ResourceLocation POWERED_REDSTONE_LOCATION_SPRITE = new ResourceLocation("container/crafter/powered_redstone");
    private static final ResourceLocation UNPOWERED_REDSTONE_LOCATION_SPRITE = new ResourceLocation("container/crafter/unpowered_redstone");
    private static final ResourceLocation CONTAINER_LOCATION = new ResourceLocation("textures/gui/container/crafter.png");
    private static final Component DISABLED_SLOT_TOOLTIP = Component.translatable("gui.togglable_slot");
    private final Player player;

    public CrafterScreen(CrafterMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
        this.player = param1.player;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void slotClicked(Slot param0, int param1, int param2, ClickType param3) {
        if (this.player.isSpectator()) {
            super.slotClicked(param0, param1, param2, param3);
        } else {
            if (param1 > -1 && param1 < 9 && param0 instanceof CrafterSlot) {
                if (param0.hasItem()) {
                    super.slotClicked(param0, param1, param2, param3);
                    return;
                }

                boolean var0 = this.menu.isSlotDisabled(param1);
                if (var0 || this.menu.getCarried().isEmpty()) {
                    this.menu.setSlotState(param1, var0);
                    super.handleSlotStateChanged(param1, this.menu.containerId, var0);
                    if (var0) {
                        this.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4F, 1.0F);
                    } else {
                        this.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4F, 0.75F);
                    }
                }
            }

            super.slotClicked(param0, param1, param2, param3);
        }
    }

    @Override
    public void renderSlot(GuiGraphics param0, Slot param1) {
        if (param1 instanceof CrafterSlot var0 && this.menu.isSlotDisabled(param1.index)) {
            this.renderDisabledSlot(param0, var0);
            return;
        }

        super.renderSlot(param0, param1);
    }

    private void renderDisabledSlot(GuiGraphics param0, CrafterSlot param1) {
        param0.blitSprite(DISABLED_SLOT_LOCATION_SPRITE, param1.x - 1, param1.y - 1, 18, 18);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.renderRedstone(param0);
        this.renderTooltip(param0, param1, param2);
        if (this.hoveredSlot instanceof CrafterSlot
            && !this.menu.isSlotDisabled(this.hoveredSlot.index)
            && this.menu.getCarried().isEmpty()
            && !this.hoveredSlot.hasItem()) {
            param0.renderTooltip(this.font, DISABLED_SLOT_TOOLTIP, param1, param2);
        }

    }

    private void renderRedstone(GuiGraphics param0) {
        int var0 = this.width / 2 + 9;
        int var1 = this.height / 2 - 48;
        ResourceLocation var2;
        if (this.menu.isPowered()) {
            var2 = POWERED_REDSTONE_LOCATION_SPRITE;
        } else {
            var2 = UNPOWERED_REDSTONE_LOCATION_SPRITE;
        }

        param0.blitSprite(var2, var0, var1, 16, 16);
    }

    @Override
    protected void renderBg(GuiGraphics param0, float param1, int param2, int param3) {
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        param0.blit(CONTAINER_LOCATION, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
    }
}
