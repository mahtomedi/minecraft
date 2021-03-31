package net.minecraft.world.item.trading;

import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface Merchant {
    void setTradingPlayer(@Nullable Player var1);

    @Nullable
    Player getTradingPlayer();

    MerchantOffers getOffers();

    void overrideOffers(MerchantOffers var1);

    void notifyTrade(MerchantOffer var1);

    void notifyTradeUpdated(ItemStack var1);

    Level getLevel();

    int getVillagerXp();

    void overrideXp(int var1);

    boolean showProgressBar();

    SoundEvent getNotifyTradeSound();

    default boolean canRestock() {
        return false;
    }

    default void openTradingScreen(Player param0, Component param1, int param2) {
        OptionalInt var0 = param0.openMenu(new SimpleMenuProvider((param0x, param1x, param2x) -> new MerchantMenu(param0x, param1x, this), param1));
        if (var0.isPresent()) {
            MerchantOffers var1 = this.getOffers();
            if (!var1.isEmpty()) {
                param0.sendMerchantOffers(var0.getAsInt(), var1, param2, this.getVillagerXp(), this.showProgressBar(), this.canRestock());
            }
        }

    }
}
