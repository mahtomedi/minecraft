package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MerchantScreen extends AbstractContainerScreen<MerchantMenu> {
    private static final ResourceLocation VILLAGER_LOCATION = new ResourceLocation("textures/gui/container/villager2.png");
    private int shopItem;
    private final MerchantScreen.TradeOfferButton[] tradeOfferButtons = new MerchantScreen.TradeOfferButton[7];
    private int scrollOff;
    private boolean isDragging;

    public MerchantScreen(MerchantMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
        this.imageWidth = 276;
    }

    private void postButtonClick() {
        this.menu.setSelectionHint(this.shopItem);
        this.menu.tryMoveItems(this.shopItem);
        this.minecraft.getConnection().send(new ServerboundSelectTradePacket(this.shopItem));
    }

    @Override
    protected void init() {
        super.init();
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        int var2 = var1 + 16 + 2;

        for(int var3 = 0; var3 < 7; ++var3) {
            this.tradeOfferButtons[var3] = this.addButton(new MerchantScreen.TradeOfferButton(var0 + 5, var2, var3, param0 -> {
                if (param0 instanceof MerchantScreen.TradeOfferButton) {
                    this.shopItem = ((MerchantScreen.TradeOfferButton)param0).getIndex() + this.scrollOff;
                    this.postButtonClick();
                }

            }));
            var2 += 20;
        }

    }

    @Override
    protected void renderLabels(int param0, int param1) {
        int var0 = this.menu.getTraderLevel();
        int var1 = this.imageHeight - 94;
        if (var0 > 0 && var0 <= 5 && this.menu.showProgressBar()) {
            String var2 = this.title.getColoredString();
            String var3 = "- " + I18n.get("merchant.level." + var0);
            int var4 = this.font.width(var2);
            int var5 = this.font.width(var3);
            int var6 = var4 + var5 + 3;
            int var7 = 49 + this.imageWidth / 2 - var6 / 2;
            this.font.draw(var2, (float)var7, 6.0F, 4210752);
            this.font.draw(this.inventory.getDisplayName().getColoredString(), 107.0F, (float)var1, 4210752);
            this.font.draw(var3, (float)(var7 + var4 + 3), 6.0F, 4210752);
        } else {
            String var8 = this.title.getColoredString();
            this.font.draw(var8, (float)(49 + this.imageWidth / 2 - this.font.width(var8) / 2), 6.0F, 4210752);
            this.font.draw(this.inventory.getDisplayName().getColoredString(), 107.0F, (float)var1, 4210752);
        }

        String var9 = I18n.get("merchant.trades");
        int var10 = this.font.width(var9);
        this.font.draw(var9, (float)(5 - var10 / 2 + 48), 6.0F, 4210752);
    }

    @Override
    protected void renderBg(float param0, int param1, int param2) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        blit(var0, var1, this.getBlitOffset(), 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 512);
        MerchantOffers var2 = this.menu.getOffers();
        if (!var2.isEmpty()) {
            int var3 = this.shopItem;
            if (var3 < 0 || var3 >= var2.size()) {
                return;
            }

            MerchantOffer var4 = var2.get(var3);
            if (var4.isOutOfStock()) {
                this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                blit(this.leftPos + 83 + 99, this.topPos + 35, this.getBlitOffset(), 311.0F, 0.0F, 28, 21, 256, 512);
            }
        }

    }

    private void renderProgressBar(int param0, int param1, MerchantOffer param2) {
        this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
        int var0 = this.menu.getTraderLevel();
        int var1 = this.menu.getTraderXp();
        if (var0 < 5) {
            blit(param0 + 136, param1 + 16, this.getBlitOffset(), 0.0F, 186.0F, 102, 5, 256, 512);
            int var2 = VillagerData.getMinXpPerLevel(var0);
            if (var1 >= var2 && VillagerData.canLevelUp(var0)) {
                int var3 = 100;
                float var4 = (float)(100 / (VillagerData.getMaxXpPerLevel(var0) - var2));
                int var5 = Mth.floor(var4 * (float)(var1 - var2));
                blit(param0 + 136, param1 + 16, this.getBlitOffset(), 0.0F, 191.0F, var5 + 1, 5, 256, 512);
                int var6 = this.menu.getFutureTraderXp();
                if (var6 > 0) {
                    int var7 = Math.min(Mth.floor((float)var6 * var4), 100 - var5);
                    blit(param0 + 136 + var5 + 1, param1 + 16 + 1, this.getBlitOffset(), 2.0F, 182.0F, var7, 3, 256, 512);
                }

            }
        }
    }

    private void renderScroller(int param0, int param1, MerchantOffers param2) {
        int var0 = param2.size() + 1 - 7;
        if (var0 > 1) {
            int var1 = 139 - (27 + (var0 - 1) * 139 / var0);
            int var2 = 1 + var1 / var0 + 139 / var0;
            int var3 = 113;
            int var4 = Math.min(113, this.scrollOff * var2);
            if (this.scrollOff == var0 - 1) {
                var4 = 113;
            }

            blit(param0 + 94, param1 + 18 + var4, this.getBlitOffset(), 0.0F, 199.0F, 6, 27, 256, 512);
        } else {
            blit(param0 + 94, param1 + 18, this.getBlitOffset(), 6.0F, 199.0F, 6, 27, 256, 512);
        }

    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        super.render(param0, param1, param2);
        MerchantOffers var0 = this.menu.getOffers();
        if (!var0.isEmpty()) {
            int var1 = (this.width - this.imageWidth) / 2;
            int var2 = (this.height - this.imageHeight) / 2;
            int var3 = var2 + 16 + 1;
            int var4 = var1 + 5 + 5;
            RenderSystem.pushMatrix();
            RenderSystem.enableRescaleNormal();
            this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
            this.renderScroller(var1, var2, var0);
            int var5 = 0;

            for(MerchantOffer var6 : var0) {
                if (this.canScroll(var0.size()) && (var5 < this.scrollOff || var5 >= 7 + this.scrollOff)) {
                    ++var5;
                } else {
                    ItemStack var7 = var6.getBaseCostA();
                    ItemStack var8 = var6.getCostA();
                    ItemStack var9 = var6.getCostB();
                    ItemStack var10 = var6.getResult();
                    this.itemRenderer.blitOffset = 100.0F;
                    int var11 = var3 + 2;
                    this.renderAndDecorateCostA(var8, var7, var4, var11);
                    if (!var9.isEmpty()) {
                        this.itemRenderer.renderAndDecorateItem(var9, var1 + 5 + 35, var11);
                        this.itemRenderer.renderGuiItemDecorations(this.font, var9, var1 + 5 + 35, var11);
                    }

                    this.renderButtonArrows(var6, var1, var11);
                    this.itemRenderer.renderAndDecorateItem(var10, var1 + 5 + 68, var11);
                    this.itemRenderer.renderGuiItemDecorations(this.font, var10, var1 + 5 + 68, var11);
                    this.itemRenderer.blitOffset = 0.0F;
                    var3 += 20;
                    ++var5;
                }
            }

            int var12 = this.shopItem;
            MerchantOffer var13 = var0.get(var12);
            if (this.menu.showProgressBar()) {
                this.renderProgressBar(var1, var2, var13);
            }

            if (var13.isOutOfStock() && this.isHovering(186, 35, 22, 21, (double)param0, (double)param1) && this.menu.canRestock()) {
                this.renderTooltip(I18n.get("merchant.deprecated"), param0, param1);
            }

            for(MerchantScreen.TradeOfferButton var14 : this.tradeOfferButtons) {
                if (var14.isHovered()) {
                    var14.renderToolTip(param0, param1);
                }

                var14.visible = var14.index < this.menu.getOffers().size();
            }

            RenderSystem.popMatrix();
            RenderSystem.enableDepthTest();
        }

        this.renderTooltip(param0, param1);
    }

    private void renderButtonArrows(MerchantOffer param0, int param1, int param2) {
        RenderSystem.enableBlend();
        this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
        if (param0.isOutOfStock()) {
            blit(param1 + 5 + 35 + 20, param2 + 3, this.getBlitOffset(), 25.0F, 171.0F, 10, 9, 256, 512);
        } else {
            blit(param1 + 5 + 35 + 20, param2 + 3, this.getBlitOffset(), 15.0F, 171.0F, 10, 9, 256, 512);
        }

    }

    private void renderAndDecorateCostA(ItemStack param0, ItemStack param1, int param2, int param3) {
        this.itemRenderer.renderAndDecorateItem(param0, param2, param3);
        if (param1.getCount() == param0.getCount()) {
            this.itemRenderer.renderGuiItemDecorations(this.font, param0, param2, param3);
        } else {
            this.itemRenderer.renderGuiItemDecorations(this.font, param1, param2, param3, param1.getCount() == 1 ? "1" : null);
            this.itemRenderer.renderGuiItemDecorations(this.font, param0, param2 + 14, param3, param0.getCount() == 1 ? "1" : null);
            this.minecraft.getTextureManager().bind(VILLAGER_LOCATION);
            this.setBlitOffset(this.getBlitOffset() + 300);
            blit(param2 + 7, param3 + 12, this.getBlitOffset(), 0.0F, 176.0F, 9, 2, 256, 512);
            this.setBlitOffset(this.getBlitOffset() - 300);
        }

    }

    private boolean canScroll(int param0) {
        return param0 > 7;
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        int var0 = this.menu.getOffers().size();
        if (this.canScroll(var0)) {
            int var1 = var0 - 7;
            this.scrollOff = (int)((double)this.scrollOff - param2);
            this.scrollOff = Mth.clamp(this.scrollOff, 0, var1);
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        int var0 = this.menu.getOffers().size();
        if (this.isDragging) {
            int var1 = this.topPos + 18;
            int var2 = var1 + 139;
            int var3 = var0 - 7;
            float var4 = ((float)param1 - (float)var1 - 13.5F) / ((float)(var2 - var1) - 27.0F);
            var4 = var4 * (float)var3 + 0.5F;
            this.scrollOff = Mth.clamp((int)var4, 0, var3);
            return true;
        } else {
            return super.mouseDragged(param0, param1, param2, param3, param4);
        }
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        this.isDragging = false;
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        if (this.canScroll(this.menu.getOffers().size())
            && param0 > (double)(var0 + 94)
            && param0 < (double)(var0 + 94 + 6)
            && param1 > (double)(var1 + 18)
            && param1 <= (double)(var1 + 18 + 139 + 1)) {
            this.isDragging = true;
        }

        return super.mouseClicked(param0, param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    class TradeOfferButton extends Button {
        final int index;

        public TradeOfferButton(int param0, int param1, int param2, Button.OnPress param3) {
            super(param0, param1, 89, 20, "", param3);
            this.index = param2;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        @Override
        public void renderToolTip(int param0, int param1) {
            if (this.isHovered && MerchantScreen.this.menu.getOffers().size() > this.index + MerchantScreen.this.scrollOff) {
                if (param0 < this.x + 20) {
                    ItemStack var0 = MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff).getCostA();
                    MerchantScreen.this.renderTooltip(var0, param0, param1);
                } else if (param0 < this.x + 50 && param0 > this.x + 30) {
                    ItemStack var1 = MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff).getCostB();
                    if (!var1.isEmpty()) {
                        MerchantScreen.this.renderTooltip(var1, param0, param1);
                    }
                } else if (param0 > this.x + 65) {
                    ItemStack var2 = MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff).getResult();
                    MerchantScreen.this.renderTooltip(var2, param0, param1);
                }
            }

        }
    }
}
