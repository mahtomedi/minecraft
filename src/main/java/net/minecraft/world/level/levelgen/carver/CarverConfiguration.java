package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CarverConfiguration extends ProbabilityFeatureConfiguration {
    public static final MapCodec<CarverConfiguration> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(param0x -> param0x.probability),
                    HeightProvider.CODEC.fieldOf("y").forGetter(param0x -> param0x.y),
                    FloatProvider.CODEC.fieldOf("yScale").forGetter(param0x -> param0x.yScale),
                    VerticalAnchor.CODEC.fieldOf("lava_level").forGetter(param0x -> param0x.lavaLevel),
                    CarverDebugSettings.CODEC.optionalFieldOf("debug_settings", CarverDebugSettings.DEFAULT).forGetter(param0x -> param0x.debugSettings),
                    RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("replaceable").forGetter(param0x -> param0x.replaceable)
                )
                .apply(param0, CarverConfiguration::new)
    );
    public final HeightProvider y;
    public final FloatProvider yScale;
    public final VerticalAnchor lavaLevel;
    public final CarverDebugSettings debugSettings;
    public final HolderSet<Block> replaceable;

    public CarverConfiguration(
        float param0, HeightProvider param1, FloatProvider param2, VerticalAnchor param3, CarverDebugSettings param4, HolderSet<Block> param5
    ) {
        super(param0);
        this.y = param1;
        this.yScale = param2;
        this.lavaLevel = param3;
        this.debugSettings = param4;
        this.replaceable = param5;
    }
}
