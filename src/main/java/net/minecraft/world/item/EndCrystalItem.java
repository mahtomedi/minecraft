package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class EndCrystalItem extends Item {
    public EndCrystalItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        BlockState var2 = var0.getBlockState(var1);
        if (!var2.is(Blocks.OBSIDIAN) && !var2.is(Blocks.BEDROCK)) {
            return InteractionResult.FAIL;
        } else {
            BlockPos var3 = var1.above();
            if (!var0.isEmptyBlock(var3)) {
                return InteractionResult.FAIL;
            } else {
                double var4 = (double)var3.getX();
                double var5 = (double)var3.getY();
                double var6 = (double)var3.getZ();
                List<Entity> var7 = var0.getEntities(null, new AABB(var4, var5, var6, var4 + 1.0, var5 + 2.0, var6 + 1.0));
                if (!var7.isEmpty()) {
                    return InteractionResult.FAIL;
                } else {
                    if (var0 instanceof ServerLevel) {
                        EndCrystal var8 = new EndCrystal(var0, var4 + 0.5, var5, var6 + 0.5);
                        var8.setShowBottom(false);
                        var0.addFreshEntity(var8);
                        var0.gameEvent(param0.getPlayer(), GameEvent.ENTITY_PLACE, var3);
                        EndDragonFight var9 = ((ServerLevel)var0).getDragonFight();
                        if (var9 != null) {
                            var9.tryRespawn();
                        }
                    }

                    param0.getItemInHand().shrink(1);
                    return InteractionResult.sidedSuccess(var0.isClientSide);
                }
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack param0) {
        return true;
    }
}
