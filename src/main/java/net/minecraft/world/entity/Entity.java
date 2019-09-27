package net.minecraft.world.entity;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.RewindableStream;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Entity implements CommandSource, Nameable {
    protected static final Logger LOGGER = LogManager.getLogger();
    private static final AtomicInteger ENTITY_COUNTER = new AtomicInteger();
    private static final List<ItemStack> EMPTY_LIST = Collections.emptyList();
    private static final AABB INITIAL_AABB = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    private static double viewScale = 1.0;
    private final EntityType<?> type;
    private int id = ENTITY_COUNTER.incrementAndGet();
    public boolean blocksBuilding;
    private final List<Entity> passengers = Lists.newArrayList();
    protected int boardingCooldown;
    private Entity vehicle;
    public boolean forcedLoading;
    public Level level;
    public double xo;
    public double yo;
    public double zo;
    public double x;
    public double y;
    public double z;
    private Vec3 deltaMovement = Vec3.ZERO;
    public float yRot;
    public float xRot;
    public float yRotO;
    public float xRotO;
    private AABB bb = INITIAL_AABB;
    public boolean onGround;
    public boolean horizontalCollision;
    public boolean verticalCollision;
    public boolean collision;
    public boolean hurtMarked;
    protected Vec3 stuckSpeedMultiplier = Vec3.ZERO;
    public boolean removed;
    public float walkDistO;
    public float walkDist;
    public float moveDist;
    public float fallDistance;
    private float nextStep = 1.0F;
    private float nextFlap = 1.0F;
    public double xOld;
    public double yOld;
    public double zOld;
    public float maxUpStep;
    public boolean noPhysics;
    public float pushthrough;
    protected final Random random = new Random();
    public int tickCount;
    private int remainingFireTicks = -this.getFireImmuneTicks();
    protected boolean wasInWater;
    protected double waterHeight;
    protected boolean wasUnderWater;
    protected boolean isInLava;
    public int invulnerableTime;
    protected boolean firstTick = true;
    protected final SynchedEntityData entityData;
    protected static final EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_AIR_SUPPLY_ID = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME = SynchedEntityData.defineId(
        Entity.class, EntityDataSerializers.OPTIONAL_COMPONENT
    );
    private static final EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SILENT = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_NO_GRAVITY = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Pose> DATA_POSE = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.POSE);
    public boolean inChunk;
    public int xChunk;
    public int yChunk;
    public int zChunk;
    public long xp;
    public long yp;
    public long zp;
    public boolean noCulling;
    public boolean hasImpulse;
    public int changingDimensionDelay;
    protected boolean isInsidePortal;
    protected int portalTime;
    public DimensionType dimension;
    protected BlockPos portalEntranceBlock;
    protected Vec3 portalEntranceOffset;
    protected Direction portalEntranceForwards;
    private boolean invulnerable;
    protected UUID uuid = Mth.createInsecureUUID(this.random);
    protected String stringUUID = this.uuid.toString();
    protected boolean glowing;
    private final Set<String> tags = Sets.newHashSet();
    private boolean teleported;
    private final double[] pistonDeltas = new double[]{0.0, 0.0, 0.0};
    private long pistonDeltasGameTime;
    private EntityDimensions dimensions;
    private float eyeHeight;

    public Entity(EntityType<?> param0, Level param1) {
        this.type = param0;
        this.level = param1;
        this.dimensions = param0.getDimensions();
        this.setPos(0.0, 0.0, 0.0);
        if (param1 != null) {
            this.dimension = param1.dimension.getType();
        }

        this.entityData = new SynchedEntityData(this);
        this.entityData.define(DATA_SHARED_FLAGS_ID, (byte)0);
        this.entityData.define(DATA_AIR_SUPPLY_ID, this.getMaxAirSupply());
        this.entityData.define(DATA_CUSTOM_NAME_VISIBLE, false);
        this.entityData.define(DATA_CUSTOM_NAME, Optional.empty());
        this.entityData.define(DATA_SILENT, false);
        this.entityData.define(DATA_NO_GRAVITY, false);
        this.entityData.define(DATA_POSE, Pose.STANDING);
        this.defineSynchedData();
        this.eyeHeight = this.getEyeHeight(Pose.STANDING, this.dimensions);
    }

    @OnlyIn(Dist.CLIENT)
    public int getTeamColor() {
        Team var0 = this.getTeam();
        return var0 != null && var0.getColor().getColor() != null ? var0.getColor().getColor() : 16777215;
    }

    public boolean isSpectator() {
        return false;
    }

    public final void unRide() {
        if (this.isVehicle()) {
            this.ejectPassengers();
        }

        if (this.isPassenger()) {
            this.stopRiding();
        }

    }

    public void setPacketCoordinates(double param0, double param1, double param2) {
        this.xp = ClientboundMoveEntityPacket.entityToPacket(param0);
        this.yp = ClientboundMoveEntityPacket.entityToPacket(param1);
        this.zp = ClientboundMoveEntityPacket.entityToPacket(param2);
    }

    public EntityType<?> getType() {
        return this.type;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int param0) {
        this.id = param0;
    }

    public Set<String> getTags() {
        return this.tags;
    }

    public boolean addTag(String param0) {
        return this.tags.size() >= 1024 ? false : this.tags.add(param0);
    }

    public boolean removeTag(String param0) {
        return this.tags.remove(param0);
    }

    public void kill() {
        this.remove();
    }

    protected abstract void defineSynchedData();

    public SynchedEntityData getEntityData() {
        return this.entityData;
    }

    @Override
    public boolean equals(Object param0) {
        if (param0 instanceof Entity) {
            return ((Entity)param0).id == this.id;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @OnlyIn(Dist.CLIENT)
    protected void resetPos() {
        if (this.level != null) {
            while(this.y > 0.0 && this.y < 256.0) {
                this.setPos(this.x, this.y, this.z);
                if (this.level.noCollision(this)) {
                    break;
                }

                ++this.y;
            }

            this.setDeltaMovement(Vec3.ZERO);
            this.xRot = 0.0F;
        }
    }

    public void remove() {
        this.removed = true;
    }

    protected void setPose(Pose param0) {
        this.entityData.set(DATA_POSE, param0);
    }

    public Pose getPose() {
        return this.entityData.get(DATA_POSE);
    }

    protected void setRot(float param0, float param1) {
        this.yRot = param0 % 360.0F;
        this.xRot = param1 % 360.0F;
    }

    public void setPos(double param0, double param1, double param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        float var0 = this.dimensions.width / 2.0F;
        float var1 = this.dimensions.height;
        this.setBoundingBox(new AABB(param0 - (double)var0, param1, param2 - (double)var0, param0 + (double)var0, param1 + (double)var1, param2 + (double)var0));
    }

    @OnlyIn(Dist.CLIENT)
    public void turn(double param0, double param1) {
        double var0 = param1 * 0.15;
        double var1 = param0 * 0.15;
        this.xRot = (float)((double)this.xRot + var0);
        this.yRot = (float)((double)this.yRot + var1);
        this.xRot = Mth.clamp(this.xRot, -90.0F, 90.0F);
        this.xRotO = (float)((double)this.xRotO + var0);
        this.yRotO = (float)((double)this.yRotO + var1);
        this.xRotO = Mth.clamp(this.xRotO, -90.0F, 90.0F);
        if (this.vehicle != null) {
            this.vehicle.onPassengerTurned(this);
        }

    }

    public void tick() {
        if (!this.level.isClientSide) {
            this.setSharedFlag(6, this.isGlowing());
        }

        this.baseTick();
    }

    public void baseTick() {
        this.level.getProfiler().push("entityBaseTick");
        if (this.isPassenger() && this.getVehicle().removed) {
            this.stopRiding();
        }

        if (this.boardingCooldown > 0) {
            --this.boardingCooldown;
        }

        this.walkDistO = this.walkDist;
        this.xRotO = this.xRot;
        this.yRotO = this.yRot;
        this.handleNetherPortal();
        this.updateSprintingState();
        this.updateWaterState();
        if (this.level.isClientSide) {
            this.clearFire();
        } else if (this.remainingFireTicks > 0) {
            if (this.fireImmune()) {
                this.remainingFireTicks -= 4;
                if (this.remainingFireTicks < 0) {
                    this.clearFire();
                }
            } else {
                if (this.remainingFireTicks % 20 == 0) {
                    this.hurt(DamageSource.ON_FIRE, 1.0F);
                }

                --this.remainingFireTicks;
            }
        }

        if (this.isInLava()) {
            this.lavaHurt();
            this.fallDistance *= 0.5F;
        }

        if (this.y < -64.0) {
            this.outOfWorld();
        }

        if (!this.level.isClientSide) {
            this.setSharedFlag(0, this.remainingFireTicks > 0);
        }

        this.firstTick = false;
        this.level.getProfiler().pop();
    }

    protected void processDimensionDelay() {
        if (this.changingDimensionDelay > 0) {
            --this.changingDimensionDelay;
        }

    }

    public int getPortalWaitTime() {
        return 1;
    }

    protected void lavaHurt() {
        if (!this.fireImmune()) {
            this.setSecondsOnFire(15);
            this.hurt(DamageSource.LAVA, 4.0F);
        }
    }

    public void setSecondsOnFire(int param0) {
        int var0 = param0 * 20;
        if (this instanceof LivingEntity) {
            var0 = ProtectionEnchantment.getFireAfterDampener((LivingEntity)this, var0);
        }

        if (this.remainingFireTicks < var0) {
            this.remainingFireTicks = var0;
        }

    }

    public void setRemainingFireTicks(int param0) {
        this.remainingFireTicks = param0;
    }

    public int getRemainingFireTicks() {
        return this.remainingFireTicks;
    }

    public void clearFire() {
        this.remainingFireTicks = 0;
    }

    protected void outOfWorld() {
        this.remove();
    }

    public boolean isFree(double param0, double param1, double param2) {
        return this.isFree(this.getBoundingBox().move(param0, param1, param2));
    }

    private boolean isFree(AABB param0) {
        return this.level.noCollision(this, param0) && !this.level.containsAnyLiquid(param0);
    }

    public void move(MoverType param0, Vec3 param1) {
        if (this.noPhysics) {
            this.setBoundingBox(this.getBoundingBox().move(param1));
            this.setLocationFromBoundingbox();
        } else {
            if (param0 == MoverType.PISTON) {
                param1 = this.limitPistonMovement(param1);
                if (param1.equals(Vec3.ZERO)) {
                    return;
                }
            }

            this.level.getProfiler().push("move");
            if (this.stuckSpeedMultiplier.lengthSqr() > 1.0E-7) {
                param1 = param1.multiply(this.stuckSpeedMultiplier);
                this.stuckSpeedMultiplier = Vec3.ZERO;
                this.setDeltaMovement(Vec3.ZERO);
            }

            param1 = this.maybeBackOffFromEdge(param1, param0);
            Vec3 var0 = this.collide(param1);
            if (var0.lengthSqr() > 1.0E-7) {
                this.setBoundingBox(this.getBoundingBox().move(var0));
                this.setLocationFromBoundingbox();
            }

            this.level.getProfiler().pop();
            this.level.getProfiler().push("rest");
            this.horizontalCollision = !Mth.equal(param1.x, var0.x) || !Mth.equal(param1.z, var0.z);
            this.verticalCollision = param1.y != var0.y;
            this.onGround = this.verticalCollision && param1.y < 0.0;
            this.collision = this.horizontalCollision || this.verticalCollision;
            int var1 = Mth.floor(this.x);
            int var2 = Mth.floor(this.y - 0.2F);
            int var3 = Mth.floor(this.z);
            BlockPos var4 = new BlockPos(var1, var2, var3);
            BlockState var5 = this.level.getBlockState(var4);
            if (var5.isAir()) {
                BlockPos var6 = var4.below();
                BlockState var7 = this.level.getBlockState(var6);
                Block var8 = var7.getBlock();
                if (var8.is(BlockTags.FENCES) || var8.is(BlockTags.WALLS) || var8 instanceof FenceGateBlock) {
                    var5 = var7;
                    var4 = var6;
                }
            }

            this.checkFallDamage(var0.y, this.onGround, var5, var4);
            Vec3 var9 = this.getDeltaMovement();
            if (param1.x != var0.x) {
                this.setDeltaMovement(0.0, var9.y, var9.z);
            }

            if (param1.z != var0.z) {
                this.setDeltaMovement(var9.x, var9.y, 0.0);
            }

            Block var10 = var5.getBlock();
            if (param1.y != var0.y) {
                var10.updateEntityAfterFallOn(this.level, this);
            }

            if (this.onGround && !this.isSteppingCarefully()) {
                var10.stepOn(this.level, var4, this);
            }

            if (this.isMovementNoisy() && !this.isPassenger()) {
                double var11 = var0.x;
                double var12 = var0.y;
                double var13 = var0.z;
                if (var10 != Blocks.LADDER && var10 != Blocks.SCAFFOLDING) {
                    var12 = 0.0;
                }

                this.walkDist = (float)((double)this.walkDist + (double)Mth.sqrt(getHorizontalDistanceSqr(var0)) * 0.6);
                this.moveDist = (float)((double)this.moveDist + (double)Mth.sqrt(var11 * var11 + var12 * var12 + var13 * var13) * 0.6);
                if (this.moveDist > this.nextStep && !var5.isAir()) {
                    this.nextStep = this.nextStep();
                    if (this.isInWater()) {
                        Entity var14 = this.isVehicle() && this.getControllingPassenger() != null ? this.getControllingPassenger() : this;
                        float var15 = var14 == this ? 0.35F : 0.4F;
                        Vec3 var16 = var14.getDeltaMovement();
                        float var17 = Mth.sqrt(var16.x * var16.x * 0.2F + var16.y * var16.y + var16.z * var16.z * 0.2F) * var15;
                        if (var17 > 1.0F) {
                            var17 = 1.0F;
                        }

                        this.playSwimSound(var17);
                    } else {
                        this.playStepSound(var4, var5);
                    }
                } else if (this.moveDist > this.nextFlap && this.makeFlySound() && var5.isAir()) {
                    this.nextFlap = this.playFlySound(this.moveDist);
                }
            }

            try {
                this.isInLava = false;
                this.checkInsideBlocks();
            } catch (Throwable var211) {
                CrashReport var19 = CrashReport.forThrowable(var211, "Checking entity block collision");
                CrashReportCategory var20 = var19.addCategory("Entity being checked for collision");
                this.fillCrashReportCategory(var20);
                throw new ReportedException(var19);
            }

            boolean var21 = this.isInWaterRainOrBubble();
            if (this.level.containsFireBlock(this.getBoundingBox().deflate(0.001))) {
                if (!var21) {
                    ++this.remainingFireTicks;
                    if (this.remainingFireTicks == 0) {
                        this.setSecondsOnFire(8);
                    }
                }

                this.burn(1);
            } else if (this.remainingFireTicks <= 0) {
                this.remainingFireTicks = -this.getFireImmuneTicks();
            }

            if (var21 && this.isOnFire()) {
                this.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                this.remainingFireTicks = -this.getFireImmuneTicks();
            }

            this.level.getProfiler().pop();
        }
    }

    protected Vec3 maybeBackOffFromEdge(Vec3 param0, MoverType param1) {
        return param0;
    }

    protected Vec3 limitPistonMovement(Vec3 param0) {
        if (param0.lengthSqr() <= 1.0E-7) {
            return param0;
        } else {
            long var0 = this.level.getGameTime();
            if (var0 != this.pistonDeltasGameTime) {
                Arrays.fill(this.pistonDeltas, 0.0);
                this.pistonDeltasGameTime = var0;
            }

            if (param0.x != 0.0) {
                double var1 = this.applyPistonMovementRestriction(Direction.Axis.X, param0.x);
                return Math.abs(var1) <= 1.0E-5F ? Vec3.ZERO : new Vec3(var1, 0.0, 0.0);
            } else if (param0.y != 0.0) {
                double var2 = this.applyPistonMovementRestriction(Direction.Axis.Y, param0.y);
                return Math.abs(var2) <= 1.0E-5F ? Vec3.ZERO : new Vec3(0.0, var2, 0.0);
            } else if (param0.z != 0.0) {
                double var3 = this.applyPistonMovementRestriction(Direction.Axis.Z, param0.z);
                return Math.abs(var3) <= 1.0E-5F ? Vec3.ZERO : new Vec3(0.0, 0.0, var3);
            } else {
                return Vec3.ZERO;
            }
        }
    }

    private double applyPistonMovementRestriction(Direction.Axis param0, double param1) {
        int var0 = param0.ordinal();
        double var1 = Mth.clamp(param1 + this.pistonDeltas[var0], -0.51, 0.51);
        param1 = var1 - this.pistonDeltas[var0];
        this.pistonDeltas[var0] = var1;
        return param1;
    }

    private Vec3 collide(Vec3 param0) {
        AABB var0 = this.getBoundingBox();
        CollisionContext var1 = CollisionContext.of(this);
        VoxelShape var2 = this.level.getWorldBorder().getCollisionShape();
        Stream<VoxelShape> var3 = Shapes.joinIsNotEmpty(var2, Shapes.create(var0.deflate(1.0E-7)), BooleanOp.AND) ? Stream.empty() : Stream.of(var2);
        Stream<VoxelShape> var4 = this.level.getEntityCollisions(this, var0.expandTowards(param0), ImmutableSet.of());
        RewindableStream<VoxelShape> var5 = new RewindableStream<>(Stream.concat(var4, var3));
        Vec3 var6 = param0.lengthSqr() == 0.0 ? param0 : collideBoundingBoxHeuristically(this, param0, var0, this.level, var1, var5);
        boolean var7 = param0.x != var6.x;
        boolean var8 = param0.y != var6.y;
        boolean var9 = param0.z != var6.z;
        boolean var10 = this.onGround || var8 && param0.y < 0.0;
        if (this.maxUpStep > 0.0F && var10 && (var7 || var9)) {
            Vec3 var11 = collideBoundingBoxHeuristically(this, new Vec3(param0.x, (double)this.maxUpStep, param0.z), var0, this.level, var1, var5);
            Vec3 var12 = collideBoundingBoxHeuristically(
                this, new Vec3(0.0, (double)this.maxUpStep, 0.0), var0.expandTowards(param0.x, 0.0, param0.z), this.level, var1, var5
            );
            if (var12.y < (double)this.maxUpStep) {
                Vec3 var13 = collideBoundingBoxHeuristically(this, new Vec3(param0.x, 0.0, param0.z), var0.move(var12), this.level, var1, var5).add(var12);
                if (getHorizontalDistanceSqr(var13) > getHorizontalDistanceSqr(var11)) {
                    var11 = var13;
                }
            }

            if (getHorizontalDistanceSqr(var11) > getHorizontalDistanceSqr(var6)) {
                return var11.add(collideBoundingBoxHeuristically(this, new Vec3(0.0, -var11.y + param0.y, 0.0), var0.move(var11), this.level, var1, var5));
            }
        }

        return var6;
    }

    public static double getHorizontalDistanceSqr(Vec3 param0) {
        return param0.x * param0.x + param0.z * param0.z;
    }

    public static Vec3 collideBoundingBoxHeuristically(
        @Nullable Entity param0, Vec3 param1, AABB param2, Level param3, CollisionContext param4, RewindableStream<VoxelShape> param5
    ) {
        boolean var0 = param1.x == 0.0;
        boolean var1 = param1.y == 0.0;
        boolean var2 = param1.z == 0.0;
        if ((!var0 || !var1) && (!var0 || !var2) && (!var1 || !var2)) {
            RewindableStream<VoxelShape> var3 = new RewindableStream<>(
                Stream.concat(param5.getStream(), param3.getBlockCollisions(param0, param2.expandTowards(param1)))
            );
            return collideBoundingBoxLegacy(param1, param2, var3);
        } else {
            return collideBoundingBox(param1, param2, param3, param4, param5);
        }
    }

    public static Vec3 collideBoundingBoxLegacy(Vec3 param0, AABB param1, RewindableStream<VoxelShape> param2) {
        double var0 = param0.x;
        double var1 = param0.y;
        double var2 = param0.z;
        if (var1 != 0.0) {
            var1 = Shapes.collide(Direction.Axis.Y, param1, param2.getStream(), var1);
            if (var1 != 0.0) {
                param1 = param1.move(0.0, var1, 0.0);
            }
        }

        boolean var3 = Math.abs(var0) < Math.abs(var2);
        if (var3 && var2 != 0.0) {
            var2 = Shapes.collide(Direction.Axis.Z, param1, param2.getStream(), var2);
            if (var2 != 0.0) {
                param1 = param1.move(0.0, 0.0, var2);
            }
        }

        if (var0 != 0.0) {
            var0 = Shapes.collide(Direction.Axis.X, param1, param2.getStream(), var0);
            if (!var3 && var0 != 0.0) {
                param1 = param1.move(var0, 0.0, 0.0);
            }
        }

        if (!var3 && var2 != 0.0) {
            var2 = Shapes.collide(Direction.Axis.Z, param1, param2.getStream(), var2);
        }

        return new Vec3(var0, var1, var2);
    }

    public static Vec3 collideBoundingBox(Vec3 param0, AABB param1, LevelReader param2, CollisionContext param3, RewindableStream<VoxelShape> param4) {
        double var0 = param0.x;
        double var1 = param0.y;
        double var2 = param0.z;
        if (var1 != 0.0) {
            var1 = Shapes.collide(Direction.Axis.Y, param1, param2, var1, param3, param4.getStream());
            if (var1 != 0.0) {
                param1 = param1.move(0.0, var1, 0.0);
            }
        }

        boolean var3 = Math.abs(var0) < Math.abs(var2);
        if (var3 && var2 != 0.0) {
            var2 = Shapes.collide(Direction.Axis.Z, param1, param2, var2, param3, param4.getStream());
            if (var2 != 0.0) {
                param1 = param1.move(0.0, 0.0, var2);
            }
        }

        if (var0 != 0.0) {
            var0 = Shapes.collide(Direction.Axis.X, param1, param2, var0, param3, param4.getStream());
            if (!var3 && var0 != 0.0) {
                param1 = param1.move(var0, 0.0, 0.0);
            }
        }

        if (!var3 && var2 != 0.0) {
            var2 = Shapes.collide(Direction.Axis.Z, param1, param2, var2, param3, param4.getStream());
        }

        return new Vec3(var0, var1, var2);
    }

    protected float nextStep() {
        return (float)((int)this.moveDist + 1);
    }

    public void setLocationFromBoundingbox() {
        AABB var0 = this.getBoundingBox();
        this.x = (var0.minX + var0.maxX) / 2.0;
        this.y = var0.minY;
        this.z = (var0.minZ + var0.maxZ) / 2.0;
    }

    protected SoundEvent getSwimSound() {
        return SoundEvents.GENERIC_SWIM;
    }

    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.GENERIC_SPLASH;
    }

    protected SoundEvent getSwimHighSpeedSplashSound() {
        return SoundEvents.GENERIC_SPLASH;
    }

    protected void checkInsideBlocks() {
        AABB var0 = this.getBoundingBox();

        try (
            BlockPos.PooledMutableBlockPos var1 = BlockPos.PooledMutableBlockPos.acquire(var0.minX + 0.001, var0.minY + 0.001, var0.minZ + 0.001);
            BlockPos.PooledMutableBlockPos var2 = BlockPos.PooledMutableBlockPos.acquire(var0.maxX - 0.001, var0.maxY - 0.001, var0.maxZ - 0.001);
            BlockPos.PooledMutableBlockPos var3 = BlockPos.PooledMutableBlockPos.acquire();
        ) {
            if (this.level.hasChunksAt(var1, var2)) {
                for(int var4 = var1.getX(); var4 <= var2.getX(); ++var4) {
                    for(int var5 = var1.getY(); var5 <= var2.getY(); ++var5) {
                        for(int var6 = var1.getZ(); var6 <= var2.getZ(); ++var6) {
                            var3.set(var4, var5, var6);
                            BlockState var7 = this.level.getBlockState(var3);

                            try {
                                var7.entityInside(this.level, var3, this);
                                this.onInsideBlock(var7);
                            } catch (Throwable var60) {
                                CrashReport var9 = CrashReport.forThrowable(var60, "Colliding entity with block");
                                CrashReportCategory var10 = var9.addCategory("Block being collided with");
                                CrashReportCategory.populateBlockDetails(var10, var3, var7);
                                throw new ReportedException(var9);
                            }
                        }
                    }
                }
            }
        }

    }

    protected void onInsideBlock(BlockState param0) {
    }

    protected void playStepSound(BlockPos param0, BlockState param1) {
        if (!param1.getMaterial().isLiquid()) {
            BlockState var0 = this.level.getBlockState(param0.above());
            SoundType var1 = var0.getBlock() == Blocks.SNOW ? var0.getSoundType() : param1.getSoundType();
            this.playSound(var1.getStepSound(), var1.getVolume() * 0.15F, var1.getPitch());
        }
    }

    protected void playSwimSound(float param0) {
        this.playSound(this.getSwimSound(), param0, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
    }

    protected float playFlySound(float param0) {
        return 0.0F;
    }

    protected boolean makeFlySound() {
        return false;
    }

    public void playSound(SoundEvent param0, float param1, float param2) {
        if (!this.isSilent()) {
            this.level.playSound(null, this.x, this.y, this.z, param0, this.getSoundSource(), param1, param2);
        }

    }

    public boolean isSilent() {
        return this.entityData.get(DATA_SILENT);
    }

    public void setSilent(boolean param0) {
        this.entityData.set(DATA_SILENT, param0);
    }

    public boolean isNoGravity() {
        return this.entityData.get(DATA_NO_GRAVITY);
    }

    public void setNoGravity(boolean param0) {
        this.entityData.set(DATA_NO_GRAVITY, param0);
    }

    protected boolean isMovementNoisy() {
        return true;
    }

    protected void checkFallDamage(double param0, boolean param1, BlockState param2, BlockPos param3) {
        if (param1) {
            if (this.fallDistance > 0.0F) {
                param2.getBlock().fallOn(this.level, param3, this, this.fallDistance);
            }

            this.fallDistance = 0.0F;
        } else if (param0 < 0.0) {
            this.fallDistance = (float)((double)this.fallDistance - param0);
        }

    }

    @Nullable
    public AABB getCollideBox() {
        return null;
    }

    protected void burn(int param0) {
        if (!this.fireImmune()) {
            this.hurt(DamageSource.IN_FIRE, (float)param0);
        }

    }

    public final boolean fireImmune() {
        return this.getType().fireImmune();
    }

    public void causeFallDamage(float param0, float param1) {
        if (this.isVehicle()) {
            for(Entity var0 : this.getPassengers()) {
                var0.causeFallDamage(param0, param1);
            }
        }

    }

    public boolean isInWater() {
        return this.wasInWater;
    }

    private boolean isInRain() {
        boolean var3;
        try (BlockPos.PooledMutableBlockPos var0 = BlockPos.PooledMutableBlockPos.acquire(this)) {
            var3 = this.level.isRainingAt(var0) || this.level.isRainingAt(var0.set(this.x, this.y + (double)this.dimensions.height, this.z));
        }

        return var3;
    }

    private boolean isInBubbleColumn() {
        return this.level.getBlockState(new BlockPos(this)).getBlock() == Blocks.BUBBLE_COLUMN;
    }

    public boolean isInWaterOrRain() {
        return this.isInWater() || this.isInRain();
    }

    public boolean isInWaterRainOrBubble() {
        return this.isInWater() || this.isInRain() || this.isInBubbleColumn();
    }

    public boolean isInWaterOrBubble() {
        return this.isInWater() || this.isInBubbleColumn();
    }

    public boolean isUnderWater() {
        return this.wasUnderWater && this.isInWater();
    }

    private void updateWaterState() {
        this.updateInWaterState();
        this.updateUnderWaterState();
        this.updateSwimming();
    }

    public void updateSwimming() {
        if (this.isSwimming()) {
            this.setSwimming(this.isSprinting() && this.isInWater() && !this.isPassenger());
        } else {
            this.setSwimming(this.isSprinting() && this.isUnderWater() && !this.isPassenger());
        }

    }

    public boolean updateInWaterState() {
        if (this.getVehicle() instanceof Boat) {
            this.wasInWater = false;
        } else if (this.checkAndHandleWater(FluidTags.WATER)) {
            if (!this.wasInWater && !this.firstTick) {
                this.doWaterSplashEffect();
            }

            this.fallDistance = 0.0F;
            this.wasInWater = true;
            this.clearFire();
        } else {
            this.wasInWater = false;
        }

        return this.wasInWater;
    }

    private void updateUnderWaterState() {
        this.wasUnderWater = this.isUnderLiquid(FluidTags.WATER, true);
    }

    protected void doWaterSplashEffect() {
        Entity var0 = this.isVehicle() && this.getControllingPassenger() != null ? this.getControllingPassenger() : this;
        float var1 = var0 == this ? 0.2F : 0.9F;
        Vec3 var2 = var0.getDeltaMovement();
        float var3 = Mth.sqrt(var2.x * var2.x * 0.2F + var2.y * var2.y + var2.z * var2.z * 0.2F) * var1;
        if (var3 > 1.0F) {
            var3 = 1.0F;
        }

        if ((double)var3 < 0.25) {
            this.playSound(this.getSwimSplashSound(), var3, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
        } else {
            this.playSound(this.getSwimHighSpeedSplashSound(), var3, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
        }

        float var4 = (float)Mth.floor(this.getBoundingBox().minY);

        for(int var5 = 0; (float)var5 < 1.0F + this.dimensions.width * 20.0F; ++var5) {
            float var6 = (this.random.nextFloat() * 2.0F - 1.0F) * this.dimensions.width;
            float var7 = (this.random.nextFloat() * 2.0F - 1.0F) * this.dimensions.width;
            this.level
                .addParticle(
                    ParticleTypes.BUBBLE,
                    this.x + (double)var6,
                    (double)(var4 + 1.0F),
                    this.z + (double)var7,
                    var2.x,
                    var2.y - (double)(this.random.nextFloat() * 0.2F),
                    var2.z
                );
        }

        for(int var8 = 0; (float)var8 < 1.0F + this.dimensions.width * 20.0F; ++var8) {
            float var9 = (this.random.nextFloat() * 2.0F - 1.0F) * this.dimensions.width;
            float var10 = (this.random.nextFloat() * 2.0F - 1.0F) * this.dimensions.width;
            this.level.addParticle(ParticleTypes.SPLASH, this.x + (double)var9, (double)(var4 + 1.0F), this.z + (double)var10, var2.x, var2.y, var2.z);
        }

    }

    public void updateSprintingState() {
        if (this.isSprinting() && !this.isInWater()) {
            this.doSprintParticleEffect();
        }

    }

    protected void doSprintParticleEffect() {
        int var0 = Mth.floor(this.x);
        int var1 = Mth.floor(this.y - 0.2F);
        int var2 = Mth.floor(this.z);
        BlockPos var3 = new BlockPos(var0, var1, var2);
        BlockState var4 = this.level.getBlockState(var3);
        if (var4.getRenderShape() != RenderShape.INVISIBLE) {
            Vec3 var5 = this.getDeltaMovement();
            this.level
                .addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, var4),
                    this.x + ((double)this.random.nextFloat() - 0.5) * (double)this.dimensions.width,
                    this.y + 0.1,
                    this.z + ((double)this.random.nextFloat() - 0.5) * (double)this.dimensions.width,
                    var5.x * -4.0,
                    1.5,
                    var5.z * -4.0
                );
        }

    }

    public boolean isUnderLiquid(Tag<Fluid> param0) {
        return this.isUnderLiquid(param0, false);
    }

    public boolean isUnderLiquid(Tag<Fluid> param0, boolean param1) {
        if (this.getVehicle() instanceof Boat) {
            return false;
        } else {
            double var0 = this.y + (double)this.getEyeHeight();
            BlockPos var1 = new BlockPos(this.x, var0, this.z);
            if (param1 && !this.level.hasChunk(var1.getX() >> 4, var1.getZ() >> 4)) {
                return false;
            } else {
                FluidState var2 = this.level.getFluidState(var1);
                return var2.is(param0) && var0 < (double)((float)var1.getY() + var2.getHeight(this.level, var1) + 0.11111111F);
            }
        }
    }

    public void setInLava() {
        this.isInLava = true;
    }

    public boolean isInLava() {
        return this.isInLava;
    }

    public void moveRelative(float param0, Vec3 param1) {
        Vec3 var0 = getInputVector(param1, param0, this.yRot);
        this.setDeltaMovement(this.getDeltaMovement().add(var0));
    }

    private static Vec3 getInputVector(Vec3 param0, float param1, float param2) {
        double var0 = param0.lengthSqr();
        if (var0 < 1.0E-7) {
            return Vec3.ZERO;
        } else {
            Vec3 var1 = (var0 > 1.0 ? param0.normalize() : param0).scale((double)param1);
            float var2 = Mth.sin(param2 * (float) (Math.PI / 180.0));
            float var3 = Mth.cos(param2 * (float) (Math.PI / 180.0));
            return new Vec3(var1.x * (double)var3 - var1.z * (double)var2, var1.y, var1.z * (double)var3 + var1.x * (double)var2);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public int getLightColor() {
        BlockPos var0 = new BlockPos(this.x, this.y + (double)this.getEyeHeight(), this.z);
        return this.level.hasChunkAt(var0) ? this.level.getLightColor(var0) : 0;
    }

    public float getBrightness() {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos(this.x, 0.0, this.z);
        if (this.level.hasChunkAt(var0)) {
            var0.setY(Mth.floor(this.y + (double)this.getEyeHeight()));
            return this.level.getBrightness(var0);
        } else {
            return 0.0F;
        }
    }

    public void setLevel(Level param0) {
        this.level = param0;
    }

    public void absMoveTo(double param0, double param1, double param2, float param3, float param4) {
        this.x = Mth.clamp(param0, -3.0E7, 3.0E7);
        this.y = param1;
        this.z = Mth.clamp(param2, -3.0E7, 3.0E7);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.setPos(this.x, this.y, this.z);
        this.yRot = param3 % 360.0F;
        this.xRot = Mth.clamp(param4, -90.0F, 90.0F) % 360.0F;
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
    }

    public void moveTo(BlockPos param0, float param1, float param2) {
        this.moveTo((double)param0.getX() + 0.5, (double)param0.getY(), (double)param0.getZ() + 0.5, param1, param2);
    }

    public void moveTo(double param0, double param1, double param2, float param3, float param4) {
        this.setPosAndOldPos(param0, param1, param2);
        this.yRot = param3;
        this.xRot = param4;
        this.setPos(this.x, this.y, this.z);
    }

    public void setPosAndOldPos(double param0, double param1, double param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.xOld = this.x;
        this.yOld = this.y;
        this.zOld = this.z;
    }

    public float distanceTo(Entity param0) {
        float var0 = (float)(this.x - param0.x);
        float var1 = (float)(this.y - param0.y);
        float var2 = (float)(this.z - param0.z);
        return Mth.sqrt(var0 * var0 + var1 * var1 + var2 * var2);
    }

    public double distanceToSqr(double param0, double param1, double param2) {
        double var0 = this.x - param0;
        double var1 = this.y - param1;
        double var2 = this.z - param2;
        return var0 * var0 + var1 * var1 + var2 * var2;
    }

    public double distanceToSqr(Entity param0) {
        return this.distanceToSqr(param0.position());
    }

    public double distanceToSqr(Vec3 param0) {
        double var0 = this.x - param0.x;
        double var1 = this.y - param0.y;
        double var2 = this.z - param0.z;
        return var0 * var0 + var1 * var1 + var2 * var2;
    }

    public void playerTouch(Player param0) {
    }

    public void push(Entity param0) {
        if (!this.isPassengerOfSameVehicle(param0)) {
            if (!param0.noPhysics && !this.noPhysics) {
                double var0 = param0.x - this.x;
                double var1 = param0.z - this.z;
                double var2 = Mth.absMax(var0, var1);
                if (var2 >= 0.01F) {
                    var2 = (double)Mth.sqrt(var2);
                    var0 /= var2;
                    var1 /= var2;
                    double var3 = 1.0 / var2;
                    if (var3 > 1.0) {
                        var3 = 1.0;
                    }

                    var0 *= var3;
                    var1 *= var3;
                    var0 *= 0.05F;
                    var1 *= 0.05F;
                    var0 *= (double)(1.0F - this.pushthrough);
                    var1 *= (double)(1.0F - this.pushthrough);
                    if (!this.isVehicle()) {
                        this.push(-var0, 0.0, -var1);
                    }

                    if (!param0.isVehicle()) {
                        param0.push(var0, 0.0, var1);
                    }
                }

            }
        }
    }

    public void push(double param0, double param1, double param2) {
        this.setDeltaMovement(this.getDeltaMovement().add(param0, param1, param2));
        this.hasImpulse = true;
    }

    protected void markHurt() {
        this.hurtMarked = true;
    }

    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            this.markHurt();
            return false;
        }
    }

    public final Vec3 getViewVector(float param0) {
        return this.calculateViewVector(this.getViewXRot(param0), this.getViewYRot(param0));
    }

    public float getViewXRot(float param0) {
        return param0 == 1.0F ? this.xRot : Mth.lerp(param0, this.xRotO, this.xRot);
    }

    public float getViewYRot(float param0) {
        return param0 == 1.0F ? this.yRot : Mth.lerp(param0, this.yRotO, this.yRot);
    }

    protected final Vec3 calculateViewVector(float param0, float param1) {
        float var0 = param0 * (float) (Math.PI / 180.0);
        float var1 = -param1 * (float) (Math.PI / 180.0);
        float var2 = Mth.cos(var1);
        float var3 = Mth.sin(var1);
        float var4 = Mth.cos(var0);
        float var5 = Mth.sin(var0);
        return new Vec3((double)(var3 * var4), (double)(-var5), (double)(var2 * var4));
    }

    public final Vec3 getUpVector(float param0) {
        return this.calculateUpVector(this.getViewXRot(param0), this.getViewYRot(param0));
    }

    protected final Vec3 calculateUpVector(float param0, float param1) {
        return this.calculateViewVector(param0 - 90.0F, param1);
    }

    public Vec3 getEyePosition(float param0) {
        if (param0 == 1.0F) {
            return new Vec3(this.x, this.y + (double)this.getEyeHeight(), this.z);
        } else {
            double var0 = Mth.lerp((double)param0, this.xo, this.x);
            double var1 = Mth.lerp((double)param0, this.yo, this.y) + (double)this.getEyeHeight();
            double var2 = Mth.lerp((double)param0, this.zo, this.z);
            return new Vec3(var0, var1, var2);
        }
    }

    public HitResult pick(double param0, float param1, boolean param2) {
        Vec3 var0 = this.getEyePosition(param1);
        Vec3 var1 = this.getViewVector(param1);
        Vec3 var2 = var0.add(var1.x * param0, var1.y * param0, var1.z * param0);
        return this.level.clip(new ClipContext(var0, var2, ClipContext.Block.OUTLINE, param2 ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, this));
    }

    public boolean isPickable() {
        return false;
    }

    public boolean isPushable() {
        return false;
    }

    public void awardKillScore(Entity param0, int param1, DamageSource param2) {
        if (param0 instanceof ServerPlayer) {
            CriteriaTriggers.ENTITY_KILLED_PLAYER.trigger((ServerPlayer)param0, this, param2);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(double param0, double param1, double param2) {
        double var0 = this.x - param0;
        double var1 = this.y - param1;
        double var2 = this.z - param2;
        double var3 = var0 * var0 + var1 * var1 + var2 * var2;
        return this.shouldRenderAtSqrDistance(var3);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(double param0) {
        double var0 = this.getBoundingBox().getSize();
        if (Double.isNaN(var0)) {
            var0 = 1.0;
        }

        var0 *= 64.0 * viewScale;
        return param0 < var0 * var0;
    }

    public boolean saveAsPassenger(CompoundTag param0) {
        String var0 = this.getEncodeId();
        if (!this.removed && var0 != null) {
            param0.putString("id", var0);
            this.saveWithoutId(param0);
            return true;
        } else {
            return false;
        }
    }

    public boolean save(CompoundTag param0) {
        return this.isPassenger() ? false : this.saveAsPassenger(param0);
    }

    public CompoundTag saveWithoutId(CompoundTag param0) {
        try {
            param0.put("Pos", this.newDoubleList(this.x, this.y, this.z));
            Vec3 var0 = this.getDeltaMovement();
            param0.put("Motion", this.newDoubleList(var0.x, var0.y, var0.z));
            param0.put("Rotation", this.newFloatList(this.yRot, this.xRot));
            param0.putFloat("FallDistance", this.fallDistance);
            param0.putShort("Fire", (short)this.remainingFireTicks);
            param0.putShort("Air", (short)this.getAirSupply());
            param0.putBoolean("OnGround", this.onGround);
            param0.putInt("Dimension", this.dimension.getId());
            param0.putBoolean("Invulnerable", this.invulnerable);
            param0.putInt("PortalCooldown", this.changingDimensionDelay);
            param0.putUUID("UUID", this.getUUID());
            Component var1 = this.getCustomName();
            if (var1 != null) {
                param0.putString("CustomName", Component.Serializer.toJson(var1));
            }

            if (this.isCustomNameVisible()) {
                param0.putBoolean("CustomNameVisible", this.isCustomNameVisible());
            }

            if (this.isSilent()) {
                param0.putBoolean("Silent", this.isSilent());
            }

            if (this.isNoGravity()) {
                param0.putBoolean("NoGravity", this.isNoGravity());
            }

            if (this.glowing) {
                param0.putBoolean("Glowing", this.glowing);
            }

            if (!this.tags.isEmpty()) {
                ListTag var2 = new ListTag();

                for(String var3 : this.tags) {
                    var2.add(StringTag.valueOf(var3));
                }

                param0.put("Tags", var2);
            }

            this.addAdditionalSaveData(param0);
            if (this.isVehicle()) {
                ListTag var4 = new ListTag();

                for(Entity var5 : this.getPassengers()) {
                    CompoundTag var6 = new CompoundTag();
                    if (var5.saveAsPassenger(var6)) {
                        var4.add(var6);
                    }
                }

                if (!var4.isEmpty()) {
                    param0.put("Passengers", var4);
                }
            }

            return param0;
        } catch (Throwable var81) {
            CrashReport var8 = CrashReport.forThrowable(var81, "Saving entity NBT");
            CrashReportCategory var9 = var8.addCategory("Entity being saved");
            this.fillCrashReportCategory(var9);
            throw new ReportedException(var8);
        }
    }

    public void load(CompoundTag param0) {
        try {
            ListTag var0 = param0.getList("Pos", 6);
            ListTag var1 = param0.getList("Motion", 6);
            ListTag var2 = param0.getList("Rotation", 5);
            double var3 = var1.getDouble(0);
            double var4 = var1.getDouble(1);
            double var5 = var1.getDouble(2);
            this.setDeltaMovement(Math.abs(var3) > 10.0 ? 0.0 : var3, Math.abs(var4) > 10.0 ? 0.0 : var4, Math.abs(var5) > 10.0 ? 0.0 : var5);
            this.setPosAndOldPos(var0.getDouble(0), var0.getDouble(1), var0.getDouble(2));
            this.yRot = var2.getFloat(0);
            this.xRot = var2.getFloat(1);
            this.yRotO = this.yRot;
            this.xRotO = this.xRot;
            this.setYHeadRot(this.yRot);
            this.setYBodyRot(this.yRot);
            this.fallDistance = param0.getFloat("FallDistance");
            this.remainingFireTicks = param0.getShort("Fire");
            this.setAirSupply(param0.getShort("Air"));
            this.onGround = param0.getBoolean("OnGround");
            if (param0.contains("Dimension")) {
                this.dimension = DimensionType.getById(param0.getInt("Dimension"));
            }

            this.invulnerable = param0.getBoolean("Invulnerable");
            this.changingDimensionDelay = param0.getInt("PortalCooldown");
            if (param0.hasUUID("UUID")) {
                this.uuid = param0.getUUID("UUID");
                this.stringUUID = this.uuid.toString();
            }

            if (!Double.isFinite(this.x) || !Double.isFinite(this.y) || !Double.isFinite(this.z)) {
                throw new IllegalStateException("Entity has invalid position");
            } else if (Double.isFinite((double)this.yRot) && Double.isFinite((double)this.xRot)) {
                this.setPos(this.x, this.y, this.z);
                this.setRot(this.yRot, this.xRot);
                if (param0.contains("CustomName", 8)) {
                    this.setCustomName(Component.Serializer.fromJson(param0.getString("CustomName")));
                }

                this.setCustomNameVisible(param0.getBoolean("CustomNameVisible"));
                this.setSilent(param0.getBoolean("Silent"));
                this.setNoGravity(param0.getBoolean("NoGravity"));
                this.setGlowing(param0.getBoolean("Glowing"));
                if (param0.contains("Tags", 9)) {
                    this.tags.clear();
                    ListTag var6 = param0.getList("Tags", 8);
                    int var7 = Math.min(var6.size(), 1024);

                    for(int var8 = 0; var8 < var7; ++var8) {
                        this.tags.add(var6.getString(var8));
                    }
                }

                this.readAdditionalSaveData(param0);
                if (this.repositionEntityAfterLoad()) {
                    this.setPos(this.x, this.y, this.z);
                }

            } else {
                throw new IllegalStateException("Entity has invalid rotation");
            }
        } catch (Throwable var14) {
            CrashReport var10 = CrashReport.forThrowable(var14, "Loading entity NBT");
            CrashReportCategory var11 = var10.addCategory("Entity being loaded");
            this.fillCrashReportCategory(var11);
            throw new ReportedException(var10);
        }
    }

    protected boolean repositionEntityAfterLoad() {
        return true;
    }

    @Nullable
    protected final String getEncodeId() {
        EntityType<?> var0 = this.getType();
        ResourceLocation var1 = EntityType.getKey(var0);
        return var0.canSerialize() && var1 != null ? var1.toString() : null;
    }

    protected abstract void readAdditionalSaveData(CompoundTag var1);

    protected abstract void addAdditionalSaveData(CompoundTag var1);

    protected ListTag newDoubleList(double... param0) {
        ListTag var0 = new ListTag();

        for(double var1 : param0) {
            var0.add(DoubleTag.valueOf(var1));
        }

        return var0;
    }

    protected ListTag newFloatList(float... param0) {
        ListTag var0 = new ListTag();

        for(float var1 : param0) {
            var0.add(FloatTag.valueOf(var1));
        }

        return var0;
    }

    @Nullable
    public ItemEntity spawnAtLocation(ItemLike param0) {
        return this.spawnAtLocation(param0, 0);
    }

    @Nullable
    public ItemEntity spawnAtLocation(ItemLike param0, int param1) {
        return this.spawnAtLocation(new ItemStack(param0), (float)param1);
    }

    @Nullable
    public ItemEntity spawnAtLocation(ItemStack param0) {
        return this.spawnAtLocation(param0, 0.0F);
    }

    @Nullable
    public ItemEntity spawnAtLocation(ItemStack param0, float param1) {
        if (param0.isEmpty()) {
            return null;
        } else if (this.level.isClientSide) {
            return null;
        } else {
            ItemEntity var0 = new ItemEntity(this.level, this.x, this.y + (double)param1, this.z, param0);
            var0.setDefaultPickUpDelay();
            this.level.addFreshEntity(var0);
            return var0;
        }
    }

    public boolean isAlive() {
        return !this.removed;
    }

    public boolean isInWall() {
        if (this.noPhysics) {
            return false;
        } else {
            try (BlockPos.PooledMutableBlockPos var0 = BlockPos.PooledMutableBlockPos.acquire()) {
                for(int var1 = 0; var1 < 8; ++var1) {
                    int var2 = Mth.floor(this.y + (double)(((float)((var1 >> 0) % 2) - 0.5F) * 0.1F) + (double)this.eyeHeight);
                    int var3 = Mth.floor(this.x + (double)(((float)((var1 >> 1) % 2) - 0.5F) * this.dimensions.width * 0.8F));
                    int var4 = Mth.floor(this.z + (double)(((float)((var1 >> 2) % 2) - 0.5F) * this.dimensions.width * 0.8F));
                    if (var0.getX() != var3 || var0.getY() != var2 || var0.getZ() != var4) {
                        var0.set(var3, var2, var4);
                        if (this.level.getBlockState(var0).isViewBlocking(this.level, var0)) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }
    }

    public boolean interact(Player param0, InteractionHand param1) {
        return false;
    }

    @Nullable
    public AABB getCollideAgainstBox(Entity param0) {
        return null;
    }

    public void rideTick() {
        this.setDeltaMovement(Vec3.ZERO);
        this.tick();
        if (this.isPassenger()) {
            this.getVehicle().positionRider(this);
        }
    }

    public void positionRider(Entity param0) {
        if (this.hasPassenger(param0)) {
            param0.setPos(this.x, this.y + this.getRideHeight() + param0.getRidingHeight(), this.z);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void onPassengerTurned(Entity param0) {
    }

    public double getRidingHeight() {
        return 0.0;
    }

    public double getRideHeight() {
        return (double)this.dimensions.height * 0.75;
    }

    public boolean startRiding(Entity param0) {
        return this.startRiding(param0, false);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean showVehicleHealth() {
        return this instanceof LivingEntity;
    }

    public boolean startRiding(Entity param0, boolean param1) {
        for(Entity var0 = param0; var0.vehicle != null; var0 = var0.vehicle) {
            if (var0.vehicle == this) {
                return false;
            }
        }

        if (param1 || this.canRide(param0) && param0.canAddPassenger(this)) {
            if (this.isPassenger()) {
                this.stopRiding();
            }

            this.vehicle = param0;
            this.vehicle.addPassenger(this);
            return true;
        } else {
            return false;
        }
    }

    protected boolean canRide(Entity param0) {
        return this.boardingCooldown <= 0;
    }

    protected boolean canEnterPose(Pose param0) {
        return this.level.noCollision(this, this.getBoundingBoxForPose(param0));
    }

    public void ejectPassengers() {
        for(int var0 = this.passengers.size() - 1; var0 >= 0; --var0) {
            this.passengers.get(var0).stopRiding();
        }

    }

    public void stopRiding() {
        if (this.vehicle != null) {
            Entity var0 = this.vehicle;
            this.vehicle = null;
            var0.removePassenger(this);
        }

    }

    protected void addPassenger(Entity param0) {
        if (param0.getVehicle() != this) {
            throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
        } else {
            if (!this.level.isClientSide && param0 instanceof Player && !(this.getControllingPassenger() instanceof Player)) {
                this.passengers.add(0, param0);
            } else {
                this.passengers.add(param0);
            }

        }
    }

    protected void removePassenger(Entity param0) {
        if (param0.getVehicle() == this) {
            throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
        } else {
            this.passengers.remove(param0);
            param0.boardingCooldown = 60;
        }
    }

    protected boolean canAddPassenger(Entity param0) {
        return this.getPassengers().size() < 1;
    }

    @OnlyIn(Dist.CLIENT)
    public void lerpTo(double param0, double param1, double param2, float param3, float param4, int param5, boolean param6) {
        this.setPos(param0, param1, param2);
        this.setRot(param3, param4);
    }

    @OnlyIn(Dist.CLIENT)
    public void lerpHeadTo(float param0, int param1) {
        this.setYHeadRot(param0);
    }

    public float getPickRadius() {
        return 0.0F;
    }

    public Vec3 getLookAngle() {
        return this.calculateViewVector(this.xRot, this.yRot);
    }

    public Vec2 getRotationVector() {
        return new Vec2(this.xRot, this.yRot);
    }

    @OnlyIn(Dist.CLIENT)
    public Vec3 getForward() {
        return Vec3.directionFromRotation(this.getRotationVector());
    }

    public void handleInsidePortal(BlockPos param0) {
        if (this.changingDimensionDelay > 0) {
            this.changingDimensionDelay = this.getDimensionChangingDelay();
        } else {
            if (!this.level.isClientSide && !param0.equals(this.portalEntranceBlock)) {
                this.portalEntranceBlock = new BlockPos(param0);
                BlockPattern.BlockPatternMatch var0 = NetherPortalBlock.getPortalShape(this.level, this.portalEntranceBlock);
                double var1 = var0.getForwards().getAxis() == Direction.Axis.X ? (double)var0.getFrontTopLeft().getZ() : (double)var0.getFrontTopLeft().getX();
                double var2 = Math.abs(
                    Mth.pct(
                        (var0.getForwards().getAxis() == Direction.Axis.X ? this.z : this.x)
                            - (double)(var0.getForwards().getClockWise().getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 1 : 0),
                        var1,
                        var1 - (double)var0.getWidth()
                    )
                );
                double var3 = Mth.pct(this.y - 1.0, (double)var0.getFrontTopLeft().getY(), (double)(var0.getFrontTopLeft().getY() - var0.getHeight()));
                this.portalEntranceOffset = new Vec3(var2, var3, 0.0);
                this.portalEntranceForwards = var0.getForwards();
            }

            this.isInsidePortal = true;
        }
    }

    protected void handleNetherPortal() {
        if (this.level instanceof ServerLevel) {
            int var0 = this.getPortalWaitTime();
            if (this.isInsidePortal) {
                if (this.level.getServer().isNetherEnabled() && !this.isPassenger() && this.portalTime++ >= var0) {
                    this.level.getProfiler().push("portal");
                    this.portalTime = var0;
                    this.changingDimensionDelay = this.getDimensionChangingDelay();
                    this.changeDimension(this.level.dimension.getType() == DimensionType.NETHER ? DimensionType.OVERWORLD : DimensionType.NETHER);
                    this.level.getProfiler().pop();
                }

                this.isInsidePortal = false;
            } else {
                if (this.portalTime > 0) {
                    this.portalTime -= 4;
                }

                if (this.portalTime < 0) {
                    this.portalTime = 0;
                }
            }

            this.processDimensionDelay();
        }
    }

    public int getDimensionChangingDelay() {
        return 300;
    }

    @OnlyIn(Dist.CLIENT)
    public void lerpMotion(double param0, double param1, double param2) {
        this.setDeltaMovement(param0, param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(byte param0) {
    }

    @OnlyIn(Dist.CLIENT)
    public void animateHurt() {
    }

    public Iterable<ItemStack> getHandSlots() {
        return EMPTY_LIST;
    }

    public Iterable<ItemStack> getArmorSlots() {
        return EMPTY_LIST;
    }

    public Iterable<ItemStack> getAllSlots() {
        return Iterables.concat(this.getHandSlots(), this.getArmorSlots());
    }

    public void setItemSlot(EquipmentSlot param0, ItemStack param1) {
    }

    public boolean isOnFire() {
        boolean var0 = this.level != null && this.level.isClientSide;
        return !this.fireImmune() && (this.remainingFireTicks > 0 || var0 && this.getSharedFlag(0));
    }

    public boolean isPassenger() {
        return this.getVehicle() != null;
    }

    public boolean isVehicle() {
        return !this.getPassengers().isEmpty();
    }

    public boolean rideableUnderWater() {
        return true;
    }

    public void setShiftKeyDown(boolean param0) {
        this.setSharedFlag(1, param0);
    }

    public boolean isShiftKeyDown() {
        return this.getSharedFlag(1);
    }

    public boolean isSteppingCarefully() {
        return this.isShiftKeyDown();
    }

    public boolean isSuppressingBounce() {
        return this.isShiftKeyDown();
    }

    public boolean isDiscrete() {
        return this.isShiftKeyDown();
    }

    public boolean isDescending() {
        return this.isShiftKeyDown();
    }

    public boolean isCrouching() {
        return this.getPose() == Pose.CROUCHING;
    }

    public boolean isSprinting() {
        return this.getSharedFlag(3);
    }

    public void setSprinting(boolean param0) {
        this.setSharedFlag(3, param0);
    }

    public boolean isSwimming() {
        return this.getSharedFlag(4);
    }

    public boolean isVisuallySwimming() {
        return this.getPose() == Pose.SWIMMING;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isVisuallyCrawling() {
        return this.isVisuallySwimming() && !this.isInWater();
    }

    public void setSwimming(boolean param0) {
        this.setSharedFlag(4, param0);
    }

    public boolean isGlowing() {
        return this.glowing || this.level.isClientSide && this.getSharedFlag(6);
    }

    public void setGlowing(boolean param0) {
        this.glowing = param0;
        if (!this.level.isClientSide) {
            this.setSharedFlag(6, this.glowing);
        }

    }

    public boolean isInvisible() {
        return this.getSharedFlag(5);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isInvisibleTo(Player param0) {
        if (param0.isSpectator()) {
            return false;
        } else {
            Team var0 = this.getTeam();
            return var0 != null && param0 != null && param0.getTeam() == var0 && var0.canSeeFriendlyInvisibles() ? false : this.isInvisible();
        }
    }

    @Nullable
    public Team getTeam() {
        return this.level.getScoreboard().getPlayersTeam(this.getScoreboardName());
    }

    public boolean isAlliedTo(Entity param0) {
        return this.isAlliedTo(param0.getTeam());
    }

    public boolean isAlliedTo(Team param0) {
        return this.getTeam() != null ? this.getTeam().isAlliedTo(param0) : false;
    }

    public void setInvisible(boolean param0) {
        this.setSharedFlag(5, param0);
    }

    protected boolean getSharedFlag(int param0) {
        return (this.entityData.get(DATA_SHARED_FLAGS_ID) & 1 << param0) != 0;
    }

    protected void setSharedFlag(int param0, boolean param1) {
        byte var0 = this.entityData.get(DATA_SHARED_FLAGS_ID);
        if (param1) {
            this.entityData.set(DATA_SHARED_FLAGS_ID, (byte)(var0 | 1 << param0));
        } else {
            this.entityData.set(DATA_SHARED_FLAGS_ID, (byte)(var0 & ~(1 << param0)));
        }

    }

    public int getMaxAirSupply() {
        return 300;
    }

    public int getAirSupply() {
        return this.entityData.get(DATA_AIR_SUPPLY_ID);
    }

    public void setAirSupply(int param0) {
        this.entityData.set(DATA_AIR_SUPPLY_ID, param0);
    }

    public void thunderHit(LightningBolt param0) {
        ++this.remainingFireTicks;
        if (this.remainingFireTicks == 0) {
            this.setSecondsOnFire(8);
        }

        this.hurt(DamageSource.LIGHTNING_BOLT, 5.0F);
    }

    public void onAboveBubbleCol(boolean param0) {
        Vec3 var0 = this.getDeltaMovement();
        double var1;
        if (param0) {
            var1 = Math.max(-0.9, var0.y - 0.03);
        } else {
            var1 = Math.min(1.8, var0.y + 0.1);
        }

        this.setDeltaMovement(var0.x, var1, var0.z);
    }

    public void onInsideBubbleColumn(boolean param0) {
        Vec3 var0 = this.getDeltaMovement();
        double var1;
        if (param0) {
            var1 = Math.max(-0.3, var0.y - 0.03);
        } else {
            var1 = Math.min(0.7, var0.y + 0.06);
        }

        this.setDeltaMovement(var0.x, var1, var0.z);
        this.fallDistance = 0.0F;
    }

    public void killed(LivingEntity param0) {
    }

    protected void checkInBlock(double param0, double param1, double param2) {
        BlockPos var0 = new BlockPos(param0, param1, param2);
        Vec3 var1 = new Vec3(param0 - (double)var0.getX(), param1 - (double)var0.getY(), param2 - (double)var0.getZ());
        BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();
        Direction var3 = Direction.UP;
        double var4 = Double.MAX_VALUE;

        for(Direction var5 : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP}) {
            var2.set(var0).move(var5);
            if (!this.level.getBlockState(var2).isCollisionShapeFullBlock(this.level, var2)) {
                double var6 = var1.get(var5.getAxis());
                double var7 = var5.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - var6 : var6;
                if (var7 < var4) {
                    var4 = var7;
                    var3 = var5;
                }
            }
        }

        float var8 = this.random.nextFloat() * 0.2F + 0.1F;
        float var9 = (float)var3.getAxisDirection().getStep();
        Vec3 var10 = this.getDeltaMovement().scale(0.75);
        if (var3.getAxis() == Direction.Axis.X) {
            this.setDeltaMovement((double)(var9 * var8), var10.y, var10.z);
        } else if (var3.getAxis() == Direction.Axis.Y) {
            this.setDeltaMovement(var10.x, (double)(var9 * var8), var10.z);
        } else if (var3.getAxis() == Direction.Axis.Z) {
            this.setDeltaMovement(var10.x, var10.y, (double)(var9 * var8));
        }

    }

    public void makeStuckInBlock(BlockState param0, Vec3 param1) {
        this.fallDistance = 0.0F;
        this.stuckSpeedMultiplier = param1;
    }

    private static void removeAction(Component param0) {
        param0.withStyle(param0x -> param0x.setClickEvent(null)).getSiblings().forEach(Entity::removeAction);
    }

    @Override
    public Component getName() {
        Component var0 = this.getCustomName();
        if (var0 != null) {
            Component var1 = var0.deepCopy();
            removeAction(var1);
            return var1;
        } else {
            return this.type.getDescription();
        }
    }

    public boolean is(Entity param0) {
        return this == param0;
    }

    public float getYHeadRot() {
        return 0.0F;
    }

    public void setYHeadRot(float param0) {
    }

    public void setYBodyRot(float param0) {
    }

    public boolean isAttackable() {
        return true;
    }

    public boolean skipAttackInteraction(Entity param0) {
        return false;
    }

    @Override
    public String toString() {
        return String.format(
            Locale.ROOT,
            "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]",
            this.getClass().getSimpleName(),
            this.getName().getContents(),
            this.id,
            this.level == null ? "~NULL~" : this.level.getLevelData().getLevelName(),
            this.x,
            this.y,
            this.z
        );
    }

    public boolean isInvulnerableTo(DamageSource param0) {
        return this.invulnerable && param0 != DamageSource.OUT_OF_WORLD && !param0.isCreativePlayer();
    }

    public boolean isInvulnerable() {
        return this.invulnerable;
    }

    public void setInvulnerable(boolean param0) {
        this.invulnerable = param0;
    }

    public void copyPosition(Entity param0) {
        this.moveTo(param0.x, param0.y, param0.z, param0.yRot, param0.xRot);
    }

    public void restoreFrom(Entity param0) {
        CompoundTag var0 = param0.saveWithoutId(new CompoundTag());
        var0.remove("Dimension");
        this.load(var0);
        this.changingDimensionDelay = param0.changingDimensionDelay;
        this.portalEntranceBlock = param0.portalEntranceBlock;
        this.portalEntranceOffset = param0.portalEntranceOffset;
        this.portalEntranceForwards = param0.portalEntranceForwards;
    }

    @Nullable
    public Entity changeDimension(DimensionType param0) {
        if (!this.level.isClientSide && !this.removed) {
            this.level.getProfiler().push("changeDimension");
            MinecraftServer var0 = this.getServer();
            DimensionType var1 = this.dimension;
            ServerLevel var2 = var0.getLevel(var1);
            ServerLevel var3 = var0.getLevel(param0);
            this.dimension = param0;
            this.unRide();
            this.level.getProfiler().push("reposition");
            Vec3 var4 = this.getDeltaMovement();
            float var5 = 0.0F;
            BlockPos var6;
            if (var1 == DimensionType.THE_END && param0 == DimensionType.OVERWORLD) {
                var6 = var3.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, var3.getSharedSpawnPos());
            } else if (param0 == DimensionType.THE_END) {
                var6 = var3.getDimensionSpecificSpawn();
            } else {
                double var8 = this.x;
                double var9 = this.z;
                double var10 = 8.0;
                if (var1 == DimensionType.OVERWORLD && param0 == DimensionType.NETHER) {
                    var8 /= 8.0;
                    var9 /= 8.0;
                } else if (var1 == DimensionType.NETHER && param0 == DimensionType.OVERWORLD) {
                    var8 *= 8.0;
                    var9 *= 8.0;
                }

                double var11 = Math.min(-2.9999872E7, var3.getWorldBorder().getMinX() + 16.0);
                double var12 = Math.min(-2.9999872E7, var3.getWorldBorder().getMinZ() + 16.0);
                double var13 = Math.min(2.9999872E7, var3.getWorldBorder().getMaxX() - 16.0);
                double var14 = Math.min(2.9999872E7, var3.getWorldBorder().getMaxZ() - 16.0);
                var8 = Mth.clamp(var8, var11, var13);
                var9 = Mth.clamp(var9, var12, var14);
                Vec3 var15 = this.getPortalEntranceOffset();
                var6 = new BlockPos(var8, this.y, var9);
                BlockPattern.PortalInfo var17 = var3.getPortalForcer()
                    .findPortal(var6, var4, this.getPortalEntranceForwards(), var15.x, var15.y, this instanceof Player);
                if (var17 == null) {
                    return null;
                }

                var6 = new BlockPos(var17.pos);
                var4 = var17.speed;
                var5 = (float)var17.angle;
            }

            this.level.getProfiler().popPush("reloading");
            Entity var18 = this.getType().create(var3);
            if (var18 != null) {
                var18.restoreFrom(this);
                var18.moveTo(var6, var18.yRot + var5, var18.xRot);
                var18.setDeltaMovement(var4);
                var3.addFromAnotherDimension(var18);
            }

            this.removed = true;
            this.level.getProfiler().pop();
            var2.resetEmptyTime();
            var3.resetEmptyTime();
            this.level.getProfiler().pop();
            return var18;
        } else {
            return null;
        }
    }

    public boolean canChangeDimensions() {
        return true;
    }

    public float getBlockExplosionResistance(Explosion param0, BlockGetter param1, BlockPos param2, BlockState param3, FluidState param4, float param5) {
        return param5;
    }

    public boolean shouldBlockExplode(Explosion param0, BlockGetter param1, BlockPos param2, BlockState param3, float param4) {
        return true;
    }

    public int getMaxFallDistance() {
        return 3;
    }

    public Vec3 getPortalEntranceOffset() {
        return this.portalEntranceOffset;
    }

    public Direction getPortalEntranceForwards() {
        return this.portalEntranceForwards;
    }

    public boolean isIgnoringBlockTriggers() {
        return false;
    }

    public void fillCrashReportCategory(CrashReportCategory param0) {
        param0.setDetail("Entity Type", () -> EntityType.getKey(this.getType()) + " (" + this.getClass().getCanonicalName() + ")");
        param0.setDetail("Entity ID", this.id);
        param0.setDetail("Entity Name", () -> this.getName().getString());
        param0.setDetail("Entity's Exact location", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", this.x, this.y, this.z));
        param0.setDetail("Entity's Block location", CrashReportCategory.formatLocation(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z)));
        Vec3 var0 = this.getDeltaMovement();
        param0.setDetail("Entity's Momentum", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", var0.x, var0.y, var0.z));
        param0.setDetail("Entity's Passengers", () -> this.getPassengers().toString());
        param0.setDetail("Entity's Vehicle", () -> this.getVehicle().toString());
    }

    @OnlyIn(Dist.CLIENT)
    public boolean displayFireAnimation() {
        return this.isOnFire() && !this.isSpectator();
    }

    public void setUUID(UUID param0) {
        this.uuid = param0;
        this.stringUUID = this.uuid.toString();
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getStringUUID() {
        return this.stringUUID;
    }

    public String getScoreboardName() {
        return this.stringUUID;
    }

    public boolean isPushedByWater() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public static double getViewScale() {
        return viewScale;
    }

    @OnlyIn(Dist.CLIENT)
    public static void setViewScale(double param0) {
        viewScale = param0;
    }

    @Override
    public Component getDisplayName() {
        return PlayerTeam.formatNameForTeam(this.getTeam(), this.getName())
            .withStyle(param0 -> param0.setHoverEvent(this.createHoverEvent()).setInsertion(this.getStringUUID()));
    }

    public void setCustomName(@Nullable Component param0) {
        this.entityData.set(DATA_CUSTOM_NAME, Optional.ofNullable(param0));
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return this.entityData.get(DATA_CUSTOM_NAME).orElse(null);
    }

    @Override
    public boolean hasCustomName() {
        return this.entityData.get(DATA_CUSTOM_NAME).isPresent();
    }

    public void setCustomNameVisible(boolean param0) {
        this.entityData.set(DATA_CUSTOM_NAME_VISIBLE, param0);
    }

    public boolean isCustomNameVisible() {
        return this.entityData.get(DATA_CUSTOM_NAME_VISIBLE);
    }

    public final void teleportToWithTicket(double param0, double param1, double param2) {
        if (this.level instanceof ServerLevel) {
            ChunkPos var0 = new ChunkPos(new BlockPos(param0, param1, param2));
            ((ServerLevel)this.level).getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, var0, 0, this.getId());
            this.level.getChunk(var0.x, var0.z);
            this.teleportTo(param0, param1, param2);
        }
    }

    public void teleportTo(double param0, double param1, double param2) {
        if (this.level instanceof ServerLevel) {
            this.teleported = true;
            this.moveTo(param0, param1, param2, this.yRot, this.xRot);
            ((ServerLevel)this.level).updateChunkPos(this);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldShowName() {
        return this.isCustomNameVisible();
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_POSE.equals(param0)) {
            this.refreshDimensions();
        }

    }

    public void refreshDimensions() {
        EntityDimensions var0 = this.dimensions;
        Pose var1 = this.getPose();
        EntityDimensions var2 = this.getDimensions(var1);
        this.dimensions = var2;
        this.eyeHeight = this.getEyeHeight(var1, var2);
        if (var2.width < var0.width) {
            double var3 = (double)var2.width / 2.0;
            this.setBoundingBox(new AABB(this.x - var3, this.y, this.z - var3, this.x + var3, this.y + (double)var2.height, this.z + var3));
        } else {
            AABB var4 = this.getBoundingBox();
            this.setBoundingBox(
                new AABB(var4.minX, var4.minY, var4.minZ, var4.minX + (double)var2.width, var4.minY + (double)var2.height, var4.minZ + (double)var2.width)
            );
            if (var2.width > var0.width && !this.firstTick && !this.level.isClientSide) {
                float var5 = var0.width - var2.width;
                this.move(MoverType.SELF, new Vec3((double)var5, 0.0, (double)var5));
            }

        }
    }

    public Direction getDirection() {
        return Direction.fromYRot((double)this.yRot);
    }

    public Direction getMotionDirection() {
        return this.getDirection();
    }

    protected HoverEvent createHoverEvent() {
        CompoundTag var0 = new CompoundTag();
        ResourceLocation var1 = EntityType.getKey(this.getType());
        var0.putString("id", this.getStringUUID());
        if (var1 != null) {
            var0.putString("type", var1.toString());
        }

        var0.putString("name", Component.Serializer.toJson(this.getName()));
        return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new TextComponent(var0.toString()));
    }

    public boolean broadcastToPlayer(ServerPlayer param0) {
        return true;
    }

    public AABB getBoundingBox() {
        return this.bb;
    }

    @OnlyIn(Dist.CLIENT)
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox();
    }

    protected AABB getBoundingBoxForPose(Pose param0) {
        EntityDimensions var0 = this.getDimensions(param0);
        float var1 = var0.width / 2.0F;
        Vec3 var2 = new Vec3(this.x - (double)var1, this.y, this.z - (double)var1);
        Vec3 var3 = new Vec3(this.x + (double)var1, this.y + (double)var0.height, this.z + (double)var1);
        return new AABB(var2, var3);
    }

    public void setBoundingBox(AABB param0) {
        this.bb = param0;
    }

    protected float getEyeHeight(Pose param0, EntityDimensions param1) {
        return param1.height * 0.85F;
    }

    @OnlyIn(Dist.CLIENT)
    public float getEyeHeight(Pose param0) {
        return this.getEyeHeight(param0, this.getDimensions(param0));
    }

    public final float getEyeHeight() {
        return this.eyeHeight;
    }

    public boolean setSlot(int param0, ItemStack param1) {
        return false;
    }

    @Override
    public void sendMessage(Component param0) {
    }

    public BlockPos getCommandSenderBlockPosition() {
        return new BlockPos(this);
    }

    public Vec3 getCommandSenderWorldPosition() {
        return new Vec3(this.x, this.y, this.z);
    }

    public Level getCommandSenderWorld() {
        return this.level;
    }

    @Nullable
    public MinecraftServer getServer() {
        return this.level.getServer();
    }

    public InteractionResult interactAt(Player param0, Vec3 param1, InteractionHand param2) {
        return InteractionResult.PASS;
    }

    public boolean ignoreExplosion() {
        return false;
    }

    protected void doEnchantDamageEffects(LivingEntity param0, Entity param1) {
        if (param1 instanceof LivingEntity) {
            EnchantmentHelper.doPostHurtEffects((LivingEntity)param1, param0);
        }

        EnchantmentHelper.doPostDamageEffects(param0, param1);
    }

    public void startSeenByPlayer(ServerPlayer param0) {
    }

    public void stopSeenByPlayer(ServerPlayer param0) {
    }

    public float rotate(Rotation param0) {
        float var0 = Mth.wrapDegrees(this.yRot);
        switch(param0) {
            case CLOCKWISE_180:
                return var0 + 180.0F;
            case COUNTERCLOCKWISE_90:
                return var0 + 270.0F;
            case CLOCKWISE_90:
                return var0 + 90.0F;
            default:
                return var0;
        }
    }

    public float mirror(Mirror param0) {
        float var0 = Mth.wrapDegrees(this.yRot);
        switch(param0) {
            case LEFT_RIGHT:
                return -var0;
            case FRONT_BACK:
                return 180.0F - var0;
            default:
                return var0;
        }
    }

    public boolean onlyOpCanSetNbt() {
        return false;
    }

    public boolean checkAndResetTeleportedFlag() {
        boolean var0 = this.teleported;
        this.teleported = false;
        return var0;
    }

    @Nullable
    public Entity getControllingPassenger() {
        return null;
    }

    public List<Entity> getPassengers() {
        return (List<Entity>)(this.passengers.isEmpty() ? Collections.emptyList() : Lists.newArrayList(this.passengers));
    }

    public boolean hasPassenger(Entity param0) {
        for(Entity var0 : this.getPassengers()) {
            if (var0.equals(param0)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasPassenger(Class<? extends Entity> param0) {
        for(Entity var0 : this.getPassengers()) {
            if (param0.isAssignableFrom(var0.getClass())) {
                return true;
            }
        }

        return false;
    }

    public Collection<Entity> getIndirectPassengers() {
        Set<Entity> var0 = Sets.newHashSet();

        for(Entity var1 : this.getPassengers()) {
            var0.add(var1);
            var1.fillIndirectPassengers(false, var0);
        }

        return var0;
    }

    public boolean hasOnePlayerPassenger() {
        Set<Entity> var0 = Sets.newHashSet();
        this.fillIndirectPassengers(true, var0);
        return var0.size() == 1;
    }

    private void fillIndirectPassengers(boolean param0, Set<Entity> param1) {
        for(Entity var0 : this.getPassengers()) {
            if (!param0 || ServerPlayer.class.isAssignableFrom(var0.getClass())) {
                param1.add(var0);
            }

            var0.fillIndirectPassengers(param0, param1);
        }

    }

    public Entity getRootVehicle() {
        Entity var0 = this;

        while(var0.isPassenger()) {
            var0 = var0.getVehicle();
        }

        return var0;
    }

    public boolean isPassengerOfSameVehicle(Entity param0) {
        return this.getRootVehicle() == param0.getRootVehicle();
    }

    public boolean hasIndirectPassenger(Entity param0) {
        for(Entity var0 : this.getPassengers()) {
            if (var0.equals(param0)) {
                return true;
            }

            if (var0.hasIndirectPassenger(param0)) {
                return true;
            }
        }

        return false;
    }

    public boolean isControlledByLocalInstance() {
        Entity var0 = this.getControllingPassenger();
        if (var0 instanceof Player) {
            return ((Player)var0).isLocalPlayer();
        } else {
            return !this.level.isClientSide;
        }
    }

    @Nullable
    public Entity getVehicle() {
        return this.vehicle;
    }

    public PushReaction getPistonPushReaction() {
        return PushReaction.NORMAL;
    }

    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }

    protected int getFireImmuneTicks() {
        return 1;
    }

    public CommandSourceStack createCommandSourceStack() {
        return new CommandSourceStack(
            this,
            new Vec3(this.x, this.y, this.z),
            this.getRotationVector(),
            this.level instanceof ServerLevel ? (ServerLevel)this.level : null,
            this.getPermissionLevel(),
            this.getName().getString(),
            this.getDisplayName(),
            this.level.getServer(),
            this
        );
    }

    protected int getPermissionLevel() {
        return 0;
    }

    public boolean hasPermissions(int param0) {
        return this.getPermissionLevel() >= param0;
    }

    @Override
    public boolean acceptsSuccess() {
        return this.level.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK);
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return true;
    }

    public void lookAt(EntityAnchorArgument.Anchor param0, Vec3 param1) {
        Vec3 var0 = param0.apply(this);
        double var1 = param1.x - var0.x;
        double var2 = param1.y - var0.y;
        double var3 = param1.z - var0.z;
        double var4 = (double)Mth.sqrt(var1 * var1 + var3 * var3);
        this.xRot = Mth.wrapDegrees((float)(-(Mth.atan2(var2, var4) * 180.0F / (float)Math.PI)));
        this.yRot = Mth.wrapDegrees((float)(Mth.atan2(var3, var1) * 180.0F / (float)Math.PI) - 90.0F);
        this.setYHeadRot(this.yRot);
        this.xRotO = this.xRot;
        this.yRotO = this.yRot;
    }

    public boolean checkAndHandleWater(Tag<Fluid> param0) {
        AABB var0 = this.getBoundingBox().deflate(0.001);
        int var1 = Mth.floor(var0.minX);
        int var2 = Mth.ceil(var0.maxX);
        int var3 = Mth.floor(var0.minY);
        int var4 = Mth.ceil(var0.maxY);
        int var5 = Mth.floor(var0.minZ);
        int var6 = Mth.ceil(var0.maxZ);
        if (!this.level.hasChunksAt(var1, var3, var5, var2, var4, var6)) {
            return false;
        } else {
            double var7 = 0.0;
            boolean var8 = this.isPushedByWater();
            boolean var9 = false;
            Vec3 var10 = Vec3.ZERO;
            int var11 = 0;

            try (BlockPos.PooledMutableBlockPos var12 = BlockPos.PooledMutableBlockPos.acquire()) {
                for(int var13 = var1; var13 < var2; ++var13) {
                    for(int var14 = var3; var14 < var4; ++var14) {
                        for(int var15 = var5; var15 < var6; ++var15) {
                            var12.set(var13, var14, var15);
                            FluidState var16 = this.level.getFluidState(var12);
                            if (var16.is(param0)) {
                                double var17 = (double)((float)var14 + var16.getHeight(this.level, var12));
                                if (var17 >= var0.minY) {
                                    var9 = true;
                                    var7 = Math.max(var17 - var0.minY, var7);
                                    if (var8) {
                                        Vec3 var18 = var16.getFlow(this.level, var12);
                                        if (var7 < 0.4) {
                                            var18 = var18.scale(var7);
                                        }

                                        var10 = var10.add(var18);
                                        ++var11;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (var10.length() > 0.0) {
                if (var11 > 0) {
                    var10 = var10.scale(1.0 / (double)var11);
                }

                if (!(this instanceof Player)) {
                    var10 = var10.normalize();
                }

                this.setDeltaMovement(this.getDeltaMovement().add(var10.scale(0.014)));
            }

            this.waterHeight = var7;
            return var9;
        }
    }

    public double getWaterHeight() {
        return this.waterHeight;
    }

    public final float getBbWidth() {
        return this.dimensions.width;
    }

    public final float getBbHeight() {
        return this.dimensions.height;
    }

    public abstract Packet<?> getAddEntityPacket();

    public EntityDimensions getDimensions(Pose param0) {
        return this.type.getDimensions();
    }

    public Vec3 position() {
        return new Vec3(this.x, this.y, this.z);
    }

    public Vec3 getDeltaMovement() {
        return this.deltaMovement;
    }

    public void setDeltaMovement(Vec3 param0) {
        this.deltaMovement = param0;
    }

    public void setDeltaMovement(double param0, double param1, double param2) {
        this.setDeltaMovement(new Vec3(param0, param1, param2));
    }
}
