package net.minecraft.world.entity.decoration;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LeashFenceKnotEntity extends HangingEntity {
    public static final double OFFSET_Y = 0.375;

    public LeashFenceKnotEntity(EntityType<? extends LeashFenceKnotEntity> param0, Level param1) {
        super(param0, param1);
    }

    public LeashFenceKnotEntity(Level param0, BlockPos param1) {
        super(EntityType.LEASH_KNOT, param0, param1);
        this.setPos((double)param1.getX(), (double)param1.getY(), (double)param1.getZ());
    }

    @Override
    protected void recalculateBoundingBox() {
        this.setPosRaw((double)this.pos.getX() + 0.5, (double)this.pos.getY() + 0.375, (double)this.pos.getZ() + 0.5);
        double var0 = (double)this.getType().getWidth() / 2.0;
        double var1 = (double)this.getType().getHeight();
        this.setBoundingBox(new AABB(this.getX() - var0, this.getY(), this.getZ() - var0, this.getX() + var0, this.getY() + var1, this.getZ() + var0));
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
        return 0.0625F;
    }

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
    public InteractionResult interact(Player param0, InteractionHand param1) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            boolean var0 = false;
            double var1 = 7.0;
            List<Mob> var2 = this.level()
                .getEntitiesOfClass(
                    Mob.class, new AABB(this.getX() - 7.0, this.getY() - 7.0, this.getZ() - 7.0, this.getX() + 7.0, this.getY() + 7.0, this.getZ() + 7.0)
                );

            for(Mob var3 : var2) {
                if (var3.getLeashHolder() == param0) {
                    var3.setLeashedTo(this, true);
                    var0 = true;
                }
            }

            boolean var4 = false;
            if (!var0) {
                this.discard();
                if (param0.getAbilities().instabuild) {
                    for(Mob var5 : var2) {
                        if (var5.isLeashed() && var5.getLeashHolder() == this) {
                            var5.dropLeash(true, false);
                            var4 = true;
                        }
                    }
                }
            }

            if (var0 || var4) {
                this.gameEvent(GameEvent.BLOCK_ATTACH, param0);
            }

            return InteractionResult.CONSUME;
        }
    }

    @Override
    public boolean survives() {
        return this.level().getBlockState(this.pos).is(BlockTags.FENCES);
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
        return var5;
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.LEASH_KNOT_PLACE, 1.0F, 1.0F);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, 0, this.getPos());
    }

    @Override
    public Vec3 getRopeHoldPosition(float param0) {
        return this.getPosition(param0).add(0.0, 0.2, 0.0);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.LEAD);
    }
}
