package net.minecraft.world.level.block.entity.trialspawner;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;

public class TrialSpawnerData {
    public static final String TAG_SPAWN_DATA = "spawn_data";
    private static final String TAG_NEXT_MOB_SPAWNS_AT = "next_mob_spawns_at";
    public static MapCodec<TrialSpawnerData> MAP_CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    UUIDUtil.CODEC_SET.optionalFieldOf("registered_players", Sets.newHashSet()).forGetter(param0x -> param0x.detectedPlayers),
                    UUIDUtil.CODEC_SET.optionalFieldOf("current_mobs", Sets.newHashSet()).forGetter(param0x -> param0x.currentMobs),
                    Codec.LONG.optionalFieldOf("cooldown_ends_at", Long.valueOf(0L)).forGetter(param0x -> param0x.cooldownEndsAt),
                    Codec.LONG.optionalFieldOf("next_mob_spawns_at", Long.valueOf(0L)).forGetter(param0x -> param0x.nextMobSpawnsAt),
                    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("total_mobs_spawned", 0).forGetter(param0x -> param0x.totalMobsSpawned),
                    SpawnData.CODEC.optionalFieldOf("spawn_data").forGetter(param0x -> param0x.nextSpawnData),
                    ResourceLocation.CODEC.optionalFieldOf("ejecting_loot_table").forGetter(param0x -> param0x.ejectingLootTable)
                )
                .apply(param0, TrialSpawnerData::new)
    );
    protected final Set<UUID> detectedPlayers = new HashSet<>();
    protected final Set<UUID> currentMobs = new HashSet<>();
    protected long cooldownEndsAt;
    protected long nextMobSpawnsAt;
    protected int totalMobsSpawned;
    protected Optional<SpawnData> nextSpawnData;
    protected Optional<ResourceLocation> ejectingLootTable;
    protected SimpleWeightedRandomList<SpawnData> spawnPotentials;
    @Nullable
    protected Entity displayEntity;
    protected double spin;
    protected double oSpin;

    public TrialSpawnerData() {
        this(Collections.emptySet(), Collections.emptySet(), 0L, 0L, 0, Optional.empty(), Optional.empty());
    }

    public TrialSpawnerData(
        Set<UUID> param0, Set<UUID> param1, long param2, long param3, int param4, Optional<SpawnData> param5, Optional<ResourceLocation> param6
    ) {
        this.detectedPlayers.addAll(param0);
        this.currentMobs.addAll(param1);
        this.cooldownEndsAt = param2;
        this.nextMobSpawnsAt = param3;
        this.totalMobsSpawned = param4;
        this.nextSpawnData = param5;
        this.ejectingLootTable = param6;
    }

    public void setSpawnPotentialsFromConfig(TrialSpawnerConfig param0) {
        SimpleWeightedRandomList<SpawnData> var0 = param0.spawnPotentialsDefinition();
        if (var0.isEmpty()) {
            this.spawnPotentials = SimpleWeightedRandomList.single(this.nextSpawnData.orElseGet(SpawnData::new));
        } else {
            this.spawnPotentials = var0;
        }

    }

    public void reset() {
        this.detectedPlayers.clear();
        this.totalMobsSpawned = 0;
        this.nextMobSpawnsAt = 0L;
        this.cooldownEndsAt = 0L;
        this.currentMobs.clear();
    }

    public boolean hasMobToSpawn() {
        boolean var0 = this.nextSpawnData.isPresent() && this.nextSpawnData.get().getEntityToSpawn().contains("id", 8);
        return var0 || !this.spawnPotentials.isEmpty();
    }

    public boolean hasFinishedSpawningAllMobs(TrialSpawnerConfig param0, int param1) {
        return this.totalMobsSpawned >= param0.calculateTargetTotalMobs(param1);
    }

    public boolean haveAllCurrentMobsDied() {
        return this.currentMobs.isEmpty();
    }

    public boolean isReadyToSpawnNextMob(ServerLevel param0, TrialSpawnerConfig param1, int param2) {
        return param0.getGameTime() >= this.nextMobSpawnsAt && this.currentMobs.size() < param1.calculateTargetSimultaneousMobs(param2);
    }

    public int countAdditionalPlayers(BlockPos param0) {
        if (this.detectedPlayers.isEmpty()) {
            Util.logAndPauseIfInIde("Trial Spawner at " + param0 + " has no detected players");
        }

        return Math.max(0, this.detectedPlayers.size() - 1);
    }

    public void tryDetectPlayers(ServerLevel param0, BlockPos param1, PlayerDetector param2, int param3) {
        List<UUID> var0 = param2.detect(param0, param1, param3);
        boolean var1 = this.detectedPlayers.addAll(var0);
        if (var1) {
            this.nextMobSpawnsAt = Math.max(param0.getGameTime() + 40L, this.nextMobSpawnsAt);
            param0.levelEvent(3013, param1, this.detectedPlayers.size());
        }

    }

    public boolean isReadyToOpenShutter(ServerLevel param0, TrialSpawnerConfig param1, float param2) {
        long var0 = this.cooldownEndsAt - (long)param1.targetCooldownLength();
        return (float)param0.getGameTime() >= (float)var0 + param2;
    }

    public boolean isReadyToEjectItems(ServerLevel param0, TrialSpawnerConfig param1, float param2) {
        long var0 = this.cooldownEndsAt - (long)param1.targetCooldownLength();
        return (float)(param0.getGameTime() - var0) % param2 == 0.0F;
    }

    public boolean isCooldownFinished(ServerLevel param0) {
        return param0.getGameTime() >= this.cooldownEndsAt;
    }

    public void setEntityId(TrialSpawner param0, RandomSource param1, EntityType<?> param2) {
        this.getOrCreateNextSpawnData(param0, param1).getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(param2).toString());
    }

    protected SpawnData getOrCreateNextSpawnData(TrialSpawner param0, RandomSource param1) {
        if (this.nextSpawnData.isPresent()) {
            return this.nextSpawnData.get();
        } else {
            this.nextSpawnData = Optional.of(this.spawnPotentials.getRandom(param1).map(WeightedEntry.Wrapper::getData).orElseGet(SpawnData::new));
            param0.markUpdated();
            return this.nextSpawnData.get();
        }
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(TrialSpawner param0, Level param1, TrialSpawnerState param2) {
        if (param0.canSpawnInLevel(param1) && param2.hasSpinningMob()) {
            if (this.displayEntity == null) {
                CompoundTag var0 = this.getOrCreateNextSpawnData(param0, param1.getRandom()).getEntityToSpawn();
                if (var0.contains("id", 8)) {
                    this.displayEntity = EntityType.loadEntityRecursive(var0, param1, Function.identity());
                }
            }

            return this.displayEntity;
        } else {
            return null;
        }
    }

    public CompoundTag getUpdateTag(TrialSpawnerState param0) {
        CompoundTag var0 = new CompoundTag();
        if (param0 == TrialSpawnerState.ACTIVE) {
            var0.putLong("next_mob_spawns_at", this.nextMobSpawnsAt);
        }

        this.nextSpawnData
            .ifPresent(
                param1 -> var0.put(
                        "spawn_data",
                        SpawnData.CODEC.encodeStart(NbtOps.INSTANCE, param1).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData"))
                    )
            );
        return var0;
    }

    public double getSpin() {
        return this.spin;
    }

    public double getOSpin() {
        return this.oSpin;
    }
}
