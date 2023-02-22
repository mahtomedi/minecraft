package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public class MultiNoiseBiomeSource extends BiomeSource {
    private static final MapCodec<Holder<Biome>> ENTRY_CODEC = Biome.CODEC.fieldOf("biome");
    public static final MapCodec<Climate.ParameterList<Holder<Biome>>> DIRECT_CODEC = Climate.ParameterList.<Holder<Biome>>codec(ENTRY_CODEC).fieldOf("biomes");
    private static final MapCodec<Holder<MultiNoiseBiomeSourceParameterList>> PRESET_CODEC = MultiNoiseBiomeSourceParameterList.CODEC
        .fieldOf("preset")
        .withLifecycle(Lifecycle.stable());
    public static final Codec<MultiNoiseBiomeSource> CODEC = Codec.mapEither(DIRECT_CODEC, PRESET_CODEC)
        .xmap(MultiNoiseBiomeSource::new, param0 -> param0.parameters)
        .codec();
    private final Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> parameters;

    private MultiNoiseBiomeSource(Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> param0) {
        this.parameters = param0;
    }

    public static MultiNoiseBiomeSource createFromList(Climate.ParameterList<Holder<Biome>> param0) {
        return new MultiNoiseBiomeSource(Either.left(param0));
    }

    public static MultiNoiseBiomeSource createFromPreset(Holder<MultiNoiseBiomeSourceParameterList> param0) {
        return new MultiNoiseBiomeSource(Either.right(param0));
    }

    private Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters
            .map(
                (Function<? super Climate.ParameterList<Holder<Biome>>, ? extends Climate.ParameterList<Holder<Biome>>>)(param0 -> param0),
                param0 -> param0.value().parameters()
            );
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.parameters().values().stream().map(Pair::getSecond);
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    public boolean stable(ResourceKey<MultiNoiseBiomeSourceParameterList> param0) {
        Optional<Holder<MultiNoiseBiomeSourceParameterList>> var0 = this.parameters.right();
        return var0.isPresent() && var0.get().is(param0);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int param0, int param1, int param2, Climate.Sampler param3) {
        return this.getNoiseBiome(param3.sample(param0, param1, param2));
    }

    @VisibleForDebug
    public Holder<Biome> getNoiseBiome(Climate.TargetPoint param0) {
        return this.parameters().findValue(param0);
    }

    @Override
    public void addDebugInfo(List<String> param0, BlockPos param1, Climate.Sampler param2) {
        int var0 = QuartPos.fromBlock(param1.getX());
        int var1 = QuartPos.fromBlock(param1.getY());
        int var2 = QuartPos.fromBlock(param1.getZ());
        Climate.TargetPoint var3 = param2.sample(var0, var1, var2);
        float var4 = Climate.unquantizeCoord(var3.continentalness());
        float var5 = Climate.unquantizeCoord(var3.erosion());
        float var6 = Climate.unquantizeCoord(var3.temperature());
        float var7 = Climate.unquantizeCoord(var3.humidity());
        float var8 = Climate.unquantizeCoord(var3.weirdness());
        double var9 = (double)NoiseRouterData.peaksAndValleys(var8);
        OverworldBiomeBuilder var10 = new OverworldBiomeBuilder();
        param0.add(
            "Biome builder PV: "
                + OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(var9)
                + " C: "
                + var10.getDebugStringForContinentalness((double)var4)
                + " E: "
                + var10.getDebugStringForErosion((double)var5)
                + " T: "
                + var10.getDebugStringForTemperature((double)var6)
                + " H: "
                + var10.getDebugStringForHumidity((double)var7)
        );
    }
}
