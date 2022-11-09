package net.minecraft.world.level;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public abstract class BaseSpawner {
    public static final String SPAWN_DATA_TAG = "SpawnData";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int EVENT_SPAWN = 1;
    private int spawnDelay = 20;
    private SimpleWeightedRandomList<SpawnData> spawnPotentials = SimpleWeightedRandomList.empty();
    @Nullable
    private SpawnData nextSpawnData;
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

    public void setEntityId(EntityType<?> param0, @Nullable Level param1, RandomSource param2, BlockPos param3) {
        this.getOrCreateNextSpawnData(param1, param2, param3).getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(param0).toString());
    }

    private boolean isNearPlayer(Level param0, BlockPos param1) {
        return param0.hasNearbyAlivePlayer(
            (double)param1.getX() + 0.5, (double)param1.getY() + 0.5, (double)param1.getZ() + 0.5, (double)this.requiredPlayerRange
        );
    }

    public void clientTick(Level param0, BlockPos param1) {
        if (!this.isNearPlayer(param0, param1)) {
            this.oSpin = this.spin;
        } else if (this.displayEntity != null) {
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
                RandomSource var1 = param0.getRandom();
                SpawnData var2 = this.getOrCreateNextSpawnData(param0, var1, param1);

                for(int var3 = 0; var3 < this.spawnCount; ++var3) {
                    CompoundTag var4 = var2.getEntityToSpawn();
                    Optional<EntityType<?>> var5 = EntityType.by(var4);
                    if (var5.isEmpty()) {
                        this.delay(param0, param1);
                        return;
                    }

                    ListTag var6 = var4.getList("Pos", 6);
                    int var7 = var6.size();
                    double var8 = var7 >= 1
                        ? var6.getDouble(0)
                        : (double)param1.getX() + (var1.nextDouble() - var1.nextDouble()) * (double)this.spawnRange + 0.5;
                    double var9 = var7 >= 2 ? var6.getDouble(1) : (double)(param1.getY() + var1.nextInt(3) - 1);
                    double var10 = var7 >= 3
                        ? var6.getDouble(2)
                        : (double)param1.getZ() + (var1.nextDouble() - var1.nextDouble()) * (double)this.spawnRange + 0.5;
                    if (param0.noCollision(var5.get().getAABB(var8, var9, var10))) {
                        BlockPos var11 = new BlockPos(var8, var9, var10);
                        if (var2.getCustomSpawnRules().isPresent()) {
                            if (!var5.get().getCategory().isFriendly() && param0.getDifficulty() == Difficulty.PEACEFUL) {
                                continue;
                            }

                            SpawnData.CustomSpawnRules var12 = var2.getCustomSpawnRules().get();
                            if (!var12.blockLightLimit().isValueInRange(param0.getBrightness(LightLayer.BLOCK, var11))
                                || !var12.skyLightLimit().isValueInRange(param0.getBrightness(LightLayer.SKY, var11))) {
                                continue;
                            }
                        } else if (!SpawnPlacements.checkSpawnRules(var5.get(), param0, MobSpawnType.SPAWNER, var11, param0.getRandom())) {
                            continue;
                        }

                        Entity var13 = EntityType.loadEntityRecursive(var4, param0, param3 -> {
                            param3.moveTo(var8, var9, var10, param3.getYRot(), param3.getXRot());
                            return param3;
                        });
                        if (var13 == null) {
                            this.delay(param0, param1);
                            return;
                        }

                        int var14 = param0.getEntitiesOfClass(
                                var13.getClass(),
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
                        if (var14 >= this.maxNearbyEntities) {
                            this.delay(param0, param1);
                            return;
                        }

                        var13.moveTo(var13.getX(), var13.getY(), var13.getZ(), var1.nextFloat() * 360.0F, 0.0F);
                        if (var13 instanceof Mob var15) {
                            if (var2.getCustomSpawnRules().isEmpty() && !var15.checkSpawnRules(param0, MobSpawnType.SPAWNER)
                                || !var15.checkSpawnObstruction(param0)) {
                                continue;
                            }

                            if (var2.getEntityToSpawn().size() == 1 && var2.getEntityToSpawn().contains("id", 8)) {
                                ((Mob)var13).finalizeSpawn(param0, param0.getCurrentDifficultyAt(var13.blockPosition()), MobSpawnType.SPAWNER, null, null);
                            }
                        }

                        if (!param0.tryAddFreshEntityWithPassengers(var13)) {
                            this.delay(param0, param1);
                            return;
                        }

                        param0.levelEvent(2004, param1, 0);
                        param0.gameEvent(var13, GameEvent.ENTITY_PLACE, var11);
                        if (var13 instanceof Mob) {
                            ((Mob)var13).spawnAnim();
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
        boolean var0 = param2.contains("SpawnData", 10);
        if (var0) {
            SpawnData var1 = SpawnData.CODEC
                .parse(NbtOps.INSTANCE, param2.getCompound("SpawnData"))
                .resultOrPartial(param0x -> LOGGER.warn("Invalid SpawnData: {}", param0x))
                .orElseGet(SpawnData::new);
            this.setNextSpawnData(param0, param1, var1);
        }

        boolean var2 = param2.contains("SpawnPotentials", 9);
        if (var2) {
            ListTag var3 = param2.getList("SpawnPotentials", 10);
            this.spawnPotentials = SpawnData.LIST_CODEC
                .parse(NbtOps.INSTANCE, var3)
                .resultOrPartial(param0x -> LOGGER.warn("Invalid SpawnPotentials list: {}", param0x))
                .orElseGet(SimpleWeightedRandomList::empty);
        } else {
            this.spawnPotentials = SimpleWeightedRandomList.single(this.nextSpawnData != null ? this.nextSpawnData : new SpawnData());
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
        if (this.nextSpawnData != null) {
            param0.put(
                "SpawnData",
                SpawnData.CODEC.encodeStart(NbtOps.INSTANCE, this.nextSpawnData).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData"))
            );
        }

        param0.put("SpawnPotentials", SpawnData.LIST_CODEC.encodeStart(NbtOps.INSTANCE, this.spawnPotentials).result().orElseThrow());
        return param0;
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(Level param0, RandomSource param1, BlockPos param2) {
        if (this.displayEntity == null) {
            CompoundTag var0 = this.getOrCreateNextSpawnData(param0, param1, param2).getEntityToSpawn();
            if (!var0.contains("id", 8)) {
                return null;
            }

            this.displayEntity = EntityType.loadEntityRecursive(var0, param0, Function.identity());
            if (var0.size() == 1 && this.displayEntity instanceof Mob) {
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

    protected void setNextSpawnData(@Nullable Level param0, BlockPos param1, SpawnData param2) {
        this.nextSpawnData = param2;
    }

    private SpawnData getOrCreateNextSpawnData(@Nullable Level param0, RandomSource param1, BlockPos param2) {
        if (this.nextSpawnData != null) {
            return this.nextSpawnData;
        } else {
            this.setNextSpawnData(param0, param2, this.spawnPotentials.getRandom(param1).map(WeightedEntry.Wrapper::getData).orElseGet(SpawnData::new));
            return this.nextSpawnData;
        }
    }

    public abstract void broadcastEvent(Level var1, BlockPos var2, int var3);

    public double getSpin() {
        return this.spin;
    }

    public double getoSpin() {
        return this.oSpin;
    }
}
