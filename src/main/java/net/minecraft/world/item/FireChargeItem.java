package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;

public class FireChargeItem extends Item {
    public FireChargeItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        BlockState var2 = var0.getBlockState(var1);
        boolean var3 = false;
        if (!CampfireBlock.canLight(var2) && !CandleBlock.canLight(var2) && !CandleCakeBlock.canLight(var2)) {
            var1 = var1.relative(param0.getClickedFace());
            if (BaseFireBlock.canBePlacedAt(var0, var1, param0.getHorizontalDirection())) {
                this.playSound(var0, var1);
                var0.setBlockAndUpdate(var1, BaseFireBlock.getState(var0, var1));
                var0.gameEvent(param0.getPlayer(), GameEvent.BLOCK_PLACE, var1);
                var3 = true;
            }
        } else {
            this.playSound(var0, var1);
            var0.setBlockAndUpdate(var1, var2.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)));
            var0.gameEvent(param0.getPlayer(), GameEvent.BLOCK_CHANGE, var1);
            var3 = true;
        }

        if (var3) {
            param0.getItemInHand().shrink(1);
            return InteractionResult.sidedSuccess(var0.isClientSide);
        } else {
            return InteractionResult.FAIL;
        }
    }

    private void playSound(Level param0, BlockPos param1) {
        RandomSource var0 = param0.getRandom();
        param0.playSound(null, param1, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (var0.nextFloat() - var0.nextFloat()) * 0.2F + 1.0F);
    }
}
