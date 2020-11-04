package net.minecraft.world.level.block.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.mutable.MutableInt;

public class BellBlockEntity extends BlockEntity {
    private long lastRingTimestamp;
    public int ticks;
    public boolean shaking;
    public Direction clickDirection;
    private List<LivingEntity> nearbyEntities;
    private boolean resonating;
    private int resonationTicks;

    public BellBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.BELL, param0, param1);
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

    private static void tick(Level param0, BlockPos param1, BlockState param2, BellBlockEntity param3, BellBlockEntity.ResonationEndAction param4) {
        if (param3.shaking) {
            ++param3.ticks;
        }

        if (param3.ticks >= 50) {
            param3.shaking = false;
            param3.ticks = 0;
        }

        if (param3.ticks >= 5 && param3.resonationTicks == 0 && areRaidersNearby(param1, param3.nearbyEntities)) {
            param3.resonating = true;
            param0.playSound(null, param1, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (param3.resonating) {
            if (param3.resonationTicks < 40) {
                ++param3.resonationTicks;
            } else {
                param4.run(param0, param1, param3.nearbyEntities);
                param3.resonating = false;
            }
        }

    }

    public static void clientTick(Level param0, BlockPos param1, BlockState param2, BellBlockEntity param3) {
        tick(param0, param1, param2, param3, BellBlockEntity::showBellParticles);
    }

    public static void serverTick(Level param0, BlockPos param1, BlockState param2, BellBlockEntity param3) {
        tick(param0, param1, param2, param3, BellBlockEntity::makeRaidersGlow);
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
                if (var2.isAlive() && !var2.isRemoved() && var0.closerThan(var2.position(), 32.0)) {
                    var2.getBrain().setMemory(MemoryModuleType.HEARD_BELL_TIME, this.level.getGameTime());
                }
            }
        }

    }

    private static boolean areRaidersNearby(BlockPos param0, List<LivingEntity> param1) {
        for(LivingEntity var0 : param1) {
            if (var0.isAlive() && !var0.isRemoved() && param0.closerThan(var0.position(), 32.0) && var0.getType().is(EntityTypeTags.RAIDERS)) {
                return true;
            }
        }

        return false;
    }

    private static void makeRaidersGlow(Level param0x, BlockPos param1x, List<LivingEntity> param2x) {
        param2x.stream().filter(param1xx -> isRaiderWithinRange(param1x, param1xx)).forEach(BellBlockEntity::glow);
    }

    private static void showBellParticles(Level param0x, BlockPos param1x, List<LivingEntity> param2x) {
        MutableInt var0 = new MutableInt(16700985);
        int var1 = (int)param2x.stream().filter(param1xx -> param1x.closerThan(param1xx.position(), 48.0)).count();
        param2x.stream()
            .filter(param1xx -> isRaiderWithinRange(param1x, param1xx))
            .forEach(
                param4 -> {
                    float var0x = 1.0F;
                    float var1x = Mth.sqrt(
                        (param4.getX() - (double)param1x.getX()) * (param4.getX() - (double)param1x.getX())
                            + (param4.getZ() - (double)param1x.getZ()) * (param4.getZ() - (double)param1x.getZ())
                    );
                    double var2x = (double)((float)param1x.getX() + 0.5F) + (double)(1.0F / var1x) * (param4.getX() - (double)param1x.getX());
                    double var3x = (double)((float)param1x.getZ() + 0.5F) + (double)(1.0F / var1x) * (param4.getZ() - (double)param1x.getZ());
                    int var4x = Mth.clamp((var1 - 21) / -2, 3, 15);
        
                    for(int var5 = 0; var5 < var4x; ++var5) {
                        int var6 = var0.addAndGet(5);
                        double var7 = (double)FastColor.ARGB32.red(var6) / 255.0;
                        double var8 = (double)FastColor.ARGB32.green(var6) / 255.0;
                        double var9 = (double)FastColor.ARGB32.blue(var6) / 255.0;
                        param0x.addParticle(ParticleTypes.ENTITY_EFFECT, var2x, (double)((float)param1x.getY() + 0.5F), var3x, var7, var8, var9);
                    }
        
                }
            );
    }

    private static boolean isRaiderWithinRange(BlockPos param0, LivingEntity param1) {
        return param1.isAlive() && !param1.isRemoved() && param0.closerThan(param1.position(), 48.0) && param1.getType().is(EntityTypeTags.RAIDERS);
    }

    private static void glow(LivingEntity param0x) {
        param0x.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60));
    }

    @FunctionalInterface
    interface ResonationEndAction {
        void run(Level var1, BlockPos var2, List<LivingEntity> var3);
    }
}
