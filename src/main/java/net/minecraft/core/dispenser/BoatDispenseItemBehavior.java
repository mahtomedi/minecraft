package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class BoatDispenseItemBehavior extends DefaultDispenseItemBehavior {
    private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
    private final Boat.Type type;
    private final boolean isChestBoat;

    public BoatDispenseItemBehavior(Boat.Type param0) {
        this(param0, false);
    }

    public BoatDispenseItemBehavior(Boat.Type param0, boolean param1) {
        this.type = param0;
        this.isChestBoat = param1;
    }

    @Override
    public ItemStack execute(BlockSource param0, ItemStack param1) {
        Direction var0 = param0.getBlockState().getValue(DispenserBlock.FACING);
        Level var1 = param0.getLevel();
        double var2 = 0.5625 + (double)EntityType.BOAT.getWidth() / 2.0;
        double var3 = param0.x() + (double)var0.getStepX() * var2;
        double var4 = param0.y() + (double)((float)var0.getStepY() * 1.125F);
        double var5 = param0.z() + (double)var0.getStepZ() * var2;
        BlockPos var6 = param0.getPos().relative(var0);
        double var7;
        if (var1.getFluidState(var6).is(FluidTags.WATER)) {
            var7 = 1.0;
        } else {
            if (!var1.getBlockState(var6).isAir() || !var1.getFluidState(var6.below()).is(FluidTags.WATER)) {
                return this.defaultDispenseItemBehavior.dispense(param0, param1);
            }

            var7 = 0.0;
        }

        Boat var10 = (Boat)(this.isChestBoat ? new ChestBoat(var1, var3, var4 + var7, var5) : new Boat(var1, var3, var4 + var7, var5));
        var10.setVariant(this.type);
        var10.setYRot(var0.toYRot());
        var1.addFreshEntity(var10);
        param1.shrink(1);
        return param1;
    }

    @Override
    protected void playSound(BlockSource param0) {
        param0.getLevel().levelEvent(1000, param0.getPos(), 0);
    }
}
