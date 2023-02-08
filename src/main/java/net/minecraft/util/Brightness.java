package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Brightness(int block, int sky) {
    public static final Codec<Integer> LIGHT_VALUE_CODEC = ExtraCodecs.intRange(0, 15);
    public static final Codec<Brightness> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(LIGHT_VALUE_CODEC.fieldOf("block").forGetter(Brightness::block), LIGHT_VALUE_CODEC.fieldOf("sky").forGetter(Brightness::sky))
                .apply(param0, Brightness::new)
    );
    public static Brightness FULL_BRIGHT = new Brightness(15, 15);

    public int pack() {
        return this.block << 4 | this.sky << 20;
    }

    public static Brightness unpack(int param0) {
        int var0 = param0 >> 4 & 65535;
        int var1 = param0 >> 20 & 65535;
        return new Brightness(var0, var1);
    }
}
