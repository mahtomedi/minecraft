package net.minecraft.world.entity.fishing;

import java.util.Collections;
import java.util.List;
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
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FishingHook extends Entity {
    private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.INT);
    private boolean inGround;
    private int life;
    private final Player owner;
    private int flightTime;
    private int nibble;
    private int timeUntilLured;
    private int timeUntilHooked;
    private float fishAngle;
    public Entity hookedIn;
    private FishingHook.FishHookState currentState = FishingHook.FishHookState.FLYING;
    private final int luck;
    private final int lureSpeed;

    private FishingHook(Level param0, Player param1, int param2, int param3) {
        super(EntityType.FISHING_BOBBER, param0);
        this.noCulling = true;
        this.owner = param1;
        this.owner.fishing = this;
        this.luck = Math.max(0, param2);
        this.lureSpeed = Math.max(0, param3);
    }

    @OnlyIn(Dist.CLIENT)
    public FishingHook(Level param0, Player param1, double param2, double param3, double param4) {
        this(param0, param1, 0, 0);
        this.setPos(param2, param3, param4);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    public FishingHook(Player param0, Level param1, int param2, int param3) {
        this(param1, param0, param2, param3);
        float var0 = this.owner.xRot;
        float var1 = this.owner.yRot;
        float var2 = Mth.cos(-var1 * (float) (Math.PI / 180.0) - (float) Math.PI);
        float var3 = Mth.sin(-var1 * (float) (Math.PI / 180.0) - (float) Math.PI);
        float var4 = -Mth.cos(-var0 * (float) (Math.PI / 180.0));
        float var5 = Mth.sin(-var0 * (float) (Math.PI / 180.0));
        double var6 = this.owner.x - (double)var3 * 0.3;
        double var7 = this.owner.y + (double)this.owner.getEyeHeight();
        double var8 = this.owner.z - (double)var2 * 0.3;
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
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_HOOKED_ENTITY.equals(param0)) {
            int var0 = this.getEntityData().get(DATA_HOOKED_ENTITY);
            this.hookedIn = var0 > 0 ? this.level.getEntity(var0 - 1) : null;
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
        super.tick();
        if (this.owner == null) {
            this.remove();
        } else if (this.level.isClientSide || !this.shouldStopFishing()) {
            if (this.inGround) {
                ++this.life;
                if (this.life >= 1200) {
                    this.remove();
                    return;
                }
            }

            float var0 = 0.0F;
            BlockPos var1 = new BlockPos(this);
            FluidState var2 = this.level.getFluidState(var1);
            if (var2.is(FluidTags.WATER)) {
                var0 = var2.getHeight(this.level, var1);
            }

            if (this.currentState == FishingHook.FishHookState.FLYING) {
                if (this.hookedIn != null) {
                    this.setDeltaMovement(Vec3.ZERO);
                    this.currentState = FishingHook.FishHookState.HOOKED_IN_ENTITY;
                    return;
                }

                if (var0 > 0.0F) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.2, 0.3));
                    this.currentState = FishingHook.FishHookState.BOBBING;
                    return;
                }

                if (!this.level.isClientSide) {
                    this.checkCollision();
                }

                if (!this.inGround && !this.onGround && !this.horizontalCollision) {
                    ++this.flightTime;
                } else {
                    this.flightTime = 0;
                    this.setDeltaMovement(Vec3.ZERO);
                }
            } else {
                if (this.currentState == FishingHook.FishHookState.HOOKED_IN_ENTITY) {
                    if (this.hookedIn != null) {
                        if (this.hookedIn.removed) {
                            this.hookedIn = null;
                            this.currentState = FishingHook.FishHookState.FLYING;
                        } else {
                            this.x = this.hookedIn.x;
                            this.y = this.hookedIn.getBoundingBox().minY + (double)this.hookedIn.getBbHeight() * 0.8;
                            this.z = this.hookedIn.z;
                            this.setPos(this.x, this.y, this.z);
                        }
                    }

                    return;
                }

                if (this.currentState == FishingHook.FishHookState.BOBBING) {
                    Vec3 var3 = this.getDeltaMovement();
                    double var4 = this.y + var3.y - (double)var1.getY() - (double)var0;
                    if (Math.abs(var4) < 0.01) {
                        var4 += Math.signum(var4) * 0.1;
                    }

                    this.setDeltaMovement(var3.x * 0.9, var3.y - var4 * (double)this.random.nextFloat() * 0.2, var3.z * 0.9);
                    if (!this.level.isClientSide && var0 > 0.0F) {
                        this.catchingFish(var1);
                    }
                }
            }

            if (!var2.is(FluidTags.WATER)) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            this.updateRotation();
            double var5 = 0.92;
            this.setDeltaMovement(this.getDeltaMovement().scale(0.92));
            this.setPos(this.x, this.y, this.z);
        }
    }

    private boolean shouldStopFishing() {
        ItemStack var0 = this.owner.getMainHandItem();
        ItemStack var1 = this.owner.getOffhandItem();
        boolean var2 = var0.getItem() == Items.FISHING_ROD;
        boolean var3 = var1.getItem() == Items.FISHING_ROD;
        if (!this.owner.removed && this.owner.isAlive() && (var2 || var3) && !(this.distanceToSqr(this.owner) > 1024.0)) {
            return false;
        } else {
            this.remove();
            return true;
        }
    }

    private void updateRotation() {
        Vec3 var0 = this.getDeltaMovement();
        float var1 = Mth.sqrt(getHorizontalDistanceSqr(var0));
        this.yRot = (float)(Mth.atan2(var0.x, var0.z) * 180.0F / (float)Math.PI);
        this.xRot = (float)(Mth.atan2(var0.y, (double)var1) * 180.0F / (float)Math.PI);

        while(this.xRot - this.xRotO < -180.0F) {
            this.xRotO -= 360.0F;
        }

        while(this.xRot - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }

        while(this.yRot - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }

        while(this.yRot - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }

        this.xRot = Mth.lerp(0.2F, this.xRotO, this.xRot);
        this.yRot = Mth.lerp(0.2F, this.yRotO, this.yRot);
    }

    private void checkCollision() {
        HitResult var0 = ProjectileUtil.getHitResult(
            this,
            this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0),
            param0 -> !param0.isSpectator() && (param0.isPickable() || param0 instanceof ItemEntity) && (param0 != this.owner || this.flightTime >= 5),
            ClipContext.Block.COLLIDER,
            true
        );
        if (var0.getType() != HitResult.Type.MISS) {
            if (var0.getType() == HitResult.Type.ENTITY) {
                this.hookedIn = ((EntityHitResult)var0).getEntity();
                this.setHookedEntity();
            } else {
                this.inGround = true;
            }
        }

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
            } else {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.2 * (double)this.random.nextFloat() * (double)this.random.nextFloat(), 0.0));
            }
        } else if (this.timeUntilHooked > 0) {
            this.timeUntilHooked -= var1;
            if (this.timeUntilHooked > 0) {
                this.fishAngle = (float)((double)this.fishAngle + this.random.nextGaussian() * 4.0);
                float var3 = this.fishAngle * (float) (Math.PI / 180.0);
                float var4 = Mth.sin(var3);
                float var5 = Mth.cos(var3);
                double var6 = this.x + (double)(var4 * (float)this.timeUntilHooked * 0.1F);
                double var7 = (double)((float)Mth.floor(this.getBoundingBox().minY) + 1.0F);
                double var8 = this.z + (double)(var5 * (float)this.timeUntilHooked * 0.1F);
                Block var9 = var0.getBlockState(new BlockPos(var6, var7 - 1.0, var8)).getBlock();
                if (var9 == Blocks.WATER) {
                    if (this.random.nextFloat() < 0.15F) {
                        var0.sendParticles(ParticleTypes.BUBBLE, var6, var7 - 0.1F, var8, 1, (double)var4, 0.1, (double)var5, 0.0);
                    }

                    float var10 = var4 * 0.04F;
                    float var11 = var5 * 0.04F;
                    var0.sendParticles(ParticleTypes.FISHING, var6, var7, var8, 0, (double)var11, 0.01, (double)(-var10), 1.0);
                    var0.sendParticles(ParticleTypes.FISHING, var6, var7, var8, 0, (double)(-var11), 0.01, (double)var10, 1.0);
                }
            } else {
                Vec3 var12 = this.getDeltaMovement();
                this.setDeltaMovement(var12.x, (double)(-0.4F * Mth.nextFloat(this.random, 0.6F, 1.0F)), var12.z);
                this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                double var13 = this.getBoundingBox().minY + 0.5;
                var0.sendParticles(
                    ParticleTypes.BUBBLE,
                    this.x,
                    var13,
                    this.z,
                    (int)(1.0F + this.getBbWidth() * 20.0F),
                    (double)this.getBbWidth(),
                    0.0,
                    (double)this.getBbWidth(),
                    0.2F
                );
                var0.sendParticles(
                    ParticleTypes.FISHING,
                    this.x,
                    var13,
                    this.z,
                    (int)(1.0F + this.getBbWidth() * 20.0F),
                    (double)this.getBbWidth(),
                    0.0,
                    (double)this.getBbWidth(),
                    0.2F
                );
                this.nibble = Mth.nextInt(this.random, 20, 40);
            }
        } else if (this.timeUntilLured > 0) {
            this.timeUntilLured -= var1;
            float var14 = 0.15F;
            if (this.timeUntilLured < 20) {
                var14 = (float)((double)var14 + (double)(20 - this.timeUntilLured) * 0.05);
            } else if (this.timeUntilLured < 40) {
                var14 = (float)((double)var14 + (double)(40 - this.timeUntilLured) * 0.02);
            } else if (this.timeUntilLured < 60) {
                var14 = (float)((double)var14 + (double)(60 - this.timeUntilLured) * 0.01);
            }

            if (this.random.nextFloat() < var14) {
                float var15 = Mth.nextFloat(this.random, 0.0F, 360.0F) * (float) (Math.PI / 180.0);
                float var16 = Mth.nextFloat(this.random, 25.0F, 60.0F);
                double var17 = this.x + (double)(Mth.sin(var15) * var16 * 0.1F);
                double var18 = (double)((float)Mth.floor(this.getBoundingBox().minY) + 1.0F);
                double var19 = this.z + (double)(Mth.cos(var15) * var16 * 0.1F);
                Block var20 = var0.getBlockState(new BlockPos(var17, var18 - 1.0, var19)).getBlock();
                if (var20 == Blocks.WATER) {
                    var0.sendParticles(ParticleTypes.SPLASH, var17, var18, var19, 2 + this.random.nextInt(2), 0.1F, 0.0, 0.1F, 0.0);
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

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
    }

    public int retrieve(ItemStack param0) {
        if (!this.level.isClientSide && this.owner != null) {
            int var0 = 0;
            if (this.hookedIn != null) {
                this.bringInHookedEntity();
                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)this.owner, param0, this, Collections.emptyList());
                this.level.broadcastEntityEvent(this, (byte)31);
                var0 = this.hookedIn instanceof ItemEntity ? 3 : 5;
            } else if (this.nibble > 0) {
                LootContext.Builder var1 = new LootContext.Builder((ServerLevel)this.level)
                    .withParameter(LootContextParams.BLOCK_POS, new BlockPos(this))
                    .withParameter(LootContextParams.TOOL, param0)
                    .withRandom(this.random)
                    .withLuck((float)this.luck + this.owner.getLuck());
                LootTable var2 = this.level.getServer().getLootTables().get(BuiltInLootTables.FISHING);
                List<ItemStack> var3 = var2.getRandomItems(var1.create(LootContextParamSets.FISHING));
                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)this.owner, param0, this, var3);

                for(ItemStack var4 : var3) {
                    ItemEntity var5 = new ItemEntity(this.level, this.x, this.y, this.z, var4);
                    double var6 = this.owner.x - this.x;
                    double var7 = this.owner.y - this.y;
                    double var8 = this.owner.z - this.z;
                    double var9 = 0.1;
                    var5.setDeltaMovement(var6 * 0.1, var7 * 0.1 + Math.sqrt(Math.sqrt(var6 * var6 + var7 * var7 + var8 * var8)) * 0.08, var8 * 0.1);
                    this.level.addFreshEntity(var5);
                    this.owner
                        .level
                        .addFreshEntity(new ExperienceOrb(this.owner.level, this.owner.x, this.owner.y + 0.5, this.owner.z + 0.5, this.random.nextInt(6) + 1));
                    if (var4.getItem().is(ItemTags.FISHES)) {
                        this.owner.awardStat(Stats.FISH_CAUGHT, 1);
                    }
                }

                var0 = 1;
            }

            if (this.inGround) {
                var0 = 2;
            }

            this.remove();
            return var0;
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
        if (this.owner != null) {
            Vec3 var0 = new Vec3(this.owner.x - this.x, this.owner.y - this.y, this.owner.z - this.z).scale(0.1);
            this.hookedIn.setDeltaMovement(this.hookedIn.getDeltaMovement().add(var0));
        }
    }

    @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    public void remove() {
        super.remove();
        if (this.owner != null) {
            this.owner.fishing = null;
        }

    }

    @Nullable
    public Player getOwner() {
        return this.owner;
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
}
