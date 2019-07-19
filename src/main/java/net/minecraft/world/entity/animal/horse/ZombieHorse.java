package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;

public class ZombieHorse extends AbstractHorse {
    public ZombieHorse(EntityType<? extends ZombieHorse> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2F);
        this.getAttribute(JUMP_STRENGTH).setBaseValue(this.generateRandomJumpStrength());
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        super.getAmbientSound();
        return SoundEvents.ZOMBIE_HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        super.getDeathSound();
        return SoundEvents.ZOMBIE_HORSE_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        super.getHurtSound(param0);
        return SoundEvents.ZOMBIE_HORSE_HURT;
    }

    @Nullable
    @Override
    public AgableMob getBreedOffspring(AgableMob param0) {
        return EntityType.ZOMBIE_HORSE.create(this.level);
    }

    @Override
    public boolean mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (var0.getItem() instanceof SpawnEggItem) {
            return super.mobInteract(param0, param1);
        } else if (!this.isTamed()) {
            return false;
        } else if (this.isBaby()) {
            return super.mobInteract(param0, param1);
        } else if (param0.isSneaking()) {
            this.openInventory(param0);
            return true;
        } else if (this.isVehicle()) {
            return super.mobInteract(param0, param1);
        } else {
            if (!var0.isEmpty()) {
                if (!this.isSaddled() && var0.getItem() == Items.SADDLE) {
                    this.openInventory(param0);
                    return true;
                }

                if (var0.interactEnemy(param0, this, param1)) {
                    return true;
                }
            }

            this.doPlayerRide(param0);
            return true;
        }
    }

    @Override
    protected void addBehaviourGoals() {
    }
}
