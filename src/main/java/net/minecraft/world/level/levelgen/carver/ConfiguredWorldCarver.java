package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;

public class ConfiguredWorldCarver<WC extends CarverConfiguration> {
    public static final Codec<ConfiguredWorldCarver<?>> DIRECT_CODEC = Registry.CARVER.dispatch(param0 -> param0.worldCarver, WorldCarver::configuredCodec);
    public static final Codec<Supplier<ConfiguredWorldCarver<?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_CARVER_REGISTRY, DIRECT_CODEC);
    public static final Codec<List<Supplier<ConfiguredWorldCarver<?>>>> LIST_CODEC = RegistryFileCodec.homogeneousList(
        Registry.CONFIGURED_CARVER_REGISTRY, DIRECT_CODEC
    );
    private final WorldCarver<WC> worldCarver;
    private final WC config;

    public ConfiguredWorldCarver(WorldCarver<WC> param0, WC param1) {
        this.worldCarver = param0;
        this.config = param1;
    }

    public WC config() {
        return this.config;
    }

    public boolean isStartChunk(Random param0) {
        return this.worldCarver.isStartChunk(this.config, param0);
    }

    public boolean carve(
        CarvingContext param0, ChunkAccess param1, Function<BlockPos, Biome> param2, Random param3, Aquifer param4, ChunkPos param5, BitSet param6
    ) {
        return this.worldCarver.carve(param0, this.config, param1, param2, param3, param4, param5, param6);
    }
}
