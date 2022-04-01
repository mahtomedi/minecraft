package net.minecraft.world.item.trading;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface Merchant {
    void setTradingPlayer(@Nullable Player var1);

    @Nullable
    Player getTradingPlayer();

    MerchantOffers getOffers();

    void overrideOffers(MerchantOffers var1);

    void notifyTrade(MerchantOffer var1);

    void notifyTradeUpdated(ItemStack var1);

    int getVillagerXp();

    void overrideXp(int var1);

    boolean showProgressBar();

    SoundEvent getNotifyTradeSound();

    default boolean canRestock() {
        return false;
    }

    boolean isClientSide();
}
