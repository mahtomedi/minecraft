package net.minecraft.world.entity.animal.horse;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.global.LightningBolt;
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
        DifficultyInstance var0 = this.horse.level.getCurrentDifficultyAt(new BlockPos(this.horse));
        this.horse.setTrap(false);
        this.horse.setTamed(true);
        this.horse.setAge(0);
        ((ServerLevel)this.horse.level).addGlobalEntity(new LightningBolt(this.horse.level, this.horse.getX(), this.horse.getY(), this.horse.getZ(), true));
        Skeleton var1 = this.createSkeleton(var0, this.horse);
        var1.startRiding(this.horse);

        for(int var2 = 0; var2 < 3; ++var2) {
            AbstractHorse var3 = this.createHorse(var0);
            Skeleton var4 = this.createSkeleton(var0, var3);
            var4.startRiding(var3);
            var3.push(this.horse.getRandom().nextGaussian() * 0.5, 0.0, this.horse.getRandom().nextGaussian() * 0.5);
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
