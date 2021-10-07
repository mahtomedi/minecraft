package net.minecraft.world.level.levelgen;

public class SingleThreadedRandomSource implements BitRandomSource {
    private static final int MODULUS_BITS = 48;
    private static final long MODULUS_MASK = 281474976710655L;
    private static final long MULTIPLIER = 25214903917L;
    private static final long INCREMENT = 11L;
    private long seed;
    private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

    public SingleThreadedRandomSource(long param0) {
        this.setSeed(param0);
    }

    @Override
    public RandomSource fork() {
        return new SingleThreadedRandomSource(this.nextLong());
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return new LegacyRandomSource.LegacyPositionalRandomFactory(this.nextLong());
    }

    @Override
    public void setSeed(long param0) {
        this.seed = (param0 ^ 25214903917L) & 281474976710655L;
    }

    @Override
    public int next(int param0) {
        long var0 = this.seed * 25214903917L + 11L & 281474976710655L;
        this.seed = var0;
        return (int)(var0 >> 48 - param0);
    }

    @Override
    public double nextGaussian() {
        return this.gaussianSource.nextGaussian();
    }
}
