package net.minecraft.world.item;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;

public class SaddleItem extends Item {
    public SaddleItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack param0, Player param1, LivingEntity param2, InteractionHand param3) {
        if (param2 instanceof Saddleable var0 && param2.isAlive() && !var0.isSaddled() && var0.isSaddleable()) {
            if (!param1.level.isClientSide) {
                var0.equipSaddle(SoundSource.NEUTRAL);
                param2.level.gameEvent(param2, GameEvent.EQUIP, param2.position());
                param0.shrink(1);
            }

            return InteractionResult.sidedSuccess(param1.level.isClientSide);
        }

        return InteractionResult.PASS;
    }
}
