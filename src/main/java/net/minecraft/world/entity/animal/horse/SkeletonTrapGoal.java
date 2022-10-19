package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
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
        ServerLevel var0 = (ServerLevel)this.horse.level;
        DifficultyInstance var1 = var0.getCurrentDifficultyAt(this.horse.blockPosition());
        this.horse.setTrap(false);
        this.horse.setTamed(true);
        this.horse.setAge(0);
        LightningBolt var2 = EntityType.LIGHTNING_BOLT.create(var0);
        if (var2 != null) {
            var2.moveTo(this.horse.getX(), this.horse.getY(), this.horse.getZ());
            var2.setVisualOnly(true);
            var0.addFreshEntity(var2);
            Skeleton var3 = this.createSkeleton(var1, this.horse);
            if (var3 != null) {
                var3.startRiding(this.horse);
                var0.addFreshEntityWithPassengers(var3);

                for(int var4 = 0; var4 < 3; ++var4) {
                    AbstractHorse var5 = this.createHorse(var1);
                    if (var5 != null) {
                        Skeleton var6 = this.createSkeleton(var1, var5);
                        if (var6 != null) {
                            var6.startRiding(var5);
                            var5.push(this.horse.getRandom().triangle(0.0, 1.1485), 0.0, this.horse.getRandom().triangle(0.0, 1.1485));
                            var0.addFreshEntityWithPassengers(var5);
                        }
                    }
                }

            }
        }
    }

    @Nullable
    private AbstractHorse createHorse(DifficultyInstance param0) {
        SkeletonHorse var0 = EntityType.SKELETON_HORSE.create(this.horse.level);
        if (var0 != null) {
            var0.finalizeSpawn((ServerLevel)this.horse.level, param0, MobSpawnType.TRIGGERED, null, null);
            var0.setPos(this.horse.getX(), this.horse.getY(), this.horse.getZ());
            var0.invulnerableTime = 60;
            var0.setPersistenceRequired();
            var0.setTamed(true);
            var0.setAge(0);
        }

        return var0;
    }

    @Nullable
    private Skeleton createSkeleton(DifficultyInstance param0, AbstractHorse param1) {
        Skeleton var0 = EntityType.SKELETON.create(param1.level);
        if (var0 != null) {
            var0.finalizeSpawn((ServerLevel)param1.level, param0, MobSpawnType.TRIGGERED, null, null);
            var0.setPos(param1.getX(), param1.getY(), param1.getZ());
            var0.invulnerableTime = 60;
            var0.setPersistenceRequired();
            if (var0.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                var0.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            }

            var0.setItemSlot(
                EquipmentSlot.MAINHAND,
                EnchantmentHelper.enchantItem(
                    var0.getRandom(),
                    this.disenchant(var0.getMainHandItem()),
                    (int)(5.0F + param0.getSpecialMultiplier() * (float)var0.getRandom().nextInt(18)),
                    false
                )
            );
            var0.setItemSlot(
                EquipmentSlot.HEAD,
                EnchantmentHelper.enchantItem(
                    var0.getRandom(),
                    this.disenchant(var0.getItemBySlot(EquipmentSlot.HEAD)),
                    (int)(5.0F + param0.getSpecialMultiplier() * (float)var0.getRandom().nextInt(18)),
                    false
                )
            );
        }

        return var0;
    }

    private ItemStack disenchant(ItemStack param0) {
        param0.removeTagKey("Enchantments");
        return param0;
    }
}
