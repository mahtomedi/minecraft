package net.minecraft.world.level.levelgen.presets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;

public class WorldPreset {
    public static final Codec<WorldPreset> DIRECT_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.unboundedMap(ResourceKey.codec(Registry.LEVEL_STEM_REGISTRY), LevelStem.CODEC)
                        .fieldOf("dimensions")
                        .forGetter(param0x -> param0x.dimensions)
                )
                .apply(param0, WorldPreset::new)
    );
    public static final Codec<Holder<WorldPreset>> CODEC = RegistryFileCodec.create(Registry.WORLD_PRESET_REGISTRY, DIRECT_CODEC);
    private final Map<ResourceKey<LevelStem>, LevelStem> dimensions;

    public WorldPreset(Map<ResourceKey<LevelStem>, LevelStem> param0) {
        this.dimensions = param0;
    }

    private Registry<LevelStem> createRegistry() {
        WritableRegistry<LevelStem> var0 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
        LevelStem.keysInOrder(this.dimensions.keySet().stream()).forEach(param1 -> {
            LevelStem var0x = this.dimensions.get(param1);
            if (var0x != null) {
                var0.register(param1, var0x, Lifecycle.stable());
            }

        });
        return var0.freeze();
    }

    public WorldGenSettings createWorldGenSettings(long param0, boolean param1, boolean param2) {
        return new WorldGenSettings(param0, param1, param2, this.createRegistry());
    }

    public WorldGenSettings recreateWorldGenSettings(WorldGenSettings param0) {
        return this.createWorldGenSettings(param0.seed(), param0.generateStructures(), param0.generateBonusChest());
    }

    public Optional<LevelStem> overworld() {
        return Optional.ofNullable(this.dimensions.get(LevelStem.OVERWORLD));
    }

    public LevelStem overworldOrThrow() {
        return this.overworld().orElseThrow(() -> new IllegalStateException("Can't find overworld in this preset"));
    }
}
