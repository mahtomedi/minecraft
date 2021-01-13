package net.minecraft.world.entity.projectile;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FishingHook extends Projectile {
    private final Random syncronizedRandom = new Random();
    private boolean biting;
    private int outOfWaterTime;
    private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_BITING = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.BOOLEAN);
    private int life;
    private int nibble;
    private int timeUntilLured;
    private int timeUntilHooked;
    private float fishAngle;
    private boolean openWater = true;
    private Entity hookedIn;
    private FishingHook.FishHookState currentState = FishingHook.FishHookState.FLYING;
    private final int luck;
    private final int lureSpeed;

    private FishingHook(Level param0, Player param1, int param2, int param3) {
        super(EntityType.FISHING_BOBBER, param0);
        this.noCulling = true;
        this.setOwner(param1);
        param1.fishing = this;
        this.luck = Math.max(0, param2);
        this.lureSpeed = Math.max(0, param3);
    }

    @OnlyIn(Dist.CLIENT)
    public FishingHook(Level param0, Player param1, double param2, double param3, double param4) {
        this(param0, param1, 0, 0);
        this.setPos(param2, param3, param4);
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
    }

    public FishingHook(Player param0, Level param1, int param2, int param3) {
        this(param1, param0, param2, param3);
        float var0 = param0.xRot;
        float var1 = param0.yRot;
        float var2 = Mth.cos(-var1 * (float) (Math.PI / 180.0) - (float) Math.PI);
        float var3 = Mth.sin(-var1 * (float) (Math.PI / 180.0) - (float) Math.PI);
        float var4 = -Mth.cos(-var0 * (float) (Math.PI / 180.0));
        float var5 = Mth.sin(-var0 * (float) (Math.PI / 180.0));
        double var6 = param0.getX() - (double)var3 * 0.3;
        double var7 = param0.getEyeY();
        double var8 = param0.getZ() - (double)var2 * 0.3;
        this.moveTo(var6, var7, var8, var1, var0);
        Vec3 var9 = new Vec3((double)(-var3), (double)Mth.clamp(-(var5 / var4), -5.0F, 5.0F), (double)(-var2));
        double var10 = var9.length();
        var9 = var9.multiply(
            0.6 / var10 + 0.5 + this.random.nextGaussian() * 0.0045,
            0.6 / var10 + 0.5 + this.random.nextGaussian() * 0.0045,
            0.6 / var10 + 0.5 + this.random.nextGaussian() * 0.0045
        );
        this.setDeltaMovement(var9);
        this.yRot = (float)(Mth.atan2(var9.x, var9.z) * 180.0F / (float)Math.PI);
        this.xRot = (float)(Mth.atan2(var9.y, (double)Mth.sqrt(getHorizontalDistanceSqr(var9))) * 180.0F / (float)Math.PI);
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_HOOKED_ENTITY, 0);
        this.getEntityData().define(DATA_BITING, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_HOOKED_ENTITY.equals(param0)) {
            int var0 = this.getEntityData().get(DATA_HOOKED_ENTITY);
            this.hookedIn = var0 > 0 ? this.level.getEntity(var0 - 1) : null;
        }

        if (DATA_BITING.equals(param0)) {
            this.biting = this.getEntityData().get(DATA_BITING);
            if (this.biting) {
                this.setDeltaMovement(this.getDeltaMovement().x, (double)(-0.4F * Mth.nextFloat(this.syncronizedRandom, 0.6F, 1.0F)), this.getDeltaMovement().z);
            }
        }

        super.onSyncedDataUpdated(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRenderAtSqrDistance(double param0) {
        double var0 = 64.0;
        return param0 < 4096.0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void lerpTo(double param0, double param1, double param2, float param3, float param4, int param5, boolean param6) {
    }

    @Override
    public void tick() {
        this.syncronizedRandom.setSeed(this.getUUID().getLeastSignificantBits() ^ this.level.getGameTime());
        super.tick();
        Player var0 = this.getPlayerOwner();
        if (var0 == null) {
            this.remove();
        } else if (this.level.isClientSide || !this.shouldStopFishing(var0)) {
            if (this.onGround) {
                ++this.life;
                if (this.life >= 1200) {
                    this.remove();
                    return;
                }
            } else {
                this.life = 0;
            }

            float var1 = 0.0F;
            BlockPos var2 = this.blockPosition();
            FluidState var3 = this.level.getFluidState(var2);
            if (var3.is(FluidTags.WATER)) {
                var1 = var3.getHeight(this.level, var2);
            }

            boolean var4 = var1 > 0.0F;
            if (this.currentState == FishingHook.FishHookState.FLYING) {
                if (this.hookedIn != null) {
                    this.setDeltaMovement(Vec3.ZERO);
                    this.currentState = FishingHook.FishHookState.HOOKED_IN_ENTITY;
                    return;
                }

                if (var4) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.2, 0.3));
                    this.currentState = FishingHook.FishHookState.BOBBING;
                    return;
                }

                this.checkCollision();
            } else {
                if (this.currentState == FishingHook.FishHookState.HOOKED_IN_ENTITY) {
                    if (this.hookedIn != null) {
                        if (this.hookedIn.removed) {
                            this.hookedIn = null;
                            this.currentState = FishingHook.FishHookState.FLYING;
                        } else {
                            this.setPos(this.hookedIn.getX(), this.hookedIn.getY(0.8), this.hookedIn.getZ());
                        }
                    }

                    return;
                }

                if (this.currentState == FishingHook.FishHookState.BOBBING) {
                    Vec3 var5 = this.getDeltaMovement();
                    double var6 = this.getY() + var5.y - (double)var2.getY() - (double)var1;
                    if (Math.abs(var6) < 0.01) {
                        var6 += Math.signum(var6) * 0.1;
                    }

                    this.setDeltaMovement(var5.x * 0.9, var5.y - var6 * (double)this.random.nextFloat() * 0.2, var5.z * 0.9);
                    if (this.nibble <= 0 && this.timeUntilHooked <= 0) {
                        this.openWater = true;
                    } else {
                        this.openWater = this.openWater && this.outOfWaterTime < 10 && this.calculateOpenWater(var2);
                    }

                    if (var4) {
                        this.outOfWaterTime = Math.max(0, this.outOfWaterTime - 1);
                        if (this.biting) {
                            this.setDeltaMovement(
                                this.getDeltaMovement()
                                    .add(0.0, -0.1 * (double)this.syncronizedRandom.nextFloat() * (double)this.syncronizedRandom.nextFloat(), 0.0)
                            );
                        }

                        if (!this.level.isClientSide) {
                            this.catchingFish(var2);
                        }
                    } else {
                        this.outOfWaterTime = Math.min(10, this.outOfWaterTime + 1);
                    }
                }
            }

            if (!var3.is(FluidTags.WATER)) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            this.updateRotation();
            if (this.currentState == FishingHook.FishHookState.FLYING && (this.onGround || this.horizontalCollision)) {
                this.setDeltaMovement(Vec3.ZERO);
            }

            double var7 = 0.92;
            this.setDeltaMovement(this.getDeltaMovement().scale(0.92));
            this.reapplyPosition();
        }
    }

    private boolean shouldStopFishing(Player param0) {
        ItemStack var0 = param0.getMainHandItem();
        ItemStack var1 = param0.getOffhandItem();
        boolean var2 = var0.getItem() == Items.FISHING_ROD;
        boolean var3 = var1.getItem() == Items.FISHING_ROD;
        if (!param0.removed && param0.isAlive() && (var2 || var3) && !(this.distanceToSqr(param0) > 1024.0)) {
            return false;
        } else {
            this.remove();
            return true;
        }
    }

    private void checkCollision() {
        HitResult var0 = ProjectileUtil.getHitResult(this, this::canHitEntity);
        this.onHit(var0);
    }

    @Override
    protected boolean canHitEntity(Entity param0) {
        return super.canHitEntity(param0) || param0.isAlive() && param0 instanceof ItemEntity;
    }

    @Override
    protected void onHitEntity(EntityHitResult param0) {
        super.onHitEntity(param0);
        if (!this.level.isClientSide) {
            this.hookedIn = param0.getEntity();
            this.setHookedEntity();
        }

    }

    @Override
    protected void onHitBlock(BlockHitResult param0) {
        super.onHitBlock(param0);
        this.setDeltaMovement(this.getDeltaMovement().normalize().scale(param0.distanceTo(this)));
    }

    private void setHookedEntity() {
        this.getEntityData().set(DATA_HOOKED_ENTITY, this.hookedIn.getId() + 1);
    }

    private void catchingFish(BlockPos param0) {
        ServerLevel var0 = (ServerLevel)this.level;
        int var1 = 1;
        BlockPos var2 = param0.above();
        if (this.random.nextFloat() < 0.25F && this.level.isRainingAt(var2)) {
            ++var1;
        }

        if (this.random.nextFloat() < 0.5F && !this.level.canSeeSky(var2)) {
            --var1;
        }

        if (this.nibble > 0) {
            --this.nibble;
            if (this.nibble <= 0) {
                this.timeUntilLured = 0;
                this.timeUntilHooked = 0;
                this.getEntityData().set(DATA_BITING, false);
            }
        } else if (this.timeUntilHooked > 0) {
            this.timeUntilHooked -= var1;
            if (this.timeUntilHooked > 0) {
                this.fishAngle = (float)((double)this.fishAngle + this.random.nextGaussian() * 4.0);
                float var3 = this.fishAngle * (float) (Math.PI / 180.0);
                float var4 = Mth.sin(var3);
                float var5 = Mth.cos(var3);
                double var6 = this.getX() + (double)(var4 * (float)this.timeUntilHooked * 0.1F);
                double var7 = (double)((float)Mth.floor(this.getY()) + 1.0F);
                double var8 = this.getZ() + (double)(var5 * (float)this.timeUntilHooked * 0.1F);
                BlockState var9 = var0.getBlockState(new BlockPos(var6, var7 - 1.0, var8));
                if (var9.is(Blocks.WATER)) {
                    if (this.random.nextFloat() < 0.15F) {
                        var0.sendParticles(ParticleTypes.BUBBLE, var6, var7 - 0.1F, var8, 1, (double)var4, 0.1, (double)var5, 0.0);
                    }

                    float var10 = var4 * 0.04F;
                    float var11 = var5 * 0.04F;
                    var0.sendParticles(ParticleTypes.FISHING, var6, var7, var8, 0, (double)var11, 0.01, (double)(-var10), 1.0);
                    var0.sendParticles(ParticleTypes.FISHING, var6, var7, var8, 0, (double)(-var11), 0.01, (double)var10, 1.0);
                }
            } else {
                this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                double var12 = this.getY() + 0.5;
                var0.sendParticles(
                    ParticleTypes.BUBBLE,
                    this.getX(),
                    var12,
                    this.getZ(),
                    (int)(1.0F + this.getBbWidth() * 20.0F),
                    (double)this.getBbWidth(),
                    0.0,
                    (double)this.getBbWidth(),
                    0.2F
                );
                var0.sendParticles(
                    ParticleTypes.FISHING,
                    this.getX(),
                    var12,
                    this.getZ(),
                    (int)(1.0F + this.getBbWidth() * 20.0F),
                    (double)this.getBbWidth(),
                    0.0,
                    (double)this.getBbWidth(),
                    0.2F
                );
                this.nibble = Mth.nextInt(this.random, 20, 40);
                this.getEntityData().set(DATA_BITING, true);
            }
        } else if (this.timeUntilLured > 0) {
            this.timeUntilLured -= var1;
            float var13 = 0.15F;
            if (this.timeUntilLured < 20) {
                var13 = (float)((double)var13 + (double)(20 - this.timeUntilLured) * 0.05);
            } else if (this.timeUntilLured < 40) {
                var13 = (float)((double)var13 + (double)(40 - this.timeUntilLured) * 0.02);
            } else if (this.timeUntilLured < 60) {
                var13 = (float)((double)var13 + (double)(60 - this.timeUntilLured) * 0.01);
            }

            if (this.random.nextFloat() < var13) {
                float var14 = Mth.nextFloat(this.random, 0.0F, 360.0F) * (float) (Math.PI / 180.0);
                float var15 = Mth.nextFloat(this.random, 25.0F, 60.0F);
                double var16 = this.getX() + (double)(Mth.sin(var14) * var15 * 0.1F);
                double var17 = (double)((float)Mth.floor(this.getY()) + 1.0F);
                double var18 = this.getZ() + (double)(Mth.cos(var14) * var15 * 0.1F);
                BlockState var19 = var0.getBlockState(new BlockPos(var16, var17 - 1.0, var18));
                if (var19.is(Blocks.WATER)) {
                    var0.sendParticles(ParticleTypes.SPLASH, var16, var17, var18, 2 + this.random.nextInt(2), 0.1F, 0.0, 0.1F, 0.0);
                }
            }

            if (this.timeUntilLured <= 0) {
                this.fishAngle = Mth.nextFloat(this.random, 0.0F, 360.0F);
                this.timeUntilHooked = Mth.nextInt(this.random, 20, 80);
            }
        } else {
            this.timeUntilLured = Mth.nextInt(this.random, 100, 600);
            this.timeUntilLured -= this.lureSpeed * 20 * 5;
        }

    }

    private boolean calculateOpenWater(BlockPos param0) {
        FishingHook.OpenWaterType var0 = FishingHook.OpenWaterType.INVALID;

        for(int var1 = -1; var1 <= 2; ++var1) {
            FishingHook.OpenWaterType var2 = this.getOpenWaterTypeForArea(param0.offset(-2, var1, -2), param0.offset(2, var1, 2));
            switch(var2) {
                case INVALID:
                    return false;
                case ABOVE_WATER:
                    if (var0 == FishingHook.OpenWaterType.INVALID) {
                        return false;
                    }
                    break;
                case INSIDE_WATER:
                    if (var0 == FishingHook.OpenWaterType.ABOVE_WATER) {
                        return false;
                    }
            }

            var0 = var2;
        }

        return true;
    }

    private FishingHook.OpenWaterType getOpenWaterTypeForArea(BlockPos param0, BlockPos param1) {
        return BlockPos.betweenClosedStream(param0, param1)
            .map(this::getOpenWaterTypeForBlock)
            .reduce((param0x, param1x) -> param0x == param1x ? param0x : FishingHook.OpenWaterType.INVALID)
            .orElse(FishingHook.OpenWaterType.INVALID);
    }

    private FishingHook.OpenWaterType getOpenWaterTypeForBlock(BlockPos param0x) {
        BlockState var0 = this.level.getBlockState(param0x);
        if (!var0.isAir() && !var0.is(Blocks.LILY_PAD)) {
            FluidState var1 = var0.getFluidState();
            return var1.is(FluidTags.WATER) && var1.isSource() && var0.getCollisionShape(this.level, param0x).isEmpty()
                ? FishingHook.OpenWaterType.INSIDE_WATER
                : FishingHook.OpenWaterType.INVALID;
        } else {
            return FishingHook.OpenWaterType.ABOVE_WATER;
        }
    }

    public boolean isOpenWaterFishing() {
        return this.openWater;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
    }

    public int retrieve(ItemStack param0) {
        Player var0 = this.getPlayerOwner();
        if (!this.level.isClientSide && var0 != null) {
            int var1 = 0;
            if (this.hookedIn != null) {
                this.bringInHookedEntity();
                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)var0, param0, this, Collections.emptyList());
                this.level.broadcastEntityEvent(this, (byte)31);
                var1 = this.hookedIn instanceof ItemEntity ? 3 : 5;
            } else if (this.nibble > 0) {
                LootContext.Builder var2 = new LootContext.Builder((ServerLevel)this.level)
                    .withParameter(LootContextParams.ORIGIN, this.position())
                    .withParameter(LootContextParams.TOOL, param0)
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withRandom(this.random)
                    .withLuck((float)this.luck + var0.getLuck());
                LootTable var3 = this.level.getServer().getLootTables().get(BuiltInLootTables.FISHING);
                List<ItemStack> var4 = var3.getRandomItems(var2.create(LootContextParamSets.FISHING));
                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)var0, param0, this, var4);

                for(ItemStack var5 : var4) {
                    ItemEntity var6 = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), var5);
                    double var7 = var0.getX() - this.getX();
                    double var8 = var0.getY() - this.getY();
                    double var9 = var0.getZ() - this.getZ();
                    double var10 = 0.1;
                    var6.setDeltaMovement(var7 * 0.1, var8 * 0.1 + Math.sqrt(Math.sqrt(var7 * var7 + var8 * var8 + var9 * var9)) * 0.08, var9 * 0.1);
                    this.level.addFreshEntity(var6);
                    var0.level.addFreshEntity(new ExperienceOrb(var0.level, var0.getX(), var0.getY() + 0.5, var0.getZ() + 0.5, this.random.nextInt(6) + 1));
                    if (var5.getItem().is(ItemTags.FISHES)) {
                        var0.awardStat(Stats.FISH_CAUGHT, 1);
                    }
                }

                var1 = 1;
            }

            if (this.onGround) {
                var1 = 2;
            }

            this.remove();
            return var1;
        } else {
            return 0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 31 && this.level.isClientSide && this.hookedIn instanceof Player && ((Player)this.hookedIn).isLocalPlayer()) {
            this.bringInHookedEntity();
        }

        super.handleEntityEvent(param0);
    }

    protected void bringInHookedEntity() {
        Entity var0 = this.getOwner();
        if (var0 != null) {
            Vec3 var1 = new Vec3(var0.getX() - this.getX(), var0.getY() - this.getY(), var0.getZ() - this.getZ()).scale(0.1);
            this.hookedIn.setDeltaMovement(this.hookedIn.getDeltaMovement().add(var1));
        }
    }

    @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    public void remove() {
        super.remove();
        Player var0 = this.getPlayerOwner();
        if (var0 != null) {
            var0.fishing = null;
        }

    }

    @Nullable
    public Player getPlayerOwner() {
        Entity var0 = this.getOwner();
        return var0 instanceof Player ? (Player)var0 : null;
    }

    @Nullable
    public Entity getHookedIn() {
        return this.hookedIn;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        Entity var0 = this.getOwner();
        return new ClientboundAddEntityPacket(this, var0 == null ? this.getId() : var0.getId());
    }

    static enum FishHookState {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING;
    }

    static enum OpenWaterType {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID;
    }
}
