package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;

public class StructureFeatureConfiguration {
    public static final Codec<StructureFeatureConfiguration> CODEC = RecordCodecBuilder.<StructureFeatureConfiguration>create(
            param0 -> param0.group(
                        Codec.intRange(0, 4096).fieldOf("spacing").forGetter(param0x -> param0x.spacing),
                        Codec.intRange(0, 4096).fieldOf("separation").forGetter(param0x -> param0x.separation),
                        Codec.intRange(0, Integer.MAX_VALUE).fieldOf("salt").forGetter(param0x -> param0x.salt)
                    )
                    .apply(param0, StructureFeatureConfiguration::new)
        )
        .comapFlatMap(
            param0 -> param0.spacing <= param0.separation ? DataResult.error("Spacing has to be smaller than separation") : DataResult.success(param0),
            Function.identity()
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
