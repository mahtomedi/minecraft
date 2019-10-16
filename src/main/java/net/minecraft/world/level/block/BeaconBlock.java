package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BeaconBlock extends BaseEntityBlock implements BeaconBeamBlock {
    public BeaconBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter param0) {
        return new BeaconBlockEntity();
    }

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (param1.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity var0 = param1.getBlockEntity(param2);
            if (var0 instanceof BeaconBlockEntity) {
                param3.openMenu((BeaconBlockEntity)var0);
                param3.awardStat(Stats.INTERACT_WITH_BEACON);
            }

            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public boolean isRedstoneConductor(BlockState param0, BlockGetter param1, BlockPos param2) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
        if (param4.hasCustomHoverName()) {
            BlockEntity var0 = param0.getBlockEntity(param1);
            if (var0 instanceof BeaconBlockEntity) {
                ((BeaconBlockEntity)var0).setCustomName(param4.getHoverName());
            }
        }

    }
}
