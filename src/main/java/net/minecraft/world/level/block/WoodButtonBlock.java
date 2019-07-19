package net.minecraft.world.level.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class WoodButtonBlock extends ButtonBlock {
    protected WoodButtonBlock(Block.Properties param0) {
        super(true, param0);
    }

    @Override
    protected SoundEvent getSound(boolean param0) {
        return param0 ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF;
    }
}
