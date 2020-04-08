package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ShearsDispenseItemBehavior extends OptionalDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource param0, ItemStack param1) {
        Level var0 = param0.getLevel();
        if (!var0.isClientSide()) {
            BlockPos var1 = param0.getPos().relative(param0.getBlockState().getValue(DispenserBlock.FACING));
            this.success = tryShearBeehive((ServerLevel)var0, var1) || tryShearLivingEntity((ServerLevel)var0, var1);
            if (this.success && param1.hurt(1, var0.getRandom(), null)) {
                param1.setCount(0);
            }
        }

        return param1;
    }

    private static boolean tryShearBeehive(ServerLevel param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        if (var0.is(BlockTags.BEEHIVES)) {
            int var1 = var0.getValue(BeehiveBlock.HONEY_LEVEL);
            if (var1 >= 5) {
                param0.playSound(null, param1, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                BeehiveBlock.dropHoneycomb(param0, param1);
                ((BeehiveBlock)var0.getBlock()).releaseBeesAndResetHoneyLevel(param0, var0, param1, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
                return true;
            }
        }

        return false;
    }

    private static boolean tryShearLivingEntity(ServerLevel param0, BlockPos param1) {
        for(LivingEntity var1 : param0.getEntitiesOfClass(LivingEntity.class, new AABB(param1), EntitySelector.NO_SPECTATORS)) {
            if (var1 instanceof Shearable) {
                Shearable var2 = (Shearable)var1;
                if (var2.readyForShearing()) {
                    var2.shear(SoundSource.BLOCKS);
                    return true;
                }
            }
        }

        return false;
    }
}
