package net.minecraft.world.entity.vehicle;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Minecart extends AbstractMinecart {
    public Minecart(EntityType<?> param0, Level param1) {
        super(param0, param1);
    }

    public Minecart(Level param0, double param1, double param2, double param3) {
        super(EntityType.MINECART, param0, param1, param2, param3);
    }

    @Override
    public boolean interact(Player param0, InteractionHand param1) {
        if (param0.isSecondaryUseActive()) {
            return false;
        } else if (this.isVehicle()) {
            return true;
        } else {
            if (!this.level.isClientSide) {
                param0.startRiding(this);
            }

            return true;
        }
    }

    @Override
    public void activateMinecart(int param0, int param1, int param2, boolean param3) {
        if (param3) {
            if (this.isVehicle()) {
                this.ejectPassengers();
            }

            if (this.getHurtTime() == 0) {
                this.setHurtDir(-this.getHurtDir());
                this.setHurtTime(10);
                this.setDamage(50.0F);
                this.markHurt();
            }
        }

    }

    @Override
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.RIDEABLE;
    }
}
