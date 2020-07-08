package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public class MinecartItem extends Item {
    private static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
        private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

        @Override
        public ItemStack execute(BlockSource param0, ItemStack param1) {
            Direction var0 = param0.getBlockState().getValue(DispenserBlock.FACING);
            Level var1 = param0.getLevel();
            double var2 = param0.x() + (double)var0.getStepX() * 1.125;
            double var3 = Math.floor(param0.y()) + (double)var0.getStepY();
            double var4 = param0.z() + (double)var0.getStepZ() * 1.125;
            BlockPos var5 = param0.getPos().relative(var0);
            BlockState var6 = var1.getBlockState(var5);
            RailShape var7 = var6.getBlock() instanceof BaseRailBlock
                ? var6.getValue(((BaseRailBlock)var6.getBlock()).getShapeProperty())
                : RailShape.NORTH_SOUTH;
            double var8;
            if (var6.is(BlockTags.RAILS)) {
                if (var7.isAscending()) {
                    var8 = 0.6;
                } else {
                    var8 = 0.1;
                }
            } else {
                if (!var6.isAir() || !var1.getBlockState(var5.below()).is(BlockTags.RAILS)) {
                    return this.defaultDispenseItemBehavior.dispense(param0, param1);
                }

                BlockState var10 = var1.getBlockState(var5.below());
                RailShape var11 = var10.getBlock() instanceof BaseRailBlock
                    ? var10.getValue(((BaseRailBlock)var10.getBlock()).getShapeProperty())
                    : RailShape.NORTH_SOUTH;
                if (var0 != Direction.DOWN && var11.isAscending()) {
                    var8 = -0.4;
                } else {
                    var8 = -0.9;
                }
            }

            AbstractMinecart var15 = AbstractMinecart.createMinecart(var1, var2, var3 + var8, var4, ((MinecartItem)param1.getItem()).type);
            if (param1.hasCustomHoverName()) {
                var15.setCustomName(param1.getHoverName());
            }

            var1.addFreshEntity(var15);
            param1.shrink(1);
            return param1;
        }

        @Override
        protected void playSound(BlockSource param0) {
            param0.getLevel().levelEvent(1000, param0.getPos(), 0);
        }
    };
    private final AbstractMinecart.Type type;

    public MinecartItem(AbstractMinecart.Type param0, Item.Properties param1) {
        super(param1);
        this.type = param0;
        DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        BlockState var2 = var0.getBlockState(var1);
        if (!var2.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        } else {
            ItemStack var3 = param0.getItemInHand();
            if (!var0.isClientSide) {
                RailShape var4 = var2.getBlock() instanceof BaseRailBlock
                    ? var2.getValue(((BaseRailBlock)var2.getBlock()).getShapeProperty())
                    : RailShape.NORTH_SOUTH;
                double var5 = 0.0;
                if (var4.isAscending()) {
                    var5 = 0.5;
                }

                AbstractMinecart var6 = AbstractMinecart.createMinecart(
                    var0, (double)var1.getX() + 0.5, (double)var1.getY() + 0.0625 + var5, (double)var1.getZ() + 0.5, this.type
                );
                if (var3.hasCustomHoverName()) {
                    var6.setCustomName(var3.getHoverName());
                }

                var0.addFreshEntity(var6);
            }

            var3.shrink(1);
            return InteractionResult.sidedSuccess(var0.isClientSide);
        }
    }
}
