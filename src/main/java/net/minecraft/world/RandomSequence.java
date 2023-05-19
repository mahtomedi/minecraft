package net.minecraft.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public class RandomSequence {
    public static final Codec<RandomSequence> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(XoroshiroRandomSource.CODEC.fieldOf("source").forGetter(param0x -> param0x.source)).apply(param0, RandomSequence::new)
    );
    private final XoroshiroRandomSource source;

    public RandomSequence(XoroshiroRandomSource param0) {
        this.source = param0;
    }

    public RandomSequence(long param0, ResourceLocation param1) {
        this(createSequence(param0, param1));
    }

    private static XoroshiroRandomSource createSequence(long param0, ResourceLocation param1) {
        XoroshiroRandomSource var0 = new XoroshiroRandomSource(RandomSupport.upgradeSeedTo128bit(param0).xor(seedForKey(param1)));
        var0.nextLong();
        return var0;
    }

    public static RandomSupport.Seed128bit seedForKey(ResourceLocation param0) {
        return RandomSupport.seedFromHashOf(param0.toString());
    }

    public RandomSource random() {
        return this.source;
    }
}
