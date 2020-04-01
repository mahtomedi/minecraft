package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Objects;
import net.minecraft.core.Registry;

public class CheckerboardColumnBiomeSource extends BiomeSource {
    private final Biome[] allowedBiomes;
    private final int bitShift;

    public CheckerboardColumnBiomeSource(CheckerboardBiomeSourceSettings param0) {
        super(ImmutableSet.copyOf(param0.getAllowedBiomes()));
        this.allowedBiomes = param0.getAllowedBiomes();
        this.bitShift = param0.getSize() + 2;
    }

    @Override
    public Biome getNoiseBiome(int param0, int param1, int param2) {
        return this.allowedBiomes[Math.abs(((param0 >> this.bitShift) + (param2 >> this.bitShift)) % this.allowedBiomes.length)];
    }

    @Override
    public BiomeSourceType<?, ?> getType() {
        return BiomeSourceType.CHECKERBOARD;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        T var0 = param0.createList(this.possibleBiomes.stream().map(Registry.BIOME::getKey).map(Objects::toString).map(param0::createString));
        return new Dynamic<>(
            param0, param0.createMap(ImmutableMap.of(param0.createString("biomes"), var0, param0.createString("bitShift"), param0.createInt(this.bitShift)))
        );
    }
}
