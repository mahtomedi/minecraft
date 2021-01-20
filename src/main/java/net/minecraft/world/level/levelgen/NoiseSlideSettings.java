package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class NoiseSlideSettings {
    public static final Codec<NoiseSlideSettings> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.INT.fieldOf("target").forGetter(NoiseSlideSettings::target),
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("size").forGetter(NoiseSlideSettings::size),
                    Codec.INT.fieldOf("offset").forGetter(NoiseSlideSettings::offset)
                )
                .apply(param0, NoiseSlideSettings::new)
    );
    private final int target;
    private final int size;
    private final int offset;

    public NoiseSlideSettings(int param0, int param1, int param2) {
        this.target = param0;
        this.size = param1;
        this.offset = param2;
    }

    public int target() {
        return this.target;
    }

    public int size() {
        return this.size;
    }

    public int offset() {
        return this.offset;
    }
}
