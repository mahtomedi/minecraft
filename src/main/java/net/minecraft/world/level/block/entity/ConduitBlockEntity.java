package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ConduitBlockEntity extends BlockEntity implements TickableBlockEntity {
    private static final Block[] VALID_BLOCKS = new Block[]{Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.SEA_LANTERN, Blocks.DARK_PRISMARINE};
    public int tickCount;
    private float activeRotation;
    private boolean isActive;
    private boolean isHunting;
    private final List<BlockPos> effectBlocks = Lists.newArrayList();
    @Nullable
    private LivingEntity destroyTarget;
    @Nullable
    private UUID destroyTargetUUID;
    private long nextAmbientSoundActivation;

    public ConduitBlockEntity() {
        this(BlockEntityType.CONDUIT);
    }

    public ConduitBlockEntity(BlockEntityType<?> param0) {
        super(param0);
    }

    @Override
    public void load(BlockState param0, CompoundTag param1) {
        super.load(param0, param1);
        if (param1.hasUUID("Target")) {
            this.destroyTargetUUID = param1.getUUID("Target");
        } else {
            this.destroyTargetUUID = null;
        }

    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        if (this.destroyTarget != null) {
            param0.putUUID("Target", this.destroyTarget.getUUID());
        }

        return param0;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 5, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    @Override
    public void tick() {
        ++this.tickCount;
        long var0 = this.level.getGameTime();
        if (var0 % 40L == 0L) {
            this.setActive(this.updateShape());
            if (!this.level.isClientSide && this.isActive()) {
                this.applyEffects();
                this.updateDestroyTarget();
            }
        }

        if (var0 % 80L == 0L && this.isActive()) {
            this.playSound(SoundEvents.CONDUIT_AMBIENT);
        }

        if (var0 > this.nextAmbientSoundActivation && this.isActive()) {
            this.nextAmbientSoundActivation = var0 + 60L + (long)this.level.getRandom().nextInt(40);
            this.playSound(SoundEvents.CONDUIT_AMBIENT_SHORT);
        }

        if (this.level.isClientSide) {
            this.updateClientTarget();
            this.animationTick();
            if (this.isActive()) {
                ++this.activeRotation;
            }
        }

    }

    private boolean updateShape() {
        this.effectBlocks.clear();

        for(int var0 = -1; var0 <= 1; ++var0) {
            for(int var1 = -1; var1 <= 1; ++var1) {
                for(int var2 = -1; var2 <= 1; ++var2) {
                    BlockPos var3 = this.worldPosition.offset(var0, var1, var2);
                    if (!this.level.isWaterAt(var3)) {
                        return false;
                    }
                }
            }
        }

        for(int var4 = -2; var4 <= 2; ++var4) {
            for(int var5 = -2; var5 <= 2; ++var5) {
                for(int var6 = -2; var6 <= 2; ++var6) {
                    int var7 = Math.abs(var4);
                    int var8 = Math.abs(var5);
                    int var9 = Math.abs(var6);
                    if ((var7 > 1 || var8 > 1 || var9 > 1)
                        && (var4 == 0 && (var8 == 2 || var9 == 2) || var5 == 0 && (var7 == 2 || var9 == 2) || var6 == 0 && (var7 == 2 || var8 == 2))) {
                        BlockPos var10 = this.worldPosition.offset(var4, var5, var6);
                        BlockState var11 = this.level.getBlockState(var10);

                        for(Block var12 : VALID_BLOCKS) {
                            if (var11.getBlock() == var12) {
                                this.effectBlocks.add(var10);
                            }
                        }
                    }
                }
            }
        }

        this.setHunting(this.effectBlocks.size() >= 42);
        return this.effectBlocks.size() >= 16;
    }

    private void applyEffects() {
        int var0 = this.effectBlocks.size();
        int var1 = var0 / 7 * 16;
        int var2 = this.worldPosition.getX();
        int var3 = this.worldPosition.getY();
        int var4 = this.worldPosition.getZ();
        AABB var5 = new AABB((double)var2, (double)var3, (double)var4, (double)(var2 + 1), (double)(var3 + 1), (double)(var4 + 1))
            .inflate((double)var1)
            .expandTowards(0.0, (double)this.level.getMaxBuildHeight(), 0.0);
        List<Player> var6 = this.level.getEntitiesOfClass(Player.class, var5);
        if (!var6.isEmpty()) {
            for(Player var7 : var6) {
                if (this.worldPosition.closerThan(var7.blockPosition(), (double)var1) && var7.isInWaterOrRain()) {
                    var7.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 260, 0, true, true));
                }
            }

        }
    }

    private void updateDestroyTarget() {
        LivingEntity var0 = this.destroyTarget;
        int var1 = this.effectBlocks.size();
        if (var1 < 42) {
            this.destroyTarget = null;
        } else if (this.destroyTarget == null && this.destroyTargetUUID != null) {
            this.destroyTarget = this.findDestroyTarget();
            this.destroyTargetUUID = null;
        } else if (this.destroyTarget == null) {
            List<LivingEntity> var2 = this.level
                .getEntitiesOfClass(LivingEntity.class, this.getDestroyRangeAABB(), param0 -> param0 instanceof Enemy && param0.isInWaterOrRain());
            if (!var2.isEmpty()) {
                this.destroyTarget = var2.get(this.level.random.nextInt(var2.size()));
            }
        } else if (!this.destroyTarget.isAlive() || !this.worldPosition.closerThan(this.destroyTarget.blockPosition(), 8.0)) {
            this.destroyTarget = null;
        }

        if (this.destroyTarget != null) {
            this.level
                .playSound(
                    null,
                    this.destroyTarget.getX(),
                    this.destroyTarget.getY(),
                    this.destroyTarget.getZ(),
                    SoundEvents.CONDUIT_ATTACK_TARGET,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
                );
            this.destroyTarget.hurt(DamageSource.MAGIC, 4.0F);
        }

        if (var0 != this.destroyTarget) {
            BlockState var3 = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, var3, var3, 2);
        }

    }

    private void updateClientTarget() {
        if (this.destroyTargetUUID == null) {
            this.destroyTarget = null;
        } else if (this.destroyTarget == null || !this.destroyTarget.getUUID().equals(this.destroyTargetUUID)) {
            this.destroyTarget = this.findDestroyTarget();
            if (this.destroyTarget == null) {
                this.destroyTargetUUID = null;
            }
        }

    }

    private AABB getDestroyRangeAABB() {
        int var0 = this.worldPosition.getX();
        int var1 = this.worldPosition.getY();
        int var2 = this.worldPosition.getZ();
        return new AABB((double)var0, (double)var1, (double)var2, (double)(var0 + 1), (double)(var1 + 1), (double)(var2 + 1)).inflate(8.0);
    }

    @Nullable
    private LivingEntity findDestroyTarget() {
        List<LivingEntity> var0 = this.level
            .getEntitiesOfClass(LivingEntity.class, this.getDestroyRangeAABB(), param0 -> param0.getUUID().equals(this.destroyTargetUUID));
        return var0.size() == 1 ? var0.get(0) : null;
    }

    private void animationTick() {
        Random var0 = this.level.random;
        double var1 = (double)(Mth.sin((float)(this.tickCount + 35) * 0.1F) / 2.0F + 0.5F);
        var1 = (var1 * var1 + var1) * 0.3F;
        Vec3 var2 = new Vec3((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 1.5 + var1, (double)this.worldPosition.getZ() + 0.5);

        for(BlockPos var3 : this.effectBlocks) {
            if (var0.nextInt(50) == 0) {
                float var4 = -0.5F + var0.nextFloat();
                float var5 = -2.0F + var0.nextFloat();
                float var6 = -0.5F + var0.nextFloat();
                BlockPos var7 = var3.subtract(this.worldPosition);
                Vec3 var8 = new Vec3((double)var4, (double)var5, (double)var6).add((double)var7.getX(), (double)var7.getY(), (double)var7.getZ());
                this.level.addParticle(ParticleTypes.NAUTILUS, var2.x, var2.y, var2.z, var8.x, var8.y, var8.z);
            }
        }

        if (this.destroyTarget != null) {
            Vec3 var9 = new Vec3(this.destroyTarget.getX(), this.destroyTarget.getEyeY(), this.destroyTarget.getZ());
            float var10 = (-0.5F + var0.nextFloat()) * (3.0F + this.destroyTarget.getBbWidth());
            float var11 = -1.0F + var0.nextFloat() * this.destroyTarget.getBbHeight();
            float var12 = (-0.5F + var0.nextFloat()) * (3.0F + this.destroyTarget.getBbWidth());
            Vec3 var13 = new Vec3((double)var10, (double)var11, (double)var12);
            this.level.addParticle(ParticleTypes.NAUTILUS, var9.x, var9.y, var9.z, var13.x, var13.y, var13.z);
        }

    }

    public boolean isActive() {
        return this.isActive;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isHunting() {
        return this.isHunting;
    }

    private void setActive(boolean param0) {
        if (param0 != this.isActive) {
            this.playSound(param0 ? SoundEvents.CONDUIT_ACTIVATE : SoundEvents.CONDUIT_DEACTIVATE);
        }

        this.isActive = param0;
    }

    private void setHunting(boolean param0) {
        this.isHunting = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public float getActiveRotation(float param0) {
        return (this.activeRotation + param0) * -0.0375F;
    }

    public void playSound(SoundEvent param0) {
        this.level.playSound(null, this.worldPosition, param0, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
