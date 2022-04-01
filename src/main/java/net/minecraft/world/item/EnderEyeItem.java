package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ConfiguredStructureTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class EnderEyeItem extends Item {
    public EnderEyeItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        HitResult var1 = getPlayerPOVHitResult(param0, param1, ClipContext.Fluid.NONE);
        if (var1.getType() == HitResult.Type.BLOCK && param0.getBlockState(((BlockHitResult)var1).getBlockPos()).is(Blocks.END_PORTAL_FRAME)) {
            return InteractionResultHolder.pass(var0);
        } else {
            param1.startUsingItem(param2);
            if (param0 instanceof ServerLevel var2) {
                BlockPos var3 = var2.findNearestMapFeature(ConfiguredStructureTags.EYE_OF_ENDER_LOCATED, param1.blockPosition(), 100, false);
                if (var3 != null) {
                    EyeOfEnder var4 = new EyeOfEnder(param0, param1.getX(), param1.getY(0.5), param1.getZ());
                    var4.setItem(var0);
                    var4.signalTo(var3);
                    param0.addFreshEntity(var4);
                    if (param1 instanceof ServerPlayer) {
                        CriteriaTriggers.USED_ENDER_EYE.trigger((ServerPlayer)param1, var3);
                    }

                    param0.playSound(
                        null,
                        param1.getX(),
                        param1.getY(),
                        param1.getZ(),
                        SoundEvents.ENDER_EYE_LAUNCH,
                        SoundSource.NEUTRAL,
                        0.5F,
                        0.4F / (param0.getRandom().nextFloat() * 0.4F + 0.8F)
                    );
                    param0.levelEvent(null, 1003, param1.blockPosition(), 0);
                    if (!param1.getAbilities().instabuild) {
                        var0.shrink(1);
                    }

                    param1.awardStat(Stats.ITEM_USED.get(this));
                    param1.swing(param2, true);
                    return InteractionResultHolder.success(var0);
                }
            }

            return InteractionResultHolder.consume(var0);
        }
    }
}
