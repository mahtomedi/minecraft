package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class GlowInkSacItem extends Item implements SignApplicator {
    public GlowInkSacItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public boolean tryApplyToSign(Level param0, SignBlockEntity param1, boolean param2, Player param3) {
        if (param1.updateText(param0x -> param0x.setHasGlowingText(true), param2)) {
            param0.playSound(null, param1.getBlockPos(), SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        } else {
            return false;
        }
    }
}
