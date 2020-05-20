package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class StructureFeatureConfiguration {
    public static final Codec<StructureFeatureConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.INT.fieldOf("spacing").forGetter(param0x -> param0x.spacing),
                    Codec.INT.fieldOf("separation").forGetter(param0x -> param0x.separation),
                    Codec.INT.fieldOf("salt").forGetter(param0x -> param0x.salt)
                )
                .apply(param0, StructureFeatureConfiguration::new)
    );
    private final int spacing;
    private final int separation;
    private final int salt;

    public StructureFeatureConfiguration(int param0, int param1, int param2) {
        this.spacing = param0;
        this.separation = param1;
        this.salt = param2;
    }

    public int spacing() {
        return this.spacing;
    }

    public int separation() {
        return this.separation;
    }

    public int salt() {
        return this.salt;
    }
}
