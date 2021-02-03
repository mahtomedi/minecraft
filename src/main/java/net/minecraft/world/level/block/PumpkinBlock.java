package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class PumpkinBlock extends StemGrownBlock {
    protected PumpkinBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        ItemStack var0 = param3.getItemInHand(param4);
        if (var0.is(Items.SHEARS)) {
            if (!param1.isClientSide) {
                Direction var1 = param5.getDirection();
                Direction var2 = var1.getAxis() == Direction.Axis.Y ? param3.getDirection().getOpposite() : var1;
                param1.playSound(null, param2, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F);
                param1.setBlock(param2, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, var2), 11);
                ItemEntity var3 = new ItemEntity(
                    param1,
                    (double)param2.getX() + 0.5 + (double)var2.getStepX() * 0.65,
                    (double)param2.getY() + 0.1,
                    (double)param2.getZ() + 0.5 + (double)var2.getStepZ() * 0.65,
                    new ItemStack(Items.PUMPKIN_SEEDS, 4)
                );
                var3.setDeltaMovement(
                    0.05 * (double)var2.getStepX() + param1.random.nextDouble() * 0.02,
                    0.05,
                    0.05 * (double)var2.getStepZ() + param1.random.nextDouble() * 0.02
                );
                param1.addFreshEntity(var3);
                var0.hurtAndBreak(1, param3, param1x -> param1x.broadcastBreakEvent(param4));
                param1.gameEvent(param3, GameEvent.SHEAR, param2);
            }

            return InteractionResult.sidedSuccess(param1.isClientSide);
        } else {
            return super.use(param0, param1, param2, param3, param4, param5);
        }
    }

    @Override
    public StemBlock getStem() {
        return (StemBlock)Blocks.PUMPKIN_STEM;
    }

    @Override
    public AttachedStemBlock getAttachedStem() {
        return (AttachedStemBlock)Blocks.ATTACHED_PUMPKIN_STEM;
    }
}
