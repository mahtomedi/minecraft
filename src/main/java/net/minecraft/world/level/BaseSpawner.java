package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseSpawner {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int EVENT_SPAWN = 1;
    private static WeightedRandomList<SpawnData> EMPTY_POTENTIALS = WeightedRandomList.create();
    private int spawnDelay = 20;
    private WeightedRandomList<SpawnData> spawnPotentials = EMPTY_POTENTIALS;
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
    private final Random random = new Random();

    @Nullable
    private ResourceLocation getEntityId(@Nullable Level param0, BlockPos param1) {
        String var0 = this.nextSpawnData.getTag().getString("id");

        try {
            return StringUtil.isNullOrEmpty(var0) ? null : new ResourceLocation(var0);
        } catch (ResourceLocationException var5) {
            LOGGER.warn(
                "Invalid entity id '{}' at spawner {}:[{},{},{}]",
                var0,
                param0 != null ? param0.dimension().location() : "<null>",
                param1.getX(),
                param1.getY(),
                param1.getZ()
            );
            return null;
        }
    }

    public void setEntityId(EntityType<?> param0) {
        this.nextSpawnData.getTag().putString("id", Registry.ENTITY_TYPE.getKey(param0).toString());
    }

    private boolean isNearPlayer(Level param0, BlockPos param1) {
        return param0.hasNearbyAlivePlayer(
            (double)param1.getX() + 0.5, (double)param1.getY() + 0.5, (double)param1.getZ() + 0.5, (double)this.requiredPlayerRange
        );
    }

    public void clientTick(Level param0, BlockPos param1) {
        if (!this.isNearPlayer(param0, param1)) {
            this.oSpin = this.spin;
        } else {
            double var0 = (double)param1.getX() + param0.random.nextDouble();
            double var1 = (double)param1.getY() + param0.random.nextDouble();
            double var2 = (double)param1.getZ() + param0.random.nextDouble();
            param0.addParticle(ParticleTypes.SMOKE, var0, var1, var2, 0.0, 0.0, 0.0);
            param0.addParticle(ParticleTypes.FLAME, var0, var1, var2, 0.0, 0.0, 0.0);
            if (this.spawnDelay > 0) {
                --this.spawnDelay;
            }

            this.oSpin = this.spin;
            this.spin = (this.spin + (double)(1000.0F / ((float)this.spawnDelay + 200.0F))) % 360.0;
        }

    }

    public void serverTick(ServerLevel param0, BlockPos param1) {
        if (this.isNearPlayer(param0, param1)) {
            if (this.spawnDelay == -1) {
                this.delay(param0, param1);
            }

            if (this.spawnDelay > 0) {
                --this.spawnDelay;
            } else {
                boolean var0 = false;

                for(int var1 = 0; var1 < this.spawnCount; ++var1) {
                    CompoundTag var2 = this.nextSpawnData.getTag();
                    Optional<EntityType<?>> var3 = EntityType.by(var2);
                    if (!var3.isPresent()) {
                        this.delay(param0, param1);
                        return;
                    }

                    ListTag var4 = var2.getList("Pos", 6);
                    int var5 = var4.size();
                    double var6 = var5 >= 1
                        ? var4.getDouble(0)
                        : (double)param1.getX() + (param0.random.nextDouble() - param0.random.nextDouble()) * (double)this.spawnRange + 0.5;
                    double var7 = var5 >= 2 ? var4.getDouble(1) : (double)(param1.getY() + param0.random.nextInt(3) - 1);
                    double var8 = var5 >= 3
                        ? var4.getDouble(2)
                        : (double)param1.getZ() + (param0.random.nextDouble() - param0.random.nextDouble()) * (double)this.spawnRange + 0.5;
                    if (param0.noCollision(var3.get().getAABB(var6, var7, var8))
                        && SpawnPlacements.checkSpawnRules(var3.get(), param0, MobSpawnType.SPAWNER, new BlockPos(var6, var7, var8), param0.getRandom())) {
                        Entity var9 = EntityType.loadEntityRecursive(var2, param0, param3 -> {
                            param3.moveTo(var6, var7, var8, param3.yRot, param3.xRot);
                            return param3;
                        });
                        if (var9 == null) {
                            this.delay(param0, param1);
                            return;
                        }

                        int var10 = param0.getEntitiesOfClass(
                                var9.getClass(),
                                new AABB(
                                        (double)param1.getX(),
                                        (double)param1.getY(),
                                        (double)param1.getZ(),
                                        (double)(param1.getX() + 1),
                                        (double)(param1.getY() + 1),
                                        (double)(param1.getZ() + 1)
                                    )
                                    .inflate((double)this.spawnRange)
                            )
                            .size();
                        if (var10 >= this.maxNearbyEntities) {
                            this.delay(param0, param1);
                            return;
                        }

                        var9.moveTo(var9.getX(), var9.getY(), var9.getZ(), param0.random.nextFloat() * 360.0F, 0.0F);
                        if (var9 instanceof Mob) {
                            Mob var11 = (Mob)var9;
                            if (!var11.checkSpawnRules(param0, MobSpawnType.SPAWNER) || !var11.checkSpawnObstruction(param0)) {
                                continue;
                            }

                            if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8)) {
                                ((Mob)var9).finalizeSpawn(param0, param0.getCurrentDifficultyAt(var9.blockPosition()), MobSpawnType.SPAWNER, null, null);
                            }
                        }

                        if (!param0.tryAddFreshEntityWithPassengers(var9)) {
                            this.delay(param0, param1);
                            return;
                        }

                        param0.levelEvent(2004, param1, 0);
                        if (var9 instanceof Mob) {
                            ((Mob)var9).spawnAnim();
                        }

                        var0 = true;
                    }
                }

                if (var0) {
                    this.delay(param0, param1);
                }

            }
        }
    }

    private void delay(Level param0, BlockPos param1) {
        if (this.maxSpawnDelay <= this.minSpawnDelay) {
            this.spawnDelay = this.minSpawnDelay;
        } else {
            this.spawnDelay = this.minSpawnDelay + this.random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        }

        this.spawnPotentials.getRandom(this.random).ifPresent(param2 -> this.setNextSpawnData(param0, param1, param2));
        this.broadcastEvent(param0, param1, 1);
    }

    public void load(@Nullable Level param0, BlockPos param1, CompoundTag param2) {
        this.spawnDelay = param2.getShort("Delay");
        List<SpawnData> var0 = Lists.newArrayList();
        if (param2.contains("SpawnPotentials", 9)) {
            ListTag var1 = param2.getList("SpawnPotentials", 10);

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                var0.add(new SpawnData(var1.getCompound(var2)));
            }
        }

        this.spawnPotentials = WeightedRandomList.create(var0);
        if (param2.contains("SpawnData", 10)) {
            this.setNextSpawnData(param0, param1, new SpawnData(1, param2.getCompound("SpawnData")));
        } else if (!var0.isEmpty()) {
            this.spawnPotentials.getRandom(this.random).ifPresent(param2x -> this.setNextSpawnData(param0, param1, param2x));
        }

        if (param2.contains("MinSpawnDelay", 99)) {
            this.minSpawnDelay = param2.getShort("MinSpawnDelay");
            this.maxSpawnDelay = param2.getShort("MaxSpawnDelay");
            this.spawnCount = param2.getShort("SpawnCount");
        }

        if (param2.contains("MaxNearbyEntities", 99)) {
            this.maxNearbyEntities = param2.getShort("MaxNearbyEntities");
            this.requiredPlayerRange = param2.getShort("RequiredPlayerRange");
        }

        if (param2.contains("SpawnRange", 99)) {
            this.spawnRange = param2.getShort("SpawnRange");
        }

        this.displayEntity = null;
    }

    public CompoundTag save(@Nullable Level param0, BlockPos param1, CompoundTag param2) {
        ResourceLocation var0 = this.getEntityId(param0, param1);
        if (var0 == null) {
            return param2;
        } else {
            param2.putShort("Delay", (short)this.spawnDelay);
            param2.putShort("MinSpawnDelay", (short)this.minSpawnDelay);
            param2.putShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
            param2.putShort("SpawnCount", (short)this.spawnCount);
            param2.putShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
            param2.putShort("RequiredPlayerRange", (short)this.requiredPlayerRange);
            param2.putShort("SpawnRange", (short)this.spawnRange);
            param2.put("SpawnData", this.nextSpawnData.getTag().copy());
            ListTag var1 = new ListTag();
            if (this.spawnPotentials.isEmpty()) {
                var1.add(this.nextSpawnData.save());
            } else {
                for(SpawnData var2 : this.spawnPotentials.unwrap()) {
                    var1.add(var2.save());
                }
            }

            param2.put("SpawnPotentials", var1);
            return param2;
        }
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(Level param0) {
        if (this.displayEntity == null) {
            this.displayEntity = EntityType.loadEntityRecursive(this.nextSpawnData.getTag(), param0, Function.identity());
            if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8) && this.displayEntity instanceof Mob) {
            }
        }

        return this.displayEntity;
    }

    public boolean onEventTriggered(Level param0, int param1) {
        if (param1 == 1) {
            if (param0.isClientSide) {
                this.spawnDelay = this.minSpawnDelay;
            }

            return true;
        } else {
            return false;
        }
    }

    public void setNextSpawnData(@Nullable Level param0, BlockPos param1, SpawnData param2) {
        this.nextSpawnData = param2;
    }

    public abstract void broadcastEvent(Level var1, BlockPos var2, int var3);

    public double getSpin() {
        return this.spin;
    }

    public double getoSpin() {
        return this.oSpin;
    }
}
