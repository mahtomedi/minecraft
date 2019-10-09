package net.minecraft.world.item;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class ArmorStandItem extends Item {
    public ArmorStandItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Direction var0 = param0.getClickedFace();
        if (var0 == Direction.DOWN) {
            return InteractionResult.FAIL;
        } else {
            Level var1 = param0.getLevel();
            BlockPlaceContext var2 = new BlockPlaceContext(param0);
            BlockPos var3 = var2.getClickedPos();
            BlockPos var4 = var3.above();
            if (var2.canPlace() && var1.getBlockState(var4).canBeReplaced(var2)) {
                double var5 = (double)var3.getX();
                double var6 = (double)var3.getY();
                double var7 = (double)var3.getZ();
                List<Entity> var8 = var1.getEntities(null, new AABB(var5, var6, var7, var5 + 1.0, var6 + 2.0, var7 + 1.0));
                if (!var8.isEmpty()) {
                    return InteractionResult.FAIL;
                } else {
                    ItemStack var9 = param0.getItemInHand();
                    if (!var1.isClientSide) {
                        var1.removeBlock(var3, false);
                        var1.removeBlock(var4, false);
                        ArmorStand var10 = new ArmorStand(var1, var5 + 0.5, var6, var7 + 0.5);
                        float var11 = (float)Mth.floor((Mth.wrapDegrees(param0.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
                        var10.moveTo(var5 + 0.5, var6, var7 + 0.5, var11, 0.0F);
                        this.randomizePose(var10, var1.random);
                        EntityType.updateCustomEntityTag(var1, param0.getPlayer(), var10, var9.getTag());
                        var1.addFreshEntity(var10);
                        var1.playSound(null, var10.getX(), var10.getY(), var10.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
                    }

                    var9.shrink(1);
                    return InteractionResult.SUCCESS;
                }
            } else {
                return InteractionResult.FAIL;
            }
        }
    }

    private void randomizePose(ArmorStand param0, Random param1) {
        Rotations var0 = param0.getHeadPose();
        float var1 = param1.nextFloat() * 5.0F;
        float var2 = param1.nextFloat() * 20.0F - 10.0F;
        Rotations var3 = new Rotations(var0.getX() + var1, var0.getY() + var2, var0.getZ());
        param0.setHeadPose(var3);
        var0 = param0.getBodyPose();
        var1 = param1.nextFloat() * 10.0F - 5.0F;
        var3 = new Rotations(var0.getX(), var0.getY() + var1, var0.getZ());
        param0.setBodyPose(var3);
    }
}
