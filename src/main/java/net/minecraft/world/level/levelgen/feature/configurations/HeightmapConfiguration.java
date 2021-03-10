package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.Heightmap;

public class HeightmapConfiguration implements DecoratorConfiguration {
    public static final Codec<HeightmapConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(param0x -> param0x.heightmap)).apply(param0, HeightmapConfiguration::new)
    );
    public final Heightmap.Types heightmap;

    public HeightmapConfiguration(Heightmap.Types param0) {
        this.heightmap = param0;
    }
}
