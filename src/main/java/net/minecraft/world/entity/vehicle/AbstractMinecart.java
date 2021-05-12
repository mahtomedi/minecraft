package net.minecraft.world.entity.vehicle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractMinecart extends Entity {
    private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_BLOCK = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_OFFSET = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ID_CUSTOM_DISPLAY = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.BOOLEAN);
    private static final ImmutableMap<Pose, ImmutableList<Integer>> POSE_DISMOUNT_HEIGHTS = ImmutableMap.of(
        Pose.STANDING, ImmutableList.of(0, 1, -1), Pose.CROUCHING, ImmutableList.of(0, 1, -1), Pose.SWIMMING, ImmutableList.of(0, 1)
    );
    protected static final float WATER_SLOWDOWN_FACTOR = 0.95F;
    private boolean flipped;
    private static final Map<RailShape, Pair<Vec3i, Vec3i>> EXITS = Util.make(Maps.newEnumMap(RailShape.class), param0 -> {
        Vec3i var0 = Direction.WEST.getNormal();
        Vec3i var1 = Direction.EAST.getNormal();
        Vec3i var2 = Direction.NORTH.getNormal();
        Vec3i var3 = Direction.SOUTH.getNormal();
        Vec3i var4 = var0.below();
        Vec3i var5 = var1.below();
        Vec3i var6 = var2.below();
        Vec3i var7 = var3.below();
        param0.put(RailShape.NORTH_SOUTH, Pair.of(var2, var3));
        param0.put(RailShape.EAST_WEST, Pair.of(var0, var1));
        param0.put(RailShape.ASCENDING_EAST, Pair.of(var4, var1));
        param0.put(RailShape.ASCENDING_WEST, Pair.of(var0, var5));
        param0.put(RailShape.ASCENDING_NORTH, Pair.of(var2, var7));
        param0.put(RailShape.ASCENDING_SOUTH, Pair.of(var6, var3));
        param0.put(RailShape.SOUTH_EAST, Pair.of(var3, var1));
        param0.put(RailShape.SOUTH_WEST, Pair.of(var3, var0));
        param0.put(RailShape.NORTH_WEST, Pair.of(var2, var0));
        param0.put(RailShape.NORTH_EAST, Pair.of(var2, var1));
    });
    private int lSteps;
    private double lx;
    private double ly;
    private double lz;
    private double lyr;
    private double lxr;
    private double lxd;
    private double lyd;
    private double lzd;

    protected AbstractMinecart(EntityType<?> param0, Level param1) {
        super(param0, param1);
        this.blocksBuilding = true;
    }

    protected AbstractMinecart(EntityType<?> param0, Level param1, double param2, double param3, double param4) {
        this(param0, param1);
        this.setPos(param2, param3, param4);
        this.xo = param2;
        this.yo = param3;
        this.zo = param4;
    }

    public static AbstractMinecart createMinecart(Level param0, double param1, double param2, double param3, AbstractMinecart.Type param4) {
        if (param4 == AbstractMinecart.Type.CHEST) {
            return new MinecartChest(param0, param1, param2, param3);
        } else if (param4 == AbstractMinecart.Type.FURNACE) {
            return new MinecartFurnace(param0, param1, param2, param3);
        } else if (param4 == AbstractMinecart.Type.TNT) {
            return new MinecartTNT(param0, param1, param2, param3);
        } else if (param4 == AbstractMinecart.Type.SPAWNER) {
            return new MinecartSpawner(param0, param1, param2, param3);
        } else if (param4 == AbstractMinecart.Type.HOPPER) {
            return new MinecartHopper(param0, param1, param2, param3);
        } else {
            return (AbstractMinecart)(param4 == AbstractMinecart.Type.COMMAND_BLOCK
                ? new MinecartCommandBlock(param0, param1, param2, param3)
                : new Minecart(param0, param1, param2, param3));
        }
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ID_HURT, 0);
        this.entityData.define(DATA_ID_HURTDIR, 1);
        this.entityData.define(DATA_ID_DAMAGE, 0.0F);
        this.entityData.define(DATA_ID_DISPLAY_BLOCK, Block.getId(Blocks.AIR.defaultBlockState()));
        this.entityData.define(DATA_ID_DISPLAY_OFFSET, 6);
        this.entityData.define(DATA_ID_CUSTOM_DISPLAY, false);
    }

    @Override
    public boolean canCollideWith(Entity param0) {
        return Boat.canVehicleCollide(this, param0);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected Vec3 getRelativePortalPosition(Direction.Axis param0, BlockUtil.FoundRectangle param1) {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(param0, param1));
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.0;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity param0) {
        Direction var0 = this.getMotionDirection();
        if (var0.getAxis() == Direction.Axis.Y) {
            return super.getDismountLocationForPassenger(param0);
        } else {
            int[][] var1 = DismountHelper.offsetsForDirection(var0);
            BlockPos var2 = this.blockPosition();
            BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();
            ImmutableList<Pose> var4 = param0.getDismountPoses();

            for(Pose var5 : var4) {
                EntityDimensions var6 = param0.getDimensions(var5);
                float var7 = Math.min(var6.width, 1.0F) / 2.0F;

                for(int var8 : POSE_DISMOUNT_HEIGHTS.get(var5)) {
                    for(int[] var9 : var1) {
                        var3.set(var2.getX() + var9[0], var2.getY() + var8, var2.getZ() + var9[1]);
                        double var10 = this.level
                            .getBlockFloorHeight(
                                DismountHelper.nonClimbableShape(this.level, var3), () -> DismountHelper.nonClimbableShape(this.level, var3.below())
                            );
                        if (DismountHelper.isBlockFloorValid(var10)) {
                            AABB var11 = new AABB((double)(-var7), 0.0, (double)(-var7), (double)var7, (double)var6.height, (double)var7);
                            Vec3 var12 = Vec3.upFromBottomCenterOf(var3, var10);
                            if (DismountHelper.canDismountTo(this.level, param0, var11.move(var12))) {
                                param0.setPose(var5);
                                return var12;
                            }
                        }
                    }
                }
            }

            double var13 = this.getBoundingBox().maxY;
            var3.set((double)var2.getX(), var13, (double)var2.getZ());

            for(Pose var14 : var4) {
                double var15 = (double)param0.getDimensions(var14).height;
                int var16 = Mth.ceil(var13 - (double)var3.getY() + var15);
                double var17 = DismountHelper.findCeilingFrom(var3, var16, param0x -> this.level.getBlockState(param0x).getCollisionShape(this.level, param0x));
                if (var13 + var15 <= var17) {
                    param0.setPose(var14);
                    break;
                }
            }

            return super.getDismountLocationForPassenger(param0);
        }
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.level.isClientSide || this.isRemoved()) {
            return true;
        } else if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
            this.markHurt();
            this.setDamage(this.getDamage() + param1 * 10.0F);
            this.gameEvent(GameEvent.ENTITY_DAMAGED, param0.getEntity());
            boolean var0 = param0.getEntity() instanceof Player && ((Player)param0.getEntity()).getAbilities().instabuild;
            if (var0 || this.getDamage() > 40.0F) {
                this.ejectPassengers();
                if (var0 && !this.hasCustomName()) {
                    this.discard();
                } else {
                    this.destroy(param0);
                }
            }

            return true;
        }
    }

    @Override
    protected float getBlockSpeedFactor() {
        BlockState var0 = this.level.getBlockState(this.blockPosition());
        return var0.is(BlockTags.RAILS) ? 1.0F : super.getBlockSpeedFactor();
    }

    public void destroy(DamageSource param0) {
        this.remove(Entity.RemovalReason.KILLED);
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack var0 = new ItemStack(Items.MINECART);
            if (this.hasCustomName()) {
                var0.setHoverName(this.getCustomName());
            }

            this.spawnAtLocation(var0);
        }

    }

    @Override
    public void animateHurt() {
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    private static Pair<Vec3i, Vec3i> exits(RailShape param0) {
        return EXITS.get(param0);
    }

    @Override
    public Direction getMotionDirection() {
        return this.flipped ? this.getDirection().getOpposite().getClockWise() : this.getDirection().getClockWise();
    }

    @Override
    public void tick() {
        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }

        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }

        this.checkOutOfWorld();
        this.handleNetherPortal();
        if (this.level.isClientSide) {
            if (this.lSteps > 0) {
                double var0 = this.getX() + (this.lx - this.getX()) / (double)this.lSteps;
                double var1 = this.getY() + (this.ly - this.getY()) / (double)this.lSteps;
                double var2 = this.getZ() + (this.lz - this.getZ()) / (double)this.lSteps;
                double var3 = Mth.wrapDegrees(this.lyr - (double)this.getYRot());
                this.setYRot(this.getYRot() + (float)var3 / (float)this.lSteps);
                this.setXRot(this.getXRot() + (float)(this.lxr - (double)this.getXRot()) / (float)this.lSteps);
                --this.lSteps;
                this.setPos(var0, var1, var2);
                this.setRot(this.getYRot(), this.getXRot());
            } else {
                this.reapplyPosition();
                this.setRot(this.getYRot(), this.getXRot());
            }

        } else {
            if (!this.isNoGravity()) {
                double var4 = this.isInWater() ? -0.005 : -0.04;
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, var4, 0.0));
            }

            int var5 = Mth.floor(this.getX());
            int var6 = Mth.floor(this.getY());
            int var7 = Mth.floor(this.getZ());
            if (this.level.getBlockState(new BlockPos(var5, var6 - 1, var7)).is(BlockTags.RAILS)) {
                --var6;
            }

            BlockPos var8 = new BlockPos(var5, var6, var7);
            BlockState var9 = this.level.getBlockState(var8);
            if (BaseRailBlock.isRail(var9)) {
                this.moveAlongTrack(var8, var9);
                if (var9.is(Blocks.ACTIVATOR_RAIL)) {
                    this.activateMinecart(var5, var6, var7, var9.getValue(PoweredRailBlock.POWERED));
                }
            } else {
                this.comeOffTrack();
            }

            this.checkInsideBlocks();
            this.setXRot(0.0F);
            double var10 = this.xo - this.getX();
            double var11 = this.zo - this.getZ();
            if (var10 * var10 + var11 * var11 > 0.001) {
                this.setYRot((float)(Mth.atan2(var11, var10) * 180.0 / Math.PI));
                if (this.flipped) {
                    this.setYRot(this.getYRot() + 180.0F);
                }
            }

            double var12 = (double)Mth.wrapDegrees(this.getYRot() - this.yRotO);
            if (var12 < -170.0 || var12 >= 170.0) {
                this.setYRot(this.getYRot() + 180.0F);
                this.flipped = !this.flipped;
            }

            this.setRot(this.getYRot(), this.getXRot());
            if (this.getMinecartType() == AbstractMinecart.Type.RIDEABLE && getHorizontalDistanceSqr(this.getDeltaMovement()) > 0.01) {
                List<Entity> var13 = this.level.getEntities(this, this.getBoundingBox().inflate(0.2F, 0.0, 0.2F), EntitySelector.pushableBy(this));
                if (!var13.isEmpty()) {
                    for(int var14 = 0; var14 < var13.size(); ++var14) {
                        Entity var15 = var13.get(var14);
                        if (!(var15 instanceof Player)
                            && !(var15 instanceof IronGolem)
                            && !(var15 instanceof AbstractMinecart)
                            && !this.isVehicle()
                            && !var15.isPassenger()) {
                            var15.startRiding(this);
                        } else {
                            var15.push(this);
                        }
                    }
                }
            } else {
                for(Entity var16 : this.level.getEntities(this, this.getBoundingBox().inflate(0.2F, 0.0, 0.2F))) {
                    if (!this.hasPassenger(var16) && var16.isPushable() && var16 instanceof AbstractMinecart) {
                        var16.push(this);
                    }
                }
            }

            this.updateInWaterStateAndDoFluidPushing();
            if (this.isInLava()) {
                this.lavaHurt();
                this.fallDistance *= 0.5F;
            }

            this.firstTick = false;
        }
    }

    protected double getMaxSpeed() {
        return (this.isInWater() ? 4.0 : 8.0) / 20.0;
    }

    public void activateMinecart(int param0, int param1, int param2, boolean param3) {
    }

    protected void comeOffTrack() {
        double var0 = this.getMaxSpeed();
        Vec3 var1 = this.getDeltaMovement();
        this.setDeltaMovement(Mth.clamp(var1.x, -var0, var0), var1.y, Mth.clamp(var1.z, -var0, var0));
        if (this.onGround) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.onGround) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.95));
        }

    }

    protected void moveAlongTrack(BlockPos param0, BlockState param1) {
        this.fallDistance = 0.0F;
        double var0 = this.getX();
        double var1 = this.getY();
        double var2 = this.getZ();
        Vec3 var3 = this.getPos(var0, var1, var2);
        var1 = (double)param0.getY();
        boolean var4 = false;
        boolean var5 = false;
        if (param1.is(Blocks.POWERED_RAIL)) {
            var4 = param1.getValue(PoweredRailBlock.POWERED);
            var5 = !var4;
        }

        double var6 = 0.0078125;
        if (this.isInWater()) {
            var6 *= 0.2;
        }

        Vec3 var7 = this.getDeltaMovement();
        RailShape var8 = param1.getValue(((BaseRailBlock)param1.getBlock()).getShapeProperty());
        switch(var8) {
            case ASCENDING_EAST:
                this.setDeltaMovement(var7.add(-var6, 0.0, 0.0));
                ++var1;
                break;
            case ASCENDING_WEST:
                this.setDeltaMovement(var7.add(var6, 0.0, 0.0));
                ++var1;
                break;
            case ASCENDING_NORTH:
                this.setDeltaMovement(var7.add(0.0, 0.0, var6));
                ++var1;
                break;
            case ASCENDING_SOUTH:
                this.setDeltaMovement(var7.add(0.0, 0.0, -var6));
                ++var1;
        }

        var7 = this.getDeltaMovement();
        Pair<Vec3i, Vec3i> var9 = exits(var8);
        Vec3i var10 = var9.getFirst();
        Vec3i var11 = var9.getSecond();
        double var12 = (double)(var11.getX() - var10.getX());
        double var13 = (double)(var11.getZ() - var10.getZ());
        double var14 = Math.sqrt(var12 * var12 + var13 * var13);
        double var15 = var7.x * var12 + var7.z * var13;
        if (var15 < 0.0) {
            var12 = -var12;
            var13 = -var13;
        }

        double var16 = Math.min(2.0, Math.sqrt(getHorizontalDistanceSqr(var7)));
        var7 = new Vec3(var16 * var12 / var14, var7.y, var16 * var13 / var14);
        this.setDeltaMovement(var7);
        Entity var17 = this.getFirstPassenger();
        if (var17 instanceof Player) {
            Vec3 var18 = var17.getDeltaMovement();
            double var19 = getHorizontalDistanceSqr(var18);
            double var20 = getHorizontalDistanceSqr(this.getDeltaMovement());
            if (var19 > 1.0E-4 && var20 < 0.01) {
                this.setDeltaMovement(this.getDeltaMovement().add(var18.x * 0.1, 0.0, var18.z * 0.1));
                var5 = false;
            }
        }

        if (var5) {
            double var21 = Math.sqrt(getHorizontalDistanceSqr(this.getDeltaMovement()));
            if (var21 < 0.03) {
                this.setDeltaMovement(Vec3.ZERO);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.0, 0.5));
            }
        }

        double var22 = (double)param0.getX() + 0.5 + (double)var10.getX() * 0.5;
        double var23 = (double)param0.getZ() + 0.5 + (double)var10.getZ() * 0.5;
        double var24 = (double)param0.getX() + 0.5 + (double)var11.getX() * 0.5;
        double var25 = (double)param0.getZ() + 0.5 + (double)var11.getZ() * 0.5;
        var12 = var24 - var22;
        var13 = var25 - var23;
        double var26;
        if (var12 == 0.0) {
            var26 = var2 - (double)param0.getZ();
        } else if (var13 == 0.0) {
            var26 = var0 - (double)param0.getX();
        } else {
            double var28 = var0 - var22;
            double var29 = var2 - var23;
            var26 = (var28 * var12 + var29 * var13) * 2.0;
        }

        var0 = var22 + var12 * var26;
        var2 = var23 + var13 * var26;
        this.setPos(var0, var1, var2);
        double var31 = this.isVehicle() ? 0.75 : 1.0;
        double var32 = this.getMaxSpeed();
        var7 = this.getDeltaMovement();
        this.move(MoverType.SELF, new Vec3(Mth.clamp(var31 * var7.x, -var32, var32), 0.0, Mth.clamp(var31 * var7.z, -var32, var32)));
        if (var10.getY() != 0 && Mth.floor(this.getX()) - param0.getX() == var10.getX() && Mth.floor(this.getZ()) - param0.getZ() == var10.getZ()) {
            this.setPos(this.getX(), this.getY() + (double)var10.getY(), this.getZ());
        } else if (var11.getY() != 0 && Mth.floor(this.getX()) - param0.getX() == var11.getX() && Mth.floor(this.getZ()) - param0.getZ() == var11.getZ()) {
            this.setPos(this.getX(), this.getY() + (double)var11.getY(), this.getZ());
        }

        this.applyNaturalSlowdown();
        Vec3 var33 = this.getPos(this.getX(), this.getY(), this.getZ());
        if (var33 != null && var3 != null) {
            double var34 = (var3.y - var33.y) * 0.05;
            Vec3 var35 = this.getDeltaMovement();
            double var36 = Math.sqrt(getHorizontalDistanceSqr(var35));
            if (var36 > 0.0) {
                this.setDeltaMovement(var35.multiply((var36 + var34) / var36, 1.0, (var36 + var34) / var36));
            }

            this.setPos(this.getX(), var33.y, this.getZ());
        }

        int var37 = Mth.floor(this.getX());
        int var38 = Mth.floor(this.getZ());
        if (var37 != param0.getX() || var38 != param0.getZ()) {
            Vec3 var39 = this.getDeltaMovement();
            double var40 = Math.sqrt(getHorizontalDistanceSqr(var39));
            this.setDeltaMovement(var40 * (double)(var37 - param0.getX()), var39.y, var40 * (double)(var38 - param0.getZ()));
        }

        if (var4) {
            Vec3 var41 = this.getDeltaMovement();
            double var42 = Math.sqrt(getHorizontalDistanceSqr(var41));
            if (var42 > 0.01) {
                double var43 = 0.06;
                this.setDeltaMovement(var41.add(var41.x / var42 * 0.06, 0.0, var41.z / var42 * 0.06));
            } else {
                Vec3 var44 = this.getDeltaMovement();
                double var45 = var44.x;
                double var46 = var44.z;
                if (var8 == RailShape.EAST_WEST) {
                    if (this.isRedstoneConductor(param0.west())) {
                        var45 = 0.02;
                    } else if (this.isRedstoneConductor(param0.east())) {
                        var45 = -0.02;
                    }
                } else {
                    if (var8 != RailShape.NORTH_SOUTH) {
                        return;
                    }

                    if (this.isRedstoneConductor(param0.north())) {
                        var46 = 0.02;
                    } else if (this.isRedstoneConductor(param0.south())) {
                        var46 = -0.02;
                    }
                }

                this.setDeltaMovement(var45, var44.y, var46);
            }
        }

    }

    private boolean isRedstoneConductor(BlockPos param0) {
        return this.level.getBlockState(param0).isRedstoneConductor(this.level, param0);
    }

    protected void applyNaturalSlowdown() {
        double var0 = this.isVehicle() ? 0.997 : 0.96;
        Vec3 var1 = this.getDeltaMovement();
        var1 = var1.multiply(var0, 0.0, var0);
        if (this.isInWater()) {
            var1 = var1.scale(0.95F);
        }

        this.setDeltaMovement(var1);
    }

    @Nullable
    public Vec3 getPosOffs(double param0, double param1, double param2, double param3) {
        int var0 = Mth.floor(param0);
        int var1 = Mth.floor(param1);
        int var2 = Mth.floor(param2);
        if (this.level.getBlockState(new BlockPos(var0, var1 - 1, var2)).is(BlockTags.RAILS)) {
            --var1;
        }

        BlockState var3 = this.level.getBlockState(new BlockPos(var0, var1, var2));
        if (BaseRailBlock.isRail(var3)) {
            RailShape var4 = var3.getValue(((BaseRailBlock)var3.getBlock()).getShapeProperty());
            param1 = (double)var1;
            if (var4.isAscending()) {
                param1 = (double)(var1 + 1);
            }

            Pair<Vec3i, Vec3i> var5 = exits(var4);
            Vec3i var6 = var5.getFirst();
            Vec3i var7 = var5.getSecond();
            double var8 = (double)(var7.getX() - var6.getX());
            double var9 = (double)(var7.getZ() - var6.getZ());
            double var10 = Math.sqrt(var8 * var8 + var9 * var9);
            var8 /= var10;
            var9 /= var10;
            param0 += var8 * param3;
            param2 += var9 * param3;
            if (var6.getY() != 0 && Mth.floor(param0) - var0 == var6.getX() && Mth.floor(param2) - var2 == var6.getZ()) {
                param1 += (double)var6.getY();
            } else if (var7.getY() != 0 && Mth.floor(param0) - var0 == var7.getX() && Mth.floor(param2) - var2 == var7.getZ()) {
                param1 += (double)var7.getY();
            }

            return this.getPos(param0, param1, param2);
        } else {
            return null;
        }
    }

    @Nullable
    public Vec3 getPos(double param0, double param1, double param2) {
        int var0 = Mth.floor(param0);
        int var1 = Mth.floor(param1);
        int var2 = Mth.floor(param2);
        if (this.level.getBlockState(new BlockPos(var0, var1 - 1, var2)).is(BlockTags.RAILS)) {
            --var1;
        }

        BlockState var3 = this.level.getBlockState(new BlockPos(var0, var1, var2));
        if (BaseRailBlock.isRail(var3)) {
            RailShape var4 = var3.getValue(((BaseRailBlock)var3.getBlock()).getShapeProperty());
            Pair<Vec3i, Vec3i> var5 = exits(var4);
            Vec3i var6 = var5.getFirst();
            Vec3i var7 = var5.getSecond();
            double var8 = (double)var0 + 0.5 + (double)var6.getX() * 0.5;
            double var9 = (double)var1 + 0.0625 + (double)var6.getY() * 0.5;
            double var10 = (double)var2 + 0.5 + (double)var6.getZ() * 0.5;
            double var11 = (double)var0 + 0.5 + (double)var7.getX() * 0.5;
            double var12 = (double)var1 + 0.0625 + (double)var7.getY() * 0.5;
            double var13 = (double)var2 + 0.5 + (double)var7.getZ() * 0.5;
            double var14 = var11 - var8;
            double var15 = (var12 - var9) * 2.0;
            double var16 = var13 - var10;
            double var17;
            if (var14 == 0.0) {
                var17 = param2 - (double)var2;
            } else if (var16 == 0.0) {
                var17 = param0 - (double)var0;
            } else {
                double var19 = param0 - var8;
                double var20 = param2 - var10;
                var17 = (var19 * var14 + var20 * var16) * 2.0;
            }

            param0 = var8 + var14 * var17;
            param1 = var9 + var15 * var17;
            param2 = var10 + var16 * var17;
            if (var15 < 0.0) {
                ++param1;
            } else if (var15 > 0.0) {
                param1 += 0.5;
            }

            return new Vec3(param0, param1, param2);
        } else {
            return null;
        }
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        AABB var0 = this.getBoundingBox();
        return this.hasCustomDisplay() ? var0.inflate((double)Math.abs(this.getDisplayOffset()) / 16.0) : var0;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        if (param0.getBoolean("CustomDisplayTile")) {
            this.setDisplayBlockState(NbtUtils.readBlockState(param0.getCompound("DisplayState")));
            this.setDisplayOffset(param0.getInt("DisplayOffset"));
        }

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        if (this.hasCustomDisplay()) {
            param0.putBoolean("CustomDisplayTile", true);
            param0.put("DisplayState", NbtUtils.writeBlockState(this.getDisplayBlockState()));
            param0.putInt("DisplayOffset", this.getDisplayOffset());
        }

    }

    @Override
    public void push(Entity param0) {
        if (!this.level.isClientSide) {
            if (!param0.noPhysics && !this.noPhysics) {
                if (!this.hasPassenger(param0)) {
                    double var0 = param0.getX() - this.getX();
                    double var1 = param0.getZ() - this.getZ();
                    double var2 = var0 * var0 + var1 * var1;
                    if (var2 >= 1.0E-4F) {
                        var2 = (double)Mth.sqrt(var2);
                        var0 /= var2;
                        var1 /= var2;
                        double var3 = 1.0 / var2;
                        if (var3 > 1.0) {
                            var3 = 1.0;
                        }

                        var0 *= var3;
                        var1 *= var3;
                        var0 *= 0.1F;
                        var1 *= 0.1F;
                        var0 *= 0.5;
                        var1 *= 0.5;
                        if (param0 instanceof AbstractMinecart) {
                            double var4 = param0.getX() - this.getX();
                            double var5 = param0.getZ() - this.getZ();
                            Vec3 var6 = new Vec3(var4, 0.0, var5).normalize();
                            Vec3 var7 = new Vec3(
                                    (double)Mth.cos(this.getYRot() * (float) (Math.PI / 180.0)),
                                    0.0,
                                    (double)Mth.sin(this.getYRot() * (float) (Math.PI / 180.0))
                                )
                                .normalize();
                            double var8 = Math.abs(var6.dot(var7));
                            if (var8 < 0.8F) {
                                return;
                            }

                            Vec3 var9 = this.getDeltaMovement();
                            Vec3 var10 = param0.getDeltaMovement();
                            if (((AbstractMinecart)param0).getMinecartType() == AbstractMinecart.Type.FURNACE
                                && this.getMinecartType() != AbstractMinecart.Type.FURNACE) {
                                this.setDeltaMovement(var9.multiply(0.2, 1.0, 0.2));
                                this.push(var10.x - var0, 0.0, var10.z - var1);
                                param0.setDeltaMovement(var10.multiply(0.95, 1.0, 0.95));
                            } else if (((AbstractMinecart)param0).getMinecartType() != AbstractMinecart.Type.FURNACE
                                && this.getMinecartType() == AbstractMinecart.Type.FURNACE) {
                                param0.setDeltaMovement(var10.multiply(0.2, 1.0, 0.2));
                                param0.push(var9.x + var0, 0.0, var9.z + var1);
                                this.setDeltaMovement(var9.multiply(0.95, 1.0, 0.95));
                            } else {
                                double var11 = (var10.x + var9.x) / 2.0;
                                double var12 = (var10.z + var9.z) / 2.0;
                                this.setDeltaMovement(var9.multiply(0.2, 1.0, 0.2));
                                this.push(var11 - var0, 0.0, var12 - var1);
                                param0.setDeltaMovement(var10.multiply(0.2, 1.0, 0.2));
                                param0.push(var11 + var0, 0.0, var12 + var1);
                            }
                        } else {
                            this.push(-var0, 0.0, -var1);
                            param0.push(var0 / 4.0, 0.0, var1 / 4.0);
                        }
                    }

                }
            }
        }
    }

    @Override
    public void lerpTo(double param0, double param1, double param2, float param3, float param4, int param5, boolean param6) {
        this.lx = param0;
        this.ly = param1;
        this.lz = param2;
        this.lyr = (double)param3;
        this.lxr = (double)param4;
        this.lSteps = param5 + 2;
        this.setDeltaMovement(this.lxd, this.lyd, this.lzd);
    }

    @Override
    public void lerpMotion(double param0, double param1, double param2) {
        this.lxd = param0;
        this.lyd = param1;
        this.lzd = param2;
        this.setDeltaMovement(this.lxd, this.lyd, this.lzd);
    }

    public void setDamage(float param0) {
        this.entityData.set(DATA_ID_DAMAGE, param0);
    }

    public float getDamage() {
        return this.entityData.get(DATA_ID_DAMAGE);
    }

    public void setHurtTime(int param0) {
        this.entityData.set(DATA_ID_HURT, param0);
    }

    public int getHurtTime() {
        return this.entityData.get(DATA_ID_HURT);
    }

    public void setHurtDir(int param0) {
        this.entityData.set(DATA_ID_HURTDIR, param0);
    }

    public int getHurtDir() {
        return this.entityData.get(DATA_ID_HURTDIR);
    }

    public abstract AbstractMinecart.Type getMinecartType();

    public BlockState getDisplayBlockState() {
        return !this.hasCustomDisplay() ? this.getDefaultDisplayBlockState() : Block.stateById(this.getEntityData().get(DATA_ID_DISPLAY_BLOCK));
    }

    public BlockState getDefaultDisplayBlockState() {
        return Blocks.AIR.defaultBlockState();
    }

    public int getDisplayOffset() {
        return !this.hasCustomDisplay() ? this.getDefaultDisplayOffset() : this.getEntityData().get(DATA_ID_DISPLAY_OFFSET);
    }

    public int getDefaultDisplayOffset() {
        return 6;
    }

    public void setDisplayBlockState(BlockState param0) {
        this.getEntityData().set(DATA_ID_DISPLAY_BLOCK, Block.getId(param0));
        this.setCustomDisplay(true);
    }

    public void setDisplayOffset(int param0) {
        this.getEntityData().set(DATA_ID_DISPLAY_OFFSET, param0);
        this.setCustomDisplay(true);
    }

    public boolean hasCustomDisplay() {
        return this.getEntityData().get(DATA_ID_CUSTOM_DISPLAY);
    }

    public void setCustomDisplay(boolean param0) {
        this.getEntityData().set(DATA_ID_CUSTOM_DISPLAY, param0);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(switch(this.getMinecartType()) {
            case FURNACE -> Items.FURNACE_MINECART;
            case CHEST -> Items.CHEST_MINECART;
            case TNT -> Items.TNT_MINECART;
            case HOPPER -> Items.HOPPER_MINECART;
            case COMMAND_BLOCK -> Items.COMMAND_BLOCK_MINECART;
            default -> Items.MINECART;
        });
    }

    public static enum Type {
        RIDEABLE,
        CHEST,
        FURNACE,
        TNT,
        SPAWNER,
        HOPPER,
        COMMAND_BLOCK;
    }
}
