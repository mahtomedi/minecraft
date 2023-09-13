package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

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
        Direction var0 = param0.state().getValue(DispenserBlock.FACING);
        Level var1 = param0.level();
        Vec3 var2 = param0.center();
        double var3 = 0.5625 + (double)EntityType.BOAT.getWidth() / 2.0;
        double var4 = var2.x() + (double)var0.getStepX() * var3;
        double var5 = var2.y() + (double)((float)var0.getStepY() * 1.125F);
        double var6 = var2.z() + (double)var0.getStepZ() * var3;
        BlockPos var7 = param0.pos().relative(var0);
        double var8;
        if (var1.getFluidState(var7).is(FluidTags.WATER)) {
            var8 = 1.0;
        } else {
            if (!var1.getBlockState(var7).isAir() || !var1.getFluidState(var7.below()).is(FluidTags.WATER)) {
                return this.defaultDispenseItemBehavior.dispense(param0, param1);
            }

            var8 = 0.0;
        }

        Boat var11 = (Boat)(this.isChestBoat ? new ChestBoat(var1, var4, var5 + var8, var6) : new Boat(var1, var4, var5 + var8, var6));
        var11.setVariant(this.type);
        var11.setYRot(var0.toYRot());
        var1.addFreshEntity(var11);
        param1.shrink(1);
        return param1;
    }

    @Override
    protected void playSound(BlockSource param0) {
        param0.level().levelEvent(1000, param0.pos(), 0);
    }
}
