package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeaconScreen extends AbstractContainerScreen<BeaconMenu> {
    private static final ResourceLocation BEACON_LOCATION = new ResourceLocation("textures/gui/container/beacon.png");
    static final ResourceLocation BUTTON_DISABLED_SPRITE = new ResourceLocation("container/beacon/button_disabled");
    static final ResourceLocation BUTTON_SELECTED_SPRITE = new ResourceLocation("container/beacon/button_selected");
    static final ResourceLocation BUTTON_HIGHLIGHTED_SPRITE = new ResourceLocation("container/beacon/button_highlighted");
    static final ResourceLocation BUTTON_SPRITE = new ResourceLocation("container/beacon/button");
    static final ResourceLocation CONFIRM_SPRITE = new ResourceLocation("container/beacon/confirm");
    static final ResourceLocation CANCEL_SPRITE = new ResourceLocation("container/beacon/cancel");
    private static final Component PRIMARY_EFFECT_LABEL = Component.translatable("block.minecraft.beacon.primary");
    private static final Component SECONDARY_EFFECT_LABEL = Component.translatable("block.minecraft.beacon.secondary");
    private final List<BeaconScreen.BeaconButton> beaconButtons = Lists.newArrayList();
    @Nullable
    MobEffect primary;
    @Nullable
    MobEffect secondary;

    public BeaconScreen(final BeaconMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
        this.imageWidth = 230;
        this.imageHeight = 219;
        param0.addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu param0x, int param1, ItemStack param2) {
            }

            @Override
            public void dataChanged(AbstractContainerMenu param0x, int param1, int param2) {
                BeaconScreen.this.primary = param0.getPrimaryEffect();
                BeaconScreen.this.secondary = param0.getSecondaryEffect();
            }
        });
    }

    private <T extends AbstractWidget & BeaconScreen.BeaconButton> void addBeaconButton(T param0) {
        this.addRenderableWidget(param0);
        this.beaconButtons.add(param0);
    }

    @Override
    protected void init() {
        super.init();
        this.beaconButtons.clear();
        this.addBeaconButton(new BeaconScreen.BeaconConfirmButton(this.leftPos + 164, this.topPos + 107));
        this.addBeaconButton(new BeaconScreen.BeaconCancelButton(this.leftPos + 190, this.topPos + 107));

        for(int var0 = 0; var0 <= 2; ++var0) {
            int var1 = BeaconBlockEntity.BEACON_EFFECTS[var0].length;
            int var2 = var1 * 22 + (var1 - 1) * 2;

            for(int var3 = 0; var3 < var1; ++var3) {
                MobEffect var4 = BeaconBlockEntity.BEACON_EFFECTS[var0][var3];
                BeaconScreen.BeaconPowerButton var5 = new BeaconScreen.BeaconPowerButton(
                    this.leftPos + 76 + var3 * 24 - var2 / 2, this.topPos + 22 + var0 * 25, var4, true, var0
                );
                var5.active = false;
                this.addBeaconButton(var5);
            }
        }

        int var6 = 3;
        int var7 = BeaconBlockEntity.BEACON_EFFECTS[3].length + 1;
        int var8 = var7 * 22 + (var7 - 1) * 2;

        for(int var9 = 0; var9 < var7 - 1; ++var9) {
            MobEffect var10 = BeaconBlockEntity.BEACON_EFFECTS[3][var9];
            BeaconScreen.BeaconPowerButton var11 = new BeaconScreen.BeaconPowerButton(
                this.leftPos + 167 + var9 * 24 - var8 / 2, this.topPos + 47, var10, false, 3
            );
            var11.active = false;
            this.addBeaconButton(var11);
        }

        BeaconScreen.BeaconPowerButton var12 = new BeaconScreen.BeaconUpgradePowerButton(
            this.leftPos + 167 + (var7 - 1) * 24 - var8 / 2, this.topPos + 47, BeaconBlockEntity.BEACON_EFFECTS[0][0]
        );
        var12.visible = false;
        this.addBeaconButton(var12);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.updateButtons();
    }

    void updateButtons() {
        int var0 = this.menu.getLevels();
        this.beaconButtons.forEach(param1 -> param1.updateStatus(var0));
    }

    @Override
    protected void renderLabels(GuiGraphics param0, int param1, int param2) {
        param0.drawCenteredString(this.font, PRIMARY_EFFECT_LABEL, 62, 10, 14737632);
        param0.drawCenteredString(this.font, SECONDARY_EFFECT_LABEL, 169, 10, 14737632);
    }

    @Override
    protected void renderBg(GuiGraphics param0, float param1, int param2, int param3) {
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        param0.blit(BEACON_LOCATION, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        param0.pose().pushPose();
        param0.pose().translate(0.0F, 0.0F, 100.0F);
        param0.renderItem(new ItemStack(Items.NETHERITE_INGOT), var0 + 20, var1 + 109);
        param0.renderItem(new ItemStack(Items.EMERALD), var0 + 41, var1 + 109);
        param0.renderItem(new ItemStack(Items.DIAMOND), var0 + 41 + 22, var1 + 109);
        param0.renderItem(new ItemStack(Items.GOLD_INGOT), var0 + 42 + 44, var1 + 109);
        param0.renderItem(new ItemStack(Items.IRON_INGOT), var0 + 42 + 66, var1 + 109);
        param0.pose().popPose();
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    interface BeaconButton {
        void updateStatus(int var1);
    }

    @OnlyIn(Dist.CLIENT)
    class BeaconCancelButton extends BeaconScreen.BeaconSpriteScreenButton {
        public BeaconCancelButton(int param0, int param1) {
            super(param0, param1, BeaconScreen.CANCEL_SPRITE, CommonComponents.GUI_CANCEL);
        }

        @Override
        public void onPress() {
            BeaconScreen.this.minecraft.player.closeContainer();
        }

        @Override
        public void updateStatus(int param0) {
        }
    }

    @OnlyIn(Dist.CLIENT)
    class BeaconConfirmButton extends BeaconScreen.BeaconSpriteScreenButton {
        public BeaconConfirmButton(int param0, int param1) {
            super(param0, param1, BeaconScreen.CONFIRM_SPRITE, CommonComponents.GUI_DONE);
        }

        @Override
        public void onPress() {
            BeaconScreen.this.minecraft
                .getConnection()
                .send(new ServerboundSetBeaconPacket(Optional.ofNullable(BeaconScreen.this.primary), Optional.ofNullable(BeaconScreen.this.secondary)));
            BeaconScreen.this.minecraft.player.closeContainer();
        }

        @Override
        public void updateStatus(int param0) {
            this.active = BeaconScreen.this.menu.hasPayment() && BeaconScreen.this.primary != null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class BeaconPowerButton extends BeaconScreen.BeaconScreenButton {
        private final boolean isPrimary;
        protected final int tier;
        private MobEffect effect;
        private TextureAtlasSprite sprite;

        public BeaconPowerButton(int param0, int param1, MobEffect param2, boolean param3, int param4) {
            super(param0, param1);
            this.isPrimary = param3;
            this.tier = param4;
            this.setEffect(param2);
        }

        protected void setEffect(MobEffect param0) {
            this.effect = param0;
            this.sprite = Minecraft.getInstance().getMobEffectTextures().get(param0);
            this.setTooltip(Tooltip.create(this.createEffectDescription(param0), null));
        }

        protected MutableComponent createEffectDescription(MobEffect param0) {
            return Component.translatable(param0.getDescriptionId());
        }

        @Override
        public void onPress() {
            if (!this.isSelected()) {
                if (this.isPrimary) {
                    BeaconScreen.this.primary = this.effect;
                } else {
                    BeaconScreen.this.secondary = this.effect;
                }

                BeaconScreen.this.updateButtons();
            }
        }

        @Override
        protected void renderIcon(GuiGraphics param0) {
            param0.blit(this.getX() + 2, this.getY() + 2, 0, 18, 18, this.sprite);
        }

        @Override
        public void updateStatus(int param0) {
            this.active = this.tier < param0;
            this.setSelected(this.effect == (this.isPrimary ? BeaconScreen.this.primary : BeaconScreen.this.secondary));
        }

        @Override
        protected MutableComponent createNarrationMessage() {
            return this.createEffectDescription(this.effect);
        }
    }

    @OnlyIn(Dist.CLIENT)
    abstract static class BeaconScreenButton extends AbstractButton implements BeaconScreen.BeaconButton {
        private boolean selected;

        protected BeaconScreenButton(int param0, int param1) {
            super(param0, param1, 22, 22, CommonComponents.EMPTY);
        }

        protected BeaconScreenButton(int param0, int param1, Component param2) {
            super(param0, param1, 22, 22, param2);
        }

        @Override
        public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
            ResourceLocation var0;
            if (!this.active) {
                var0 = BeaconScreen.BUTTON_DISABLED_SPRITE;
            } else if (this.selected) {
                var0 = BeaconScreen.BUTTON_SELECTED_SPRITE;
            } else if (this.isHoveredOrFocused()) {
                var0 = BeaconScreen.BUTTON_HIGHLIGHTED_SPRITE;
            } else {
                var0 = BeaconScreen.BUTTON_SPRITE;
            }

            param0.blitSprite(var0, this.getX(), this.getY(), this.width, this.height);
            this.renderIcon(param0);
        }

        protected abstract void renderIcon(GuiGraphics var1);

        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(boolean param0) {
            this.selected = param0;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput param0) {
            this.defaultButtonNarrationText(param0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    abstract static class BeaconSpriteScreenButton extends BeaconScreen.BeaconScreenButton {
        private final ResourceLocation sprite;

        protected BeaconSpriteScreenButton(int param0, int param1, ResourceLocation param2, Component param3) {
            super(param0, param1, param3);
            this.sprite = param2;
        }

        @Override
        protected void renderIcon(GuiGraphics param0) {
            param0.blitSprite(this.sprite, this.getX() + 2, this.getY() + 2, 18, 18);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class BeaconUpgradePowerButton extends BeaconScreen.BeaconPowerButton {
        public BeaconUpgradePowerButton(int param0, int param1, MobEffect param2) {
            super(param0, param1, param2, false, 3);
        }

        @Override
        protected MutableComponent createEffectDescription(MobEffect param0) {
            return Component.translatable(param0.getDescriptionId()).append(" II");
        }

        @Override
        public void updateStatus(int param0) {
            if (BeaconScreen.this.primary != null) {
                this.visible = true;
                this.setEffect(BeaconScreen.this.primary);
                super.updateStatus(param0);
            } else {
                this.visible = false;
            }

        }
    }
}
