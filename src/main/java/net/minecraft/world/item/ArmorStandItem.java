package net.minecraft.world.item;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
                    Consumer<ArmorStand> var8 = EntityType.createDefaultStackConfig(var7, var4, param0.getPlayer());
                    ArmorStand var9 = EntityType.ARMOR_STAND.create(var7, var4.getTag(), var8, var3, MobSpawnType.SPAWN_EGG, true, true);
                    if (var9 == null) {
                        return InteractionResult.FAIL;
                    }

                    float var10 = (float)Mth.floor((Mth.wrapDegrees(param0.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
                    var9.moveTo(var9.getX(), var9.getY(), var9.getZ(), var10, 0.0F);
                    var7.addFreshEntityWithPassengers(var9);
                    var1.playSound(null, var9.getX(), var9.getY(), var9.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
                    var9.gameEvent(GameEvent.ENTITY_PLACE, param0.getPlayer());
                }

                var4.shrink(1);
                return InteractionResult.sidedSuccess(var1.isClientSide);
            } else {
                return InteractionResult.FAIL;
            }
        }
    }
}
