package net.minecraft.world.entity.animal.horse;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class SkeletonTrapGoal extends Goal {
    private final SkeletonHorse horse;

    public SkeletonTrapGoal(SkeletonHorse param0) {
        this.horse = param0;
    }

    @Override
    public boolean canUse() {
        return this.horse.level.hasNearbyAlivePlayer(this.horse.getX(), this.horse.getY(), this.horse.getZ(), 10.0);
    }

    @Override
    public void tick() {
        DifficultyInstance var0 = this.horse.level.getCurrentDifficultyAt(this.horse.blockPosition());
        this.horse.setTrap(false);
        this.horse.setTamed(true);
        this.horse.setAge(0);
        LightningBolt var1 = EntityType.LIGHTNING_BOLT.create(this.horse.level);
        var1.moveTo(this.horse.getX(), this.horse.getY(), this.horse.getZ());
        var1.setVisualOnly(true);
        this.horse.level.addFreshEntity(var1);
        Skeleton var2 = this.createSkeleton(var0, this.horse);
        var2.startRiding(this.horse);

        for(int var3 = 0; var3 < 3; ++var3) {
            AbstractHorse var4 = this.createHorse(var0);
            Skeleton var5 = this.createSkeleton(var0, var4);
            var5.startRiding(var4);
            var4.push(this.horse.getRandom().nextGaussian() * 0.5, 0.0, this.horse.getRandom().nextGaussian() * 0.5);
        }

    }

    private AbstractHorse createHorse(DifficultyInstance param0) {
        SkeletonHorse var0 = EntityType.SKELETON_HORSE.create(this.horse.level);
        var0.finalizeSpawn(this.horse.level, param0, MobSpawnType.TRIGGERED, null, null);
        var0.setPos(this.horse.getX(), this.horse.getY(), this.horse.getZ());
        var0.invulnerableTime = 60;
        var0.setPersistenceRequired();
        var0.setTamed(true);
        var0.setAge(0);
        var0.level.addFreshEntity(var0);
        return var0;
    }

    private Skeleton createSkeleton(DifficultyInstance param0, AbstractHorse param1) {
        Skeleton var0 = EntityType.SKELETON.create(param1.level);
        var0.finalizeSpawn(param1.level, param0, MobSpawnType.TRIGGERED, null, null);
        var0.setPos(param1.getX(), param1.getY(), param1.getZ());
        var0.invulnerableTime = 60;
        var0.setPersistenceRequired();
        if (var0.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            var0.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        }

        var0.setItemSlot(
            EquipmentSlot.MAINHAND,
            EnchantmentHelper.enchantItem(
                var0.getRandom(), var0.getMainHandItem(), (int)(5.0F + param0.getSpecialMultiplier() * (float)var0.getRandom().nextInt(18)), false
            )
        );
        var0.setItemSlot(
            EquipmentSlot.HEAD,
            EnchantmentHelper.enchantItem(
                var0.getRandom(),
                var0.getItemBySlot(EquipmentSlot.HEAD),
                (int)(5.0F + param0.getSpecialMultiplier() * (float)var0.getRandom().nextInt(18)),
                false
            )
        );
        var0.level.addFreshEntity(var0);
        return var0;
    }
}
