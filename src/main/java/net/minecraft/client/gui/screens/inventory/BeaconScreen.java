package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
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
    private static final Component PRIMARY_EFFECT_LABEL = new TranslatableComponent("block.minecraft.beacon.primary");
    private static final Component SECONDARY_EFFECT_LABEL = new TranslatableComponent("block.minecraft.beacon.secondary");
    private BeaconScreen.BeaconConfirmButton confirmButton;
    private boolean initPowerButtons;
    private MobEffect primary;
    private MobEffect secondary;

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
                BeaconScreen.this.initPowerButtons = true;
            }
        });
    }

    @Override
    protected void init() {
        super.init();
        this.confirmButton = this.addButton(new BeaconScreen.BeaconConfirmButton(this.leftPos + 164, this.topPos + 107));
        this.addButton(new BeaconScreen.BeaconCancelButton(this.leftPos + 190, this.topPos + 107));
        this.initPowerButtons = true;
        this.confirmButton.active = false;
    }

    @Override
    public void tick() {
        super.tick();
        int var0 = this.menu.getLevels();
        if (this.initPowerButtons && var0 >= 0) {
            this.initPowerButtons = false;

            for(int var1 = 0; var1 <= 2; ++var1) {
                int var2 = BeaconBlockEntity.BEACON_EFFECTS[var1].length;
                int var3 = var2 * 22 + (var2 - 1) * 2;

                for(int var4 = 0; var4 < var2; ++var4) {
                    MobEffect var5 = BeaconBlockEntity.BEACON_EFFECTS[var1][var4];
                    BeaconScreen.BeaconPowerButton var6 = new BeaconScreen.BeaconPowerButton(
                        this.leftPos + 76 + var4 * 24 - var3 / 2, this.topPos + 22 + var1 * 25, var5, true
                    );
                    this.addButton(var6);
                    if (var1 >= var0) {
                        var6.active = false;
                    } else if (var5 == this.primary) {
                        var6.setSelected(true);
                    }
                }
            }

            int var7 = 3;
            int var8 = BeaconBlockEntity.BEACON_EFFECTS[3].length + 1;
            int var9 = var8 * 22 + (var8 - 1) * 2;

            for(int var10 = 0; var10 < var8 - 1; ++var10) {
                MobEffect var11 = BeaconBlockEntity.BEACON_EFFECTS[3][var10];
                BeaconScreen.BeaconPowerButton var12 = new BeaconScreen.BeaconPowerButton(
                    this.leftPos + 167 + var10 * 24 - var9 / 2, this.topPos + 47, var11, false
                );
                this.addButton(var12);
                if (3 >= var0) {
                    var12.active = false;
                } else if (var11 == this.secondary) {
                    var12.setSelected(true);
                }
            }

            if (this.primary != null) {
                BeaconScreen.BeaconPowerButton var13 = new BeaconScreen.BeaconPowerButton(
                    this.leftPos + 167 + (var8 - 1) * 24 - var9 / 2, this.topPos + 47, this.primary, false
                );
                this.addButton(var13);
                if (3 >= var0) {
                    var13.active = false;
                } else if (this.primary == this.secondary) {
                    var13.setSelected(true);
                }
            }
        }

        this.confirmButton.active = this.menu.hasPayment() && this.primary != null;
    }

    @Override
    protected void renderLabels(PoseStack param0, int param1, int param2) {
        drawCenteredString(param0, this.font, PRIMARY_EFFECT_LABEL, 62, 10, 14737632);
        drawCenteredString(param0, this.font, SECONDARY_EFFECT_LABEL, 169, 10, 14737632);

        for(AbstractWidget var0 : this.buttons) {
            if (var0.isHovered()) {
                var0.renderToolTip(param0, param1 - this.leftPos, param2 - this.topPos);
                break;
            }
        }

    }

    @Override
    protected void renderBg(PoseStack param0, float param1, int param2, int param3) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BEACON_LOCATION);
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        this.blit(param0, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        this.itemRenderer.blitOffset = 100.0F;
        this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.NETHERITE_INGOT), var0 + 20, var1 + 109);
        this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.EMERALD), var0 + 41, var1 + 109);
        this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.DIAMOND), var0 + 41 + 22, var1 + 109);
        this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.GOLD_INGOT), var0 + 42 + 44, var1 + 109);
        this.itemRenderer.renderAndDecorateItem(new ItemStack(Items.IRON_INGOT), var0 + 42 + 66, var1 + 109);
        this.itemRenderer.blitOffset = 0.0F;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        super.render(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    class BeaconCancelButton extends BeaconScreen.BeaconSpriteScreenButton {
        public BeaconCancelButton(int param0, int param1) {
            super(param0, param1, 112, 220);
        }

        @Override
        public void onPress() {
            BeaconScreen.this.minecraft.player.closeContainer();
        }

        @Override
        public void renderToolTip(PoseStack param0, int param1, int param2) {
            BeaconScreen.this.renderTooltip(param0, CommonComponents.GUI_CANCEL, param1, param2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class BeaconConfirmButton extends BeaconScreen.BeaconSpriteScreenButton {
        public BeaconConfirmButton(int param0, int param1) {
            super(param0, param1, 90, 220);
        }

        @Override
        public void onPress() {
            BeaconScreen.this.minecraft
                .getConnection()
                .send(new ServerboundSetBeaconPacket(MobEffect.getId(BeaconScreen.this.primary), MobEffect.getId(BeaconScreen.this.secondary)));
            BeaconScreen.this.minecraft.player.closeContainer();
        }

        @Override
        public void renderToolTip(PoseStack param0, int param1, int param2) {
            BeaconScreen.this.renderTooltip(param0, CommonComponents.GUI_DONE, param1, param2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class BeaconPowerButton extends BeaconScreen.BeaconScreenButton {
        private final MobEffect effect;
        private final TextureAtlasSprite sprite;
        private final boolean isPrimary;
        private final Component tooltip;

        public BeaconPowerButton(int param0, int param1, MobEffect param2, boolean param3) {
            super(param0, param1);
            this.effect = param2;
            this.sprite = Minecraft.getInstance().getMobEffectTextures().get(param2);
            this.isPrimary = param3;
            this.tooltip = this.createTooltip(param2, param3);
        }

        private Component createTooltip(MobEffect param0, boolean param1) {
            MutableComponent var0 = new TranslatableComponent(param0.getDescriptionId());
            if (!param1 && param0 != MobEffects.REGENERATION) {
                var0.append(" II");
            }

            return var0;
        }

        @Override
        public void onPress() {
            if (!this.isSelected()) {
                if (this.isPrimary) {
                    BeaconScreen.this.primary = this.effect;
                } else {
                    BeaconScreen.this.secondary = this.effect;
                }

                BeaconScreen.this.buttons.clear();
                BeaconScreen.this.children.clear();
                BeaconScreen.this.init();
                BeaconScreen.this.tick();
            }
        }

        @Override
        public void renderToolTip(PoseStack param0, int param1, int param2) {
            BeaconScreen.this.renderTooltip(param0, this.tooltip, param1, param2);
        }

        @Override
        protected void renderIcon(PoseStack param0) {
            RenderSystem.setShaderTexture(0, this.sprite.atlas().location());
            blit(param0, this.x + 2, this.y + 2, this.getBlitOffset(), 18, 18, this.sprite);
        }
    }

    @OnlyIn(Dist.CLIENT)
    abstract static class BeaconScreenButton extends AbstractButton {
        private boolean selected;

        protected BeaconScreenButton(int param0, int param1) {
            super(param0, param1, 22, 22, TextComponent.EMPTY);
        }

        @Override
        public void renderButton(PoseStack param0, int param1, int param2, float param3) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, BeaconScreen.BEACON_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int var0 = 219;
            int var1 = 0;
            if (!this.active) {
                var1 += this.width * 2;
            } else if (this.selected) {
                var1 += this.width * 1;
            } else if (this.isHovered()) {
                var1 += this.width * 3;
            }

            this.blit(param0, this.x, this.y, var1, 219, this.width, this.height);
            this.renderIcon(param0);
        }

        protected abstract void renderIcon(PoseStack var1);

        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(boolean param0) {
            this.selected = param0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    abstract static class BeaconSpriteScreenButton extends BeaconScreen.BeaconScreenButton {
        private final int iconX;
        private final int iconY;

        protected BeaconSpriteScreenButton(int param0, int param1, int param2, int param3) {
            super(param0, param1);
            this.iconX = param2;
            this.iconY = param3;
        }

        @Override
        protected void renderIcon(PoseStack param0) {
            this.blit(param0, this.x + 2, this.y + 2, this.iconX, this.iconY, 18, 18);
        }
    }
}
