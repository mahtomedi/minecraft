package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractBannerBlock extends BaseEntityBlock {
    private final DyeColor color;

    protected AbstractBannerBlock(DyeColor param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.color = param0;
    }

    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new BannerBlockEntity(param0, param1, this.color);
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, @Nullable LivingEntity param3, ItemStack param4) {
        if (param0.isClientSide) {
            param0.getBlockEntity(param1, BlockEntityType.BANNER).ifPresent(param1x -> param1x.fromItem(param4));
        } else if (param4.hasCustomHoverName()) {
            param0.getBlockEntity(param1, BlockEntityType.BANNER).ifPresent(param1x -> param1x.setCustomName(param4.getHoverName()));
        }

    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter param0, BlockPos param1, BlockState param2) {
        BlockEntity var0 = param0.getBlockEntity(param1);
        return var0 instanceof BannerBlockEntity ? ((BannerBlockEntity)var0).getItem() : super.getCloneItemStack(param0, param1, param2);
    }

    public DyeColor getColor() {
        return this.color;
    }
}
