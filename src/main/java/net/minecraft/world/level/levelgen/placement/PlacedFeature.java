package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.apache.commons.lang3.mutable.MutableBoolean;

public record PlacedFeature(Holder<ConfiguredFeature<?, ?>> feature, List<PlacementModifier> placement) {
    public static final Codec<PlacedFeature> DIRECT_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ConfiguredFeature.CODEC.fieldOf("feature").forGetter(param0x -> param0x.feature),
                    PlacementModifier.CODEC.listOf().fieldOf("placement").forGetter(param0x -> param0x.placement)
                )
                .apply(param0, PlacedFeature::new)
    );
    public static final Codec<Holder<PlacedFeature>> CODEC = RegistryFileCodec.create(Registry.PLACED_FEATURE_REGISTRY, DIRECT_CODEC);
    public static final Codec<HolderSet<PlacedFeature>> LIST_CODEC = RegistryCodecs.homogeneousList(Registry.PLACED_FEATURE_REGISTRY, DIRECT_CODEC);
    public static final Codec<List<HolderSet<PlacedFeature>>> LIST_OF_LISTS_CODEC = RegistryCodecs.homogeneousList(
            Registry.PLACED_FEATURE_REGISTRY, DIRECT_CODEC, true
        )
        .listOf();

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, RandomSource param2, BlockPos param3) {
        return this.placeWithContext(new PlacementContext(param0, param1, Optional.empty()), param2, param3);
    }

    public boolean placeWithBiomeCheck(WorldGenLevel param0, ChunkGenerator param1, RandomSource param2, BlockPos param3) {
        return this.placeWithContext(new PlacementContext(param0, param1, Optional.of(this)), param2, param3);
    }

    private boolean placeWithContext(PlacementContext param0, RandomSource param1, BlockPos param2) {
        Stream<BlockPos> var0 = Stream.of(param2);

        for(PlacementModifier var1 : this.placement) {
            var0 = var0.flatMap(param3 -> var1.getPositions(param0, param1, param3));
        }

        ConfiguredFeature<?, ?> var2 = this.feature.value();
        MutableBoolean var3 = new MutableBoolean();
        var0.forEach(param4 -> {
            if (var2.place(param0.getLevel(), param0.generator(), param1, param4)) {
                var3.setTrue();
            }

        });
        return var3.isTrue();
    }

    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return this.feature.value().getFeatures();
    }

    @Override
    public String toString() {
        return "Placed " + this.feature;
    }

    static record test(int a) {
    }
}
