package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.slf4j.Logger;

public class MobSpawnSettings {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float DEFAULT_CREATURE_SPAWN_PROBABILITY = 0.1F;
    public static final WeightedRandomList<MobSpawnSettings.SpawnerData> EMPTY_MOB_LIST = WeightedRandomList.create();
    public static final MobSpawnSettings EMPTY = new MobSpawnSettings.Builder().build();
    public static final MapCodec<MobSpawnSettings> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    Codec.floatRange(0.0F, 0.9999999F)
                        .optionalFieldOf("creature_spawn_probability", 0.1F)
                        .forGetter(param0x -> param0x.creatureGenerationProbability),
                    Codec.simpleMap(
                            MobCategory.CODEC,
                            WeightedRandomList.codec(MobSpawnSettings.SpawnerData.CODEC).promotePartial(Util.prefix("Spawn data: ", LOGGER::error)),
                            StringRepresentable.keys(MobCategory.values())
                        )
                        .fieldOf("spawners")
                        .forGetter(param0x -> param0x.spawners),
                    Codec.simpleMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), MobSpawnSettings.MobSpawnCost.CODEC, BuiltInRegistries.ENTITY_TYPE)
                        .fieldOf("spawn_costs")
                        .forGetter(param0x -> param0x.mobSpawnCosts)
                )
                .apply(param0, MobSpawnSettings::new)
    );
    private final float creatureGenerationProbability;
    private final Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> spawners;
    private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> mobSpawnCosts;

    MobSpawnSettings(
        float param0, Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> param1, Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> param2
    ) {
        this.creatureGenerationProbability = param0;
        this.spawners = ImmutableMap.copyOf(param1);
        this.mobSpawnCosts = ImmutableMap.copyOf(param2);
    }

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobs(MobCategory param0) {
        return this.spawners.getOrDefault(param0, EMPTY_MOB_LIST);
    }

    @Nullable
    public MobSpawnSettings.MobSpawnCost getMobSpawnCost(EntityType<?> param0) {
        return this.mobSpawnCosts.get(param0);
    }

    public float getCreatureProbability() {
        return this.creatureGenerationProbability;
    }

    public static class Builder {
        private final Map<MobCategory, List<MobSpawnSettings.SpawnerData>> spawners = Stream.of(MobCategory.values())
            .collect(ImmutableMap.toImmutableMap(param0 -> param0, param0 -> Lists.newArrayList()));
        private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> mobSpawnCosts = Maps.newLinkedHashMap();
        private float creatureGenerationProbability = 0.1F;

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

        public MobSpawnSettings build() {
            return new MobSpawnSettings(
                this.creatureGenerationProbability,
                this.spawners.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, param0 -> WeightedRandomList.create(param0.getValue()))),
                ImmutableMap.copyOf(this.mobSpawnCosts)
            );
        }
    }

    public static record MobSpawnCost(double energyBudget, double charge) {
        public static final Codec<MobSpawnSettings.MobSpawnCost> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.DOUBLE.fieldOf("energy_budget").forGetter(param0x -> param0x.energyBudget),
                        Codec.DOUBLE.fieldOf("charge").forGetter(param0x -> param0x.charge)
                    )
                    .apply(param0, MobSpawnSettings.MobSpawnCost::new)
        );
    }

    public static class SpawnerData extends WeightedEntry.IntrusiveBase {
        public static final Codec<MobSpawnSettings.SpawnerData> CODEC = ExtraCodecs.validate(
            RecordCodecBuilder.create(
                param0 -> param0.group(
                            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(param0x -> param0x.type),
                            Weight.CODEC.fieldOf("weight").forGetter(WeightedEntry.IntrusiveBase::getWeight),
                            ExtraCodecs.POSITIVE_INT.fieldOf("minCount").forGetter(param0x -> param0x.minCount),
                            ExtraCodecs.POSITIVE_INT.fieldOf("maxCount").forGetter(param0x -> param0x.maxCount)
                        )
                        .apply(param0, MobSpawnSettings.SpawnerData::new)
            ),
            param0 -> param0.minCount > param0.maxCount
                    ? DataResult.error(() -> "minCount needs to be smaller or equal to maxCount")
                    : DataResult.success(param0)
        );
        public final EntityType<?> type;
        public final int minCount;
        public final int maxCount;

        public SpawnerData(EntityType<?> param0, int param1, int param2, int param3) {
            this(param0, Weight.of(param1), param2, param3);
        }

        public SpawnerData(EntityType<?> param0, Weight param1, int param2, int param3) {
            super(param1);
            this.type = param0.getCategory() == MobCategory.MISC ? EntityType.PIG : param0;
            this.minCount = param2;
            this.maxCount = param3;
        }

        @Override
        public String toString() {
            return EntityType.getKey(this.type) + "*(" + this.minCount + "-" + this.maxCount + "):" + this.getWeight();
        }
    }
}
