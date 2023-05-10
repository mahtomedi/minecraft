package net.minecraft.world.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;

public record SpawnData(CompoundTag entityToSpawn, Optional<SpawnData.CustomSpawnRules> customSpawnRules) {
    public static final String ENTITY_TAG = "entity";
    public static final Codec<SpawnData> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    CompoundTag.CODEC.fieldOf("entity").forGetter(param0x -> param0x.entityToSpawn),
                    SpawnData.CustomSpawnRules.CODEC.optionalFieldOf("custom_spawn_rules").forGetter(param0x -> param0x.customSpawnRules)
                )
                .apply(param0, SpawnData::new)
    );
    public static final Codec<SimpleWeightedRandomList<SpawnData>> LIST_CODEC = SimpleWeightedRandomList.wrappedCodecAllowingEmpty(CODEC);

    public SpawnData() {
        this(new CompoundTag(), Optional.empty());
    }

    public SpawnData(CompoundTag param0, Optional<SpawnData.CustomSpawnRules> param1) {
        if (param0.contains("id")) {
            ResourceLocation var0 = ResourceLocation.tryParse(param0.getString("id"));
            if (var0 != null) {
                param0.putString("id", var0.toString());
            } else {
                param0.remove("id");
            }
        }

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
                        lightLimit("block_light_limit").forGetter(param0x -> param0x.blockLightLimit),
                        lightLimit("sky_light_limit").forGetter(param0x -> param0x.skyLightLimit)
                    )
                    .apply(param0, SpawnData.CustomSpawnRules::new)
        );

        private static DataResult<InclusiveRange<Integer>> checkLightBoundaries(InclusiveRange<Integer> param0) {
            return !LIGHT_RANGE.contains(param0) ? DataResult.error(() -> "Light values must be withing range " + LIGHT_RANGE) : DataResult.success(param0);
        }

        private static MapCodec<InclusiveRange<Integer>> lightLimit(String param0) {
            return ExtraCodecs.validate(InclusiveRange.INT.optionalFieldOf(param0, LIGHT_RANGE), SpawnData.CustomSpawnRules::checkLightBoundaries);
        }
    }
}
