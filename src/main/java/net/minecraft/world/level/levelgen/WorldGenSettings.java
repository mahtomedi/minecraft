package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;

public record WorldGenSettings<T>(WorldOptions options, WorldDimensions dimensions) {
    public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(WorldOptions.CODEC.forGetter(WorldGenSettings::options), WorldDimensions.CODEC.forGetter(WorldGenSettings::dimensions))
                .apply(param0, param0.stable(WorldGenSettings::new))
    );

    public static <T> DataResult<T> encode(DynamicOps<T> param0, WorldOptions param1, WorldDimensions param2) {
        return CODEC.encodeStart(param0, new WorldGenSettings(param1, param2));
    }

    public static <T> DataResult<T> encode(DynamicOps<T> param0, WorldOptions param1, RegistryAccess param2) {
        return encode(param0, param1, new WorldDimensions(param2.registryOrThrow(Registries.LEVEL_STEM)));
    }
}
