package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ConduitBlockEntity extends BlockEntity {
    private static final int BLOCK_REFRESH_RATE = 2;
    private static final int EFFECT_DURATION = 13;
    private static final float ROTATION_SPEED = -0.0375F;
    private static final int MIN_ACTIVE_SIZE = 16;
    private static final int MIN_KILL_SIZE = 42;
    private static final int KILL_RANGE = 8;
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

    public ConduitBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.CONDUIT, param0, param1);
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        if (param0.hasUUID("Target")) {
            this.destroyTargetUUID = param0.getUUID("Target");
        } else {
            this.destroyTargetUUID = null;
        }

    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        if (this.destroyTarget != null) {
            param0.putUUID("Target", this.destroyTarget.getUUID());
        }

    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public static void clientTick(Level param0, BlockPos param1, BlockState param2, ConduitBlockEntity param3) {
        ++param3.tickCount;
        long var0 = param0.getGameTime();
        List<BlockPos> var1 = param3.effectBlocks;
        if (var0 % 40L == 0L) {
            param3.isActive = updateShape(param0, param1, var1);
            updateHunting(param3, var1);
        }

        updateClientTarget(param0, param1, param3);
        animationTick(param0, param1, var1, param3.destroyTarget, param3.tickCount);
        if (param3.isActive()) {
            ++param3.activeRotation;
        }

    }

    public static void serverTick(Level param0, BlockPos param1, BlockState param2, ConduitBlockEntity param3) {
        ++param3.tickCount;
        long var0 = param0.getGameTime();
        List<BlockPos> var1 = param3.effectBlocks;
        if (var0 % 40L == 0L) {
            boolean var2 = updateShape(param0, param1, var1);
            if (var2 != param3.isActive) {
                SoundEvent var3 = var2 ? SoundEvents.CONDUIT_ACTIVATE : SoundEvents.CONDUIT_DEACTIVATE;
                param0.playSound(null, param1, var3, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            param3.isActive = var2;
            updateHunting(param3, var1);
            if (var2) {
                applyEffects(param0, param1, var1);
                updateDestroyTarget(param0, param1, param2, var1, param3);
            }
        }

        if (param3.isActive()) {
            if (var0 % 80L == 0L) {
                param0.playSound(null, param1, SoundEvents.CONDUIT_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            if (var0 > param3.nextAmbientSoundActivation) {
                param3.nextAmbientSoundActivation = var0 + 60L + (long)param0.getRandom().nextInt(40);
                param0.playSound(null, param1, SoundEvents.CONDUIT_AMBIENT_SHORT, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }

    }

    private static void updateHunting(ConduitBlockEntity param0, List<BlockPos> param1) {
        param0.setHunting(param1.size() >= 42);
    }

    private static boolean updateShape(Level param0, BlockPos param1, List<BlockPos> param2) {
        param2.clear();

        for(int var0 = -1; var0 <= 1; ++var0) {
            for(int var1 = -1; var1 <= 1; ++var1) {
                for(int var2 = -1; var2 <= 1; ++var2) {
                    BlockPos var3 = param1.offset(var0, var1, var2);
                    if (!param0.isWaterAt(var3)) {
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
                        BlockPos var10 = param1.offset(var4, var5, var6);
                        BlockState var11 = param0.getBlockState(var10);

                        for(Block var12 : VALID_BLOCKS) {
                            if (var11.is(var12)) {
                                param2.add(var10);
                            }
                        }
                    }
                }
            }
        }

        return param2.size() >= 16;
    }

    private static void applyEffects(Level param0, BlockPos param1, List<BlockPos> param2) {
        int var0 = param2.size();
        int var1 = var0 / 7 * 16;
        int var2 = param1.getX();
        int var3 = param1.getY();
        int var4 = param1.getZ();
        AABB var5 = new AABB((double)var2, (double)var3, (double)var4, (double)(var2 + 1), (double)(var3 + 1), (double)(var4 + 1))
            .inflate((double)var1)
            .expandTowards(0.0, (double)param0.getHeight(), 0.0);
        List<Player> var6 = param0.getEntitiesOfClass(Player.class, var5);
        if (!var6.isEmpty()) {
            for(Player var7 : var6) {
                if (param1.closerThan(var7.blockPosition(), (double)var1) && var7.isInWaterOrRain()) {
                    var7.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 260, 0, true, true));
                }
            }

        }
    }

    private static void updateDestroyTarget(Level param0, BlockPos param1, BlockState param2, List<BlockPos> param3, ConduitBlockEntity param4) {
        LivingEntity var0 = param4.destroyTarget;
        int var1 = param3.size();
        if (var1 < 42) {
            param4.destroyTarget = null;
        } else if (param4.destroyTarget == null && param4.destroyTargetUUID != null) {
            param4.destroyTarget = findDestroyTarget(param0, param1, param4.destroyTargetUUID);
            param4.destroyTargetUUID = null;
        } else if (param4.destroyTarget == null) {
            List<LivingEntity> var2 = param0.getEntitiesOfClass(
                LivingEntity.class, getDestroyRangeAABB(param1), param0x -> param0x instanceof Enemy && param0x.isInWaterOrRain()
            );
            if (!var2.isEmpty()) {
                param4.destroyTarget = var2.get(param0.random.nextInt(var2.size()));
            }
        } else if (!param4.destroyTarget.isAlive() || !param1.closerThan(param4.destroyTarget.blockPosition(), 8.0)) {
            param4.destroyTarget = null;
        }

        if (param4.destroyTarget != null) {
            param0.playSound(
                null,
                param4.destroyTarget.getX(),
                param4.destroyTarget.getY(),
                param4.destroyTarget.getZ(),
                SoundEvents.CONDUIT_ATTACK_TARGET,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
            );
            param4.destroyTarget.hurt(DamageSource.MAGIC, 4.0F);
        }

        if (var0 != param4.destroyTarget) {
            param0.sendBlockUpdated(param1, param2, param2, 2);
        }

    }

    private static void updateClientTarget(Level param0, BlockPos param1, ConduitBlockEntity param2) {
        if (param2.destroyTargetUUID == null) {
            param2.destroyTarget = null;
        } else if (param2.destroyTarget == null || !param2.destroyTarget.getUUID().equals(param2.destroyTargetUUID)) {
            param2.destroyTarget = findDestroyTarget(param0, param1, param2.destroyTargetUUID);
            if (param2.destroyTarget == null) {
                param2.destroyTargetUUID = null;
            }
        }

    }

    private static AABB getDestroyRangeAABB(BlockPos param0) {
        int var0 = param0.getX();
        int var1 = param0.getY();
        int var2 = param0.getZ();
        return new AABB((double)var0, (double)var1, (double)var2, (double)(var0 + 1), (double)(var1 + 1), (double)(var2 + 1)).inflate(8.0);
    }

    @Nullable
    private static LivingEntity findDestroyTarget(Level param0, BlockPos param1, UUID param2) {
        List<LivingEntity> var0 = param0.getEntitiesOfClass(LivingEntity.class, getDestroyRangeAABB(param1), param1x -> param1x.getUUID().equals(param2));
        return var0.size() == 1 ? var0.get(0) : null;
    }

    private static void animationTick(Level param0, BlockPos param1, List<BlockPos> param2, @Nullable Entity param3, int param4) {
        RandomSource var0 = param0.random;
        double var1 = (double)(Mth.sin((float)(param4 + 35) * 0.1F) / 2.0F + 0.5F);
        var1 = (var1 * var1 + var1) * 0.3F;
        Vec3 var2 = new Vec3((double)param1.getX() + 0.5, (double)param1.getY() + 1.5 + var1, (double)param1.getZ() + 0.5);

        for(BlockPos var3 : param2) {
            if (var0.nextInt(50) == 0) {
                BlockPos var4 = var3.subtract(param1);
                float var5 = -0.5F + var0.nextFloat() + (float)var4.getX();
                float var6 = -2.0F + var0.nextFloat() + (float)var4.getY();
                float var7 = -0.5F + var0.nextFloat() + (float)var4.getZ();
                param0.addParticle(ParticleTypes.NAUTILUS, var2.x, var2.y, var2.z, (double)var5, (double)var6, (double)var7);
            }
        }

        if (param3 != null) {
            Vec3 var8 = new Vec3(param3.getX(), param3.getEyeY(), param3.getZ());
            float var9 = (-0.5F + var0.nextFloat()) * (3.0F + param3.getBbWidth());
            float var10 = -1.0F + var0.nextFloat() * param3.getBbHeight();
            float var11 = (-0.5F + var0.nextFloat()) * (3.0F + param3.getBbWidth());
            Vec3 var12 = new Vec3((double)var9, (double)var10, (double)var11);
            param0.addParticle(ParticleTypes.NAUTILUS, var8.x, var8.y, var8.z, var12.x, var12.y, var12.z);
        }

    }

    public boolean isActive() {
        return this.isActive;
    }

    public boolean isHunting() {
        return this.isHunting;
    }

    private void setHunting(boolean param0) {
        this.isHunting = param0;
    }

    public float getActiveRotation(float param0) {
        return (this.activeRotation + param0) * -0.0375F;
    }
}
