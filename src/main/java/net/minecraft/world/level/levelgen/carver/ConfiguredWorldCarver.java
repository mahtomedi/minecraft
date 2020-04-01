package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ConfiguredWorldCarver<WC extends CarverConfiguration> {
    public final WorldCarver<WC> worldCarver;
    public final WC config;

    public ConfiguredWorldCarver(WorldCarver<WC> param0, WC param1) {
        this.worldCarver = param0;
        this.config = param1;
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("name"),
                    param0.createString(Registry.CARVER.getKey(this.worldCarver).toString()),
                    param0.createString("config"),
                    this.config.serialize(param0).getValue()
                )
            )
        );
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
