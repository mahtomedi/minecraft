package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class BellBlockEntity extends BlockEntity implements TickableBlockEntity {
    private long lastRingTimestamp;
    public int ticks;
    public boolean shaking;
    public Direction clickDirection;
    private List<LivingEntity> nearbyEntities;
    private boolean resonating;
    private int resonationTicks;

    public BellBlockEntity() {
        super(BlockEntityType.BELL);
    }

    @Override
    public boolean triggerEvent(int param0, int param1) {
        if (param0 == 1) {
            this.updateEntities();
            this.resonationTicks = 0;
            this.clickDirection = Direction.from3DDataValue(param1);
            this.ticks = 0;
            this.shaking = true;
            return true;
        } else {
            return super.triggerEvent(param0, param1);
        }
    }

    @Override
    public void tick() {
        if (this.shaking) {
            ++this.ticks;
        }

        if (this.ticks >= 50) {
            this.shaking = false;
            this.ticks = 0;
        }

        if (this.ticks >= 5 && this.resonationTicks == 0 && this.areRaidersNearby()) {
            this.resonating = true;
            this.playResonateSound();
        }

        if (this.resonating) {
            if (this.resonationTicks < 40) {
                ++this.resonationTicks;
            } else {
                this.makeRaidersGlow(this.level);
                this.showBellParticles(this.level);
                this.resonating = false;
            }
        }

    }

    private void playResonateSound() {
        this.level.playSound(null, this.getBlockPos(), SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public void onHit(Direction param0) {
        BlockPos var0 = this.getBlockPos();
        this.clickDirection = param0;
        if (this.shaking) {
            this.ticks = 0;
        } else {
            this.shaking = true;
        }

        this.level.blockEvent(var0, this.getBlockState().getBlock(), 1, param0.get3DDataValue());
    }

    private void updateEntities() {
        BlockPos var0 = this.getBlockPos();
        if (this.level.getGameTime() > this.lastRingTimestamp + 60L || this.nearbyEntities == null) {
            this.lastRingTimestamp = this.level.getGameTime();
            AABB var1 = new AABB(var0).inflate(48.0);
            this.nearbyEntities = this.level.getEntitiesOfClass(LivingEntity.class, var1);
        }

        if (!this.level.isClientSide) {
            for(LivingEntity var2 : this.nearbyEntities) {
                if (var2.isAlive() && !var2.removed && var0.closerThan(var2.position(), 32.0)) {
                    var2.getBrain().setMemory(MemoryModuleType.HEARD_BELL_TIME, this.level.getGameTime());
                }
            }
        }

    }

    private boolean areRaidersNearby() {
        BlockPos var0 = this.getBlockPos();

        for(LivingEntity var1 : this.nearbyEntities) {
            if (var1.isAlive() && !var1.removed && var0.closerThan(var1.position(), 32.0) && var1.getType().is(EntityTypeTags.RAIDERS)) {
                return true;
            }
        }

        return false;
    }

    private void makeRaidersGlow(Level param0) {
        if (!param0.isClientSide) {
            this.nearbyEntities.stream().filter(this::isRaiderWithinRange).forEach(this::glow);
        }
    }

    private void showBellParticles(Level param0) {
        if (param0.isClientSide) {
            BlockPos var0 = this.getBlockPos();
            AtomicInteger var1 = new AtomicInteger(16700985);
            int var2 = (int)this.nearbyEntities.stream().filter(param1 -> var0.closerThan(param1.position(), 48.0)).count();
            this.nearbyEntities
                .stream()
                .filter(this::isRaiderWithinRange)
                .forEach(
                    param4 -> {
                        float var0x = 1.0F;
                        float var1x = Mth.sqrt(
                            (param4.getX() - (double)var0.getX()) * (param4.getX() - (double)var0.getX())
                                + (param4.getZ() - (double)var0.getZ()) * (param4.getZ() - (double)var0.getZ())
                        );
                        double var2x = (double)((float)var0.getX() + 0.5F) + (double)(1.0F / var1x) * (param4.getX() - (double)var0.getX());
                        double var3x = (double)((float)var0.getZ() + 0.5F) + (double)(1.0F / var1x) * (param4.getZ() - (double)var0.getZ());
                        int var4x = Mth.clamp((var2 - 21) / -2, 3, 15);
        
                        for(int var5 = 0; var5 < var4x; ++var5) {
                            var1.addAndGet(5);
                            double var6 = (double)(var1.get() >> 16 & 0xFF) / 255.0;
                            double var7 = (double)(var1.get() >> 8 & 0xFF) / 255.0;
                            double var8 = (double)(var1.get() & 0xFF) / 255.0;
                            param0.addParticle(ParticleTypes.ENTITY_EFFECT, var2x, (double)((float)var0.getY() + 0.5F), var3x, var6, var7, var8);
                        }
        
                    }
                );
        }
    }

    private boolean isRaiderWithinRange(LivingEntity param0x) {
        return param0x.isAlive() && !param0x.removed && this.getBlockPos().closerThan(param0x.position(), 48.0) && param0x.getType().is(EntityTypeTags.RAIDERS);
    }

    private void glow(LivingEntity param0x) {
        param0x.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60));
    }
}
