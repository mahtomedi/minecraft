package net.minecraft.world;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class RandomSequences extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final long seed;
    private final Map<ResourceLocation, RandomSequence> sequences = new Object2ObjectOpenHashMap<>();

    public RandomSequences(long param0) {
        this.seed = param0;
    }

    public RandomSource get(ResourceLocation param0) {
        final RandomSource var0 = this.sequences.computeIfAbsent(param0, param0x -> new RandomSequence(this.seed, param0x)).random();
        return new RandomSource() {
            @Override
            public RandomSource fork() {
                RandomSequences.this.setDirty();
                return var0.fork();
            }

            @Override
            public PositionalRandomFactory forkPositional() {
                RandomSequences.this.setDirty();
                return var0.forkPositional();
            }

            @Override
            public void setSeed(long param0) {
                RandomSequences.this.setDirty();
                var0.setSeed(param0);
            }

            @Override
            public int nextInt() {
                RandomSequences.this.setDirty();
                return var0.nextInt();
            }

            @Override
            public int nextInt(int param0) {
                RandomSequences.this.setDirty();
                return var0.nextInt(param0);
            }

            @Override
            public long nextLong() {
                RandomSequences.this.setDirty();
                return var0.nextLong();
            }

            @Override
            public boolean nextBoolean() {
                RandomSequences.this.setDirty();
                return var0.nextBoolean();
            }

            @Override
            public float nextFloat() {
                RandomSequences.this.setDirty();
                return var0.nextFloat();
            }

            @Override
            public double nextDouble() {
                RandomSequences.this.setDirty();
                return var0.nextDouble();
            }

            @Override
            public double nextGaussian() {
                RandomSequences.this.setDirty();
                return var0.nextGaussian();
            }
        };
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        this.sequences
            .forEach((param1, param2) -> param0.put(param1.toString(), RandomSequence.CODEC.encodeStart(NbtOps.INSTANCE, param2).result().orElseThrow()));
        return param0;
    }

    public static RandomSequences load(long param0, CompoundTag param1) {
        RandomSequences var0 = new RandomSequences(param0);

        for(String var2 : param1.getAllKeys()) {
            try {
                RandomSequence var3 = RandomSequence.CODEC.decode(NbtOps.INSTANCE, param1.get(var2)).result().get().getFirst();
                var0.sequences.put(new ResourceLocation(var2), var3);
            } catch (Exception var8) {
                LOGGER.error("Failed to load random sequence {}", var2, var8);
            }
        }

        return var0;
    }
}
