package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseSpawner {
    private static final Logger LOGGER = LogManager.getLogger();
    private int spawnDelay = 20;
    private final List<SpawnData> spawnPotentials = Lists.newArrayList();
    private SpawnData nextSpawnData = new SpawnData();
    private double spin;
    private double oSpin;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    @Nullable
    private Entity displayEntity;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;

    @Nullable
    private ResourceLocation getEntityId() {
        String var0 = this.nextSpawnData.getTag().getString("id");

        try {
            return StringUtil.isNullOrEmpty(var0) ? null : new ResourceLocation(var0);
        } catch (ResourceLocationException var4) {
            BlockPos var2 = this.getPos();
            LOGGER.warn("Invalid entity id '{}' at spawner {}:[{},{},{}]", var0, this.getLevel().dimension.getType(), var2.getX(), var2.getY(), var2.getZ());
            return null;
        }
    }

    public void setEntityId(EntityType<?> param0) {
        this.nextSpawnData.getTag().putString("id", Registry.ENTITY_TYPE.getKey(param0).toString());
    }

    private boolean isNearPlayer() {
        BlockPos var0 = this.getPos();
        return this.getLevel()
            .hasNearbyAlivePlayer((double)var0.getX() + 0.5, (double)var0.getY() + 0.5, (double)var0.getZ() + 0.5, (double)this.requiredPlayerRange);
    }

    public void tick() {
        if (!this.isNearPlayer()) {
            this.oSpin = this.spin;
        } else {
            Level var0 = this.getLevel();
            BlockPos var1 = this.getPos();
            if (var0.isClientSide) {
                double var2 = (double)var1.getX() + (double)var0.random.nextFloat();
                double var3 = (double)var1.getY() + (double)var0.random.nextFloat();
                double var4 = (double)var1.getZ() + (double)var0.random.nextFloat();
                var0.addParticle(ParticleTypes.SMOKE, var2, var3, var4, 0.0, 0.0, 0.0);
                var0.addParticle(ParticleTypes.FLAME, var2, var3, var4, 0.0, 0.0, 0.0);
                if (this.spawnDelay > 0) {
                    --this.spawnDelay;
                }

                this.oSpin = this.spin;
                this.spin = (this.spin + (double)(1000.0F / ((float)this.spawnDelay + 200.0F))) % 360.0;
            } else {
                if (this.spawnDelay == -1) {
                    this.delay();
                }

                if (this.spawnDelay > 0) {
                    --this.spawnDelay;
                    return;
                }

                boolean var5 = false;

                for(int var6 = 0; var6 < this.spawnCount; ++var6) {
                    CompoundTag var7 = this.nextSpawnData.getTag();
                    Optional<EntityType<?>> var8 = EntityType.by(var7);
                    if (!var8.isPresent()) {
                        this.delay();
                        return;
                    }

                    ListTag var9 = var7.getList("Pos", 6);
                    int var10 = var9.size();
                    double var11 = var10 >= 1
                        ? var9.getDouble(0)
                        : (double)var1.getX() + (var0.random.nextDouble() - var0.random.nextDouble()) * (double)this.spawnRange + 0.5;
                    double var12 = var10 >= 2 ? var9.getDouble(1) : (double)(var1.getY() + var0.random.nextInt(3) - 1);
                    double var13 = var10 >= 3
                        ? var9.getDouble(2)
                        : (double)var1.getZ() + (var0.random.nextDouble() - var0.random.nextDouble()) * (double)this.spawnRange + 0.5;
                    if (var0.noCollision(var8.get().getAABB(var11, var12, var13))
                        && SpawnPlacements.checkSpawnRules(
                            var8.get(), var0.getLevel(), MobSpawnType.SPAWNER, new BlockPos(var11, var12, var13), var0.getRandom()
                        )) {
                        Entity var14 = EntityType.loadEntityRecursive(var7, var0, param3 -> {
                            param3.moveTo(var11, var12, var13, param3.yRot, param3.xRot);
                            return param3;
                        });
                        if (var14 == null) {
                            this.delay();
                            return;
                        }

                        int var15 = var0.getEntitiesOfClass(
                                var14.getClass(),
                                new AABB(
                                        (double)var1.getX(),
                                        (double)var1.getY(),
                                        (double)var1.getZ(),
                                        (double)(var1.getX() + 1),
                                        (double)(var1.getY() + 1),
                                        (double)(var1.getZ() + 1)
                                    )
                                    .inflate((double)this.spawnRange)
                            )
                            .size();
                        if (var15 >= this.maxNearbyEntities) {
                            this.delay();
                            return;
                        }

                        var14.moveTo(var14.getX(), var14.getY(), var14.getZ(), var0.random.nextFloat() * 360.0F, 0.0F);
                        if (var14 instanceof Mob) {
                            Mob var16 = (Mob)var14;
                            if (!var16.checkSpawnRules(var0, MobSpawnType.SPAWNER) || !var16.checkSpawnObstruction(var0)) {
                                continue;
                            }

                            if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8)) {
                                ((Mob)var14).finalizeSpawn(var0, var0.getCurrentDifficultyAt(new BlockPos(var14)), MobSpawnType.SPAWNER, null, null);
                            }
                        }

                        this.addWithPassengers(var14);
                        var0.levelEvent(2004, var1, 0);
                        if (var14 instanceof Mob) {
                            ((Mob)var14).spawnAnim();
                        }

                        var5 = true;
                    }
                }

                if (var5) {
                    this.delay();
                }
            }

        }
    }

    private void addWithPassengers(Entity param0) {
        if (this.getLevel().addFreshEntity(param0)) {
            for(Entity var0 : param0.getPassengers()) {
                this.addWithPassengers(var0);
            }

        }
    }

    private void delay() {
        if (this.maxSpawnDelay <= this.minSpawnDelay) {
            this.spawnDelay = this.minSpawnDelay;
        } else {
            this.spawnDelay = this.minSpawnDelay + this.getLevel().random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        }

        if (!this.spawnPotentials.isEmpty()) {
            this.setNextSpawnData(WeighedRandom.getRandomItem(this.getLevel().random, this.spawnPotentials));
        }

        this.broadcastEvent(1);
    }

    public void load(CompoundTag param0) {
        this.spawnDelay = param0.getShort("Delay");
        this.spawnPotentials.clear();
        if (param0.contains("SpawnPotentials", 9)) {
            ListTag var0 = param0.getList("SpawnPotentials", 10);

            for(int var1 = 0; var1 < var0.size(); ++var1) {
                this.spawnPotentials.add(new SpawnData(var0.getCompound(var1)));
            }
        }

        if (param0.contains("SpawnData", 10)) {
            this.setNextSpawnData(new SpawnData(1, param0.getCompound("SpawnData")));
        } else if (!this.spawnPotentials.isEmpty()) {
            this.setNextSpawnData(WeighedRandom.getRandomItem(this.getLevel().random, this.spawnPotentials));
        }

        if (param0.contains("MinSpawnDelay", 99)) {
            this.minSpawnDelay = param0.getShort("MinSpawnDelay");
            this.maxSpawnDelay = param0.getShort("MaxSpawnDelay");
            this.spawnCount = param0.getShort("SpawnCount");
        }

        if (param0.contains("MaxNearbyEntities", 99)) {
            this.maxNearbyEntities = param0.getShort("MaxNearbyEntities");
            this.requiredPlayerRange = param0.getShort("RequiredPlayerRange");
        }

        if (param0.contains("SpawnRange", 99)) {
            this.spawnRange = param0.getShort("SpawnRange");
        }

        if (this.getLevel() != null) {
            this.displayEntity = null;
        }

    }

    public CompoundTag save(CompoundTag param0) {
        ResourceLocation var0 = this.getEntityId();
        if (var0 == null) {
            return param0;
        } else {
            param0.putShort("Delay", (short)this.spawnDelay);
            param0.putShort("MinSpawnDelay", (short)this.minSpawnDelay);
            param0.putShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
            param0.putShort("SpawnCount", (short)this.spawnCount);
            param0.putShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
            param0.putShort("RequiredPlayerRange", (short)this.requiredPlayerRange);
            param0.putShort("SpawnRange", (short)this.spawnRange);
            param0.put("SpawnData", this.nextSpawnData.getTag().copy());
            ListTag var1 = new ListTag();
            if (this.spawnPotentials.isEmpty()) {
                var1.add(this.nextSpawnData.save());
            } else {
                for(SpawnData var2 : this.spawnPotentials) {
                    var1.add(var2.save());
                }
            }

            param0.put("SpawnPotentials", var1);
            return param0;
        }
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public Entity getOrCreateDisplayEntity() {
        if (this.displayEntity == null) {
            this.displayEntity = EntityType.loadEntityRecursive(this.nextSpawnData.getTag(), this.getLevel(), Function.identity());
            if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8) && this.displayEntity instanceof Mob) {
                ((Mob)this.displayEntity)
                    .finalizeSpawn(this.getLevel(), this.getLevel().getCurrentDifficultyAt(new BlockPos(this.displayEntity)), MobSpawnType.SPAWNER, null, null);
            }
        }

        return this.displayEntity;
    }

    public boolean onEventTriggered(int param0) {
        if (param0 == 1 && this.getLevel().isClientSide) {
            this.spawnDelay = this.minSpawnDelay;
            return true;
        } else {
            return false;
        }
    }

    public void setNextSpawnData(SpawnData param0) {
        this.nextSpawnData = param0;
    }

    public abstract void broadcastEvent(int var1);

    public abstract Level getLevel();

    public abstract BlockPos getPos();

    @OnlyIn(Dist.CLIENT)
    public double getSpin() {
        return this.spin;
    }

    @OnlyIn(Dist.CLIENT)
    public double getoSpin() {
        return this.oSpin;
    }
}
