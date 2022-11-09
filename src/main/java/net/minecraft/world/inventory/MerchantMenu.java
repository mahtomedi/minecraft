package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;

public class MerchantMenu extends AbstractContainerMenu {
    protected static final int PAYMENT1_SLOT = 0;
    protected static final int PAYMENT2_SLOT = 1;
    protected static final int RESULT_SLOT = 2;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int USE_ROW_SLOT_START = 30;
    private static final int USE_ROW_SLOT_END = 39;
    private static final int SELLSLOT1_X = 136;
    private static final int SELLSLOT2_X = 162;
    private static final int BUYSLOT_X = 220;
    private static final int ROW_Y = 37;
    private final Merchant trader;
    private final MerchantContainer tradeContainer;
    private int merchantLevel;
    private boolean showProgressBar;
    private boolean canRestock;

    public MerchantMenu(int param0, Inventory param1) {
        this(param0, param1, new ClientSideMerchant(param1.player));
    }

    public MerchantMenu(int param0, Inventory param1, Merchant param2) {
        super(MenuType.MERCHANT, param0);
        this.trader = param2;
        this.tradeContainer = new MerchantContainer(param2);
        this.addSlot(new Slot(this.tradeContainer, 0, 136, 37));
        this.addSlot(new Slot(this.tradeContainer, 1, 162, 37));
        this.addSlot(new MerchantResultSlot(param1.player, param2, this.tradeContainer, 2, 220, 37));

        for(int var0 = 0; var0 < 3; ++var0) {
            for(int var1 = 0; var1 < 9; ++var1) {
                this.addSlot(new Slot(param1, var1 + var0 * 9 + 9, 108 + var1 * 18, 84 + var0 * 18));
            }
        }

        for(int var2 = 0; var2 < 9; ++var2) {
            this.addSlot(new Slot(param1, var2, 108 + var2 * 18, 142));
        }

    }

    public void setShowProgressBar(boolean param0) {
        this.showProgressBar = param0;
    }

    @Override
    public void slotsChanged(Container param0) {
        this.tradeContainer.updateSellItem();
        super.slotsChanged(param0);
    }

    public void setSelectionHint(int param0) {
        this.tradeContainer.setSelectionHint(param0);
    }

    @Override
    public boolean stillValid(Player param0) {
        return this.trader.getTradingPlayer() == param0;
    }

    public int getTraderXp() {
        return this.trader.getVillagerXp();
    }

    public int getFutureTraderXp() {
        return this.tradeContainer.getFutureXp();
    }

    public void setXp(int param0) {
        this.trader.overrideXp(param0);
    }

    public int getTraderLevel() {
        return this.merchantLevel;
    }

    public void setMerchantLevel(int param0) {
        this.merchantLevel = param0;
    }

    public void setCanRestock(boolean param0) {
        this.canRestock = param0;
    }

    public boolean canRestock() {
        return this.canRestock;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack param0, Slot param1) {
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            if (param1 == 2) {
                if (!this.moveItemStackTo(var2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                var1.onQuickCraft(var2, var0);
                this.playTradeSound();
            } else if (param1 != 0 && param1 != 1) {
                if (param1 >= 3 && param1 < 30) {
                    if (!this.moveItemStackTo(var2, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (param1 >= 30 && param1 < 39 && !this.moveItemStackTo(var2, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(var2, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (var2.isEmpty()) {
                var1.set(ItemStack.EMPTY);
            } else {
                var1.setChanged();
            }

            if (var2.getCount() == var0.getCount()) {
                return ItemStack.EMPTY;
            }

            var1.onTake(param0, var2);
        }

        return var0;
    }

    private void playTradeSound() {
        if (!this.trader.isClientSide()) {
            Entity var0 = (Entity)this.trader;
            var0.getLevel().playLocalSound(var0.getX(), var0.getY(), var0.getZ(), this.trader.getNotifyTradeSound(), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
        }

    }

    @Override
    public void removed(Player param0) {
        super.removed(param0);
        this.trader.setTradingPlayer(null);
        if (!this.trader.isClientSide()) {
            if (!param0.isAlive() || param0 instanceof ServerPlayer && ((ServerPlayer)param0).hasDisconnected()) {
                ItemStack var0 = this.tradeContainer.removeItemNoUpdate(0);
                if (!var0.isEmpty()) {
                    param0.drop(var0, false);
                }

                var0 = this.tradeContainer.removeItemNoUpdate(1);
                if (!var0.isEmpty()) {
                    param0.drop(var0, false);
                }
            } else if (param0 instanceof ServerPlayer) {
                param0.getInventory().placeItemBackInInventory(this.tradeContainer.removeItemNoUpdate(0));
                param0.getInventory().placeItemBackInInventory(this.tradeContainer.removeItemNoUpdate(1));
            }

        }
    }

    public void tryMoveItems(int param0) {
        if (param0 >= 0 && this.getOffers().size() > param0) {
            ItemStack var0 = this.tradeContainer.getItem(0);
            if (!var0.isEmpty()) {
                if (!this.moveItemStackTo(var0, 3, 39, true)) {
                    return;
                }

                this.tradeContainer.setItem(0, var0);
            }

            ItemStack var1 = this.tradeContainer.getItem(1);
            if (!var1.isEmpty()) {
                if (!this.moveItemStackTo(var1, 3, 39, true)) {
                    return;
                }

                this.tradeContainer.setItem(1, var1);
            }

            if (this.tradeContainer.getItem(0).isEmpty() && this.tradeContainer.getItem(1).isEmpty()) {
                ItemStack var2 = this.getOffers().get(param0).getCostA();
                this.moveFromInventoryToPaymentSlot(0, var2);
                ItemStack var3 = this.getOffers().get(param0).getCostB();
                this.moveFromInventoryToPaymentSlot(1, var3);
            }

        }
    }

    private void moveFromInventoryToPaymentSlot(int param0, ItemStack param1) {
        if (!param1.isEmpty()) {
            for(int var0 = 3; var0 < 39; ++var0) {
                ItemStack var1 = this.slots.get(var0).getItem();
                if (!var1.isEmpty() && ItemStack.isSameItemSameTags(param1, var1)) {
                    ItemStack var2 = this.tradeContainer.getItem(param0);
                    int var3 = var2.isEmpty() ? 0 : var2.getCount();
                    int var4 = Math.min(param1.getMaxStackSize() - var3, var1.getCount());
                    ItemStack var5 = var1.copy();
                    int var6 = var3 + var4;
                    var1.shrink(var4);
                    var5.setCount(var6);
                    this.tradeContainer.setItem(param0, var5);
                    if (var6 >= param1.getMaxStackSize()) {
                        break;
                    }
                }
            }
        }

    }

    public void setOffers(MerchantOffers param0) {
        this.trader.overrideOffers(param0);
    }

    public MerchantOffers getOffers() {
        return this.trader.getOffers();
    }

    public boolean showProgressBar() {
        return this.showProgressBar;
    }
}
