package net.minecraft.world.entity.decoration;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LeashFenceKnotEntity extends HangingEntity {
    public LeashFenceKnotEntity(EntityType<? extends LeashFenceKnotEntity> param0, Level param1) {
        super(param0, param1);
    }

    public LeashFenceKnotEntity(Level param0, BlockPos param1) {
        super(EntityType.LEASH_KNOT, param0, param1);
        this.setPos((double)param1.getX() + 0.5, (double)param1.getY() + 0.5, (double)param1.getZ() + 0.5);
        float var0 = 0.125F;
        float var1 = 0.1875F;
        float var2 = 0.25F;
        this.setBoundingBox(new AABB(this.x - 0.1875, this.y - 0.25 + 0.125, this.z - 0.1875, this.x + 0.1875, this.y + 0.25 + 0.125, this.z + 0.1875));
        this.forcedLoading = true;
    }

    @Override
    public void setPos(double param0, double param1, double param2) {
        super.setPos((double)Mth.floor(param0) + 0.5, (double)Mth.floor(param1) + 0.5, (double)Mth.floor(param2) + 0.5);
    }

    @Override
    protected void recalculateBoundingBox() {
        this.x = (double)this.pos.getX() + 0.5;
        this.y = (double)this.pos.getY() + 0.5;
        this.z = (double)this.pos.getZ() + 0.5;
    }

    @Override
    public void setDirection(Direction param0) {
    }

    @Override
    public int getWidth() {
        return 9;
    }

    @Override
    public int getHeight() {
        return 9;
    }

    @Override
    protected float getEyeHeight(Pose param0, EntityDimensions param1) {
        return -0.0625F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRenderAtSqrDistance(double param0) {
        return param0 < 1024.0;
    }

    @Override
    public void dropItem(@Nullable Entity param0) {
        this.playSound(SoundEvents.LEASH_KNOT_BREAK, 1.0F, 1.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
    }

    @Override
    public boolean interact(Player param0, InteractionHand param1) {
        if (this.level.isClientSide) {
            return true;
        } else {
            boolean var0 = false;
            double var1 = 7.0;
            List<Mob> var2 = this.level
                .getEntitiesOfClass(Mob.class, new AABB(this.x - 7.0, this.y - 7.0, this.z - 7.0, this.x + 7.0, this.y + 7.0, this.z + 7.0));

            for(Mob var3 : var2) {
                if (var3.getLeashHolder() == param0) {
                    var3.setLeashedTo(this, true);
                    var0 = true;
                }
            }

            if (!var0) {
                this.remove();
                if (param0.abilities.instabuild) {
                    for(Mob var4 : var2) {
                        if (var4.isLeashed() && var4.getLeashHolder() == this) {
                            var4.dropLeash(true, false);
                        }
                    }
                }
            }

            return true;
        }
    }

    @Override
    public boolean survives() {
        return this.level.getBlockState(this.pos).getBlock().is(BlockTags.FENCES);
    }

    public static LeashFenceKnotEntity getOrCreateKnot(Level param0, BlockPos param1) {
        int var0 = param1.getX();
        int var1 = param1.getY();
        int var2 = param1.getZ();

        for(LeashFenceKnotEntity var4 : param0.getEntitiesOfClass(
            LeashFenceKnotEntity.class,
            new AABB((double)var0 - 1.0, (double)var1 - 1.0, (double)var2 - 1.0, (double)var0 + 1.0, (double)var1 + 1.0, (double)var2 + 1.0)
        )) {
            if (var4.getPos().equals(param1)) {
                return var4;
            }
        }

        LeashFenceKnotEntity var5 = new LeashFenceKnotEntity(param0, param1);
        param0.addFreshEntity(var5);
        var5.playPlacementSound();
        return var5;
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.LEASH_KNOT_PLACE, 1.0F, 1.0F);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, this.getType(), 0, this.getPos());
    }
}
