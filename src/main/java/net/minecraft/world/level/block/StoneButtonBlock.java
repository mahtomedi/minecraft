package net.minecraft.world.level.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class StoneButtonBlock extends ButtonBlock {
    protected StoneButtonBlock(BlockBehaviour.Properties param0) {
        super(false, param0);
    }

    @Override
    protected SoundEvent getSound(boolean param0) {
        return param0 ? SoundEvents.STONE_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_OFF;
    }
}
