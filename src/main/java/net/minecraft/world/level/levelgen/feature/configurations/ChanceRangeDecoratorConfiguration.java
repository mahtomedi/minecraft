package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ChanceRangeDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<ChanceRangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.FLOAT.fieldOf("chance").forGetter(param0x -> param0x.chance),
                    Codec.INT.fieldOf("bottom_offset").withDefault(0).forGetter(param0x -> param0x.bottomOffset),
                    Codec.INT.fieldOf("top_offset").withDefault(0).forGetter(param0x -> param0x.topOffset),
                    Codec.INT.fieldOf("top").withDefault(0).forGetter(param0x -> param0x.top)
                )
                .apply(param0, ChanceRangeDecoratorConfiguration::new)
    );
    public final float chance;
    public final int bottomOffset;
    public final int topOffset;
    public final int top;

    public ChanceRangeDecoratorConfiguration(float param0, int param1, int param2, int param3) {
        this.chance = param0;
        this.bottomOffset = param1;
        this.topOffset = param2;
        this.top = param3;
    }
}
