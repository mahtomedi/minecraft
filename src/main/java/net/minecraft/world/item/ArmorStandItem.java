package net.minecraft.world.item;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;

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
            ItemStack var4 = param0.getItemInHand();
            ArmorStand var5 = EntityType.ARMOR_STAND.create(var1, var4.getTag(), null, param0.getPlayer(), var3, MobSpawnType.SPAWN_EGG, true, true);
            if (var1.noCollision(var5) && var1.getEntities(var5, var5.getBoundingBox()).isEmpty()) {
                if (!var1.isClientSide) {
                    float var6 = (float)Mth.floor((Mth.wrapDegrees(param0.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
                    var5.moveTo(var5.getX(), var5.getY(), var5.getZ(), var6, 0.0F);
                    this.randomizePose(var5, var1.random);
                    var1.addFreshEntity(var5);
                    var1.playSound(null, var5.getX(), var5.getY(), var5.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
                }

                var4.shrink(1);
                return InteractionResult.SUCCESS;
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
