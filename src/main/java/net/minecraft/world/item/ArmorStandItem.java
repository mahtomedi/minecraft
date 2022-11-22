package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
            Vec3 var5 = Vec3.atBottomCenterOf(var3);
            AABB var6 = EntityType.ARMOR_STAND.getDimensions().makeBoundingBox(var5.x(), var5.y(), var5.z());
            if (var1.noCollision(null, var6) && var1.getEntities(null, var6).isEmpty()) {
                if (var1 instanceof ServerLevel var7) {
                    ArmorStand var8 = EntityType.ARMOR_STAND.create(var7, var4.getTag(), null, var3, MobSpawnType.SPAWN_EGG, true, true);
                    if (var8 == null) {
                        return InteractionResult.FAIL;
                    }

                    float var9 = (float)Mth.floor((Mth.wrapDegrees(param0.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
                    var8.moveTo(var8.getX(), var8.getY(), var8.getZ(), var9, 0.0F);
                    this.randomizePose(var8, var1.random);
                    var7.addFreshEntityWithPassengers(var8);
                    var1.playSound(null, var8.getX(), var8.getY(), var8.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
                    var8.gameEvent(GameEvent.ENTITY_PLACE, param0.getPlayer());
                }

                var4.shrink(1);
                return InteractionResult.sidedSuccess(var1.isClientSide);
            } else {
                return InteractionResult.FAIL;
            }
        }
    }

    private void randomizePose(ArmorStand param0, RandomSource param1) {
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
