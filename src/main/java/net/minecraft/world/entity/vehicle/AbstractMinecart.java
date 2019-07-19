package net.minecraft.world.entity.vehicle;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    private static final int[][][] EXITS = new int[][][]{
        {{0, 0, -1}, {0, 0, 1}},
        {{-1, 0, 0}, {1, 0, 0}},
        {{-1, -1, 0}, {1, 0, 0}},
        {{-1, 0, 0}, {1, -1, 0}},
        {{0, 0, -1}, {0, -1, 1}},
        {{0, -1, -1}, {0, 0, 1}},
        {{0, 0, 1}, {1, 0, 0}},
        {{0, 0, 1}, {-1, 0, 0}},
        {{0, 0, -1}, {-1, 0, 0}},
        {{0, 0, -1}, {1, 0, 0}}
    };
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
    protected boolean makeStepSound() {
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

        if (this.y < -64.0) {
            this.outOfWorld();
        }

        this.handleNetherPortal();
        if (this.level.isClientSide) {
            if (this.lSteps > 0) {
                double var0 = this.x + (this.lx - this.x) / (double)this.lSteps;
                double var1 = this.y + (this.ly - this.y) / (double)this.lSteps;
                double var2 = this.z + (this.lz - this.z) / (double)this.lSteps;
                double var3 = Mth.wrapDegrees(this.lyr - (double)this.yRot);
                this.yRot = (float)((double)this.yRot + var3 / (double)this.lSteps);
                this.xRot = (float)((double)this.xRot + (this.lxr - (double)this.xRot) / (double)this.lSteps);
                --this.lSteps;
                this.setPos(var0, var1, var2);
                this.setRot(this.yRot, this.xRot);
            } else {
                this.setPos(this.x, this.y, this.z);
                this.setRot(this.yRot, this.xRot);
            }

        } else {
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
            }

            int var4 = Mth.floor(this.x);
            int var5 = Mth.floor(this.y);
            int var6 = Mth.floor(this.z);
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
            double var9 = this.xo - this.x;
            double var10 = this.zo - this.z;
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
        Vec3 var0 = this.getPos(this.x, this.y, this.z);
        this.y = (double)param0.getY();
        boolean var1 = false;
        boolean var2 = false;
        BaseRailBlock var3 = (BaseRailBlock)param1.getBlock();
        if (var3 == Blocks.POWERED_RAIL) {
            var1 = param1.getValue(PoweredRailBlock.POWERED);
            var2 = !var1;
        }

        double var4 = 0.0078125;
        Vec3 var5 = this.getDeltaMovement();
        RailShape var6 = param1.getValue(var3.getShapeProperty());
        switch(var6) {
            case ASCENDING_EAST:
                this.setDeltaMovement(var5.add(-0.0078125, 0.0, 0.0));
                ++this.y;
                break;
            case ASCENDING_WEST:
                this.setDeltaMovement(var5.add(0.0078125, 0.0, 0.0));
                ++this.y;
                break;
            case ASCENDING_NORTH:
                this.setDeltaMovement(var5.add(0.0, 0.0, 0.0078125));
                ++this.y;
                break;
            case ASCENDING_SOUTH:
                this.setDeltaMovement(var5.add(0.0, 0.0, -0.0078125));
                ++this.y;
        }

        var5 = this.getDeltaMovement();
        int[][] var7 = EXITS[var6.getData()];
        double var8 = (double)(var7[1][0] - var7[0][0]);
        double var9 = (double)(var7[1][2] - var7[0][2]);
        double var10 = Math.sqrt(var8 * var8 + var9 * var9);
        double var11 = var5.x * var8 + var5.z * var9;
        if (var11 < 0.0) {
            var8 = -var8;
            var9 = -var9;
        }

        double var12 = Math.min(2.0, Math.sqrt(getHorizontalDistanceSqr(var5)));
        var5 = new Vec3(var12 * var8 / var10, var5.y, var12 * var9 / var10);
        this.setDeltaMovement(var5);
        Entity var13 = this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
        if (var13 instanceof Player) {
            Vec3 var14 = var13.getDeltaMovement();
            double var15 = getHorizontalDistanceSqr(var14);
            double var16 = getHorizontalDistanceSqr(this.getDeltaMovement());
            if (var15 > 1.0E-4 && var16 < 0.01) {
                this.setDeltaMovement(this.getDeltaMovement().add(var14.x * 0.1, 0.0, var14.z * 0.1));
                var2 = false;
            }
        }

        if (var2) {
            double var17 = Math.sqrt(getHorizontalDistanceSqr(this.getDeltaMovement()));
            if (var17 < 0.03) {
                this.setDeltaMovement(Vec3.ZERO);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.0, 0.5));
            }
        }

        double var18 = (double)param0.getX() + 0.5 + (double)var7[0][0] * 0.5;
        double var19 = (double)param0.getZ() + 0.5 + (double)var7[0][2] * 0.5;
        double var20 = (double)param0.getX() + 0.5 + (double)var7[1][0] * 0.5;
        double var21 = (double)param0.getZ() + 0.5 + (double)var7[1][2] * 0.5;
        var8 = var20 - var18;
        var9 = var21 - var19;
        double var22;
        if (var8 == 0.0) {
            this.x = (double)param0.getX() + 0.5;
            var22 = this.z - (double)param0.getZ();
        } else if (var9 == 0.0) {
            this.z = (double)param0.getZ() + 0.5;
            var22 = this.x - (double)param0.getX();
        } else {
            double var24 = this.x - var18;
            double var25 = this.z - var19;
            var22 = (var24 * var8 + var25 * var9) * 2.0;
        }

        this.x = var18 + var8 * var22;
        this.z = var19 + var9 * var22;
        this.setPos(this.x, this.y, this.z);
        double var27 = this.isVehicle() ? 0.75 : 1.0;
        double var28 = this.getMaxSpeed();
        var5 = this.getDeltaMovement();
        this.move(MoverType.SELF, new Vec3(Mth.clamp(var27 * var5.x, -var28, var28), 0.0, Mth.clamp(var27 * var5.z, -var28, var28)));
        if (var7[0][1] != 0 && Mth.floor(this.x) - param0.getX() == var7[0][0] && Mth.floor(this.z) - param0.getZ() == var7[0][2]) {
            this.setPos(this.x, this.y + (double)var7[0][1], this.z);
        } else if (var7[1][1] != 0 && Mth.floor(this.x) - param0.getX() == var7[1][0] && Mth.floor(this.z) - param0.getZ() == var7[1][2]) {
            this.setPos(this.x, this.y + (double)var7[1][1], this.z);
        }

        this.applyNaturalSlowdown();
        Vec3 var29 = this.getPos(this.x, this.y, this.z);
        if (var29 != null && var0 != null) {
            double var30 = (var0.y - var29.y) * 0.05;
            Vec3 var31 = this.getDeltaMovement();
            double var32 = Math.sqrt(getHorizontalDistanceSqr(var31));
            if (var32 > 0.0) {
                this.setDeltaMovement(var31.multiply((var32 + var30) / var32, 1.0, (var32 + var30) / var32));
            }

            this.setPos(this.x, var29.y, this.z);
        }

        int var33 = Mth.floor(this.x);
        int var34 = Mth.floor(this.z);
        if (var33 != param0.getX() || var34 != param0.getZ()) {
            Vec3 var35 = this.getDeltaMovement();
            double var36 = Math.sqrt(getHorizontalDistanceSqr(var35));
            this.setDeltaMovement(var36 * (double)(var33 - param0.getX()), var35.y, var36 * (double)(var34 - param0.getZ()));
        }

        if (var1) {
            Vec3 var37 = this.getDeltaMovement();
            double var38 = Math.sqrt(getHorizontalDistanceSqr(var37));
            if (var38 > 0.01) {
                double var39 = 0.06;
                this.setDeltaMovement(var37.add(var37.x / var38 * 0.06, 0.0, var37.z / var38 * 0.06));
            } else {
                Vec3 var40 = this.getDeltaMovement();
                double var41 = var40.x;
                double var42 = var40.z;
                if (var6 == RailShape.EAST_WEST) {
                    if (this.isRedstoneConductor(param0.west())) {
                        var41 = 0.02;
                    } else if (this.isRedstoneConductor(param0.east())) {
                        var41 = -0.02;
                    }
                } else {
                    if (var6 != RailShape.NORTH_SOUTH) {
                        return;
                    }

                    if (this.isRedstoneConductor(param0.north())) {
                        var42 = 0.02;
                    } else if (this.isRedstoneConductor(param0.south())) {
                        var42 = -0.02;
                    }
                }

                this.setDeltaMovement(var41, var40.y, var42);
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

            int[][] var5 = EXITS[var4.getData()];
            double var6 = (double)(var5[1][0] - var5[0][0]);
            double var7 = (double)(var5[1][2] - var5[0][2]);
            double var8 = Math.sqrt(var6 * var6 + var7 * var7);
            var6 /= var8;
            var7 /= var8;
            param0 += var6 * param3;
            param2 += var7 * param3;
            if (var5[0][1] != 0 && Mth.floor(param0) - var0 == var5[0][0] && Mth.floor(param2) - var2 == var5[0][2]) {
                param1 += (double)var5[0][1];
            } else if (var5[1][1] != 0 && Mth.floor(param0) - var0 == var5[1][0] && Mth.floor(param2) - var2 == var5[1][2]) {
                param1 += (double)var5[1][1];
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
            int[][] var5 = EXITS[var4.getData()];
            double var6 = (double)var0 + 0.5 + (double)var5[0][0] * 0.5;
            double var7 = (double)var1 + 0.0625 + (double)var5[0][1] * 0.5;
            double var8 = (double)var2 + 0.5 + (double)var5[0][2] * 0.5;
            double var9 = (double)var0 + 0.5 + (double)var5[1][0] * 0.5;
            double var10 = (double)var1 + 0.0625 + (double)var5[1][1] * 0.5;
            double var11 = (double)var2 + 0.5 + (double)var5[1][2] * 0.5;
            double var12 = var9 - var6;
            double var13 = (var10 - var7) * 2.0;
            double var14 = var11 - var8;
            double var15;
            if (var12 == 0.0) {
                var15 = param2 - (double)var2;
            } else if (var14 == 0.0) {
                var15 = param0 - (double)var0;
            } else {
                double var17 = param0 - var6;
                double var18 = param2 - var8;
                var15 = (var17 * var12 + var18 * var14) * 2.0;
            }

            param0 = var6 + var12 * var15;
            param1 = var7 + var13 * var15;
            param2 = var8 + var14 * var15;
            if (var13 < 0.0) {
                ++param1;
            }

            if (var13 > 0.0) {
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
                    double var0 = param0.x - this.x;
                    double var1 = param0.z - this.z;
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
                            double var4 = param0.x - this.x;
                            double var5 = param0.z - this.z;
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
