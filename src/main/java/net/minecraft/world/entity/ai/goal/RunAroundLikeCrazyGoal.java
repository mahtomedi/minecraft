package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class RunAroundLikeCrazyGoal extends Goal {
    private final AbstractHorse horse;
    private final double speedModifier;
    private double posX;
    private double posY;
    private double posZ;

    public RunAroundLikeCrazyGoal(AbstractHorse param0, double param1) {
        this.horse = param0;
        this.speedModifier = param1;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.horse.isTamed() && this.horse.isVehicle()) {
            Vec3 var0 = DefaultRandomPos.getPos(this.horse, 5, 4);
            if (var0 == null) {
                return false;
            } else {
                this.posX = var0.x;
                this.posY = var0.y;
                this.posZ = var0.z;
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        this.horse.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.horse.isTamed() && !this.horse.getNavigation().isDone() && this.horse.isVehicle();
    }

    @Override
    public void tick() {
        if (!this.horse.isTamed() && this.horse.getRandom().nextInt(this.adjustedTickDelay(50)) == 0) {
            Entity var0 = this.horse.getPassengers().get(0);
            if (var0 == null) {
                return;
            }

            if (var0 instanceof Player) {
                int var1 = this.horse.getTemper();
                int var2 = this.horse.getMaxTemper();
                if (var2 > 0 && this.horse.getRandom().nextInt(var2) < var1) {
                    this.horse.tameWithName((Player)var0);
                    return;
                }

                this.horse.modifyTemper(5);
            }

            this.horse.ejectPassengers();
            this.horse.makeMad();
            this.horse.level.broadcastEntityEvent(this.horse, (byte)6);
        }

    }
}
