package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class LeadItem extends Item {
    public LeadItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        BlockState var2 = var0.getBlockState(var1);
        if (var2.is(BlockTags.FENCES)) {
            Player var3 = param0.getPlayer();
            if (!var0.isClientSide && var3 != null) {
                bindPlayerMobs(var3, var0, var1);
            }

            return InteractionResult.sidedSuccess(var0.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    public static InteractionResult bindPlayerMobs(Player param0, Level param1, BlockPos param2) {
        LeashFenceKnotEntity var0 = null;
        boolean var1 = false;
        double var2 = 7.0;
        int var3 = param2.getX();
        int var4 = param2.getY();
        int var5 = param2.getZ();

        for(Mob var7 : param1.getEntitiesOfClass(
            Mob.class, new AABB((double)var3 - 7.0, (double)var4 - 7.0, (double)var5 - 7.0, (double)var3 + 7.0, (double)var4 + 7.0, (double)var5 + 7.0)
        )) {
            if (var7.getLeashHolder() == param0) {
                if (var0 == null) {
                    var0 = LeashFenceKnotEntity.getOrCreateKnot(param1, param2);
                }

                var7.setLeashedTo(var0, true);
                var1 = true;
            }
        }

        return var1 ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
