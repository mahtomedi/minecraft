package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ThrownExperienceBottle extends ThrowableItemProjectile {
    public ThrownExperienceBottle(EntityType<? extends ThrownExperienceBottle> param0, Level param1) {
        super(param0, param1);
    }

    public ThrownExperienceBottle(Level param0, LivingEntity param1) {
        super(EntityType.EXPERIENCE_BOTTLE, param1, param0);
    }

    public ThrownExperienceBottle(Level param0, double param1, double param2, double param3) {
        super(EntityType.EXPERIENCE_BOTTLE, param1, param2, param3, param0);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.EXPERIENCE_BOTTLE;
    }

    @Override
    protected float getGravity() {
        return 0.07F;
    }

    @Override
    protected void onHit(HitResult param0) {
        if (!this.level.isClientSide) {
            this.level.levelEvent(2002, new BlockPos(this), PotionUtils.getColor(Potions.WATER));
            int var0 = 3 + this.level.random.nextInt(5) + this.level.random.nextInt(5);

            while(var0 > 0) {
                int var1 = ExperienceOrb.getExperienceValue(var0);
                var0 -= var1;
                this.level.addFreshEntity(new ExperienceOrb(this.level, this.x, this.y, this.z, var1));
            }

            this.remove();
        }

    }
}
