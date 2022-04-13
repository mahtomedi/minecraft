package net.minecraft.world.level;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public abstract class BaseSpawner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int EVENT_SPAWN = 1;
    private int spawnDelay = 20;
    private SimpleWeightedRandomList<SpawnData> spawnPotentials = SimpleWeightedRandomList.empty();
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

    public void setEntityId(EntityType<?> param0) {
        this.nextSpawnData.getEntityToSpawn().putString("id", Registry.ENTITY_TYPE.getKey(param0).toString());
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
            RandomSource var0 = param0.getRandom();
            double var1 = (double)param1.getX() + var0.nextDouble();
            double var2 = (double)param1.getY() + var0.nextDouble();
            double var3 = (double)param1.getZ() + var0.nextDouble();
            param0.addParticle(ParticleTypes.SMOKE, var1, var2, var3, 0.0, 0.0, 0.0);
            param0.addParticle(ParticleTypes.FLAME, var1, var2, var3, 0.0, 0.0, 0.0);
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
                    CompoundTag var2 = this.nextSpawnData.getEntityToSpawn();
                    Optional<EntityType<?>> var3 = EntityType.by(var2);
                    if (var3.isEmpty()) {
                        this.delay(param0, param1);
                        return;
                    }

                    ListTag var4 = var2.getList("Pos", 6);
                    int var5 = var4.size();
                    RandomSource var6 = param0.getRandom();
                    double var7 = var5 >= 1
                        ? var4.getDouble(0)
                        : (double)param1.getX() + (var6.nextDouble() - var6.nextDouble()) * (double)this.spawnRange + 0.5;
                    double var8 = var5 >= 2 ? var4.getDouble(1) : (double)(param1.getY() + var6.nextInt(3) - 1);
                    double var9 = var5 >= 3
                        ? var4.getDouble(2)
                        : (double)param1.getZ() + (var6.nextDouble() - var6.nextDouble()) * (double)this.spawnRange + 0.5;
                    if (param0.noCollision(var3.get().getAABB(var7, var8, var9))) {
                        BlockPos var10 = new BlockPos(var7, var8, var9);
                        if (this.nextSpawnData.getCustomSpawnRules().isPresent()) {
                            if (!var3.get().getCategory().isFriendly() && param0.getDifficulty() == Difficulty.PEACEFUL) {
                                continue;
                            }

                            SpawnData.CustomSpawnRules var11 = this.nextSpawnData.getCustomSpawnRules().get();
                            if (!var11.blockLightLimit().isValueInRange(param0.getBrightness(LightLayer.BLOCK, var10))
                                || !var11.skyLightLimit().isValueInRange(param0.getBrightness(LightLayer.SKY, var10))) {
                                continue;
                            }
                        } else if (!SpawnPlacements.checkSpawnRules(var3.get(), param0, MobSpawnType.SPAWNER, var10, param0.getRandom())) {
                            continue;
                        }

                        Entity var12 = EntityType.loadEntityRecursive(var2, param0, param3 -> {
                            param3.moveTo(var7, var8, var9, param3.getYRot(), param3.getXRot());
                            return param3;
                        });
                        if (var12 == null) {
                            this.delay(param0, param1);
                            return;
                        }

                        int var13 = param0.getEntitiesOfClass(
                                var12.getClass(),
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
                        if (var13 >= this.maxNearbyEntities) {
                            this.delay(param0, param1);
                            return;
                        }

                        var12.moveTo(var12.getX(), var12.getY(), var12.getZ(), var6.nextFloat() * 360.0F, 0.0F);
                        if (var12 instanceof Mob var14) {
                            if (this.nextSpawnData.getCustomSpawnRules().isEmpty() && !var14.checkSpawnRules(param0, MobSpawnType.SPAWNER)
                                || !var14.checkSpawnObstruction(param0)) {
                                continue;
                            }

                            if (this.nextSpawnData.getEntityToSpawn().size() == 1 && this.nextSpawnData.getEntityToSpawn().contains("id", 8)) {
                                ((Mob)var12).finalizeSpawn(param0, param0.getCurrentDifficultyAt(var12.blockPosition()), MobSpawnType.SPAWNER, null, null);
                            }
                        }

                        if (!param0.tryAddFreshEntityWithPassengers(var12)) {
                            this.delay(param0, param1);
                            return;
                        }

                        param0.levelEvent(2004, param1, 0);
                        if (var12 instanceof Mob) {
                            ((Mob)var12).spawnAnim();
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
        RandomSource var0 = param0.random;
        if (this.maxSpawnDelay <= this.minSpawnDelay) {
            this.spawnDelay = this.minSpawnDelay;
        } else {
            this.spawnDelay = this.minSpawnDelay + var0.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        }

        this.spawnPotentials.getRandom(var0).ifPresent(param2 -> this.setNextSpawnData(param0, param1, param2.getData()));
        this.broadcastEvent(param0, param1, 1);
    }

    public void load(@Nullable Level param0, BlockPos param1, CompoundTag param2) {
        this.spawnDelay = param2.getShort("Delay");
        boolean var0 = param2.contains("SpawnPotentials", 9);
        boolean var1 = param2.contains("SpawnData", 10);
        if (!var0) {
            SpawnData var2;
            if (var1) {
                var2 = SpawnData.CODEC
                    .parse(NbtOps.INSTANCE, param2.getCompound("SpawnData"))
                    .resultOrPartial(param0x -> LOGGER.warn("Invalid SpawnData: {}", param0x))
                    .orElseGet(SpawnData::new);
            } else {
                var2 = new SpawnData();
            }

            this.spawnPotentials = SimpleWeightedRandomList.single(var2);
            this.setNextSpawnData(param0, param1, var2);
        } else {
            ListTag var4 = param2.getList("SpawnPotentials", 10);
            this.spawnPotentials = SpawnData.LIST_CODEC
                .parse(NbtOps.INSTANCE, var4)
                .resultOrPartial(param0x -> LOGGER.warn("Invalid SpawnPotentials list: {}", param0x))
                .orElseGet(SimpleWeightedRandomList::empty);
            if (var1) {
                SpawnData var5 = SpawnData.CODEC
                    .parse(NbtOps.INSTANCE, param2.getCompound("SpawnData"))
                    .resultOrPartial(param0x -> LOGGER.warn("Invalid SpawnData: {}", param0x))
                    .orElseGet(SpawnData::new);
                this.setNextSpawnData(param0, param1, var5);
            } else {
                this.spawnPotentials.getRandom(param0.getRandom()).ifPresent(param2x -> this.setNextSpawnData(param0, param1, param2x.getData()));
            }
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

    public CompoundTag save(CompoundTag param0) {
        param0.putShort("Delay", (short)this.spawnDelay);
        param0.putShort("MinSpawnDelay", (short)this.minSpawnDelay);
        param0.putShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
        param0.putShort("SpawnCount", (short)this.spawnCount);
        param0.putShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
        param0.putShort("RequiredPlayerRange", (short)this.requiredPlayerRange);
        param0.putShort("SpawnRange", (short)this.spawnRange);
        param0.put(
            "SpawnData",
            SpawnData.CODEC.encodeStart(NbtOps.INSTANCE, this.nextSpawnData).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData"))
        );
        param0.put("SpawnPotentials", SpawnData.LIST_CODEC.encodeStart(NbtOps.INSTANCE, this.spawnPotentials).result().orElseThrow());
        return param0;
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(Level param0) {
        if (this.displayEntity == null) {
            this.displayEntity = EntityType.loadEntityRecursive(this.nextSpawnData.getEntityToSpawn(), param0, Function.identity());
            if (this.nextSpawnData.getEntityToSpawn().size() == 1
                && this.nextSpawnData.getEntityToSpawn().contains("id", 8)
                && this.displayEntity instanceof Mob) {
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
