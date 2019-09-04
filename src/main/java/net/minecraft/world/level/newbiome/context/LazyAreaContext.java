package net.minecraft.world.level.newbiome.context;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.Random;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.newbiome.area.LazyArea;
import net.minecraft.world.level.newbiome.layer.traits.PixelTransformer;

public class LazyAreaContext implements BigContext<LazyArea> {
    private final Long2IntLinkedOpenHashMap cache;
    private final int maxCache;
    private final ImprovedNoise biomeNoise;
    private final long seed;
    private long rval;

    public LazyAreaContext(int param0, long param1, long param2) {
        this.seed = mixSeed(param1, param2);
        this.biomeNoise = new ImprovedNoise(new Random(param1));
        this.cache = new Long2IntLinkedOpenHashMap(16, 0.25F);
        this.cache.defaultReturnValue(Integer.MIN_VALUE);
        this.maxCache = param0;
    }

    public LazyArea createResult(PixelTransformer param0) {
        return new LazyArea(this.cache, this.maxCache, param0);
    }

    public LazyArea createResult(PixelTransformer param0, LazyArea param1) {
        return new LazyArea(this.cache, Math.min(1024, param1.getMaxCache() * 4), param0);
    }

    public LazyArea createResult(PixelTransformer param0, LazyArea param1, LazyArea param2) {
        return new LazyArea(this.cache, Math.min(1024, Math.max(param1.getMaxCache(), param2.getMaxCache()) * 4), param0);
    }

    @Override
    public void initRandom(long param0, long param1) {
        long var0 = this.seed;
        var0 = LinearCongruentialGenerator.next(var0, param0);
        var0 = LinearCongruentialGenerator.next(var0, param1);
        var0 = LinearCongruentialGenerator.next(var0, param0);
        var0 = LinearCongruentialGenerator.next(var0, param1);
        this.rval = var0;
    }

    @Override
    public int nextRandom(int param0) {
        int var0 = (int)Math.floorMod(this.rval >> 24, (long)param0);
        this.rval = LinearCongruentialGenerator.next(this.rval, this.seed);
        return var0;
    }

    @Override
    public ImprovedNoise getBiomeNoise() {
        return this.biomeNoise;
    }

    private static long mixSeed(long param0, long param1) {
        long var0 = LinearCongruentialGenerator.next(param1, param1);
        var0 = LinearCongruentialGenerator.next(var0, param1);
        var0 = LinearCongruentialGenerator.next(var0, param1);
        long var1 = LinearCongruentialGenerator.next(param0, var0);
        var1 = LinearCongruentialGenerator.next(var1, var0);
        return LinearCongruentialGenerator.next(var1, var0);
    }
}
