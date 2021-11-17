package net.minecraft.world.level;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.Difficulty;
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
    private final Random random = new Random();

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
                    CompoundTag var2 = this.nextSpawnData.getEntityToSpawn();
                    Optional<EntityType<?>> var3 = EntityType.by(var2);
                    if (var3.isEmpty()) {
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
                    if (param0.noCollision(var3.get().getAABB(var6, var7, var8))) {
                        BlockPos var9 = new BlockPos(var6, var7, var8);
                        if (this.nextSpawnData.getCustomSpawnRules().isPresent()) {
                            if (!var3.get().getCategory().isFriendly() && param0.getDifficulty() == Difficulty.PEACEFUL) {
                                continue;
                            }

                            SpawnData.CustomSpawnRules var10 = this.nextSpawnData.getCustomSpawnRules().get();
                            if (!var10.blockLightLimit().isValueInRange(param0.getBrightness(LightLayer.BLOCK, var9))
                                || !var10.skyLightLimit().isValueInRange(param0.getBrightness(LightLayer.SKY, var9))) {
                                continue;
                            }
                        } else if (!SpawnPlacements.checkSpawnRules(var3.get(), param0, MobSpawnType.SPAWNER, var9, param0.getRandom())) {
                            continue;
                        }

                        Entity var11 = EntityType.loadEntityRecursive(var2, param0, param3 -> {
                            param3.moveTo(var6, var7, var8, param3.getYRot(), param3.getXRot());
                            return param3;
                        });
                        if (var11 == null) {
                            this.delay(param0, param1);
                            return;
                        }

                        int var12 = param0.getEntitiesOfClass(
                                var11.getClass(),
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
                        if (var12 >= this.maxNearbyEntities) {
                            this.delay(param0, param1);
                            return;
                        }

                        var11.moveTo(var11.getX(), var11.getY(), var11.getZ(), param0.random.nextFloat() * 360.0F, 0.0F);
                        if (var11 instanceof Mob var13) {
                            if (this.nextSpawnData.getCustomSpawnRules().isEmpty() && !var13.checkSpawnRules(param0, MobSpawnType.SPAWNER)
                                || !var13.checkSpawnObstruction(param0)) {
                                continue;
                            }

                            if (this.nextSpawnData.getEntityToSpawn().size() == 1 && this.nextSpawnData.getEntityToSpawn().contains("id", 8)) {
                                ((Mob)var11).finalizeSpawn(param0, param0.getCurrentDifficultyAt(var11.blockPosition()), MobSpawnType.SPAWNER, null, null);
                            }
                        }

                        if (!param0.tryAddFreshEntityWithPassengers(var11)) {
                            this.delay(param0, param1);
                            return;
                        }

                        param0.levelEvent(2004, param1, 0);
                        if (var11 instanceof Mob) {
                            ((Mob)var11).spawnAnim();
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

        this.spawnPotentials.getRandom(this.random).ifPresent(param2 -> this.setNextSpawnData(param0, param1, param2.getData()));
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
                this.spawnPotentials.getRandom(this.random).ifPresent(param2x -> this.setNextSpawnData(param0, param1, param2x.getData()));
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
