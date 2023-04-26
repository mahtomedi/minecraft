package net.minecraft.world.entity.npc;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class ClientSideMerchant implements Merchant {
    private final Player source;
    private MerchantOffers offers = new MerchantOffers();
    private int xp;

    public ClientSideMerchant(Player param0) {
        this.source = param0;
    }

    @Override
    public Player getTradingPlayer() {
        return this.source;
    }

    @Override
    public void setTradingPlayer(@Nullable Player param0) {
    }

    @Override
    public MerchantOffers getOffers() {
        return this.offers;
    }

    @Override
    public void overrideOffers(MerchantOffers param0) {
        this.offers = param0;
    }

    @Override
    public void notifyTrade(MerchantOffer param0) {
        param0.increaseUses();
    }

    @Override
    public void notifyTradeUpdated(ItemStack param0) {
    }

    @Override
    public boolean isClientSide() {
        return this.source.level().isClientSide;
    }

    @Override
    public int getVillagerXp() {
        return this.xp;
    }

    @Override
    public void overrideXp(int param0) {
        this.xp = param0;
    }

    @Override
    public boolean showProgressBar() {
        return true;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }
}
