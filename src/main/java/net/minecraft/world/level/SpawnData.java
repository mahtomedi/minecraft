package net.minecraft.world.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;

public record SpawnData(CompoundTag entityToSpawn, Optional<SpawnData.CustomSpawnRules> customSpawnRules) {
    public static final Codec<SpawnData> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    CompoundTag.CODEC.fieldOf("entity").forGetter(param0x -> param0x.entityToSpawn),
                    SpawnData.CustomSpawnRules.CODEC.optionalFieldOf("custom_spawn_rules").forGetter(param0x -> param0x.customSpawnRules)
                )
                .apply(param0, SpawnData::new)
    );
    public static final Codec<SimpleWeightedRandomList<SpawnData>> LIST_CODEC = SimpleWeightedRandomList.wrappedCodec(CODEC);
    public static final String DEFAULT_TYPE = "minecraft:pig";

    public SpawnData() {
        this(Util.make(new CompoundTag(), param0 -> param0.putString("id", "minecraft:pig")), Optional.empty());
    }

    public SpawnData(CompoundTag param0, Optional<SpawnData.CustomSpawnRules> param1) {
        ResourceLocation var0 = ResourceLocation.tryParse(param0.getString("id"));
        param0.putString("id", var0 != null ? var0.toString() : "minecraft:pig");
        this.entityToSpawn = param0;
        this.customSpawnRules = param1;
    }

    public CompoundTag getEntityToSpawn() {
        return this.entityToSpawn;
    }

    public Optional<SpawnData.CustomSpawnRules> getCustomSpawnRules() {
        return this.customSpawnRules;
    }

    public static record CustomSpawnRules(InclusiveRange<Integer> blockLightLimit, InclusiveRange<Integer> skyLightLimit) {
        private static final InclusiveRange<Integer> LIGHT_RANGE = new InclusiveRange<>(0, 15);
        public static final Codec<SpawnData.CustomSpawnRules> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        InclusiveRange.INT
                            .optionalFieldOf("block_light_limit", LIGHT_RANGE)
                            .flatXmap(SpawnData.CustomSpawnRules::checkLightBoundaries, SpawnData.CustomSpawnRules::checkLightBoundaries)
                            .forGetter(param0x -> param0x.blockLightLimit),
                        InclusiveRange.INT
                            .optionalFieldOf("sky_light_limit", LIGHT_RANGE)
                            .flatXmap(SpawnData.CustomSpawnRules::checkLightBoundaries, SpawnData.CustomSpawnRules::checkLightBoundaries)
                            .forGetter(param0x -> param0x.skyLightLimit)
                    )
                    .apply(param0, SpawnData.CustomSpawnRules::new)
        );

        private static DataResult<InclusiveRange<Integer>> checkLightBoundaries(InclusiveRange<Integer> param0) {
            return !LIGHT_RANGE.contains(param0) ? DataResult.error("Light values must be withing range " + LIGHT_RANGE) : DataResult.success(param0);
        }
    }
}
