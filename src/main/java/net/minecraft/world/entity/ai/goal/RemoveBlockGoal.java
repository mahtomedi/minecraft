package net.minecraft.world.entity.ai.goal;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.Vec3;

public class RemoveBlockGoal extends MoveToBlockGoal {
    private final Block blockToRemove;
    private final Mob removerMob;
    private int ticksSinceReachedGoal;

    public RemoveBlockGoal(Block param0, PathfinderMob param1, double param2, int param3) {
        super(param1, param2, 24, param3);
        this.blockToRemove = param0;
        this.removerMob = param1;
    }

    @Override
    public boolean canUse() {
        if (!this.removerMob.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
        } else if (this.nextStartTick > 0) {
            --this.nextStartTick;
            return false;
        } else if (this.tryFindBlock()) {
            this.nextStartTick = 20;
            return true;
        } else {
            this.nextStartTick = this.nextStartTick(this.mob);
            return false;
        }
    }

    private boolean tryFindBlock() {
        return this.blockPos != null && this.isValidTarget(this.mob.level, this.blockPos) ? true : this.findNearestBlock();
    }

    @Override
    public void stop() {
        super.stop();
        this.removerMob.fallDistance = 1.0F;
    }

    @Override
    public void start() {
        super.start();
        this.ticksSinceReachedGoal = 0;
    }

    public void playDestroyProgressSound(LevelAccessor param0, BlockPos param1) {
    }

    public void playBreakSound(Level param0, BlockPos param1) {
    }

    @Override
    public void tick() {
        super.tick();
        Level var0 = this.removerMob.level;
        BlockPos var1 = this.removerMob.blockPosition();
        BlockPos var2 = this.getPosWithBlock(var1, var0);
        Random var3 = this.removerMob.getRandom();
        if (this.isReachedTarget() && var2 != null) {
            if (this.ticksSinceReachedGoal > 0) {
                Vec3 var4 = this.removerMob.getDeltaMovement();
                this.removerMob.setDeltaMovement(var4.x, 0.3, var4.z);
                if (!var0.isClientSide) {
                    double var5 = 0.08;
                    ((ServerLevel)var0)
                        .sendParticles(
                            new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.EGG)),
                            (double)var2.getX() + 0.5,
                            (double)var2.getY() + 0.7,
                            (double)var2.getZ() + 0.5,
                            3,
                            ((double)var3.nextFloat() - 0.5) * 0.08,
                            ((double)var3.nextFloat() - 0.5) * 0.08,
                            ((double)var3.nextFloat() - 0.5) * 0.08,
                            0.15F
                        );
                }
            }

            if (this.ticksSinceReachedGoal % 2 == 0) {
                Vec3 var6 = this.removerMob.getDeltaMovement();
                this.removerMob.setDeltaMovement(var6.x, -0.3, var6.z);
                if (this.ticksSinceReachedGoal % 6 == 0) {
                    this.playDestroyProgressSound(var0, this.blockPos);
                }
            }

            if (this.ticksSinceReachedGoal > 60) {
                var0.removeBlock(var2, false);
                if (!var0.isClientSide) {
                    for(int var7 = 0; var7 < 20; ++var7) {
                        double var8 = var3.nextGaussian() * 0.02;
                        double var9 = var3.nextGaussian() * 0.02;
                        double var10 = var3.nextGaussian() * 0.02;
                        ((ServerLevel)var0)
                            .sendParticles(
                                ParticleTypes.POOF, (double)var2.getX() + 0.5, (double)var2.getY(), (double)var2.getZ() + 0.5, 1, var8, var9, var10, 0.15F
                            );
                    }

                    this.playBreakSound(var0, var2);
                }
            }

            ++this.ticksSinceReachedGoal;
        }

    }

    @Nullable
    private BlockPos getPosWithBlock(BlockPos param0, BlockGetter param1) {
        if (param1.getBlockState(param0).is(this.blockToRemove)) {
            return param0;
        } else {
            BlockPos[] var0 = new BlockPos[]{param0.below(), param0.west(), param0.east(), param0.north(), param0.south(), param0.below().below()};

            for(BlockPos var1 : var0) {
                if (param1.getBlockState(var1).is(this.blockToRemove)) {
                    return var1;
                }
            }

            return null;
        }
    }

    @Override
    protected boolean isValidTarget(LevelReader param0, BlockPos param1) {
        ChunkAccess var0 = param0.getChunk(param1.getX() >> 4, param1.getZ() >> 4, ChunkStatus.FULL, false);
        if (var0 == null) {
            return false;
        } else {
            return var0.getBlockState(param1).is(this.blockToRemove)
                && var0.getBlockState(param1.above()).isAir()
                && var0.getBlockState(param1.above(2)).isAir();
        }
    }
}
