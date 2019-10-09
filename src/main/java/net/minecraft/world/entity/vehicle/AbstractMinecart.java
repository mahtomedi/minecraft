package net.minecraft.world.entity.vehicle;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
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
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractMinecart extends Entity {
    private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_BLOCK = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_OFFSET = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ID_CUSTOM_DISPLAY = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.BOOLEAN);
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
    @OnlyIn(Dist.CLIENT)
    private double lxd;
    @OnlyIn(Dist.CLIENT)
    private double lyd;
    @OnlyIn(Dist.CLIENT)
    private double lzd;

    protected AbstractMinecart(EntityType<?> param0, Level param1) {
        super(param0, param1);
        this.blocksBuilding = true;
    }

    protected AbstractMinecart(EntityType<?> param0, Level param1, double param2, double param3, double param4) {
        this(param0, param1);
        this.setPos(param2, param3, param4);
        this.setDeltaMovement(Vec3.ZERO);
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
    protected boolean isMovementNoisy() {
        return false;
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

    @Nullable
    @Override
    public AABB getCollideAgainstBox(Entity param0) {
        return param0.isPushable() ? param0.getBoundingBox() : null;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public double getRideHeight() {
        return 0.0;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.level.isClientSide || this.removed) {
            return true;
        } else if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
            this.markHurt();
            this.setDamage(this.getDamage() + param1 * 10.0F);
            boolean var0 = param0.getEntity() instanceof Player && ((Player)param0.getEntity()).abilities.instabuild;
            if (var0 || this.getDamage() > 40.0F) {
                this.ejectPassengers();
                if (var0 && !this.hasCustomName()) {
                    this.remove();
                } else {
                    this.destroy(param0);
                }
            }

            return true;
        }
    }

    public void destroy(DamageSource param0) {
        this.remove();
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack var0 = new ItemStack(Items.MINECART);
            if (this.hasCustomName()) {
                var0.setHoverName(this.getCustomName());
            }

            this.spawnAtLocation(var0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateHurt() {
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
    }

    @Override
    public boolean isPickable() {
        return !this.removed;
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

        if (this.getY() < -64.0) {
            this.outOfWorld();
        }

        this.handleNetherPortal();
        if (this.level.isClientSide) {
            if (this.lSteps > 0) {
                double var0 = this.getX() + (this.lx - this.getX()) / (double)this.lSteps;
                double var1 = this.getY() + (this.ly - this.getY()) / (double)this.lSteps;
                double var2 = this.getZ() + (this.lz - this.getZ()) / (double)this.lSteps;
                double var3 = Mth.wrapDegrees(this.lyr - (double)this.yRot);
                this.yRot = (float)((double)this.yRot + var3 / (double)this.lSteps);
                this.xRot = (float)((double)this.xRot + (this.lxr - (double)this.xRot) / (double)this.lSteps);
                --this.lSteps;
                this.setPos(var0, var1, var2);
                this.setRot(this.yRot, this.xRot);
            } else {
                this.refreshBoundingBox();
                this.setRot(this.yRot, this.xRot);
            }

        } else {
            if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
            }

            int var4 = Mth.floor(this.getX());
            int var5 = Mth.floor(this.getY());
            int var6 = Mth.floor(this.getZ());
            if (this.level.getBlockState(new BlockPos(var4, var5 - 1, var6)).is(BlockTags.RAILS)) {
                --var5;
            }

            BlockPos var7 = new BlockPos(var4, var5, var6);
            BlockState var8 = this.level.getBlockState(var7);
            if (var8.is(BlockTags.RAILS)) {
                this.moveAlongTrack(var7, var8);
                if (var8.getBlock() == Blocks.ACTIVATOR_RAIL) {
                    this.activateMinecart(var4, var5, var6, var8.getValue(PoweredRailBlock.POWERED));
                }
            } else {
                this.comeOffTrack();
            }

            this.checkInsideBlocks();
            this.xRot = 0.0F;
            double var9 = this.xo - this.getX();
            double var10 = this.zo - this.getZ();
            if (var9 * var9 + var10 * var10 > 0.001) {
                this.yRot = (float)(Mth.atan2(var10, var9) * 180.0 / Math.PI);
                if (this.flipped) {
                    this.yRot += 180.0F;
                }
            }

            double var11 = (double)Mth.wrapDegrees(this.yRot - this.yRotO);
            if (var11 < -170.0 || var11 >= 170.0) {
                this.yRot += 180.0F;
                this.flipped = !this.flipped;
            }

            this.setRot(this.yRot, this.xRot);
            if (this.getMinecartType() == AbstractMinecart.Type.RIDEABLE && getHorizontalDistanceSqr(this.getDeltaMovement()) > 0.01) {
                List<Entity> var12 = this.level.getEntities(this, this.getBoundingBox().inflate(0.2F, 0.0, 0.2F), EntitySelector.pushableBy(this));
                if (!var12.isEmpty()) {
                    for(int var13 = 0; var13 < var12.size(); ++var13) {
                        Entity var14 = var12.get(var13);
                        if (!(var14 instanceof Player)
                            && !(var14 instanceof IronGolem)
                            && !(var14 instanceof AbstractMinecart)
                            && !this.isVehicle()
                            && !var14.isPassenger()) {
                            var14.startRiding(this);
                        } else {
                            var14.push(this);
                        }
                    }
                }
            } else {
                for(Entity var15 : this.level.getEntities(this, this.getBoundingBox().inflate(0.2F, 0.0, 0.2F))) {
                    if (!this.hasPassenger(var15) && var15.isPushable() && var15 instanceof AbstractMinecart) {
                        var15.push(this);
                    }
                }
            }

            this.updateInWaterState();
        }
    }

    protected double getMaxSpeed() {
        return 0.4;
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
        BaseRailBlock var6 = (BaseRailBlock)param1.getBlock();
        if (var6 == Blocks.POWERED_RAIL) {
            var4 = param1.getValue(PoweredRailBlock.POWERED);
            var5 = !var4;
        }

        double var7 = 0.0078125;
        Vec3 var8 = this.getDeltaMovement();
        RailShape var9 = param1.getValue(var6.getShapeProperty());
        switch(var9) {
            case ASCENDING_EAST:
                this.setDeltaMovement(var8.add(-0.0078125, 0.0, 0.0));
                ++var1;
                break;
            case ASCENDING_WEST:
                this.setDeltaMovement(var8.add(0.0078125, 0.0, 0.0));
                ++var1;
                break;
            case ASCENDING_NORTH:
                this.setDeltaMovement(var8.add(0.0, 0.0, 0.0078125));
                ++var1;
                break;
            case ASCENDING_SOUTH:
                this.setDeltaMovement(var8.add(0.0, 0.0, -0.0078125));
                ++var1;
        }

        var8 = this.getDeltaMovement();
        Pair<Vec3i, Vec3i> var10 = exits(var9);
        Vec3i var11 = var10.getFirst();
        Vec3i var12 = var10.getSecond();
        double var13 = (double)(var12.getX() - var11.getX());
        double var14 = (double)(var12.getZ() - var11.getZ());
        double var15 = Math.sqrt(var13 * var13 + var14 * var14);
        double var16 = var8.x * var13 + var8.z * var14;
        if (var16 < 0.0) {
            var13 = -var13;
            var14 = -var14;
        }

        double var17 = Math.min(2.0, Math.sqrt(getHorizontalDistanceSqr(var8)));
        var8 = new Vec3(var17 * var13 / var15, var8.y, var17 * var14 / var15);
        this.setDeltaMovement(var8);
        Entity var18 = this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
        if (var18 instanceof Player) {
            Vec3 var19 = var18.getDeltaMovement();
            double var20 = getHorizontalDistanceSqr(var19);
            double var21 = getHorizontalDistanceSqr(this.getDeltaMovement());
            if (var20 > 1.0E-4 && var21 < 0.01) {
                this.setDeltaMovement(this.getDeltaMovement().add(var19.x * 0.1, 0.0, var19.z * 0.1));
                var5 = false;
            }
        }

        if (var5) {
            double var22 = Math.sqrt(getHorizontalDistanceSqr(this.getDeltaMovement()));
            if (var22 < 0.03) {
                this.setDeltaMovement(Vec3.ZERO);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.0, 0.5));
            }
        }

        double var23 = (double)param0.getX() + 0.5 + (double)var11.getX() * 0.5;
        double var24 = (double)param0.getZ() + 0.5 + (double)var11.getZ() * 0.5;
        double var25 = (double)param0.getX() + 0.5 + (double)var12.getX() * 0.5;
        double var26 = (double)param0.getZ() + 0.5 + (double)var12.getZ() * 0.5;
        var13 = var25 - var23;
        var14 = var26 - var24;
        double var27;
        if (var13 == 0.0) {
            var27 = var2 - (double)param0.getZ();
        } else if (var14 == 0.0) {
            var27 = var0 - (double)param0.getX();
        } else {
            double var29 = var0 - var23;
            double var30 = var2 - var24;
            var27 = (var29 * var13 + var30 * var14) * 2.0;
        }

        var0 = var23 + var13 * var27;
        var2 = var24 + var14 * var27;
        this.setPos(var0, var1, var2);
        double var32 = this.isVehicle() ? 0.75 : 1.0;
        double var33 = this.getMaxSpeed();
        var8 = this.getDeltaMovement();
        this.move(MoverType.SELF, new Vec3(Mth.clamp(var32 * var8.x, -var33, var33), 0.0, Mth.clamp(var32 * var8.z, -var33, var33)));
        if (var11.getY() != 0 && Mth.floor(this.getX()) - param0.getX() == var11.getX() && Mth.floor(this.getZ()) - param0.getZ() == var11.getZ()) {
            this.setPos(this.getX(), this.getY() + (double)var11.getY(), this.getZ());
        } else if (var12.getY() != 0 && Mth.floor(this.getX()) - param0.getX() == var12.getX() && Mth.floor(this.getZ()) - param0.getZ() == var12.getZ()) {
            this.setPos(this.getX(), this.getY() + (double)var12.getY(), this.getZ());
        }

        this.applyNaturalSlowdown();
        Vec3 var34 = this.getPos(this.getX(), this.getY(), this.getZ());
        if (var34 != null && var3 != null) {
            double var35 = (var3.y - var34.y) * 0.05;
            Vec3 var36 = this.getDeltaMovement();
            double var37 = Math.sqrt(getHorizontalDistanceSqr(var36));
            if (var37 > 0.0) {
                this.setDeltaMovement(var36.multiply((var37 + var35) / var37, 1.0, (var37 + var35) / var37));
            }

            this.setPos(this.getX(), var34.y, this.getZ());
        }

        int var38 = Mth.floor(this.getX());
        int var39 = Mth.floor(this.getZ());
        if (var38 != param0.getX() || var39 != param0.getZ()) {
            Vec3 var40 = this.getDeltaMovement();
            double var41 = Math.sqrt(getHorizontalDistanceSqr(var40));
            this.setDeltaMovement(var41 * (double)(var38 - param0.getX()), var40.y, var41 * (double)(var39 - param0.getZ()));
        }

        if (var4) {
            Vec3 var42 = this.getDeltaMovement();
            double var43 = Math.sqrt(getHorizontalDistanceSqr(var42));
            if (var43 > 0.01) {
                double var44 = 0.06;
                this.setDeltaMovement(var42.add(var42.x / var43 * 0.06, 0.0, var42.z / var43 * 0.06));
            } else {
                Vec3 var45 = this.getDeltaMovement();
                double var46 = var45.x;
                double var47 = var45.z;
                if (var9 == RailShape.EAST_WEST) {
                    if (this.isRedstoneConductor(param0.west())) {
                        var46 = 0.02;
                    } else if (this.isRedstoneConductor(param0.east())) {
                        var46 = -0.02;
                    }
                } else {
                    if (var9 != RailShape.NORTH_SOUTH) {
                        return;
                    }

                    if (this.isRedstoneConductor(param0.north())) {
                        var47 = 0.02;
                    } else if (this.isRedstoneConductor(param0.south())) {
                        var47 = -0.02;
                    }
                }

                this.setDeltaMovement(var46, var45.y, var47);
            }
        }

    }

    private boolean isRedstoneConductor(BlockPos param0) {
        return this.level.getBlockState(param0).isRedstoneConductor(this.level, param0);
    }

    protected void applyNaturalSlowdown() {
        double var0 = this.isVehicle() ? 0.997 : 0.96;
        this.setDeltaMovement(this.getDeltaMovement().multiply(var0, 0.0, var0));
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public Vec3 getPosOffs(double param0, double param1, double param2, double param3) {
        int var0 = Mth.floor(param0);
        int var1 = Mth.floor(param1);
        int var2 = Mth.floor(param2);
        if (this.level.getBlockState(new BlockPos(var0, var1 - 1, var2)).is(BlockTags.RAILS)) {
            --var1;
        }

        BlockState var3 = this.level.getBlockState(new BlockPos(var0, var1, var2));
        if (var3.is(BlockTags.RAILS)) {
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
        if (var3.is(BlockTags.RAILS)) {
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

    @OnlyIn(Dist.CLIENT)
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
                        var0 *= (double)(1.0F - this.pushthrough);
                        var1 *= (double)(1.0F - this.pushthrough);
                        var0 *= 0.5;
                        var1 *= 0.5;
                        if (param0 instanceof AbstractMinecart) {
                            double var4 = param0.getX() - this.getX();
                            double var5 = param0.getZ() - this.getZ();
                            Vec3 var6 = new Vec3(var4, 0.0, var5).normalize();
                            Vec3 var7 = new Vec3(
                                    (double)Mth.cos(this.yRot * (float) (Math.PI / 180.0)), 0.0, (double)Mth.sin(this.yRot * (float) (Math.PI / 180.0))
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

    @OnlyIn(Dist.CLIENT)
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

    @OnlyIn(Dist.CLIENT)
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
