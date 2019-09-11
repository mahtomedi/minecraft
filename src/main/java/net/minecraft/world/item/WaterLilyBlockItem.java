package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class WaterLilyBlockItem extends BlockItem {
    public WaterLilyBlockItem(Block param0, Item.Properties param1) {
        super(param0, param1);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        HitResult var1 = getPlayerPOVHitResult(param0, param1, ClipContext.Fluid.SOURCE_ONLY);
        if (var1.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(var0);
        } else {
            if (var1.getType() == HitResult.Type.BLOCK) {
                BlockHitResult var2 = (BlockHitResult)var1;
                BlockPos var3 = var2.getBlockPos();
                Direction var4 = var2.getDirection();
                if (!param0.mayInteract(param1, var3) || !param1.mayUseItemAt(var3.relative(var4), var4, var0)) {
                    return InteractionResultHolder.fail(var0);
                }

                BlockPos var5 = var3.above();
                BlockState var6 = param0.getBlockState(var3);
                Material var7 = var6.getMaterial();
                FluidState var8 = param0.getFluidState(var3);
                if ((var8.getType() == Fluids.WATER || var7 == Material.ICE) && param0.isEmptyBlock(var5)) {
                    param0.setBlock(var5, Blocks.LILY_PAD.defaultBlockState(), 11);
                    if (param1 instanceof ServerPlayer) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)param1, var5, var0);
                    }

                    if (!param1.abilities.instabuild) {
                        var0.shrink(1);
                    }

                    param1.awardStat(Stats.ITEM_USED.get(this));
                    param0.playSound(param1, var3, SoundEvents.LILY_PAD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResultHolder.success(var0);
                }
            }

            return InteractionResultHolder.fail(var0);
        }
    }
}
