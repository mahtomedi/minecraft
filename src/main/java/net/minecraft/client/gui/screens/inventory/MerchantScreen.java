package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
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
    private static final ResourceLocation OUT_OF_STOCK_SPRITE = new ResourceLocation("container/villager/out_of_stock");
    private static final ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE = new ResourceLocation("container/villager/experience_bar_background");
    private static final ResourceLocation EXPERIENCE_BAR_CURRENT_SPRITE = new ResourceLocation("container/villager/experience_bar_current");
    private static final ResourceLocation EXPERIENCE_BAR_RESULT_SPRITE = new ResourceLocation("container/villager/experience_bar_result");
    private static final ResourceLocation SCROLLER_SPRITE = new ResourceLocation("container/villager/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = new ResourceLocation("container/villager/scroller_disabled");
    private static final ResourceLocation TRADE_ARROW_OUT_OF_STOCK_SPRITE = new ResourceLocation("container/villager/trade_arrow_out_of_stock");
    private static final ResourceLocation TRADE_ARROW_SPRITE = new ResourceLocation("container/villager/trade_arrow");
    private static final ResourceLocation DISCOUNT_STRIKETHRUOGH_SPRITE = new ResourceLocation("container/villager/discount_strikethrough");
    private static final ResourceLocation VILLAGER_LOCATION = new ResourceLocation("textures/gui/container/villager.png");
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int MERCHANT_MENU_PART_X = 99;
    private static final int PROGRESS_BAR_X = 136;
    private static final int PROGRESS_BAR_Y = 16;
    private static final int SELL_ITEM_1_X = 5;
    private static final int SELL_ITEM_2_X = 35;
    private static final int BUY_ITEM_X = 68;
    private static final int LABEL_Y = 6;
    private static final int NUMBER_OF_OFFER_BUTTONS = 7;
    private static final int TRADE_BUTTON_X = 5;
    private static final int TRADE_BUTTON_HEIGHT = 20;
    private static final int TRADE_BUTTON_WIDTH = 88;
    private static final int SCROLLER_HEIGHT = 27;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLL_BAR_HEIGHT = 139;
    private static final int SCROLL_BAR_TOP_POS_Y = 18;
    private static final int SCROLL_BAR_START_X = 94;
    private static final Component TRADES_LABEL = Component.translatable("merchant.trades");
    private static final Component DEPRECATED_TOOLTIP = Component.translatable("merchant.deprecated");
    private int shopItem;
    private final MerchantScreen.TradeOfferButton[] tradeOfferButtons = new MerchantScreen.TradeOfferButton[7];
    int scrollOff;
    private boolean isDragging;

    public MerchantScreen(MerchantMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
        this.imageWidth = 276;
        this.inventoryLabelX = 107;
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
            this.tradeOfferButtons[var3] = this.addRenderableWidget(new MerchantScreen.TradeOfferButton(var0 + 5, var2, var3, param0 -> {
                if (param0 instanceof MerchantScreen.TradeOfferButton) {
                    this.shopItem = ((MerchantScreen.TradeOfferButton)param0).getIndex() + this.scrollOff;
                    this.postButtonClick();
                }

            }));
            var2 += 20;
        }

    }

    @Override
    protected void renderLabels(GuiGraphics param0, int param1, int param2) {
        int var0 = this.menu.getTraderLevel();
        if (var0 > 0 && var0 <= 5 && this.menu.showProgressBar()) {
            Component var1 = Component.translatable("merchant.title", this.title, Component.translatable("merchant.level." + var0));
            int var2 = this.font.width(var1);
            int var3 = 49 + this.imageWidth / 2 - var2 / 2;
            param0.drawString(this.font, var1, var3, 6, 4210752, false);
        } else {
            param0.drawString(this.font, this.title, 49 + this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
        }

        param0.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
        int var4 = this.font.width(TRADES_LABEL);
        param0.drawString(this.font, TRADES_LABEL, 5 - var4 / 2 + 48, 6, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics param0, float param1, int param2, int param3) {
        int var0 = (this.width - this.imageWidth) / 2;
        int var1 = (this.height - this.imageHeight) / 2;
        param0.blit(VILLAGER_LOCATION, var0, var1, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 512, 256);
        MerchantOffers var2 = this.menu.getOffers();
        if (!var2.isEmpty()) {
            int var3 = this.shopItem;
            if (var3 < 0 || var3 >= var2.size()) {
                return;
            }

            MerchantOffer var4 = var2.get(var3);
            if (var4.isOutOfStock()) {
                param0.blitSprite(OUT_OF_STOCK_SPRITE, this.leftPos + 83 + 99, this.topPos + 35, 0, 28, 21);
            }
        }

    }

    private void renderProgressBar(GuiGraphics param0, int param1, int param2, MerchantOffer param3) {
        int var0 = this.menu.getTraderLevel();
        int var1 = this.menu.getTraderXp();
        if (var0 < 5) {
            param0.blitSprite(EXPERIENCE_BAR_BACKGROUND_SPRITE, param1 + 136, param2 + 16, 0, 102, 5);
            int var2 = VillagerData.getMinXpPerLevel(var0);
            if (var1 >= var2 && VillagerData.canLevelUp(var0)) {
                int var3 = 102;
                float var4 = 102.0F / (float)(VillagerData.getMaxXpPerLevel(var0) - var2);
                int var5 = Math.min(Mth.floor(var4 * (float)(var1 - var2)), 102);
                param0.blitSprite(EXPERIENCE_BAR_CURRENT_SPRITE, 102, 5, 0, 0, param1 + 136, param2 + 16, 0, var5, 5);
                int var6 = this.menu.getFutureTraderXp();
                if (var6 > 0) {
                    int var7 = Math.min(Mth.floor((float)var6 * var4), 102 - var5);
                    param0.blitSprite(EXPERIENCE_BAR_RESULT_SPRITE, 102, 5, var5, 0, param1 + 136 + var5, param2 + 16, 0, var7, 5);
                }

            }
        }
    }

    private void renderScroller(GuiGraphics param0, int param1, int param2, MerchantOffers param3) {
        int var0 = param3.size() + 1 - 7;
        if (var0 > 1) {
            int var1 = 139 - (27 + (var0 - 1) * 139 / var0);
            int var2 = 1 + var1 / var0 + 139 / var0;
            int var3 = 113;
            int var4 = Math.min(113, this.scrollOff * var2);
            if (this.scrollOff == var0 - 1) {
                var4 = 113;
            }

            param0.blitSprite(SCROLLER_SPRITE, param1 + 94, param2 + 18 + var4, 0, 6, 27);
        } else {
            param0.blitSprite(SCROLLER_DISABLED_SPRITE, param1 + 94, param2 + 18, 0, 6, 27);
        }

    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        MerchantOffers var0 = this.menu.getOffers();
        if (!var0.isEmpty()) {
            int var1 = (this.width - this.imageWidth) / 2;
            int var2 = (this.height - this.imageHeight) / 2;
            int var3 = var2 + 16 + 1;
            int var4 = var1 + 5 + 5;
            this.renderScroller(param0, var1, var2, var0);
            int var5 = 0;

            for(MerchantOffer var6 : var0) {
                if (this.canScroll(var0.size()) && (var5 < this.scrollOff || var5 >= 7 + this.scrollOff)) {
                    ++var5;
                } else {
                    ItemStack var7 = var6.getBaseCostA();
                    ItemStack var8 = var6.getCostA();
                    ItemStack var9 = var6.getCostB();
                    ItemStack var10 = var6.getResult();
                    param0.pose().pushPose();
                    param0.pose().translate(0.0F, 0.0F, 100.0F);
                    int var11 = var3 + 2;
                    this.renderAndDecorateCostA(param0, var8, var7, var4, var11);
                    if (!var9.isEmpty()) {
                        param0.renderFakeItem(var9, var1 + 5 + 35, var11);
                        param0.renderItemDecorations(this.font, var9, var1 + 5 + 35, var11);
                    }

                    this.renderButtonArrows(param0, var6, var1, var11);
                    param0.renderFakeItem(var10, var1 + 5 + 68, var11);
                    param0.renderItemDecorations(this.font, var10, var1 + 5 + 68, var11);
                    param0.pose().popPose();
                    var3 += 20;
                    ++var5;
                }
            }

            int var12 = this.shopItem;
            MerchantOffer var13 = var0.get(var12);
            if (this.menu.showProgressBar()) {
                this.renderProgressBar(param0, var1, var2, var13);
            }

            if (var13.isOutOfStock() && this.isHovering(186, 35, 22, 21, (double)param1, (double)param2) && this.menu.canRestock()) {
                param0.renderTooltip(this.font, DEPRECATED_TOOLTIP, param1, param2);
            }

            for(MerchantScreen.TradeOfferButton var14 : this.tradeOfferButtons) {
                if (var14.isHoveredOrFocused()) {
                    var14.renderToolTip(param0, param1, param2);
                }

                var14.visible = var14.index < this.menu.getOffers().size();
            }

            RenderSystem.enableDepthTest();
        }

        this.renderTooltip(param0, param1, param2);
    }

    private void renderButtonArrows(GuiGraphics param0, MerchantOffer param1, int param2, int param3) {
        RenderSystem.enableBlend();
        if (param1.isOutOfStock()) {
            param0.blitSprite(TRADE_ARROW_OUT_OF_STOCK_SPRITE, param2 + 5 + 35 + 20, param3 + 3, 0, 10, 9);
        } else {
            param0.blitSprite(TRADE_ARROW_SPRITE, param2 + 5 + 35 + 20, param3 + 3, 0, 10, 9);
        }

    }

    private void renderAndDecorateCostA(GuiGraphics param0, ItemStack param1, ItemStack param2, int param3, int param4) {
        param0.renderFakeItem(param1, param3, param4);
        if (param2.getCount() == param1.getCount()) {
            param0.renderItemDecorations(this.font, param1, param3, param4);
        } else {
            param0.renderItemDecorations(this.font, param2, param3, param4, param2.getCount() == 1 ? "1" : null);
            param0.renderItemDecorations(this.font, param1, param3 + 14, param4, param1.getCount() == 1 ? "1" : null);
            param0.pose().pushPose();
            param0.pose().translate(0.0F, 0.0F, 300.0F);
            param0.blitSprite(DISCOUNT_STRIKETHRUOGH_SPRITE, param3 + 7, param4 + 12, 0, 9, 2);
            param0.pose().popPose();
        }

    }

    private boolean canScroll(int param0) {
        return param0 > 7;
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2, double param3) {
        int var0 = this.menu.getOffers().size();
        if (this.canScroll(var0)) {
            int var1 = var0 - 7;
            this.scrollOff = Mth.clamp((int)((double)this.scrollOff - param3), 0, var1);
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
            super(param0, param1, 88, 20, CommonComponents.EMPTY, param3, DEFAULT_NARRATION);
            this.index = param2;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderToolTip(GuiGraphics param0, int param1, int param2) {
            if (this.isHovered && MerchantScreen.this.menu.getOffers().size() > this.index + MerchantScreen.this.scrollOff) {
                if (param1 < this.getX() + 20) {
                    ItemStack var0 = MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff).getCostA();
                    param0.renderTooltip(MerchantScreen.this.font, var0, param1, param2);
                } else if (param1 < this.getX() + 50 && param1 > this.getX() + 30) {
                    ItemStack var1 = MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff).getCostB();
                    if (!var1.isEmpty()) {
                        param0.renderTooltip(MerchantScreen.this.font, var1, param1, param2);
                    }
                } else if (param1 > this.getX() + 65) {
                    ItemStack var2 = MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff).getResult();
                    param0.renderTooltip(MerchantScreen.this.font, var2, param1, param2);
                }
            }

        }
    }
}
