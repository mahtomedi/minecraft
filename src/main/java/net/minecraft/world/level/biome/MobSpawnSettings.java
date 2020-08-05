package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MobSpawnSettings {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final MobSpawnSettings EMPTY = new MobSpawnSettings(
        0.1F, Stream.of(MobCategory.values()).collect(ImmutableMap.toImmutableMap(param0 -> param0, param0 -> ImmutableList.of())), ImmutableMap.of(), false
    );
    public static final MapCodec<MobSpawnSettings> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    Codec.FLOAT.optionalFieldOf("creature_spawn_probability", Float.valueOf(0.1F)).forGetter(param0x -> param0x.creatureGenerationProbability),
                    Codec.simpleMap(
                            MobCategory.CODEC,
                            MobSpawnSettings.SpawnerData.CODEC.listOf().promotePartial(Util.prefix("Spawn data: ", LOGGER::error)),
                            StringRepresentable.keys(MobCategory.values())
                        )
                        .fieldOf("spawners")
                        .forGetter(param0x -> param0x.spawners),
                    Codec.simpleMap(Registry.ENTITY_TYPE, MobSpawnSettings.MobSpawnCost.CODEC, Registry.ENTITY_TYPE)
                        .fieldOf("spawn_costs")
                        .forGetter(param0x -> param0x.mobSpawnCosts),
                    Codec.BOOL.fieldOf("player_spawn_friendly").orElse(false).forGetter(MobSpawnSettings::playerSpawnFriendly)
                )
                .apply(param0, MobSpawnSettings::new)
    );
    private final float creatureGenerationProbability;
    private final Map<MobCategory, List<MobSpawnSettings.SpawnerData>> spawners;
    private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> mobSpawnCosts;
    private final boolean playerSpawnFriendly;

    private MobSpawnSettings(
        float param0, Map<MobCategory, List<MobSpawnSettings.SpawnerData>> param1, Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> param2, boolean param3
    ) {
        this.creatureGenerationProbability = param0;
        this.spawners = param1;
        this.mobSpawnCosts = param2;
        this.playerSpawnFriendly = param3;
    }

    public List<MobSpawnSettings.SpawnerData> getMobs(MobCategory param0) {
        return this.spawners.get(param0);
    }

    @Nullable
    public MobSpawnSettings.MobSpawnCost getMobSpawnCost(EntityType<?> param0) {
        return this.mobSpawnCosts.get(param0);
    }

    public float getCreatureProbability() {
        return this.creatureGenerationProbability;
    }

    public boolean playerSpawnFriendly() {
        return this.playerSpawnFriendly;
    }

    public static class Builder {
        private final Map<MobCategory, List<MobSpawnSettings.SpawnerData>> spawners = Stream.of(MobCategory.values())
            .collect(ImmutableMap.toImmutableMap(param0 -> param0, param0 -> Lists.newArrayList()));
        private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> mobSpawnCosts = Maps.newLinkedHashMap();
        private float creatureGenerationProbability = 0.1F;
        private boolean playerCanSpawn;

        public MobSpawnSettings.Builder addSpawn(MobCategory param0, MobSpawnSettings.SpawnerData param1) {
            this.spawners.get(param0).add(param1);
            return this;
        }

        public MobSpawnSettings.Builder addMobCharge(EntityType<?> param0, double param1, double param2) {
            this.mobSpawnCosts.put(param0, new MobSpawnSettings.MobSpawnCost(param2, param1));
            return this;
        }

        public MobSpawnSettings.Builder creatureGenerationProbability(float param0) {
            this.creatureGenerationProbability = param0;
            return this;
        }

        public MobSpawnSettings.Builder setPlayerCanSpawn() {
            this.playerCanSpawn = true;
            return this;
        }

        public MobSpawnSettings build() {
            return new MobSpawnSettings(
                this.creatureGenerationProbability,
                this.spawners
                    .entrySet()
                    .stream()
                    .collect(ImmutableMap.toImmutableMap(Entry::getKey, param0 -> ImmutableList.copyOf((Collection)param0.getValue()))),
                ImmutableMap.copyOf(this.mobSpawnCosts),
                this.playerCanSpawn
            );
        }
    }

    public static class MobSpawnCost {
        public static final Codec<MobSpawnSettings.MobSpawnCost> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.DOUBLE.fieldOf("energy_budget").forGetter(param0x -> param0x.energyBudget),
                        Codec.DOUBLE.fieldOf("charge").forGetter(param0x -> param0x.charge)
                    )
                    .apply(param0, MobSpawnSettings.MobSpawnCost::new)
        );
        private final double energyBudget;
        private final double charge;

        private MobSpawnCost(double param0, double param1) {
            this.energyBudget = param0;
            this.charge = param1;
        }

        public double getEnergyBudget() {
            return this.energyBudget;
        }

        public double getCharge() {
            return this.charge;
        }
    }

    public static class SpawnerData extends WeighedRandom.WeighedRandomItem {
        public static final Codec<MobSpawnSettings.SpawnerData> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Registry.ENTITY_TYPE.fieldOf("type").forGetter(param0x -> param0x.type),
                        Codec.INT.fieldOf("weight").forGetter(param0x -> param0x.weight),
                        Codec.INT.fieldOf("minCount").forGetter(param0x -> param0x.minCount),
                        Codec.INT.fieldOf("maxCount").forGetter(param0x -> param0x.maxCount)
                    )
                    .apply(param0, MobSpawnSettings.SpawnerData::new)
        );
        public final EntityType<?> type;
        public final int minCount;
        public final int maxCount;

        public SpawnerData(EntityType<?> param0, int param1, int param2, int param3) {
            super(param1);
            this.type = param0.getCategory() == MobCategory.MISC ? EntityType.PIG : param0;
            this.minCount = param2;
            this.maxCount = param3;
        }

        @Override
        public String toString() {
            return EntityType.getKey(this.type) + "*(" + this.minCount + "-" + this.maxCount + "):" + this.weight;
        }
    }
}
