package net.minecraft.world.level.newbiome.context;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.Random;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.newbiome.area.LazyArea;
import net.minecraft.world.level.newbiome.layer.traits.PixelTransformer;

public class LazyAreaContext implements BigContext<LazyArea> {
    private final Long2IntLinkedOpenHashMap cache;
    private final int maxCache;
    protected long seedMixup;
    protected ImprovedNoise biomeNoise;
    private long seed;
    private long rval;

    public LazyAreaContext(int param0, long param1, long param2) {
        this.seedMixup = param2;
        this.seedMixup *= this.seedMixup * 6364136223846793005L + 1442695040888963407L;
        this.seedMixup += param2;
        this.seedMixup *= this.seedMixup * 6364136223846793005L + 1442695040888963407L;
        this.seedMixup += param2;
        this.seedMixup *= this.seedMixup * 6364136223846793005L + 1442695040888963407L;
        this.seedMixup += param2;
        this.cache = new Long2IntLinkedOpenHashMap(16, 0.25F);
        this.cache.defaultReturnValue(Integer.MIN_VALUE);
        this.maxCache = param0;
        this.init(param1);
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

    public void init(long param0) {
        this.seed = param0;
        this.seed *= this.seed * 6364136223846793005L + 1442695040888963407L;
        this.seed += this.seedMixup;
        this.seed *= this.seed * 6364136223846793005L + 1442695040888963407L;
        this.seed += this.seedMixup;
        this.seed *= this.seed * 6364136223846793005L + 1442695040888963407L;
        this.seed += this.seedMixup;
        this.biomeNoise = new ImprovedNoise(new Random(param0));
    }

    @Override
    public void initRandom(long param0, long param1) {
        this.rval = this.seed;
        this.rval *= this.rval * 6364136223846793005L + 1442695040888963407L;
        this.rval += param0;
        this.rval *= this.rval * 6364136223846793005L + 1442695040888963407L;
        this.rval += param1;
        this.rval *= this.rval * 6364136223846793005L + 1442695040888963407L;
        this.rval += param0;
        this.rval *= this.rval * 6364136223846793005L + 1442695040888963407L;
        this.rval += param1;
    }

    @Override
    public int nextRandom(int param0) {
        int var0 = (int)((this.rval >> 24) % (long)param0);
        if (var0 < 0) {
            var0 += param0;
        }

        this.rval *= this.rval * 6364136223846793005L + 1442695040888963407L;
        this.rval += this.seed;
        return var0;
    }

    @Override
    public ImprovedNoise getBiomeNoise() {
        return this.biomeNoise;
    }
}
