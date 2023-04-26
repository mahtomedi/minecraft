package net.minecraft.world.entity.decoration;

import com.mojang.logging.LogUtils;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public abstract class HangingEntity extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected static final Predicate<Entity> HANGING_ENTITY = param0 -> param0 instanceof HangingEntity;
    private int checkInterval;
    protected BlockPos pos;
    protected Direction direction = Direction.SOUTH;

    protected HangingEntity(EntityType<? extends HangingEntity> param0, Level param1) {
        super(param0, param1);
    }

    protected HangingEntity(EntityType<? extends HangingEntity> param0, Level param1, BlockPos param2) {
        this(param0, param1);
        this.pos = param2;
    }

    @Override
    protected void defineSynchedData() {
    }

    protected void setDirection(Direction param0) {
        Validate.notNull(param0);
        Validate.isTrue(param0.getAxis().isHorizontal());
        this.direction = param0;
        this.setYRot((float)(this.direction.get2DDataValue() * 90));
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    protected void recalculateBoundingBox() {
        if (this.direction != null) {
            double var0 = (double)this.pos.getX() + 0.5;
            double var1 = (double)this.pos.getY() + 0.5;
            double var2 = (double)this.pos.getZ() + 0.5;
            double var3 = 0.46875;
            double var4 = this.offs(this.getWidth());
            double var5 = this.offs(this.getHeight());
            var0 -= (double)this.direction.getStepX() * 0.46875;
            var2 -= (double)this.direction.getStepZ() * 0.46875;
            var1 += var5;
            Direction var6 = this.direction.getCounterClockWise();
            var0 += var4 * (double)var6.getStepX();
            var2 += var4 * (double)var6.getStepZ();
            this.setPosRaw(var0, var1, var2);
            double var7 = (double)this.getWidth();
            double var8 = (double)this.getHeight();
            double var9 = (double)this.getWidth();
            if (this.direction.getAxis() == Direction.Axis.Z) {
                var9 = 1.0;
            } else {
                var7 = 1.0;
            }

            var7 /= 32.0;
            var8 /= 32.0;
            var9 /= 32.0;
            this.setBoundingBox(new AABB(var0 - var7, var1 - var8, var2 - var9, var0 + var7, var1 + var8, var2 + var9));
        }
    }

    private double offs(int param0) {
        return param0 % 32 == 0 ? 0.5 : 0.0;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            this.checkOutOfWorld();
            if (this.checkInterval++ == 100) {
                this.checkInterval = 0;
                if (!this.isRemoved() && !this.survives()) {
                    this.discard();
                    this.dropItem(null);
                }
            }
        }

    }

    public boolean survives() {
        if (!this.level().noCollision(this)) {
            return false;
        } else {
            int var0 = Math.max(1, this.getWidth() / 16);
            int var1 = Math.max(1, this.getHeight() / 16);
            BlockPos var2 = this.pos.relative(this.direction.getOpposite());
            Direction var3 = this.direction.getCounterClockWise();
            BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

            for(int var5 = 0; var5 < var0; ++var5) {
                for(int var6 = 0; var6 < var1; ++var6) {
                    int var7 = (var0 - 1) / -2;
                    int var8 = (var1 - 1) / -2;
                    var4.set(var2).move(var3, var5 + var7).move(Direction.UP, var6 + var8);
                    BlockState var9 = this.level().getBlockState(var4);
                    if (!var9.isSolid() && !DiodeBlock.isDiode(var9)) {
                        return false;
                    }
                }
            }

            return this.level().getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity param0) {
        if (param0 instanceof Player var0) {
            return !this.level().mayInteract(var0, this.pos) ? true : this.hurt(this.damageSources().playerAttack(var0), 0.0F);
        } else {
            return false;
        }
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            if (!this.isRemoved() && !this.level().isClientSide) {
                this.kill();
                this.markHurt();
                this.dropItem(param0.getEntity());
            }

            return true;
        }
    }

    @Override
    public void move(MoverType param0, Vec3 param1) {
        if (!this.level().isClientSide && !this.isRemoved() && param1.lengthSqr() > 0.0) {
            this.kill();
            this.dropItem(null);
        }

    }

    @Override
    public void push(double param0, double param1, double param2) {
        if (!this.level().isClientSide && !this.isRemoved() && param0 * param0 + param1 * param1 + param2 * param2 > 0.0) {
            this.kill();
            this.dropItem(null);
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        BlockPos var0 = this.getPos();
        param0.putInt("TileX", var0.getX());
        param0.putInt("TileY", var0.getY());
        param0.putInt("TileZ", var0.getZ());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        BlockPos var0 = new BlockPos(param0.getInt("TileX"), param0.getInt("TileY"), param0.getInt("TileZ"));
        if (!var0.closerThan(this.blockPosition(), 16.0)) {
            LOGGER.error("Hanging entity at invalid position: {}", var0);
        } else {
            this.pos = var0;
        }
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void dropItem(@Nullable Entity var1);

    public abstract void playPlacementSound();

    @Override
    public ItemEntity spawnAtLocation(ItemStack param0, float param1) {
        ItemEntity var0 = new ItemEntity(
            this.level(),
            this.getX() + (double)((float)this.direction.getStepX() * 0.15F),
            this.getY() + (double)param1,
            this.getZ() + (double)((float)this.direction.getStepZ() * 0.15F),
            param0
        );
        var0.setDefaultPickUpDelay();
        this.level().addFreshEntity(var0);
        return var0;
    }

    @Override
    protected boolean repositionEntityAfterLoad() {
        return false;
    }

    @Override
    public void setPos(double param0, double param1, double param2) {
        this.pos = BlockPos.containing(param0, param1, param2);
        this.recalculateBoundingBox();
        this.hasImpulse = true;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    @Override
    public float rotate(Rotation param0) {
        if (this.direction.getAxis() != Direction.Axis.Y) {
            switch(param0) {
                case CLOCKWISE_180:
                    this.direction = this.direction.getOpposite();
                    break;
                case COUNTERCLOCKWISE_90:
                    this.direction = this.direction.getCounterClockWise();
                    break;
                case CLOCKWISE_90:
                    this.direction = this.direction.getClockWise();
            }
        }

        float var0 = Mth.wrapDegrees(this.getYRot());
        switch(param0) {
            case CLOCKWISE_180:
                return var0 + 180.0F;
            case COUNTERCLOCKWISE_90:
                return var0 + 90.0F;
            case CLOCKWISE_90:
                return var0 + 270.0F;
            default:
                return var0;
        }
    }

    @Override
    public float mirror(Mirror param0) {
        return this.rotate(param0.getRotation(this.direction));
    }

    @Override
    public void thunderHit(ServerLevel param0, LightningBolt param1) {
    }

    @Override
    public void refreshDimensions() {
    }
}
