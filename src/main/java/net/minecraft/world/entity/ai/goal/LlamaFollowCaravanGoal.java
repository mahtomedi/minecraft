package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.phys.Vec3;

public class LlamaFollowCaravanGoal extends Goal {
    public final Llama llama;
    private double speedModifier;
    private int distCheckCounter;

    public LlamaFollowCaravanGoal(Llama param0, double param1) {
        this.llama = param0;
        this.speedModifier = param1;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.llama.isLeashed() && !this.llama.inCaravan()) {
            List<Entity> var0 = this.llama.level.getEntities(this.llama, this.llama.getBoundingBox().inflate(9.0, 4.0, 9.0), param0 -> {
                EntityType<?> var0x = param0.getType();
                return var0x == EntityType.LLAMA || var0x == EntityType.TRADER_LLAMA;
            });
            Llama var1 = null;
            double var2 = Double.MAX_VALUE;

            for(Entity var3 : var0) {
                Llama var4 = (Llama)var3;
                if (var4.inCaravan() && !var4.hasCaravanTail()) {
                    double var5 = this.llama.distanceToSqr(var4);
                    if (!(var5 > var2)) {
                        var2 = var5;
                        var1 = var4;
                    }
                }
            }

            if (var1 == null) {
                for(Entity var6 : var0) {
                    Llama var7 = (Llama)var6;
                    if (var7.isLeashed() && !var7.hasCaravanTail()) {
                        double var8 = this.llama.distanceToSqr(var7);
                        if (!(var8 > var2)) {
                            var2 = var8;
                            var1 = var7;
                        }
                    }
                }
            }

            if (var1 == null) {
                return false;
            } else if (var2 < 4.0) {
                return false;
            } else if (!var1.isLeashed() && !this.firstIsLeashed(var1, 1)) {
                return false;
            } else {
                this.llama.joinCaravan(var1);
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.llama.inCaravan() && this.llama.getCaravanHead().isAlive() && this.firstIsLeashed(this.llama, 0)) {
            double var0 = this.llama.distanceToSqr(this.llama.getCaravanHead());
            if (var0 > 676.0) {
                if (this.speedModifier <= 3.0) {
                    this.speedModifier *= 1.2;
                    this.distCheckCounter = 40;
                    return true;
                }

                if (this.distCheckCounter == 0) {
                    return false;
                }
            }

            if (this.distCheckCounter > 0) {
                --this.distCheckCounter;
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void stop() {
        this.llama.leaveCaravan();
        this.speedModifier = 2.1;
    }

    @Override
    public void tick() {
        if (this.llama.inCaravan()) {
            Llama var0 = this.llama.getCaravanHead();
            double var1 = (double)this.llama.distanceTo(var0);
            float var2 = 2.0F;
            Vec3 var3 = new Vec3(var0.getX() - this.llama.getX(), var0.getY() - this.llama.getY(), var0.getZ() - this.llama.getZ())
                .normalize()
                .scale(Math.max(var1 - 2.0, 0.0));
            this.llama.getNavigation().moveTo(this.llama.getX() + var3.x, this.llama.getY() + var3.y, this.llama.getZ() + var3.z, this.speedModifier);
        }
    }

    private boolean firstIsLeashed(Llama param0, int param1) {
        if (param1 > 8) {
            return false;
        } else if (param0.inCaravan()) {
            return param0.getCaravanHead().isLeashed() ? true : this.firstIsLeashed(param0.getCaravanHead(), ++param1);
        } else {
            return false;
        }
    }
}
