package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FlintAndSteelItem extends Item {
    public FlintAndSteelItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Player var0 = param0.getPlayer();
        LevelAccessor var1 = param0.getLevel();
        BlockPos var2 = param0.getClickedPos();
        BlockState var3 = var1.getBlockState(var2);
        if (canLightCampFire(var3)) {
            var1.playSound(var0, var2, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
            var1.setBlock(var2, var3.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
            if (var0 != null) {
                param0.getItemInHand().hurtAndBreak(1, var0, param1 -> param1.broadcastBreakEvent(param0.getHand()));
            }

            return InteractionResult.SUCCESS;
        } else {
            BlockPos var4 = var2.relative(param0.getClickedFace());
            if (canUse(var1.getBlockState(var4), var1, var4)) {
                var1.playSound(var0, var4, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.4F + 0.8F);
                BlockState var5 = BaseFireBlock.getState(var1, var4);
                var1.setBlock(var4, var5, 11);
                ItemStack var6 = param0.getItemInHand();
                if (var0 instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)var0, var4, var6);
                    var6.hurtAndBreak(1, var0, param1 -> param1.broadcastBreakEvent(param0.getHand()));
                }

                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.FAIL;
            }
        }
    }

    public static boolean canLightCampFire(BlockState param0) {
        return param0.getBlock() == Blocks.CAMPFIRE && !param0.getValue(BlockStateProperties.WATERLOGGED) && !param0.getValue(BlockStateProperties.LIT);
    }

    public static boolean canUse(BlockState param0, LevelAccessor param1, BlockPos param2) {
        BlockState var0 = BaseFireBlock.getState(param1, param2);
        boolean var1 = false;

        for(Direction var2 : Direction.Plane.HORIZONTAL) {
            if (param1.getBlockState(param2.relative(var2)).getBlock() == Blocks.OBSIDIAN && NetherPortalBlock.isPortal(param1, param2) != null) {
                var1 = true;
            }
        }

        return param0.isAir() && (var0.canSurvive(param1, param2) || var1);
    }
}
