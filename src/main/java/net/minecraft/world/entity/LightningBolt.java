package net.minecraft.world.entity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LightningBolt extends Entity {
    private int life;
    public long seed;
    private int flashes;
    private boolean visualOnly;
    @Nullable
    private ServerPlayer cause;

    public LightningBolt(EntityType<? extends LightningBolt> param0, Level param1) {
        super(param0, param1);
        this.noCulling = true;
        this.life = 2;
        this.seed = this.random.nextLong();
        this.flashes = this.random.nextInt(3) + 1;
    }

    public void setVisualOnly(boolean param0) {
        this.visualOnly = param0;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.WEATHER;
    }

    public void setCause(@Nullable ServerPlayer param0) {
        this.cause = param0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.life == 2) {
            Difficulty var0 = this.level.getDifficulty();
            if (var0 == Difficulty.NORMAL || var0 == Difficulty.HARD) {
                this.spawnFire(4);
            }

            this.level
                .playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.WEATHER,
                    10000.0F,
                    0.8F + this.random.nextFloat() * 0.2F
                );
            this.level
                .playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT,
                    SoundSource.WEATHER,
                    2.0F,
                    0.5F + this.random.nextFloat() * 0.2F
                );
        }

        --this.life;
        if (this.life < 0) {
            if (this.flashes == 0) {
                this.remove();
            } else if (this.life < -this.random.nextInt(10)) {
                --this.flashes;
                this.life = 1;
                this.seed = this.random.nextLong();
                this.spawnFire(0);
            }
        }

        if (this.life >= 0) {
            if (this.level.isClientSide) {
                this.level.setSkyFlashTime(2);
            } else if (!this.visualOnly) {
                double var1 = 3.0;
                List<Entity> var2 = this.level
                    .getEntities(
                        this,
                        new AABB(this.getX() - 3.0, this.getY() - 3.0, this.getZ() - 3.0, this.getX() + 3.0, this.getY() + 6.0 + 3.0, this.getZ() + 3.0),
                        Entity::isAlive
                    );

                for(Entity var3 : var2) {
                    var3.thunderHit(this);
                }

                if (this.cause != null) {
                    CriteriaTriggers.CHANNELED_LIGHTNING.trigger(this.cause, var2);
                }
            }
        }

    }

    private void spawnFire(int param0) {
        if (!this.visualOnly && !this.level.isClientSide && this.level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            BlockPos var0 = this.blockPosition();
            BlockState var1 = BaseFireBlock.getState(this.level, var0);
            if (this.level.getBlockState(var0).isAir() && var1.canSurvive(this.level, var0)) {
                this.level.setBlockAndUpdate(var0, var1);
            }

            for(int var2 = 0; var2 < param0; ++var2) {
                BlockPos var3 = var0.offset(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1, this.random.nextInt(3) - 1);
                var1 = BaseFireBlock.getState(this.level, var3);
                if (this.level.getBlockState(var3).isAir() && var1.canSurvive(this.level, var3)) {
                    this.level.setBlockAndUpdate(var3, var1);
                }
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRenderAtSqrDistance(double param0) {
        double var0 = 64.0 * getViewScale();
        return param0 < var0 * var0;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
