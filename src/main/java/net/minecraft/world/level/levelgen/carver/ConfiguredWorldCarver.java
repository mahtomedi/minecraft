package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ConfiguredWorldCarver<WC extends CarverConfiguration> {
    public static final MapCodec<ConfiguredWorldCarver<?>> DIRECT_CODEC = Registry.CARVER
        .dispatchMap("name", param0 -> param0.worldCarver, WorldCarver::configuredCodec);
    public static final Codec<Supplier<ConfiguredWorldCarver<?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_CARVER_REGISTRY, DIRECT_CODEC);
    private final WorldCarver<WC> worldCarver;
    private final WC config;

    public ConfiguredWorldCarver(WorldCarver<WC> param0, WC param1) {
        this.worldCarver = param0;
        this.config = param1;
    }

    public WC config() {
        return this.config;
    }

    public boolean isStartChunk(Random param0, int param1, int param2) {
        return this.worldCarver.isStartChunk(param0, param1, param2, this.config);
    }

    public boolean carve(
        ChunkAccess param0, Function<BlockPos, Biome> param1, Random param2, int param3, int param4, int param5, int param6, int param7, BitSet param8
    ) {
        return this.worldCarver.carve(param0, param1, param2, param3, param4, param5, param6, param7, param8, this.config);
    }
}
