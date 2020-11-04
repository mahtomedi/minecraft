package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SpyglassItem extends Item {
    public SpyglassItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public int getUseDuration(ItemStack param0) {
        return 1200;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack param0) {
        return UseAnim.SPYGLASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        param1.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
        return ItemUtils.startUsingInstantly(param0, param1, param2);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack param0, Level param1, LivingEntity param2) {
        this.stopUsing(param2);
        return param0;
    }

    @Override
    public void releaseUsing(ItemStack param0, Level param1, LivingEntity param2, int param3) {
        this.stopUsing(param2);
    }

    private void stopUsing(LivingEntity param0) {
        param0.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
    }
}
