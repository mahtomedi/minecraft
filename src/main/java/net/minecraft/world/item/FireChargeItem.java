package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

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
        if (var2.is(BlockTags.CAMPFIRES, param0x -> param0x.hasProperty(CampfireBlock.LIT) && param0x.hasProperty(CampfireBlock.WATERLOGGED))) {
            if (!var2.getValue(CampfireBlock.LIT) && !var2.getValue(CampfireBlock.WATERLOGGED)) {
                this.playSound(var0, var1);
                var0.setBlockAndUpdate(var1, var2.setValue(CampfireBlock.LIT, Boolean.valueOf(true)));
                var3 = true;
            }
        } else {
            var1 = var1.relative(param0.getClickedFace());
            if (var0.getBlockState(var1).isAir()) {
                this.playSound(var0, var1);
                var0.setBlockAndUpdate(var1, BaseFireBlock.getState(var0, var1));
                var3 = true;
            }
        }

        if (var3) {
            param0.getItemInHand().shrink(1);
            return InteractionResult.sidedSuccess(var0.isClientSide);
        } else {
            return InteractionResult.FAIL;
        }
    }

    private void playSound(Level param0, BlockPos param1) {
        param0.playSound(null, param1, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
    }
}
